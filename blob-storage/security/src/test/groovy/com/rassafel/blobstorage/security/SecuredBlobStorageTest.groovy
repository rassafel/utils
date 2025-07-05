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

package com.rassafel.blobstorage.security

import spock.lang.Shared
import spock.lang.Specification

import com.rassafel.blobstorage.core.BlobStorage
import com.rassafel.blobstorage.core.NotFoundBlobException
import com.rassafel.blobstorage.core.query.StoreBlobRequest
import com.rassafel.blobstorage.core.query.impl.DefaultStoreBlobRequest
import com.rassafel.blobstorage.core.query.impl.DefaultStoreBlobResponse
import com.rassafel.blobstorage.core.query.impl.DefaultUpdateAttributesRequest
import com.rassafel.blobstorage.core.query.impl.DefaultUpdateAttributesResponse
import com.rassafel.blobstorage.test.TestStoredBlobObject

import static com.rassafel.blobstorage.BlobStorageTestUtils.toInputStream

class SecuredBlobStorageTest extends Specification {
    def blobStorage = Mock(BlobStorage)
    def policyAttribute = SecuredBlobStorage.DEFAULT_POLICY_ATTRIBUTE
    def delegate = new SecuredBlobStorage(blobStorage, policyAttribute, new StaticBlobPolicyHandler(false), [
            "attributes": new TestBlobPolicyHandler([
                    "XS-Custom-Header"  : "test",
                    "XS-Conflict-Header": "conflict"
            ]),
            "denied"    : new StaticBlobPolicyHandler(false),
            "allowed"   : new StaticBlobPolicyHandler(true),
    ])

    @Shared
    def ref = "/static/test"

    TestStoredBlobObject.Builder blobBuilder = TestStoredBlobObject.builder()
            .originalName("test.txt")
            .attribute("X-Meta", "Value1")
            .contentType("text/plain")
            .storedRef(ref)

    TestStoredBlobObject.Builder attributedBlobBuilder = TestStoredBlobObject.builder(blobBuilder.build())
            .attribute(policyAttribute, "attributes")
            .attribute("XS-Custom-Header", "test")
            .attribute("XS-Conflict-Header", "conflict")

//region store test
    def storeRequest = DefaultStoreBlobRequest.builder()
            .originalName("test.txt")
            .attribute("X-Meta", "Value1")
            .build()

    def "store by default policy"() {
        when:
        delegate.store(toInputStream("Test"), storeRequest)

        then:
        thrown(StoreBlobDeniedException)
        0 * blobStorage.store(_, _)
    }

    def "store by attributes policy"() {
        given:
        def captured

        when:
        def actual = delegate.store(toInputStream("Test"), storeRequest.toBuilder()
                .attribute(policyAttribute, "attributes")
                .attribute("XS-Conflict-Header", "MyAttribute")
                .build())

        then:
        1 * blobStorage.store(_, _) >> { arguments ->
            captured = arguments[1]
            return DefaultStoreBlobResponse.of(attributedBlobBuilder.build())
        }
        0 * _
        verifyAll(captured as StoreBlobRequest) {
            it.getAttributes().get("X-Meta") == "Value1"
            it.getAttributes().get("XS-Custom-Header") == "test"
            it.getAttributes().get("XS-Conflict-Header") == "conflict"
        }
        verifyAll(actual) {
            verifyAll(it.getStoredObject()) {
                it.originalName == "test.txt"
                it.contentType == "text/plain"
                it.storedRef == ref
                it.getAttribute("X-Meta") == "Value1"
                it.getAttribute(policyAttribute) == "attributes"
                it.getAttribute("XS-Custom-Header") == null
                it.getAttribute("XS-Conflict-Header") == null
                it.getAttributes().size() == 2
                it.getAttributes().get("X-Meta") == "Value1"
                it.getAttributes().get(policyAttribute) == "attributes"
            }
        }
    }

    def "store by denied policy"() {
        when:
        delegate.store(toInputStream("Test"), storeRequest.toBuilder()
                .attribute(policyAttribute, "denied")
                .build())

        then:
        thrown(StoreBlobDeniedException)
        0 * _
    }

