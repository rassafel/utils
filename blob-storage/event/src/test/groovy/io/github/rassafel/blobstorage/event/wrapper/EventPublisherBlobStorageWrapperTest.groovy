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

package io.github.rassafel.blobstorage.event.wrapper

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

import spock.lang.Specification
import spock.util.time.MutableClock

import io.github.rassafel.blobstorage.core.BlobStorage
import io.github.rassafel.blobstorage.core.NotFoundBlobException
import io.github.rassafel.blobstorage.core.query.impl.DefaultStoreBlobRequest
import io.github.rassafel.blobstorage.core.query.impl.DefaultStoreBlobResponse
import io.github.rassafel.blobstorage.core.query.impl.DefaultUpdateAttributesRequest
import io.github.rassafel.blobstorage.core.query.impl.DefaultUpdateAttributesResponse
import io.github.rassafel.blobstorage.event.BlobEventPublisher
import io.github.rassafel.blobstorage.event.type.HardDeleteBlobEvent
import io.github.rassafel.blobstorage.event.type.UpdateAttributesBlobEvent
import io.github.rassafel.blobstorage.event.type.UploadBlobEvent
import io.github.rassafel.blobstorage.test.TestStoredBlobObject

import static io.github.rassafel.blobstorage.BlobStorageTestUtils.toInputStream

class EventPublisherBlobStorageWrapperTest extends Specification {
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
