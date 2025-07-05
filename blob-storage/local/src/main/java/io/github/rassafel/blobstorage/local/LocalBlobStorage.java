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

package io.github.rassafel.blobstorage.local;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.LinkedCaseInsensitiveMap;

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
 * Local implementation of BlobStorage. Stores blobs in a local directory.
 */
@Slf4j
public class LocalBlobStorage implements BlobStorage {
    protected static final String METADATA_FILE_SUFFIX = ".metadata.properties";
    protected static final String CORE_DATA_PREFIX = "";
    protected static final String METADATA_PREFIX = CORE_DATA_PREFIX + "Meta-";
    protected static final String CONTENT_TYPE_ATTRIBUTE = "Content-Type";
    protected static final String CONTENT_LENGTH_ATTRIBUTE = "Content-Length";
    protected static final String ORIGINAL_NAME_ATTRIBUTE = "Original-Name";
    protected static final String UPLOADED_AT_ATTRIBUTE = "Uploaded-At";
    protected static final String LAST_MODIFIED_AT_ATTRIBUTE = "Last-Modified-At";
    protected static final Charset METADATA_CHARSET = StandardCharsets.UTF_8;
    protected static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @NonNull
    protected final KeyGenerator keyGenerator;
    @NonNull
    protected final String coreDataPrefix;
    @NonNull
    protected final String metadataPrefix;
    protected final Path documentRootPath;
    protected final Path metadataRootPath;
    protected final Clock clock;
    protected final String metadataFileSuffix;

    public LocalBlobStorage(
            KeyGenerator keyGenerator, String storageDir, String documentDir, String metadataDir, Clock clock) {
        this(
                keyGenerator,
                storageDir,
                documentDir,
                metadataDir,
                clock,
                METADATA_FILE_SUFFIX,
                CORE_DATA_PREFIX,
                METADATA_PREFIX);
    }

    public LocalBlobStorage(
            KeyGenerator keyGenerator,
            String storageDir,
            String documentDir,
            String metadataDir,
            Clock clock,
            String metadataFileSuffix,
            String coreDataPrefix,
            String metadataPrefix) {
        Assert.hasText(storageDir, "storageDir must not be empty");
        Assert.hasText(metadataFileSuffix, "metadataFileSuffix must not be empty");
        this.keyGenerator = keyGenerator;
        this.coreDataPrefix = coreDataPrefix;
        this.metadataPrefix = metadataPrefix;
        var storageRoot = Path.of(storageDir);
        documentDir = StringUtils.defaultIfEmpty(documentDir, "");
        metadataDir = StringUtils.defaultIfEmpty(metadataDir, "");
        this.documentRootPath = storageRoot.resolve(documentDir);
        this.metadataRootPath = storageRoot.resolve(metadataDir).resolve(documentDir);
        createDir("storage root", storageRoot);
        createDir("document dir", documentRootPath);
        createDir("metadata dir", metadataRootPath);
        this.clock = clock;
        this.metadataFileSuffix = metadataFileSuffix;
    }

    protected void createDir(String name, Path path) {
        if (Files.notExists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException ex) {
                log.error("Fail create {} directory", name);
                throw new StoreBlobException("", ex);
            }
        }
        Assert.isTrue(Files.isDirectory(path), name + " must be directory");
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
        var documentPath = resolveDocumentPath(blobKey);
        var metadataPath = resolveMetadataPath(blobKey);