    def "store by allowed policy"() {
        given:
        def expectedBlob = blobBuilder.build()

        when:
        def actual = delegate.store(toInputStream("Test"), storeRequest.toBuilder()
                .attribute(policyAttribute, "allowed")
                .build())

        then:
        1 * blobStorage.store(_, _) >> DefaultStoreBlobResponse.of(expectedBlob)
        0 * _
        verifyAll(actual) {
            it.storedObject.is(expectedBlob)
        }
    }

    def "store with ex"() {
        when:
        delegate.store(toInputStream("Test"), storeRequest.toBuilder()
                .attribute(policyAttribute, "allowed")
                .build())

        then:
        def ex = thrown(RuntimeException)
        ex.message == "test"
        1 * blobStorage.store(_, _) >> { throw new RuntimeException("test") }
        0 * _
    }
//endregion
//region update test
    def updateRequest = DefaultUpdateAttributesRequest.builder()
            .attribute("X-Test", "Value")
            .build()

    def "update is override security policy"() {
        given:
        def stored = blobBuilder
                .attribute(policyAttribute, "allowed")
                .build()
        def captured

        when:
        def actual = delegate.updateByRef(ref, updateRequest.toBuilder()
                .attribute(policyAttribute, "denied")
                .build())

        then:
        1 * blobStorage.getByRef(ref) >> stored
        1 * blobStorage.updateByRef(ref, _) >> { arguments ->
            captured = arguments[1]
            return DefaultUpdateAttributesResponse.of(stored.toBuilder()
                    .attribute(policyAttribute, "denied")
                    .attribute("X-Test", "Value")
                    .build())
        }
        0 * _
        verifyAll(actual.storedObject) {
            it.originalName == "test.txt"
            it.contentType == "text/plain"
            it.storedRef == ref
            it.getAttribute("X-Meta") == "Value1"
            it.getAttribute("X-Test") == "Value"
            it.getAttribute(policyAttribute) == "denied"
            it.getAttributes().size() == 3
            it.getAttributes().get("X-Meta") == "Value1"
            it.getAttributes().get("X-Test") == "Value"
            it.getAttributes().get(policyAttribute) == "denied"
        }
    }

    def "update by default policy"() {
        given:
        def stored = blobBuilder.build()

        when:
        delegate.updateByRef(ref, updateRequest)

        then:
        thrown(NotFoundBlobException)
        1 * blobStorage.getByRef(ref) >> stored
        0 * _
    }

    def "update by attributes policy"() {
        given:
        def stored = attributedBlobBuilder.build()

        when:
        def actual = delegate.updateByRef(ref, updateRequest)

        then:
        1 * blobStorage.getByRef(ref) >> stored
        1 * blobStorage.updateByRef(ref, _) >> DefaultUpdateAttributesResponse.of(stored.toBuilder()
                .attribute("X-Test", "Value")
                .build())
        verifyAll(actual.storedObject) {
            it.originalName == "test.txt"
            it.contentType == "text/plain"
            it.storedRef == ref
            it.getAttribute("X-Meta") == "Value1"
            it.getAttribute("X-Test") == "Value"
            it.getAttribute(policyAttribute) == "attributes"
            it.getAttribute("XS-Custom-Header") == null
            it.getAttribute("XS-Conflict-Header") == null
            it.getAttributes().size() == 3
            it.getAttributes().get("X-Meta") == "Value1"
            it.getAttributes().get("X-Test") == "Value"
            it.getAttributes().get(policyAttribute) == "attributes"
        }
    }

    def "update by denied policy"() {
        given:
        def stored = blobBuilder
                .attribute(policyAttribute, "denied")
                .build()

        when:
        delegate.updateByRef(ref, updateRequest)

        then:
        thrown(NotFoundBlobException)
        1 * blobStorage.getByRef(ref) >> stored
        0 * _
    }

    def "update by allowed policy"() {
        given:
        def stored = blobBuilder
                .attribute(policyAttribute, "allowed")
                .build()

        when:
        def actual = delegate.updateByRef(ref, updateRequest)

        then:
        1 * blobStorage.getByRef(ref) >> stored
        1 * blobStorage.updateByRef(ref, _) >> { DefaultUpdateAttributesResponse.of(stored) }
        0 * _
        verifyAll(actual) {
            it.storedObject.is(stored)
        }
    }

