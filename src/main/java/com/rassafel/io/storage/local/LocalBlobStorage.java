package com.rassafel.io.storage.local;

import com.google.common.collect.Maps;
import com.rassafel.commons.util.Assert;
import com.rassafel.io.storage.core.NotFoundBlobException;
import com.rassafel.io.storage.core.StoreBlobException;
import com.rassafel.io.storage.core.StoredBlobObject;
import com.rassafel.io.storage.core.impl.AbstractBlobStorage;
import com.rassafel.io.storage.core.impl.KeyGenerator;
import com.rassafel.io.storage.core.query.StoreBlobRequest;
import com.rassafel.io.storage.core.query.StoreBlobResponse;
import com.rassafel.io.storage.core.query.UpdateAttributesRequest;
import com.rassafel.io.storage.core.query.UpdateAttributesResponse;
import com.rassafel.io.storage.core.query.impl.DefaultStoreBlobResponse;
import com.rassafel.io.storage.core.query.impl.DefaultUpdateAttributesResponse;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.LinkedCaseInsensitiveMap;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Properties;

public class LocalBlobStorage extends AbstractBlobStorage {
    private static final String METADATA_FILE_SUFFIX = ".metadata.properties";
    protected static final String CONTENT_TYPE_ATTRIBUTE = "Content-Type";
    protected static final String CONTENT_LENGTH_ATTRIBUTE = "Content-Length";
    protected static final Charset METADATA_CHARSET = StandardCharsets.UTF_8;
    protected Path documentRootPath;
    protected Path metadataRootPath;
    protected Clock clock;
    protected final String metadataFileSuffix;

    public LocalBlobStorage(KeyGenerator keyGenerator,
                            String storageDir,
                            String documentDir,
                            String metadataDir,
                            Clock clock) {
        this(keyGenerator, storageDir, documentDir, metadataDir, clock, METADATA_FILE_SUFFIX, CORE_DATA_PREFIX, METADATA_PREFIX);
    }

    public LocalBlobStorage(KeyGenerator keyGenerator,
                            String storageDir,
                            String documentDir,
                            String metadataDir,
                            Clock clock,
                            String metadataFileSuffix,
                            String coreDataPrefix,
                            String metadataPrefix) {
        super(keyGenerator, coreDataPrefix, metadataPrefix);
        Assert.hasText(storageDir, "storageDir must not be empty");
        Assert.hasText(metadataFileSuffix, "metadataFileSuffix must not be empty");
        val storageRoot = Path.of(storageDir);
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
    protected StoreBlobResponse store(String blobKey, InputStream inputStream, StoreBlobRequest request) {
        val documentPath = resolveDocumentPath(blobKey);
        val metadataPath = resolveMetadataPath(blobKey);

        try (val outputStream = Files.newOutputStream(documentPath, StandardOpenOption.CREATE)) {
            val bytes = IOUtils.copyLarge(inputStream, outputStream);

            val blob = fromRequest(request)
                .storedRef(blobKey)
                .localFile(documentPath)
                .size(bytes).build();
            val metadata = objectToMetadata(blob);
            storeMetadata(metadata, metadataPath, blobKey);

            log.debug("The uploading is stored, document path: {}; original name: {}; metadata path: {}",
                documentPath, blob.getOriginalName(), metadataPath);
            return new DefaultStoreBlobResponse(blob);
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

    protected LocalStoredBlobObject.Builder fromRequest(StoreBlobRequest request) {
        val now = LocalDateTime.now(clock);
        return LocalStoredBlobObject.builder(request)
            .uploadedAt(now)
            .lastModifiedAt(now);
    }

    @Override
    protected Map<String, String> objectToMetadata(StoredBlobObject object) {
        val metadata = super.objectToMetadata(object);
        metadata.put(coreDataPrefix + CONTENT_TYPE_ATTRIBUTE, object.getContentType());
        metadata.put(coreDataPrefix + CONTENT_LENGTH_ATTRIBUTE, String.valueOf(object.getSize()));
        return metadata;
    }

    protected void storeMetadata(Map<String, String> metadata, Path path, String blobKey) throws IOException {
        try (val writer = Files.newBufferedWriter(path, METADATA_CHARSET, StandardOpenOption.CREATE)) {
            val metadataProperties = new Properties();
            metadataProperties.putAll(metadata);
            metadataProperties.store(writer, "blob object " + blobKey);
        }
    }

    @Override
    public boolean existsByRef(String ref) {
        val documentPath = resolveDocumentPath(ref);
        return Files.exists(documentPath);
    }

    @Override
    public LocalStoredBlobObject getByRef(String ref) {
        val documentPath = resolveDocumentPath(ref);
        val metadataPath = resolveMetadataPath(ref);

        if (Files.notExists(documentPath)) {
            log.debug("Blob not found, ref: {}; document path: {}", ref, documentPath);
            return null;
        }

        val metadata = loadMetadata(metadataPath);

        return metadataToObject(metadata)
            .storedRef(ref)
            .localFile(documentPath)
            .build();
    }

    protected Map<String, String> loadMetadata(Path path) {
        val properties = new Properties();
        if (Files.exists(path)) {
            try (val metadataReader = Files.newBufferedReader(path, METADATA_CHARSET)) {
                properties.load(metadataReader);
            } catch (IOException ex) {
                log.error("Error loading metadata from local storage, path: {}", path, ex);
            }
        } else {
            log.debug("Metadata not found, path: {}", path);
        }
        if (properties.isEmpty()) return new LinkedCaseInsensitiveMap<>();
        val metadata = new LinkedCaseInsensitiveMap<String>(properties.size());
        for (val propertyName : properties.stringPropertyNames()) {
            metadata.put(propertyName, properties.getProperty(propertyName));
        }
        return metadata;
    }

    protected LocalStoredBlobObject.Builder metadataToObject(Map<String, String> metadata) {
        val coreDataPrefix = this.coreDataPrefix;
        val metadataPrefix = this.metadataPrefix;
        val builder = LocalStoredBlobObject.builder()
            .originalName(metadata.get(coreDataPrefix + ORIGINAL_NAME_ATTRIBUTE))
            .size(Long.parseLong(metadata.get(coreDataPrefix + CONTENT_LENGTH_ATTRIBUTE)))
            .contentType(metadata.get(coreDataPrefix + CONTENT_TYPE_ATTRIBUTE))
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
        val documentPath = resolveDocumentPath(ref);
        val metadataPath = resolveMetadataPath(ref);
        var deleted = false;
        try {
            if (Files.deleteIfExists(documentPath)) {
                deleted = true;
            }
            Files.deleteIfExists(metadataPath);
        } catch (IOException ex) {
            log.error("Error delete blob from local storage", ex);
            val message = String.format("Could not delete blob %s.", ref);
            throw new StoreBlobException(message, ex);
        }
        return deleted;
    }

    @Override
    public UpdateAttributesResponse updateByRef(String ref, UpdateAttributesRequest request) {
        val coreDataPrefix = this.coreDataPrefix;
        val metadataPrefix = this.metadataPrefix;
        val metadataPath = resolveMetadataPath(ref);
        val documentPath = resolveDocumentPath(ref);
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
            storeMetadata(resultMetadata, metadataPath, ref);
        } catch (IOException ex) {
            log.error("Error update blob from local storage", ex);
            val message = String.format("Could not update blob %s.", ref);
            throw new StoreBlobException(message, ex);
        }

        val result = metadataToObject(metadata)
            .storedRef(ref)
            .localFile(documentPath)
            .build();
        return new DefaultUpdateAttributesResponse(result);
    }
}
