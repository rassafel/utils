package com.rassafel.io.storage.aws;

import com.google.common.collect.Maps;
import com.rassafel.commons.util.Assert;
import com.rassafel.io.storage.core.NotFoundBlobException;
import com.rassafel.io.storage.core.StoreBlobException;
import com.rassafel.io.storage.core.impl.AbstractBlobStorage;
import com.rassafel.io.storage.core.impl.KeyGenerator;
import com.rassafel.io.storage.core.query.StoreBlobRequest;
import com.rassafel.io.storage.core.query.StoreBlobResponse;
import com.rassafel.io.storage.core.query.UpdateAttributesRequest;
import com.rassafel.io.storage.core.query.UpdateAttributesResponse;
import com.rassafel.io.storage.core.query.impl.DefaultStoreBlobResponse;
import com.rassafel.io.storage.core.query.impl.DefaultUpdateAttributesResponse;
import lombok.Setter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.LinkedCaseInsensitiveMap;
import org.springframework.util.unit.DataSize;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.internal.util.Mimetype;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

@Setter
public class S3BlobStorage extends AbstractBlobStorage {
    protected DataSize chunkSize;
    protected String bucket;
    protected S3ClientProvider s3ClientProvider;
    protected Clock clock;

    public S3BlobStorage(DataSize chunkSize,
                         String bucket, S3ClientProvider s3ClientProvider,
                         Clock clock, KeyGenerator keyGenerator) {
        this(chunkSize, bucket, s3ClientProvider, clock, CORE_DATA_PREFIX, METADATA_PREFIX, keyGenerator);
    }

    public S3BlobStorage(DataSize chunkSize,
                         String bucket, S3ClientProvider s3ClientProvider,
                         Clock clock, String coreDataPrefix, String metadataPrefix,
                         KeyGenerator keyGenerator) {
        super(keyGenerator, coreDataPrefix, metadataPrefix);
        Assert.isTrue(chunkSize.toBytes() > 0, "chunkSize must be greater then 0 bytes");
        Assert.hasText(bucket, "bucket must not be empty");
        Assert.notNull(s3ClientProvider, "s3ClientProvider must not be null");
        Assert.notNull(clock, "clock must not be null");
        this.chunkSize = chunkSize;
        this.bucket = bucket;
        this.s3ClientProvider = s3ClientProvider;
        this.clock = clock;
    }

    protected S3Client getClient() {
        return s3ClientProvider.getS3Client();
    }