        try (var outputStream = Files.newOutputStream(documentPath, StandardOpenOption.CREATE)) {
            var bytes = IOUtils.copyLarge(inputStream, outputStream, 0, 100);

            var blob = fromRequest(request)
                    .storedRef(blobKey)
                    .localFile(documentPath)
                    .size(bytes)
                    .build();
            var metadata = objectToMetadata(blob);
            storeMetadata(metadata, metadataPath, blobKey);

            log.debug("The uploading is stored, document path: {}; original name: {}; metadata path: {}",
                    documentPath, blob.getOriginalName(), metadataPath);
            return DefaultStoreBlobResponse.of(blob);
        } catch (IOException ex) {
            log.error("Error saving blob to local storage", ex);
            throw new StoreBlobException("Could not save blob", ex);
        }
    }

    protected Path resolveDocumentPath(String blobKey) {
        blobKey = blobKey.replaceFirst("^" + KeyGenerator.SEPARATOR + "+", "");
        return documentRootPath.resolve(blobKey);
    }

    protected Path resolveMetadataPath(String blobKey) {
        blobKey = blobKey.replaceFirst("^" + KeyGenerator.SEPARATOR + "+", "");
        return metadataRootPath.resolve(blobKey + metadataFileSuffix);
    }

    protected LocalStoredBlobObject.Builder<?, ?> fromRequest(StoreBlobRequest request) {
        var now = LocalDateTime.now(clock);
        return LocalStoredBlobObject.builder(request)
                .uploadedAt(now)
                .lastModifiedAt(now);
    }

    protected Map<String, String> objectToMetadata(StoredBlobObject object) {
        var coreDataPrefix = this.coreDataPrefix;
        var attributes = object.getAttributes();
        var metadata = new LinkedCaseInsensitiveMap<String>(attributes.size() + 5);
        metadata.put(coreDataPrefix + ORIGINAL_NAME_ATTRIBUTE, object.getOriginalName());
        metadata.put(coreDataPrefix + UPLOADED_AT_ATTRIBUTE, TIME_FORMATTER.format(object.getUploadedAt()));
        metadata.put(coreDataPrefix + LAST_MODIFIED_AT_ATTRIBUTE, TIME_FORMATTER.format(object.getLastModifiedAt()));
        metadata.put(coreDataPrefix + CONTENT_TYPE_ATTRIBUTE, object.getContentType());
        metadata.put(coreDataPrefix + CONTENT_LENGTH_ATTRIBUTE, String.valueOf(object.getSize()));
        metadata.putAll(toStoreMetadata(attributes));
        return metadata;
    }

    protected Map<String, String> toStoreMetadata(Map<String, String> source) {
        var metadataPrefix = this.metadataPrefix;
        return source.entrySet().stream()
                .filter(e -> StringUtils.isNotBlank(e.getValue()))
                .collect(Collectors.toMap(e -> metadataPrefix + e.getKey(), Map.Entry::getValue));
    }

    protected void storeMetadata(Map<String, String> metadata, Path path, String blobKey) throws IOException {
        try (var writer = Files.newBufferedWriter(path, METADATA_CHARSET, StandardOpenOption.CREATE)) {
            var metadataProperties = new Properties();
            metadataProperties.putAll(metadata);
            metadataProperties.store(writer, "blob object " + blobKey);
        }
    }

    @Override
    public boolean existsByRef(String ref) {
        var documentPath = resolveDocumentPath(ref);
        return documentExists(documentPath);
    }

    @Override
    @Nullable
    public LocalStoredBlobObject getByRef(String ref) {
        var documentPath = resolveDocumentPath(ref);
        var metadataPath = resolveMetadataPath(ref);

        if (!documentExists(documentPath)) {
            log.debug("Blob not found, ref: {}; document path: {}", ref, documentPath);
            return null;
        }

        var metadata = loadMetadata(metadataPath);

        return metadataToObject(metadata)
                .storedRef(ref)
                .localFile(documentPath)
                .build();
    }

    protected Map<String, String> loadMetadata(Path path) {
        var properties = new Properties();
        if (Files.exists(path)) {
            try (var metadataReader = Files.newBufferedReader(path, METADATA_CHARSET)) {
                properties.load(metadataReader);
            } catch (IOException ex) {
                log.error("Error loading metadata from local storage, path: {}", path, ex);
            }
        } else {
            log.debug("Metadata not found, path: {}", path);
        }
        if (properties.isEmpty()) return new LinkedCaseInsensitiveMap<>();
        var metadata = new LinkedCaseInsensitiveMap<String>(properties.size());
        for (var propertyName : properties.stringPropertyNames()) {
            metadata.put(propertyName, properties.getProperty(propertyName));
        }
        return metadata;
    }

    protected LocalStoredBlobObject.Builder<?, ?> metadataToObject(Map<String, String> metadata) {
        var coreDataPrefix = this.coreDataPrefix;
        var metadataPrefix = this.metadataPrefix;
        var builder = LocalStoredBlobObject.builder()
                .originalName(metadata.get(coreDataPrefix + ORIGINAL_NAME_ATTRIBUTE))
                .size(Long.parseLong(metadata.get(coreDataPrefix + CONTENT_LENGTH_ATTRIBUTE)))
                .contentType(metadata.get(coreDataPrefix + CONTENT_TYPE_ATTRIBUTE))
                .uploadedAt(LocalDateTime.from(TIME_FORMATTER.parse(metadata.get(coreDataPrefix + UPLOADED_AT_ATTRIBUTE))))
                .lastModifiedAt(LocalDateTime.from(TIME_FORMATTER.parse(metadata.get(coreDataPrefix + LAST_MODIFIED_AT_ATTRIBUTE))));

        var metadataPrefixLength = metadataPrefix.length();
        metadata.forEach((k, v) -> {
            if (!StringUtils.startsWithIgnoreCase(k, metadataPrefix)) return;
            var key = k.substring(metadataPrefixLength);
            builder.attribute(key, v);
        });
        return builder;
    }

    @Override
    public boolean deleteByRef(String ref) {
        var documentPath = resolveDocumentPath(ref);
        var metadataPath = resolveMetadataPath(ref);
        var deleted = false;
        if (!documentExists(documentPath)) {
            return false;
        }
        try {
            if (Files.deleteIfExists(documentPath)) {
                deleted = true;
            }
            Files.deleteIfExists(metadataPath);
        } catch (IOException ex) {
            log.error("Error delete blob from local storage", ex);
            var message = String.format("Could not delete blob %s.", ref);
            throw new StoreBlobException(message, ex);
        }
        return deleted;
    }

    @Override
    public UpdateAttributesResponse updateByRef(String ref, UpdateAttributesRequest request) {
        var coreDataPrefix = this.coreDataPrefix;
        var metadataPrefix = this.metadataPrefix;
        var metadataPath = resolveMetadataPath(ref);
        var documentPath = resolveDocumentPath(ref);
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
            storeMetadata(resultMetadata, metadataPath, ref);
        } catch (IOException ex) {
            log.error("Error update blob from local storage", ex);
            var message = String.format("Could not update blob %s.", ref);
            throw new StoreBlobException(message, ex);
        }

        var result = metadataToObject(metadata)
                .storedRef(ref)
                .localFile(documentPath)
                .build();
        return DefaultUpdateAttributesResponse.of(result);
    }

    protected boolean documentExists(Path document) {
        if (Files.notExists(document)) {
            return false;
        }
        try {
            var realPath = document.toRealPath();
            if (!document.toString().equals(realPath.toString())) {
                return false;
            }
            if (!realPath.startsWith(documentRootPath.toRealPath())) {
                return false;
            }
        } catch (IOException ignored) {
        }
        return !Files.isDirectory(document);
    }
}
