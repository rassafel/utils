/*
 * Copyright 2024-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.rassafel.blobstorage.aws;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.LinkedCaseInsensitiveMap;
import org.springframework.util.unit.DataSize;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.internal.util.Mimetype;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import io.github.rassafel.blobstorage.core.BlobStorage;
import io.github.rassafel.blobstorage.core.NotFoundBlobException;
import io.github.rassafel.blobstorage.core.StoreBlobException;
import io.github.rassafel.blobstorage.core.StoredBlobObject;
import io.github.rassafel.blobstorage.core.impl.KeyGenerator;
import io.github.rassafel.blobstorage.core.query.StoreBlobRequest;
import io.github.rassafel.blobstorage.core.query.StoreBlobResponse;
import io.github.rassafel.blobstorage.core.query.UpdateAttributesRequest;
import io.github.rassafel.blobstorage.core.query.UpdateAttributesResponse;
import io.github.rassafel.blobstorage.core.query.impl.DefaultStoreBlobResponse;
import io.github.rassafel.blobstorage.core.query.impl.DefaultUpdateAttributesResponse;
import io.github.rassafel.blobstorage.core.util.FileTypeUtils;

/**
 * S3BlobStorage class implements the BlobStorage interface using AWS S3 as the storage backend.
 */
@Slf4j
public class S3BlobStorage implements BlobStorage {
    protected static final String CORE_DATA_PREFIX = "";
    protected static final String METADATA_PREFIX = CORE_DATA_PREFIX + "Meta-";
    protected static final String ORIGINAL_NAME_ATTRIBUTE = "Original-Name";
    protected static final String UPLOADED_AT_ATTRIBUTE = "Uploaded-At";
    protected static final String LAST_MODIFIED_AT_ATTRIBUTE = "Last-Modified-At";
    protected static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    @NonNull
    protected final KeyGenerator keyGenerator;
    @NonNull
    protected final String coreDataPrefix;
    @NonNull
    protected final String metadataPrefix;
    @NonNull
    protected final DataSize chunkSize;
    @NonNull
    protected final String bucket;
    @NonNull
    protected final S3ClientProvider s3ClientProvider;
    @NonNull
    protected final Clock clock;

    public S3BlobStorage(
            DataSize chunkSize,
            String bucket,
            S3ClientProvider s3ClientProvider,
            Clock clock,
            KeyGenerator keyGenerator) {
        this(chunkSize, bucket, s3ClientProvider, clock, CORE_DATA_PREFIX, METADATA_PREFIX, keyGenerator);
    }

    public S3BlobStorage(
            DataSize chunkSize,
            String bucket,
            S3ClientProvider s3ClientProvider,
            Clock clock,
            String coreDataPrefix,
            String metadataPrefix,
            KeyGenerator keyGenerator) {
        Assert.isTrue(chunkSize.toBytes() > 0, "chunkSize must be greater then 0 bytes");
        Assert.hasText(bucket, "bucket must not be empty");
        this.keyGenerator = keyGenerator;
        this.coreDataPrefix = coreDataPrefix;
        this.metadataPrefix = metadataPrefix;
        this.chunkSize = chunkSize;
        this.bucket = bucket;
        this.s3ClientProvider = s3ClientProvider;
        this.clock = clock;
    }

    protected S3Client getClient() {
        return s3ClientProvider.getS3Client();
    }

    @Override
    public StoreBlobResponse store(InputStream inputStream, StoreBlobRequest request) {
        var originalName = request.getOriginalName();
        var blobKey = generateBlobKey(originalName);
        var builder = request.toBuilder();
        var contentType = request.getContentType();
        if (StringUtils.isBlank(contentType)) {
            var type = FileTypeUtils.getMimeTypeFromName(originalName);
            builder.contentType(type.toString());
        }
        return store(blobKey, inputStream, builder.build());
    }

    protected String generateBlobKey(String originalName) {
        var blobKey = KeyGenerator.SEPARATOR + keyGenerator.createKey(originalName);
        var extension = FilenameUtils.getExtension(originalName);
        if (StringUtils.isNotBlank(extension)) {
            return blobKey + "." + extension;
        }
        return blobKey;
    }

