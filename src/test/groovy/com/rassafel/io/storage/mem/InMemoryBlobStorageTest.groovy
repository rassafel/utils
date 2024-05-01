package com.rassafel.io.storage.mem

import com.rassafel.io.storage.core.BlobStorageSpecification
import com.rassafel.io.storage.core.NotFoundBlobException
import com.rassafel.io.storage.core.impl.keygen.StaticKeyGenerator
import com.rassafel.io.storage.core.query.impl.DefaultStoreBlobRequest
import com.rassafel.io.storage.core.query.impl.DefaultUpdateAttributesRequest
import spock.lang.Shared
import spock.lang.Stepwise
import spock.util.time.MutableClock

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

@Stepwise
class InMemoryBlobStorageTest extends BlobStorageSpecification {
    @Shared
    LocalDateTime now = LocalDateTime.of(LocalDate.of(2024, 4, 30), LocalTime.MIDNIGHT)
    @Shared
    LocalDateTime tickedNow = now
    @Shared
    MutableClock clock = new MutableClock(now.toInstant(ZoneOffset.UTC), ZoneOffset.UTC)

    @Shared
    def staticKey = "static"
    @Shared
    def keyGen = new StaticKeyGenerator(staticKey)
    def body = "Test body"
    def name = "test.txt"
    def expectedRef = "/${staticKey}.txt"

    @Shared
    def storage = new InMemoryBlobStorage(keyGen, clock)

    def "before store check exists"() {
        when:
        def deleteRes = storage.deleteByRef(expectedRef)

        then:
        !deleteRes
        existsCheck(false, expectedRef)
    }

    def "before store update"() {
        given:
        def request = DefaultUpdateAttributesRequest.builder()
            .attribute("X-Replace-Meta", "Value4")
            .attribute("X-New-Meta", "Value5")
            .removeAttribute("X-Delete-Meta")
            .build()

        when:
        def response = storage.updateByRef(expectedRef, request)
            .getStoredObject()

        then:
        thrown(NotFoundBlobException)
    }

    def "before store and update check exists"() {
        when:
        def deleteRes = storage.deleteByRef(expectedRef)

        then:
        !deleteRes
        existsCheck(false, expectedRef)
    }

    def "store"() {
        given:
        def request = DefaultStoreBlobRequest.builder()
            .originalName(name)
            .attribute("X-Meta", "Value1")
            .attribute("X-Replace-Meta", "Value2")
            .attribute("X-Delete-Meta", "Value3")
            .build()

        when:
        def response = storage.store(toInputStream(body), request)

        then:
        response != null
        verifyAll(response.getStoredObject()) {
            getAttribute("X-Meta") == "Value1"
            getAttribute("X-Replace-Meta") == "Value2"
            getAttribute("X-Delete-Meta") == "Value3"
            getContentType() == "text/plain"
            getOriginalName() == name
            blobToString(it) == body
            getUploadedAt() == now
            getLastModifiedAt() == now
            getSize() == getBytesSize(body)
            getStoredRef() == expectedRef
        }
    }

    def "after store check exists"() {
        expect:
        existsCheck(true, expectedRef)
    }

    def "update after store"() {
        given:
        tickedNow = now.plusSeconds(10)
        clock.setInstant(tickedNow.toInstant(ZoneOffset.UTC))
        def request = DefaultUpdateAttributesRequest.builder()
            .attribute("X-Replace-Meta", "Value4")
            .attribute("X-New-Meta", "Value5")
            .removeAttribute("X-Delete-Meta")
            .build()

        when:
        def response = storage.updateByRef(expectedRef, request)

        then:
        response != null
        verifyAll(response.getStoredObject()) {
            getAttribute("X-Delete-Meta") == null
            getAttribute("X-Meta") == "Value1"
            getAttribute("X-Replace-Meta") == "Value4"
            getAttribute("X-New-Meta") == "Value5"
            getContentType() == "text/plain"
            getOriginalName() == name
            blobToString(it) == body
            getUploadedAt() == now
            getLastModifiedAt() == tickedNow
            getSize() == getBytesSize(body)
            getStoredRef() == expectedRef
        }
    }

    def "after update check exists"() {
        expect:
        existsCheck(true, expectedRef)
    }

    def "delete"() {
        when:
        def result = storage.deleteByRef(expectedRef)

        then:
        result
    }

    def "after delete check exists"() {
        expect:
        existsCheck(false, expectedRef)
    }

    def "after delete again delete"() {
        when:
        def result = storage.deleteByRef(expectedRef)

        then:
        !result
    }

    def "after delete update"() {
        given:
        def request = DefaultUpdateAttributesRequest.builder()
            .attribute("X-Fail-Edit", "Value6")
            .build()

        when:
        def response = storage.updateByRef(expectedRef, request)

        then:
        thrown(NotFoundBlobException)
    }

    def existsCheck(boolean expected, String ref) {
        def obj = storage.getByRef(ref)
        def objOpt = storage.findByRef(ref)
        def exists = storage.existsByRef(ref)

        (obj != null) == expected && exists == expected && objOpt.isPresent() == expected
    }
}
