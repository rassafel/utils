package com.rassafel.io.storage.core.impl

import com.rassafel.io.storage.core.BlobStorage
import com.rassafel.io.storage.core.query.impl.*
import spock.lang.Specification

import java.nio.charset.StandardCharsets

class DelegatingBlobStorageTest extends Specification {
    BlobStorage defaultStorage = Mock()
    BlobStorage storage1 = Mock()
    BlobStorage storage2 = Mock()

    def storage = new DelegatingBlobStorage("default", [
        "default" : defaultStorage,
        "storage1": storage1,
        "storage2": storage2,
    ])

    def defaultRef = "/static/test"

    TestStoredBlobObject defaultBlobObject = TestStoredBlobObject.builder()
        .originalName("test.txt")
        .attribute("X-Meta", "Value1")
        .contentType("text/plain")
        .storedRef(defaultRef)
        .build()

    def "default store"() {
        given:
        def body = "Test body"
        def name = "test.txt"
        def request = DefaultStoreBlobRequest.builder()
            .originalName(name)
            .attribute("X-Meta", "Value1")
            .build()
        defaultStorage.store(_, _) >> new DefaultStoreBlobResponse(defaultBlobObject)

        when:
        def response = storage.store(toInputStream(body), request)

        then:
        0 * storage1.store(_, _)
        0 * storage2.store(_, _)
        verifyAll(response.getStoredObject()) {
            getStoredRef() == "default:$defaultRef"
        }
    }

    def "delegate store"() {
        given:
        def body = "Test body"
        def name = "test.txt"
        def request = DefaultStoreBlobRequest.builder()
            .originalName(name)
            .attribute("X-Meta", "Value1")
            .build()
        storage1.store(_, _) >> new DefaultStoreBlobResponse(defaultBlobObject)

        when:
        def response = storage.store("storage1", toInputStream(body), request)

        then:
        0 * defaultStorage.store(_, _)
        0 * storage2.store(_, _)
        verifyAll(response.getStoredObject()) {
            getStoredRef() == "storage1:$defaultRef"
        }
    }

    def "delegate not found store"() {
        given:
        def body = "Test body"
        def name = "test.txt"
        def request = DefaultStoreBlobRequest.builder()
            .originalName(name)
            .attribute("X-Meta", "Value1")
            .build()

        when:
        def response = storage.store("not_found", toInputStream(body), request)

        then:
        thrown(UnsupportedOperationException)
        0 * defaultStorage.store(_, _)
        0 * storage1.store(_, _)
        0 * storage2.store(_, _)
    }