    protected StoreBlobResponse store(String blobKey, InputStream inputStream, StoreBlobRequest request) {
        var chunkSize = Math.toIntExact(this.chunkSize.toBytes());
        var s3Client = getClient();
        var bucket = this.bucket;
        var storedBlobBuilder = fromRequest(request)
                .s3Client(s3Client)
                .bucket(bucket)
                .storedRef(blobKey);
        var metadata = objectToMetadata(storedBlobBuilder.build());

        try (var bis = new BufferedInputStream(inputStream, chunkSize)) {
            var buffer = new byte[chunkSize];
            var nBytes = bis.read(buffer);
            if (nBytes < chunkSize) {
                s3Client.putObject(
                        objectBuilder -> objectBuilder
                                .bucket(bucket)
                                .key(blobKey)
                                .contentType(request.getContentType())
                                .metadata(metadata)
                                .build(),
                        fromBytes(buffer, nBytes));
                var blob = storedBlobBuilder.size(nBytes).build();
                log.debug("The uploading is stored, bucket: {}; key: {}; original name: {}",
                        bucket, blobKey, blob.getOriginalName());
                return DefaultStoreBlobResponse.of(blob);
            }

            var response = s3Client.createMultipartUpload(uploadBuilder -> uploadBuilder
                    .bucket(bucket)
                    .key(blobKey)
                    .contentType(request.getContentType())
                    .metadata(metadata));

            var completedParts = new ArrayList<CompletedPart>();
            long readBytes = nBytes;
            var partBuilder = UploadPartRequest.builder()
                    .bucket(bucket)
                    .key(blobKey)
                    .uploadId(response.uploadId());
            for (var partNumber = 1; 0 < nBytes; partNumber++) {
                var partResponse = s3Client.uploadPart(
                        partBuilder.partNumber(partNumber).build(),
                        fromBytes(buffer, nBytes));
                var completedPart = CompletedPart.builder()
                        .partNumber(partNumber)
                        .eTag(partResponse.eTag())
                        .build();
                readBytes += nBytes;
                completedParts.add(completedPart);
                nBytes = bis.read(buffer);
            }

            s3Client.completeMultipartUpload(completeBuilder -> completeBuilder
                    .bucket(bucket)
                    .key(blobKey)
                    .uploadId(response.uploadId())
                    .multipartUpload(multipartBuilder -> multipartBuilder.parts(completedParts)));
            var blob = storedBlobBuilder.size(readBytes).build();
            log.debug("The uploading is stored, bucket: {}; key: {}; original name: {}",
                    bucket, blobKey, blob.getOriginalName());
            return DefaultStoreBlobResponse.of(blob);
        } catch (IOException | SdkException ex) {
            log.error("Error saving file to S3 storage", ex);
            throw new StoreBlobException("Could not save file", ex);
        }
    }

    protected S3StoredBlobObject.Builder<?, ?> fromRequest(StoreBlobRequest request) {
        var now = LocalDateTime.now(clock);
        return S3StoredBlobObject.builder(request)
                .uploadedAt(now)
                .lastModifiedAt(now);
    }

    protected Map<String, String> objectToMetadata(StoredBlobObject object) {
        var coreDataPrefix = this.coreDataPrefix;
        var attributes = object.getAttributes();
        var result = new LinkedCaseInsensitiveMap<String>(attributes.size() + 3);
        result.put(coreDataPrefix + ORIGINAL_NAME_ATTRIBUTE, object.getOriginalName());
        result.put(coreDataPrefix + UPLOADED_AT_ATTRIBUTE, TIME_FORMATTER.format(object.getUploadedAt()));
        result.put(coreDataPrefix + LAST_MODIFIED_AT_ATTRIBUTE, TIME_FORMATTER.format(object.getLastModifiedAt()));
        result.putAll(toStoreMetadata(attributes));
        return result;
    }

    protected Map<String, String> toStoreMetadata(Map<String, String> source) {
        var metadataPrefix = this.metadataPrefix;
        return source.entrySet().stream()
                .filter(e -> StringUtils.isNotBlank(e.getValue()))
                .collect(Collectors.toMap(e -> metadataPrefix + e.getKey(), Map.Entry::getValue));
    }

    private RequestBody fromBytes(byte[] buffer, int length) {
        length = Math.max(0, length);
        var bytes = Arrays.copyOf(buffer, length);
        return RequestBody.fromContentProvider(() -> new ByteArrayInputStream(bytes), length, Mimetype.MIMETYPE_OCTET_STREAM);
    }

    @Override
    public boolean existsByRef(String ref) {
        var s3Client = getClient();
        try {
            var response = s3Client.listObjectsV2(b -> b
                    .bucket(bucket)
                    .prefix(ref)
                    .maxKeys(1));
            return response.contents().stream()
                    .anyMatch(e -> ref.equals(e.key()));
        } catch (NoSuchKeyException ignore) {
        } catch (SdkException ex) {
            log.error("Error find blob from S3 storage", ex);
            var message = String.format("Could not find blob %s.", ref);
            throw new StoreBlobException(message, ex);
        }
        return false;
    }

