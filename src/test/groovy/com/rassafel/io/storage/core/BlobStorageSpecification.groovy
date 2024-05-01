package com.rassafel.io.storage.core

import org.springframework.lang.Nullable
import spock.lang.Specification

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

abstract class BlobStorageSpecification extends Specification {
    public final Charset CHARSET = StandardCharsets.UTF_8;

    int getBytesSize(String value) {
        return value.getBytes(CHARSET).length;
    }

    ByteArrayInputStream toInputStream(String value) {
        return new ByteArrayInputStream(value.getBytes(CHARSET));
    }

    @Nullable
    String blobToString(@Nullable StoredBlobObject object) throws IOException {
        if (object == null) return null;
        return fromInputStream(object.toInputStream());
    }

    @Nullable
    String fromInputStream(@Nullable InputStream inputStream) throws IOException {
        if (inputStream == null) return null;
        return new String(inputStream.readAllBytes(), CHARSET);
    }
}