    @Override
    protected StoreBlobResponse store(String blobKey, InputStream inputStream, StoreBlobRequest request) {
        val chunkSize = Math.toIntExact(this.chunkSize.toBytes());
        val s3Client = getClient();
        val bucket = this.bucket;
        val storedBlobBuilder = fromRequest(request)
            .s3Client(s3Client)
            .bucket(bucket)
            .storedRef(blobKey);
        val metadata = objectToMetadata(storedBlobBuilder.build());

        try (val bis = new BufferedInputStream(inputStream, chunkSize)) {
            val buffer = new byte[chunkSize];
            int nBytes = bis.read(buffer);
            if (nBytes < chunkSize) {
                s3Client.putObject(objectBuilder -> objectBuilder
                    .bucket(bucket)
                    .key(blobKey)
                    .contentType(request.getContentType())
                    .metadata(metadata)
                    .build(), fromBytes(buffer, nBytes));
                val blob = storedBlobBuilder.size(nBytes).build();
                log.debug("The uploading is stored, bucket: {}; key: {}; original name: {}",
                    bucket, blobKey, blob.getOriginalName());
                return new DefaultStoreBlobResponse(blob);
            }

            val response = s3Client.createMultipartUpload(uploadBuilder -> uploadBuilder
                .bucket(bucket)
                .key(blobKey)
                .contentType(request.getContentType())
                .metadata(metadata));

            val completedParts = new ArrayList<CompletedPart>();
            long readBytes = nBytes;
            val partBuilder = UploadPartRequest.builder()
                .bucket(bucket)
                .key(blobKey)
                .uploadId(response.uploadId());
            for (int partNumber = 1; 0 < nBytes; partNumber++) {
                val partResponse = s3Client.uploadPart(partBuilder
                    .partNumber(partNumber)
                    .build(), fromBytes(buffer, nBytes));
                val completedPart = CompletedPart.builder()
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
            val blob = storedBlobBuilder
                .size(readBytes)
                .build();
            log.debug("The uploading is stored, bucket: {}; key: {}; original name: {}",
                bucket, blobKey, blob.getOriginalName());
            return new DefaultStoreBlobResponse(blob);
        } catch (IOException | SdkException ex) {
            log.error("Error saving file to S3 storage", ex);
            throw new StoreBlobException("Could not save file", ex);
        }
    }

    protected S3StoredBlobObject.Builder fromRequest(StoreBlobRequest request) {
        val now = LocalDateTime.now(clock);
        return S3StoredBlobObject.builder(request)
            .uploadedAt(now)
            .lastModifiedAt(now);
    }

    private RequestBody fromBytes(byte[] buffer, int length) {
        length = Math.max(0, length);
        val bytes = Arrays.copyOf(buffer, length);
        return RequestBody.fromContentProvider(() -> new ByteArrayInputStream(bytes), length, Mimetype.MIMETYPE_OCTET_STREAM);
    }

    @Override
    public boolean existsByRef(String ref) {
        val s3Client = getClient();
        try {
            val response = s3Client.listObjectsV2(b -> b
                .bucket(bucket)
                .prefix(ref)
                .maxKeys(1));
            return response.contents().stream()
                .anyMatch(e -> ref.equals(e.key()));
        } catch (NoSuchKeyException ignore) {
        } catch (SdkException ex) {
            log.error("Error find blob from S3 storage", ex);
            val message = String.format("Could not find blob %s.", ref);
            throw new StoreBlobException(message, ex);
        }
        return false;
    }

    @Override
    public S3StoredBlobObject getByRef(String ref) {
        val bucket = this.bucket;
        val s3Client = getClient();
        GetObjectResponse response = null;
        try {
            response = s3Client.getObject(builder -> builder
                .bucket(bucket)
                .key(ref)).response();
        } catch (NoSuchKeyException ignore) {
        } catch (SdkException ex) {
            log.error("Error loading blob from S3 storage", ex);
            val message = String.format("Could not load blob %s.", ref);
            throw new StoreBlobException(message, ex);
        }
        if (response == null) {
            log.debug("Blob not found, ref: {}", ref);
            return null;
        }
        log.debug("Blob found, ref: {}", ref);
        val metadata = new LinkedCaseInsensitiveMap<String>(response.metadata().size());
        metadata.putAll(response.metadata());
        return metadataToObject(metadata)
            .s3Client(s3Client)
            .bucket(bucket)
            .storedRef(ref)
            .contentType(response.contentType())
            .size(response.contentLength())
            .build();
    }

    protected S3StoredBlobObject.Builder metadataToObject(Map<String, String> metadata) {
        val coreDataPrefix = this.coreDataPrefix;
        val metadataPrefix = this.metadataPrefix;
        val builder = S3StoredBlobObject.builder()
            .originalName(metadata.get(coreDataPrefix + ORIGINAL_NAME_ATTRIBUTE))
            .uploadedAt(LocalDateTime.parse(metadata.get(coreDataPrefix + UPLOADED_AT_ATTRIBUTE)))
            .lastModifiedAt(LocalDateTime.parse(metadata.get(coreDataPrefix + LAST_MODIFIED_AT_ATTRIBUTE)));
        val metadataPrefixLength = metadataPrefix.length();

        Maps.filterKeys(metadata, k -> StringUtils.startsWithIgnoreCase(k, metadataPrefix))
            .forEach((key, value) -> {
                key = key.substring(metadataPrefixLength);
                builder.attribute(key, value);
            });
        return builder;
    }

    @Override
    public boolean deleteByRef(String ref) {
        val s3Client = getClient();
        if (!existsByRef(ref)) return false;
        try {
            val response = s3Client.deleteObject(b -> b
                .bucket(bucket)
                .key(ref));
            return true;
        } catch (SdkException ex) {
            log.error("Error delete blob from S3 storage", ex);
            val message = String.format("Could not delete blob %s.", ref);
            throw new StoreBlobException(message, ex);
        }
    }

    @Override
    public UpdateAttributesResponse updateByRef(String ref, UpdateAttributesRequest request) {
        val bucket = this.bucket;
        val coreDataPrefix = this.coreDataPrefix;
        val metadataPrefix = this.metadataPrefix;
        val s3Client = getClient();
        val blob = getByRef(ref);
        if (blob == null) throw new NotFoundBlobException(ref);
        val now = LocalDateTime.now(clock);
        val metadata = objectToMetadata(blob);
        for (val entry : request.getAttributes().entrySet()) {
            val key = entry.getKey();
            val value = entry.getValue();
            metadata.put(metadataPrefix + key, value);
        }
        metadata.put(coreDataPrefix + LAST_MODIFIED_AT_ATTRIBUTE, now.toString());
        val resultMetadata = Maps.filterValues(metadata, StringUtils::isNotBlank);
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
            val message = String.format("Could not update blob %s.", ref);
            throw new NotFoundBlobException(ref, message, ex);
        } catch (SdkException ex) {
            log.error("Error update blob from S3 storage", ex);
            val message = String.format("Could not update blob %s.", ref);
            throw new StoreBlobException(message, ex);
        }

        val result = metadataToObject(metadata)
            .contentType(blob.getContentType())
            .lastModifiedAt(now)
            .size(blob.getSize())
            .storedRef(ref)
            .s3Client(s3Client)
            .build();
        return new DefaultUpdateAttributesResponse(result);
    }
}
