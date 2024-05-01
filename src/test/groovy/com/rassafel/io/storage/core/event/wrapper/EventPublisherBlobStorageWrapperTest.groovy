package com.rassafel.io.storage.core.event.wrapper

import com.rassafel.io.storage.core.BlobStorage
import com.rassafel.io.storage.core.BlobStorageSpecification
import com.rassafel.io.storage.core.NotFoundBlobException
import com.rassafel.io.storage.core.event.BlobEventPublisher
import com.rassafel.io.storage.core.event.type.HardDeleteBlobEvent
import com.rassafel.io.storage.core.event.type.UpdateAttributesBlobEvent
import com.rassafel.io.storage.core.event.type.UploadBlobEvent
import com.rassafel.io.storage.core.query.impl.*
import spock.util.time.MutableClock

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

class EventPublisherBlobStorageWrapperTest extends BlobStorageSpecification {
    LocalDateTime now = LocalDateTime.of(LocalDate.of(2024, 4, 30), LocalTime.MIDNIGHT)
    MutableClock clock = new MutableClock(now.toInstant(ZoneOffset.UTC), ZoneOffset.UTC)

    def staticKey = "static"
    def ref = "/${staticKey}.txt"

    TestStoredBlobObject.Builder blobBuilder = TestStoredBlobObject.builder()
        .originalName("test.txt")
        .attribute("X-Meta", "Value1")
        .contentType("text/plain")
        .storedRef(ref)
        .uploadedAt(now)
        .lastModifiedAt(now)

    BlobStorage delegate = Mock()
    BlobEventPublisher publisher = Mock()
    def storage = new EventPublisherBlobStorageWrapper(delegate, clock, publisher)

    def "store"() {
        given:
        def request = DefaultStoreBlobRequest.builder()
            .originalName("test.txt")
            .attribute("X-Meta", "Value1")
            .contentType("text/plain")
            .build()
        def expResponse = new DefaultStoreBlobResponse(blobBuilder.build())

        when:
        def response = storage.store(toInputStream("Test"), request)

        then:
        1 * delegate.store(_, _) >> expResponse
        1 * publisher.publish({
            it instanceof UploadBlobEvent && it.ref == ref
        })
        0 * _
        response.is(expResponse)
    }

    def "exists"() {
        when:
        def response = storage.existsByRef(ref)

        then:
        1 * delegate.existsByRef(ref) >> true
        0 * _
        response
    }

    def "exists if not found"() {
        when:
        def response = storage.existsByRef(ref)

        then:
        1 * delegate.existsByRef(ref) >> false
        0 * _
        !response
    }

    def "get"() {
        given:
        def expBlob = blobBuilder.build()

        when:
        def response = storage.getByRef(ref)

        then:
        1 * delegate.getByRef(ref) >> expBlob
        0 * _
        response != null
        response.is(expBlob)
    }

    def "get if not found"() {
        when:
        def response = storage.getByRef(ref)

        then:
        1 * delegate.getByRef(ref) >> null
        0 * _
        response == null
    }

    def "find"() {
        given:
        def expBlob = blobBuilder.build()

        when:
        def response = storage.findByRef(ref)

        then:
        1 * delegate.findByRef(ref) >> Optional.of(expBlob)
        0 * _
        response.isPresent()
        response.get().is(expBlob)
    }

    def "find if not found"() {
        when:
        def response = storage.findByRef(ref)

        then:
        1 * delegate.findByRef(ref) >> Optional.empty()
        0 * _
        response.isEmpty()
    }

    def "delete"() {
        when:
        def response = storage.deleteByRef(ref)

        then:
        1 * delegate.deleteByRef(ref) >> true
        1 * publisher.publish({
            it instanceof HardDeleteBlobEvent && it.ref == ref
        })
        0 * _
        response
    }

    def "delete if not found"() {
        when:
        def response = storage.deleteByRef(ref)

        then:
        1 * delegate.deleteByRef(ref) >> false
        0 * _
        !response
    }

    def "update"() {
        given:
        def request = DefaultUpdateAttributesRequest.builder()
            .build()
        def expResponse = new DefaultUpdateAttributesResponse(blobBuilder.build())

        when:
        def response = storage.updateByRef(ref, request)

        then:
        1 * delegate.updateByRef(ref, _) >> expResponse
        1 * publisher.publish({
            it instanceof UpdateAttributesBlobEvent && it.ref == ref
        })
        0 * _
        response.is(expResponse)
    }

    def "update if not found"() {
        given:
        def request = DefaultUpdateAttributesRequest.builder()
            .build()

        when:
        def response = storage.updateByRef(ref, request)

        then:
        thrown(NotFoundBlobException)
        1 * delegate.updateByRef(ref, _) >> { throw new NotFoundBlobException(ref) }
        0 * _
    }
}
