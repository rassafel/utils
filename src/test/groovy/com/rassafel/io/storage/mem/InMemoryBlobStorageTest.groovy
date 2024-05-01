package com.rassafel.io.storage.mem

import com.rassafel.io.storage.core.BlobStorageSpecification
import com.rassafel.io.storage.core.NotFoundBlobException
import com.rassafel.io.storage.core.impl.keygen.StaticKeyGenerator
import com.rassafel.io.storage.core.query.impl.DefaultStoreBlobRequest
import com.rassafel.io.storage.core.query.impl.DefaultUpdateAttributesRequest
import spock.lang.Shared
import spock.util.time.MutableClock

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

class InMemoryBlobStorageTest extends BlobStorageSpecification {
    @Shared
    LocalDateTime now = LocalDateTime.of(LocalDate.of(2024, 4, 30), LocalTime.MIDNIGHT)
    MutableClock clock = new MutableClock(now.toInstant(ZoneOffset.UTC), ZoneOffset.UTC)

    def staticKey = "static"
    def keyGen = new StaticKeyGenerator(staticKey)

    def storage = new InMemoryBlobStorage(keyGen, clock)

    def "simple test"() {
        given:
        def body = "Test body"
        def name = "test.txt"
        def expectedRef = "/${staticKey}.txt"

        expect:
        existsCheck(false, expectedRef)

        when:
        def deleteRes = storage.deleteByRef(expectedRef)
        then:
        !deleteRes


        when:
        def storeRequest = DefaultStoreBlobRequest.builder()
            .originalName(name)
            .attribute("X-Meta", "Value1")
            .attribute("X-Replace-Meta", "Value2")
            .attribute("X-Delete-Meta", "Value3")
            .build()
        def storeResponse = storage.store(toInputStream(body), storeRequest)
            .getStoredObject()

        then:
        verifyAll(storeResponse) {
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

        when:
        def tickedNow = now.plusSeconds(10)
        clock.setInstant(tickedNow.toInstant(ZoneOffset.UTC))
        def updateRequest = DefaultUpdateAttributesRequest.builder()
            .attribute("X-Replace-Meta", "Value4")
            .attribute("X-New-Meta", "Value5")
            .removeAttribute("X-Delete-Meta")
            .build()
        def updateResponse = storage.updateByRef(expectedRef, updateRequest)
            .getStoredObject()
        then:
        verifyAll(updateResponse) {
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

        expect:
        existsCheck(true, expectedRef)

        when:
        deleteRes = storage.deleteByRef(expectedRef)
        then:
        deleteRes

        expect:
        existsCheck(false, expectedRef)

        when:
        deleteRes = storage.deleteByRef(expectedRef)
        then:
        !deleteRes

        when:
        tickedNow = tickedNow.plusSeconds(10)
        clock.setInstant(tickedNow.toInstant(ZoneOffset.UTC))
        updateRequest = DefaultUpdateAttributesRequest.builder()
            .attribute("X-Fail-Edit", "Value6")
            .build()
        updateResponse = storage.updateByRef(expectedRef, updateRequest)
            .getStoredObject()
        then:
        thrown NotFoundBlobException
    }

    def existsCheck(boolean expected, String ref) {
        def obj = storage.getByRef(ref)
        def objOpt = storage.findByRef(ref)
        def exists = storage.existsByRef(ref)

        (obj != null) == expected && exists == expected && objOpt.isPresent() == expected
    }
}