    @Nullable
    @Override
    public S3StoredBlobObject getByRef(String ref) {
        var bucket = this.bucket;
        var s3Client = getClient();
        GetObjectResponse response = null;
        try {
            response = s3Client.getObject(builder -> builder
                    .bucket(bucket)
                    .key(ref)).response();
        } catch (NoSuchKeyException ignore) {
        } catch (SdkException ex) {
            log.error("Error loading blob from S3 storage", ex);
            var message = String.format("Could not load blob %s.", ref);
            throw new StoreBlobException(message, ex);
        }
        if (response == null) {
            log.debug("Blob not found, ref: {}", ref);
            return null;
        }
        log.debug("Blob found, ref: {}", ref);
        var metadata = new LinkedCaseInsensitiveMap<String>(response.metadata().size());
        metadata.putAll(response.metadata());
        return metadataToObject(metadata)
                .s3Client(s3Client)
                .bucket(bucket)
                .storedRef(ref)
                .contentType(response.contentType())
                .size(response.contentLength())
                .build();
    }

    protected S3StoredBlobObject.Builder<?, ?> metadataToObject(Map<String, String> metadata) {
        var coreDataPrefix = this.coreDataPrefix;
        var metadataPrefix = this.metadataPrefix;
        var builder = S3StoredBlobObject.builder()
                .originalName(metadata.get(coreDataPrefix + ORIGINAL_NAME_ATTRIBUTE))
                .uploadedAt(LocalDateTime.from(TIME_FORMATTER.parse(metadata.get(coreDataPrefix + UPLOADED_AT_ATTRIBUTE))))
                .lastModifiedAt(LocalDateTime.from(TIME_FORMATTER.parse(metadata.get(coreDataPrefix + LAST_MODIFIED_AT_ATTRIBUTE))));
        var metadataPrefixLength = metadataPrefix.length();

        metadata.entrySet().stream()
                .filter(e -> StringUtils.startsWithIgnoreCase(e.getKey(), metadataPrefix))
                .forEach(entry -> {
                    var key = entry.getKey();
                    key = key.substring(metadataPrefixLength);
                    var value = entry.getValue();
                    builder.attribute(key, value);
                });

        return builder;
    }

    @Override
    public boolean deleteByRef(String ref) {
        var s3Client = getClient();
        if (!existsByRef(ref)) return false;
        try {
            s3Client.deleteObject(b -> b.bucket(bucket).key(ref));
            return true;
        } catch (SdkException ex) {
            log.error("Error delete blob from S3 storage", ex);
            var message = String.format("Could not delete blob %s.", ref);
            throw new StoreBlobException(message, ex);
        }
    }

    @Override
    public UpdateAttributesResponse updateByRef(String ref, UpdateAttributesRequest request) {
        var bucket = this.bucket;
        var coreDataPrefix = this.coreDataPrefix;
        var metadataPrefix = this.metadataPrefix;
        var s3Client = getClient();
        var blob = getByRef(ref);
        if (blob == null) throw new NotFoundBlobException(ref);
        var now = LocalDateTime.now(clock);
        var metadata = objectToMetadata(blob);
        for (var entry : request.getAttributes().entrySet()) {
            var key = entry.getKey();
            var value = entry.getValue();
            metadata.put(metadataPrefix + key, value);
        }
        metadata.put(coreDataPrefix + LAST_MODIFIED_AT_ATTRIBUTE, TIME_FORMATTER.format(now));
        var resultMetadata = metadata.entrySet().stream()
                .filter(e -> StringUtils.isNotBlank(e.getKey()))
                .filter(e -> StringUtils.isNotBlank(e.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        try {
            s3Client.copyObject(builder -> builder
                    .copySource(bucket + ref)
                    .destinationBucket(bucket)
                    .destinationKey(ref)
                    .contentType(blob.getContentType())
                    .metadataDirective(MetadataDirective.REPLACE)
                    .metadata(resultMetadata));
        } catch (NoSuchKeyException ex) {
            log.error("Error update blob from S3 storage", ex);
            var message = String.format("Could not update blob %s.", ref);
            throw new NotFoundBlobException(ref, message, ex);
        } catch (SdkException ex) {
            log.error("Error update blob from S3 storage", ex);
            var message = String.format("Could not update blob %s.", ref);
            throw new StoreBlobException(message, ex);
        }

        var result = metadataToObject(metadata)
                .contentType(blob.getContentType())
                .lastModifiedAt(now)
                .size(blob.getSize())
                .storedRef(ref)
                .s3Client(s3Client)
                .build();
        return DefaultUpdateAttributesResponse.of(result);
    }
}