    def "update when not found"() {
        when:
        delegate.updateByRef(ref, updateRequest)

        then:
        thrown(NotFoundBlobException)
        1 * blobStorage.getByRef(ref) >> null
        0 * _
    }

    def "update with ex"() {
        given:
        def stored = blobBuilder
                .attribute(policyAttribute, "allowed")
                .build()

        when:
        delegate.updateByRef(ref, updateRequest)

        then:
        def ex = thrown(RuntimeException)
        ex.message == "test"
        1 * blobStorage.getByRef(ref) >> stored
        1 * blobStorage.updateByRef(ref, _) >> { throw new RuntimeException("test") }
        0 * _
    }
//endregion
//region exists test
    def "exists by default policy"() {
        given:
        def stored = blobBuilder.build()

        when:
        def actual = delegate.existsByRef(ref)

        then:
        1 * blobStorage.findByRef(ref) >> Optional.of(stored)
        0 * _
        !actual
    }

    def "exists by attributes policy"() {
        given:
        def stored = attributedBlobBuilder.build()

        when:
        def actual = delegate.existsByRef(ref)

        then:
        1 * blobStorage.findByRef(ref) >> Optional.of(stored)
        0 * _
        actual
    }

    def "exists by denied policy"() {
        given:
        def stored = blobBuilder
                .attribute(policyAttribute, "denied")
                .build()

        when:
        def actual = delegate.existsByRef(ref)

        then:
        1 * blobStorage.findByRef(ref) >> Optional.of(stored)
        0 * _
        !actual
    }

    def "exists by allowed policy"() {
        given:
        def stored = blobBuilder
                .attribute(policyAttribute, "allowed")
                .build()

        when:
        def actual = delegate.existsByRef(ref)

        then:
        1 * blobStorage.findByRef(ref) >> Optional.of(stored)
        0 * _
        actual
    }

    def "exists when not found"() {
        when:
        def actual = delegate.existsByRef(ref)

        then:
        1 * blobStorage.findByRef(ref) >> Optional.empty()
        0 * _
        !actual
    }

    def "exists with ex"() {
        when:
        delegate.existsByRef(ref)

        then:
        def ex = thrown(RuntimeException)
        ex.message == "test"
        1 * blobStorage.findByRef(ref) >> { throw new RuntimeException("test") }
        0 * _
    }
//endregion
//region delete test
    def "delete by default policy"() {
        given:
        def stored = blobBuilder.build()

        when:
        def actual = delegate.deleteByRef(ref)

        then:
        1 * blobStorage.getByRef(ref) >> stored
        0 * _
        !actual
    }

    def "delete by attributes policy"() {
        given:
        def stored = attributedBlobBuilder.build()

        when:
        def actual = delegate.deleteByRef(ref)

        then:
        1 * blobStorage.getByRef(ref) >> stored
        1 * blobStorage.deleteByRef(ref) >> true
        0 * _
        actual
    }

    def "delete by denied policy"() {
        given:
        def stored = blobBuilder
                .attribute(policyAttribute, "denied")
                .build()

        when:
        def actual = delegate.deleteByRef(ref)

        then:
        1 * blobStorage.getByRef(ref) >> stored
        0 * _
        !actual
    }

    def "delete by allowed policy"() {
        given:
        def stored = blobBuilder
                .attribute(policyAttribute, "allowed")
                .build()

        when:
        def actual = delegate.deleteByRef(ref)

        then:
        1 * blobStorage.getByRef(ref) >> stored
        1 * blobStorage.deleteByRef(ref) >> true
        0 * _
        actual
    }

    def "delete with ex"() {
        given:
        def stored = blobBuilder
                .attribute(policyAttribute, "allowed")
                .build()

        when:
        delegate.deleteByRef(ref)

        then:
        def ex = thrown(RuntimeException)
        ex.message == "test"
        1 * blobStorage.getByRef(ref) >> stored
        1 * blobStorage.deleteByRef(ref) >> { throw new RuntimeException("test") }
        0 * _
    }

    def "delete with ex when not found"() {
        when:
        def actual = delegate.deleteByRef(ref)

        then:
        1 * blobStorage.getByRef(ref) >> null
        0 * _
        !actual
    }
//endregion
//region get test
    def "get by default policy"() {
        given:
        def stored = blobBuilder.build()

        when:
        def actual = delegate.getByRef(ref)

        then:
        1 * blobStorage.getByRef(ref) >> stored
        0 * _
        actual == null
    }