    def toInputStream(String value) {
        return new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8))
    }

    def fromInputStream(InputStream inputStream) {
        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8)
    }

    def "default update"() {
        given:
        defaultStorage.updateByRef(defaultRef, _) >> new DefaultUpdateAttributesResponse(defaultBlobObject)
        def request = DefaultUpdateAttributesRequest.builder()
            .attribute("X-Test", "Value")
            .build()

        when:
        def response = storage.updateByRef(defaultRef, request)

        then:
        0 * storage1.updateByRef(_, _)
        0 * storage2.updateByRef(_, _)
        verifyAll(response.getStoredObject()) {
            getStoredRef() == "default:$defaultRef"
        }
    }

    def "delegate update"() {
        given:
        storage1.updateByRef(defaultRef, _) >> new DefaultUpdateAttributesResponse(defaultBlobObject)
        def request = DefaultUpdateAttributesRequest.builder()
            .attribute("X-Test", "Value")
            .build()

        when:
        def response = storage.updateByRef("storage1:$defaultRef", request)

        then:
        0 * defaultStorage.updateByRef(_, _)
        0 * storage2.updateByRef(_, _)
        verifyAll(response.getStoredObject()) {
            getStoredRef() == "storage1:$defaultRef"
        }
    }

    def "delegate not found update"() {
        given:
        def request = DefaultUpdateAttributesRequest.builder()
            .attribute("X-Test", "Value")
            .build()

        when:
        def response = storage.updateByRef("not_found:$defaultRef", request)

        then:
        thrown(IllegalArgumentException)
        0 * defaultStorage.updateByRef(_, _)
        0 * storage1.updateByRef(_, _)
        0 * storage2.updateByRef(_, _)
    }

    def "default find"() {
        given:
        defaultStorage.findByRef(defaultRef) >> Optional.of(defaultBlobObject)

        when:
        def obj = storage.findByRef(defaultRef)

        then:
        obj.isPresent()
        verifyAll(obj.get()) {
            getStoredRef() == "default:$defaultRef"
        }
        0 * storage1.findByRef(_)
        0 * storage2.findByRef(_)
    }

    def "delegate find"() {
        given:
        storage1.findByRef(defaultRef) >> Optional.of(defaultBlobObject)

        when:
        def obj = storage.findByRef("storage1:$defaultRef")

        then:
        obj.isPresent()
        verifyAll(obj.get()) {
            getStoredRef() == "storage1:$defaultRef"
        }
        0 * defaultStorage.findByRef(_)
        0 * storage2.findByRef(_)
    }

    def "delegate not found find"() {
        when:
        def response = storage.findByRef("not_found:$defaultRef")

        then:
        thrown(IllegalArgumentException)
        0 * defaultStorage.findByRef(_)
        0 * storage1.findByRef(_)
        0 * storage2.findByRef(_)
    }

    def "default get"() {
        given:
        defaultStorage.getByRef(defaultRef) >> defaultBlobObject

        when:
        def obj = storage.getByRef(defaultRef)

        then:
        obj != null
        verifyAll(obj) {
            getStoredRef() == "default:$defaultRef"
        }
        0 * storage1.getByRef(_)
        0 * storage2.getByRef(_)
    }

    def "delegate get"() {
        given:
        storage1.getByRef(defaultRef) >> defaultBlobObject

        when:
        def obj = storage.getByRef("storage1:$defaultRef")

        then:
        obj != null
        verifyAll(obj) {
            getStoredRef() == "storage1:$defaultRef"
        }
        0 * defaultStorage.findByRef(_)
        0 * storage2.findByRef(_)
    }

    def "delegate not found get"() {
        when:
        def response = storage.getByRef("not_found:$defaultRef")

        then:
        thrown(IllegalArgumentException)
        0 * defaultStorage.getByRef(_)
        0 * storage1.getByRef(_)
        0 * storage2.getByRef(_)
    }

    def "default exists"() {
        given:
        defaultStorage.existsByRef(defaultRef) >> true

        when:
        def obj = storage.existsByRef(defaultRef)

        then:
        obj
        0 * storage1.getByRef(_)
        0 * storage2.getByRef(_)
    }

    def "delegate exists"() {
        given:
        storage1.existsByRef(defaultRef) >> true

        when:
        def obj = storage.existsByRef("storage1:$defaultRef")

        then:
        obj
        0 * defaultStorage.findByRef(_)
        0 * storage2.findByRef(_)
    }

    def "delegate not found exists"() {
        when:
        def response = storage.existsByRef("not_found:$defaultRef")

        then:
        thrown(IllegalArgumentException)
        0 * defaultStorage.existsByRef(_)
        0 * storage1.existsByRef(_)
        0 * storage2.existsByRef(_)
    }

    def "default delete"() {
        given:
        defaultStorage.deleteByRef(defaultRef) >> true

        when:
        def obj = storage.deleteByRef(defaultRef)

        then:
        obj
        0 * storage1.deleteByRef(_)
        0 * storage2.deleteByRef(_)
    }

    def "delegate delete"() {
        given:
        storage1.deleteByRef(defaultRef) >> true

        when:
        def obj = storage.deleteByRef("storage1:$defaultRef")

        then:
        obj
        0 * defaultStorage.deleteByRef(_)
        0 * storage2.deleteByRef(_)
    }

    def "delegate not found delete"() {
        when:
        def response = storage.deleteByRef("not_found:$defaultRef")

        then:
        thrown(IllegalArgumentException)
        0 * defaultStorage.deleteByRef(_)
        0 * storage1.deleteByRef(_)
        0 * storage2.deleteByRef(_)
    }
}
