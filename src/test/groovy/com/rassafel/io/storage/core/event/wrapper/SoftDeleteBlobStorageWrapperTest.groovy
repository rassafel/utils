package com.rassafel.io.storage.core.event.wrapper

import com.rassafel.io.storage.core.BlobStorage
import com.rassafel.io.storage.core.BlobStorageSpecification
import com.rassafel.io.storage.core.NotFoundBlobException
import com.rassafel.io.storage.core.event.BlobEventPublisher
import com.rassafel.io.storage.core.event.type.RestoreSoftDeletedBlobEvent
import com.rassafel.io.storage.core.event.type.SoftDeleteBlobEvent
import com.rassafel.io.storage.core.query.impl.*
import spock.util.time.MutableClock

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

class SoftDeleteBlobStorageWrapperTest extends BlobStorageSpecification {
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

    TestStoredBlobObject.Builder deletedBlobBuilder = TestStoredBlobObject.builder(blobBuilder.build())
        .attribute(SoftDeleteBlobStorageWrapper.DEFAULT_DELETED_ATTRIBUTE, now.toString())

    BlobStorage delegate = Mock()
    BlobEventPublisher publisher = Mock()
    def storage = new SoftDeleteBlobStorageWrapper(delegate, clock, publisher)

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
        0 * _
        response.is(expResponse)
    }

    def "exists"() {
        when:
        def response = storage.existsByRef(ref)

        then:
        1 * delegate.findByRef(ref) >> Optional.of(blobBuilder.build())
        0 * _
        response
    }

    def "exists if soft deleted"() {
        when:
        def response = storage.existsByRef(ref)

        then:
        1 * delegate.findByRef(ref) >> Optional.of(deletedBlobBuilder.build())
        0 * _
        !response
    }

    def "exists if not found"() {
        when:
        def response = storage.existsByRef(ref)

        then:
        1 * delegate.findByRef(ref) >> Optional.empty()
        0 * _
        !response
    }

    def "exists ignore deleted"() {
        when:
        def response = storage.existsByRefIgnoreDeleted(ref)

        then:
        1 * delegate.existsByRef(ref) >> true
        0 * _
        response
    }

    def "exists ignore deleted if soft deleted"() {
        when:
        def response = storage.existsByRefIgnoreDeleted(ref)

        then:
        1 * delegate.existsByRef(ref) >> true
        0 * _
        response
    }

    def "exists ignore deleted if not found"() {
        when:
        def response = storage.existsByRefIgnoreDeleted(ref)

        then:
        1 * delegate.existsByRef(ref) >> false
        0 * _
        !response
    }

    def "get"() {
        given:
        def blob = blobBuilder.build()

        when:
        def response = storage.getByRef(ref)

        then:
        1 * delegate.getByRef(ref) >> blob
        0 * _
        response.is(blob)
    }

    def "get if soft deleted"() {
        when:
        def response = storage.getByRef(ref)

        then:
        1 * delegate.getByRef(ref) >> deletedBlobBuilder.build()
        0 * _
        response == null
    }

    def "get if not found"() {
        when:
        def response = storage.getByRef(ref)

        then:
        1 * delegate.getByRef(ref) >> null
        0 * _
        response == null
    }

    def "get ignore deleted"() {
        given:
        def blob = blobBuilder.build()

        when:
        def response = storage.getByRefIgnoreDeleted(ref)

        then:
        1 * delegate.getByRef(ref) >> blob
        0 * _
        response.is(blob)
    }

    def "get ignore deleted if soft deleted"() {
        given:
        def blob = deletedBlobBuilder.build()

        when:
        def response = storage.getByRefIgnoreDeleted(ref)

        then:
        1 * delegate.getByRef(ref) >> blob
        0 * _
        response.is(blob)
    }

    def "get ignore deleted if not found"() {
        when:
        def response = storage.getByRef(ref)

        then:
        1 * delegate.getByRef(ref) >> null
        0 * _
        response == null
    }

    def "find"() {
        given:
        def blob = blobBuilder.build()

        when:
        def response = storage.findByRef(ref)

        then:
        1 * delegate.findByRef(ref) >> Optional.of(blob)
        0 * _
        response.isPresent()
        response.get().is(blob)
    }

    def "find if soft deleted"() {
        when:
        def response = storage.findByRef(ref)

        then:
        1 * delegate.findByRef(ref) >> Optional.of(deletedBlobBuilder.build())
        0 * _
        response.isEmpty()
    }

    def "find if not found"() {
        when:
        def response = storage.findByRef(ref)

        then:
        1 * delegate.findByRef(ref) >> Optional.empty()
        0 * _
        response.isEmpty()
    }

    def "find ignore deleted"() {
        given:
        def blob = blobBuilder.build()

        when:
        def response = storage.findByRefIgnoreDeleted(ref)

        then:
        1 * delegate.findByRef(ref) >> Optional.of(blob)
        0 * _
        response.isPresent()
        response.get().is(blob)
    }

    def "find ignore deleted if soft deleted"() {
        given:
        def blob = deletedBlobBuilder.build()

        when:
        def response = storage.findByRefIgnoreDeleted(ref)

        then:
        1 * delegate.findByRef(ref) >> Optional.of(blob)
        0 * _
        response.isPresent()
        response.get().is(blob)
    }

    def "find ignore deleted if not found"() {
        when:
        def response = storage.findByRefIgnoreDeleted(ref)

        then:
        1 * delegate.findByRef(ref) >> Optional.empty()
        0 * _
        response.isEmpty()
    }

    def "delete"() {
        when:
        def response = storage.deleteByRef(ref)

        then:
        1 * delegate.findByRef(ref) >> Optional.of(blobBuilder.build())
        1 * delegate.updateByRef(ref, _)
        1 * publisher.publish({
            it instanceof SoftDeleteBlobEvent && it.ref == ref
        })
        0 * _
        response
    }

    def "delete but concurrency deleted"() {
        when:
        def response = storage.deleteByRef(ref)

        then:
        1 * delegate.findByRef(ref) >> Optional.of(blobBuilder.build())
        1 * delegate.updateByRef(ref, _) >> { throw new NotFoundBlobException(ref) }
        0 * _
        !response
    }

    def "delete if soft deleted"() {
        when:
        def response = storage.deleteByRef(ref)

        then:
        1 * delegate.findByRef(ref) >> Optional.of(deletedBlobBuilder.build())
        0 * _
        !response
    }

    def "delete if not found"() {
        when:
        def response = storage.deleteByRef(ref)

        then:
        1 * delegate.findByRef(ref) >> Optional.empty()
        0 * _
        !response
    }

    def "hard delete"() {
        when:
        def response = storage.hardDeleteByRef(ref)

        then:
        1 * delegate.deleteByRef(ref) >> true
        0 * _
        response
    }

    def "hard delete if soft deleted"() {
        when:
        def response = storage.hardDeleteByRef(ref)

        then:
        1 * delegate.deleteByRef(ref) >> true
        0 * _
        response
    }

    def "hard delete if not found"() {
        when:
        def response = storage.hardDeleteByRef(ref)

        then:
        1 * delegate.deleteByRef(ref) >> false
        0 * _
        !response
    }

    def "update"() {
        given:
        def request = DefaultUpdateAttributesRequest.builder()
            .build()
        def blob = blobBuilder.build()
        def expResponse = new DefaultUpdateAttributesResponse(blob)

        when:
        def response = storage.updateByRef(ref, request)

        then:
        1 * delegate.findByRef(ref) >> Optional.of(blob)
        1 * delegate.updateByRef(ref, _) >> expResponse
        0 * _
        response.is(expResponse)
    }

    def "update if soft deleted"() {
        given:
        def request = DefaultUpdateAttributesRequest.builder()
            .build()

        when:
        def response = storage.updateByRef(ref, request)

        then:
        thrown(NotFoundBlobException)
        1 * delegate.findByRef(ref) >> Optional.of(deletedBlobBuilder.build())
        0 * _
    }

    def "update if not found"() {
        given:
        def request = DefaultUpdateAttributesRequest.builder()
            .build()

        when:
        def response = storage.updateByRef(ref, request)

        then:
        thrown(NotFoundBlobException)
        1 * delegate.findByRef(ref) >> Optional.empty()
        0 * _
    }

    def "update ignore deleted"() {
        given:
        def request = DefaultUpdateAttributesRequest.builder()
            .build()

        when:
        def response = storage.updateByRefIgnoreDeleted(ref, request)

        then:
        1 * delegate.updateByRef(ref, _)
        0 * _
    }

    def "update ignore deleted if soft deleted"() {
        given:
        def request = DefaultUpdateAttributesRequest.builder()
            .build()

        when:
        def response = storage.updateByRefIgnoreDeleted(ref, request)

        then:
        1 * delegate.updateByRef(ref, _)
        0 * _
    }

    def "update ignore deleted if not found"() {
        given:
        def request = DefaultUpdateAttributesRequest.builder()
            .build()

        when:
        def response = storage.updateByRefIgnoreDeleted(ref, request)

        then:
        thrown(NotFoundBlobException)
        1 * delegate.updateByRef(ref, _) >> { throw new NotFoundBlobException(ref) }
        0 * _
    }

    def "restore"() {
        when:
        def response = storage.restoreDeletedByRef(ref)

        then:
        1 * delegate.findByRef(ref) >> Optional.of(blobBuilder.build())
        0 * _
        !response
    }

    def "restore if soft deleted"() {
        when:
        def response = storage.restoreDeletedByRef(ref)

        then:
        1 * delegate.findByRef(ref) >> Optional.of(deletedBlobBuilder.build())
        1 * delegate.updateByRef(ref, _)
        1 * publisher.publish({
            it instanceof RestoreSoftDeletedBlobEvent && it.ref == ref
        })
        0 * _
        response
    }

    def "restore if soft deleted but concurrency deleted"() {
        when:
        def response = storage.restoreDeletedByRef(ref)

        then:
        thrown(NotFoundBlobException)
        1 * delegate.findByRef(ref) >> Optional.of(deletedBlobBuilder.build())
        1 * delegate.updateByRef(ref, _) >> { throw new NotFoundBlobException(ref) }
        0 * _
    }

    def "restore if not found"() {
        when:
        def response = storage.restoreDeletedByRef(ref)

        then:
        thrown(NotFoundBlobException)
        1 * delegate.findByRef(ref) >> Optional.empty()
        0 * _
    }
}