    def "get by attributes policy"() {
        given:
        def stored = attributedBlobBuilder.build()

        when:
        def actual = delegate.getByRef(ref)

        then:
        1 * blobStorage.getByRef(ref) >> stored
        0 * _
        verifyAll(actual) {
            it.originalName == "test.txt"
            it.contentType == "text/plain"
            it.storedRef == ref
            it.getAttribute(policyAttribute) == "attributes"
            it.getAttribute("X-Meta") == "Value1"
            it.getAttribute("XS-Custom-Header") == null
            it.getAttribute("XS-Conflict-Header") == null
            it.getAttributes().size() == 2
            it.getAttributes().get("X-Meta") == "Value1"
            it.getAttributes().get(policyAttribute) == "attributes"
        }
    }

    def "get by denied policy"() {
        given:
        def stored = blobBuilder
                .attribute(policyAttribute, "denied")
                .build()

        when:
        def actual = delegate.getByRef(ref)

        then:
        1 * blobStorage.getByRef(ref) >> stored
        0 * _
        actual == null
    }

    def "get by allowed policy"() {
        given:
        def stored = blobBuilder
                .attribute(policyAttribute, "allowed")
                .build()

        when:
        def actual = delegate.getByRef(ref)

        then:
        1 * blobStorage.getByRef(ref) >> stored
        0 * _
        verifyAll(actual) {
            it.is(stored)
        }
    }

    def "get when not found"() {
        when:
        def actual = delegate.getByRef(ref)

        then:
        1 * blobStorage.getByRef(ref) >> null
        actual == null
    }

    def "get with ex"() {
        when:
        delegate.getByRef(ref)

        then:
        def ex = thrown(RuntimeException)
        ex.message == "test"
        1 * blobStorage.getByRef(ref) >> { throw new RuntimeException("test") }
        0 * _
    }
//endregion
//region find test
    def "find by default policy"() {
        given:
        def stored = blobBuilder.build()

        when:
        def actual = delegate.findByRef(ref)

        then:
        1 * blobStorage.findByRef(ref) >> Optional.of(stored)
        0 * _
        actual.isEmpty()
    }

    def "find by attributes policy"() {
        given:
        def stored = attributedBlobBuilder.build()

        when:
        def actual = delegate.findByRef(ref)

        then:
        1 * blobStorage.findByRef(ref) >> Optional.of(stored)
        0 * _
        actual.isPresent()
        verifyAll(actual.get()) {
            it.originalName == "test.txt"
            it.contentType == "text/plain"
            it.storedRef == ref
            it.getAttribute(policyAttribute) == "attributes"
            it.getAttribute("X-Meta") == "Value1"
            it.getAttribute("XS-Custom-Header") == null
            it.getAttribute("XS-Conflict-Header") == null
            it.getAttributes().size() == 2
            it.getAttributes().get("X-Meta") == "Value1"
            it.getAttributes().get(policyAttribute) == "attributes"
        }
    }

    def "find by denied policy"() {
        given:
        def stored = blobBuilder
                .attribute(policyAttribute, "denied")
                .build()

        when:
        def actual = delegate.findByRef(ref)

        then:
        1 * blobStorage.findByRef(ref) >> Optional.of(stored)
        0 * _
        actual.isEmpty()
    }

    def "find by allowed policy"() {
        given:
        def stored = blobBuilder
                .attribute(policyAttribute, "allowed")
                .build()

        when:
        def actual = delegate.findByRef(ref)

        then:
        1 * blobStorage.findByRef(ref) >> Optional.of(stored)
        0 * _
        actual.isPresent()
        verifyAll(actual.get()) {
            it.is(stored)
        }
    }

    def "find when not found"() {
        when:
        def actual = delegate.findByRef(ref)

        then:
        1 * blobStorage.findByRef(ref) >> Optional.empty()
        0 * _
        actual.isEmpty()
    }

    def "find with ex"() {
        when:
        delegate.findByRef(ref)

        then:
        def ex = thrown(RuntimeException)
        ex.message == "test"
        1 * blobStorage.findByRef(ref) >> { throw new RuntimeException("test") }
        0 * _
    }
//endregion
}
