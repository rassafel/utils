package com.rassafel.io.storage.core.impl

import com.rassafel.io.storage.core.StoredBlobObject
import com.rassafel.io.storage.core.impl.keygen.StaticKeyGenerator
import com.rassafel.io.storage.core.query.StoreBlobRequest
import com.rassafel.io.storage.core.query.StoreBlobResponse
import com.rassafel.io.storage.core.query.UpdateAttributesRequest
import com.rassafel.io.storage.core.query.UpdateAttributesResponse
import com.rassafel.io.storage.core.query.impl.DefaultStoreBlobRequest
import com.rassafel.io.storage.core.util.FileTypeUtils
import spock.lang.Specification

class AbstractBlobStorageTest extends Specification {
    def staticKey = "static"
    def storage = new AbstractBlobStorage(new StaticKeyGenerator(staticKey)) {
        public String blobKey;
        public StoreBlobRequest request;

        @Override
        protected StoreBlobResponse store(String blobKey, InputStream inputStream, StoreBlobRequest request) {
            this.blobKey = blobKey;
            this.request = request
            return null
        }

        @Override
        StoredBlobObject getByRef(String ref) {
            throw new UnsupportedOperationException();
        }

        @Override
        boolean deleteByRef(String ref) {
            throw new UnsupportedOperationException();
        }

        @Override
        UpdateAttributesResponse updateByRef(String ref, UpdateAttributesRequest request) {
            throw new UnsupportedOperationException();
        }
    }

    def "Store without contentType and without name extension"() {
        given:
        def request = DefaultStoreBlobRequest.builder()
            .originalName(name)
            .attribute("X-Meta", "Value")
            .size(1).build()
        def is = new ByteArrayInputStream(new byte[]{0});
        when:
        storage.store(is, request)

        then:
        verifyAll(storage) {
            it.request.contentType == FileTypeUtils.DEFAULT_MIME_TYPE.toString()
            blobKey == "$KeyGenerator.SEPARATOR$staticKey"
        }

        where:
        name << ["test", "", null]
    }

    def "Store without contentType and with name extension"() {
        given:
        def originalName = "${name}.$extension"
        def request = DefaultStoreBlobRequest.builder()
            .originalName(originalName)
            .attribute("X-Meta", "Value")
            .size(1).build()
        def is = new ByteArrayInputStream(new byte[]{0});
        when:
        storage.store(is, request)

        then:
        verifyAll(storage) {
            it.request.contentType == contentType
            blobKey == "$KeyGenerator.SEPARATOR${staticKey}.$extension"
        }

        where:
        name   | extension | contentType
        "test" | "txt"     | "text/plain"
        ""     | "txt"     | "text/plain"
    }

    def "Store with contentType and with name extension"() {
        given:
        def originalName = "${name}.$extension"
        def request = DefaultStoreBlobRequest.builder()
            .originalName(originalName)
            .contentType(contentType)
            .attribute("X-Meta", "Value")
            .size(1).build()
        def is = new ByteArrayInputStream(new byte[]{0});
        when:
        storage.store(is, request)

        then:
        verifyAll(storage) {
            it.request.contentType == contentType
            blobKey == "$KeyGenerator.SEPARATOR${staticKey}.$extension"
        }

        where:
        name   | extension | contentType
        "test" | "txt"     | "application/json"
        ""     | "txt"     | "application/json"
    }

    def "Store with contentType and without name extension"() {
        given:
        def request = DefaultStoreBlobRequest.builder()
            .originalName(name)
            .contentType(contentType)
            .attribute("X-Meta", "Value")
            .size(1).build()
        def is = new ByteArrayInputStream(new byte[]{0});
        when:
        storage.store(is, request)

        then:
        verifyAll(storage) {
            it.request.contentType == contentType
            blobKey == "$KeyGenerator.SEPARATOR$staticKey"
        }

        where:
        name   | contentType
        "test" | "application/json"
        ""     | "application/json"
        null   | "application/json"
    }
}
