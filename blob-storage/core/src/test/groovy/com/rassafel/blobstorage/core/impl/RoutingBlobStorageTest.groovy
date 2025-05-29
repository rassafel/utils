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

package com.rassafel.blobstorage.core.impl


import com.rassafel.blobstorage.test.TestStoredBlobObject
import com.rassafel.blobstorage.core.BlobStorage
import com.rassafel.blobstorage.core.BlobStorageLocator
import com.rassafel.blobstorage.core.query.impl.DefaultStoreBlobRequest
import com.rassafel.blobstorage.core.query.impl.DefaultStoreBlobResponse
import com.rassafel.blobstorage.core.query.impl.DefaultUpdateAttributesRequest
import com.rassafel.blobstorage.core.query.impl.DefaultUpdateAttributesResponse
import spock.lang.Shared
import spock.lang.Specification

import static com.rassafel.blobstorage.BlobStorageTestUtils.toInputStream

class RoutingBlobStorageTest extends Specification {
    BlobStorageLocator locator = Mock()

    def storage = new RoutingBlobStorage(locator)

    @Shared
    def blobKey = "/static/test"
    @Shared
    def storageName = "test"
    @Shared
    def ref = "$storageName${RoutingBlobStorage.DEFAULT_DELIMITER}$blobKey"

    @Shared
    TestStoredBlobObject.Builder blobBuilder = TestStoredBlobObject.builder()
        .originalName("test.txt")
        .attribute("X-Meta", "Value1")
        .contentType("text/plain")
        .storedRef(ref)

//region store test
    def storeRequest = DefaultStoreBlobRequest.builder()
        .originalName("test.txt")
        .attribute("X-Meta", "Value1")
        .build()

    def "store"() {
        given:
        def expectedBlob = blobBuilder.build()

        when:
        def response = storage.store(storageName, toInputStream("Test"), storeRequest)

        then:
        1 * locator.findStorage(storageName) >> Mock(BlobStorage) {
            1 * store(_, _) >> new DefaultStoreBlobResponse(expectedBlob)
        }
        0 * _
        verifyAll(response) {
            it.getStoredObject().is(expectedBlob)
        }
    }

    def "store default"() {
        given:
        def expectedBlob = blobBuilder.build()

        when:
        def response = storage.store(toInputStream("Test"), storeRequest)

        then:
        1 * locator.getDefaultStorage() >> Mock(BlobStorage) {
            1 * store(_, _) >> new DefaultStoreBlobResponse(expectedBlob)
        }
        0 * _
        verifyAll(response) {
            it.getStoredObject().is(expectedBlob)
        }
    }

    def "store with ex"() {
        when:
        def response = storage.store(storageName, toInputStream("Test"), storeRequest)

        then:
        thrown(expectedEx)
        1 * locator.findStorage(storageName) >> locatedStorage
        0 * _

        where:
        expectedEx                    | locatedStorage
        UnsupportedOperationException | Mock(BlobStorage) {
            1 * store(_, _) >> { throw new UnsupportedOperationException() }
        }
        NullPointerException          | null
    }
//endregion
//region update test
    def updateRequest = DefaultUpdateAttributesRequest.builder()
        .attribute("X-Test", "Value")
        .build()

    def "update"() {
        given:
        def expectedBlob = blobBuilder.build()

        when:
        def response = storage.updateByRef(ref, updateRequest)

        then:
        1 * locator.findStorage(storageName) >> Mock(BlobStorage) {
            1 * updateByRef(ref, _) >> new DefaultUpdateAttributesResponse(expectedBlob)
        }
        0 * _
        verifyAll(response) {
            it.getStoredObject().is(expectedBlob)
        }
    }

    def "update default"() {
        given:
        def expectedBlob = blobBuilder.build()

        when:
        def response = storage.updateByRef(blobKey, updateRequest)

        then:
        1 * locator.findStorage(null) >> Mock(BlobStorage) {
            1 * updateByRef(blobKey, _) >> new DefaultUpdateAttributesResponse(expectedBlob)
        }
        0 * _
        verifyAll(response) {
            it.getStoredObject().is(expectedBlob)
        }
    }

    def "update with ex"() {
        when:
        def response = storage.updateByRef(_ref, updateRequest)

        then:
        thrown(expectedEx)
        1 * locator.findStorage(_storageName) >> locatedStorage
        0 * _

        where:
        _storageName | _ref    | expectedEx               | locatedStorage
        storageName  | ref     | IllegalArgumentException | Mock(BlobStorage) {
            1 * updateByRef(ref, _) >> { throw new IllegalArgumentException() }
        }
        storageName  | ref     | NullPointerException     | null
        null         | blobKey | IllegalArgumentException | Mock(BlobStorage) {
            1 * updateByRef(blobKey, _) >> { throw new IllegalArgumentException() }
        }
        null         | blobKey | NullPointerException     | null
    }
//endregion
//region exists test
    def "exists"() {
        when:
        def response = storage.existsByRef(ref)

        then:
        1 * locator.findStorage(storageName) >> Mock(BlobStorage) {
            1 * existsByRef(ref) >> result
        }
        0 * _
        response == result

        where:
        result << [true, false]
    }

    def "exists default"() {
        when:
        def response = storage.existsByRef(blobKey)

        then:
        1 * locator.findStorage(null) >> Mock(BlobStorage) {
            1 * existsByRef(blobKey) >> result
        }
        0 * _
        response == result

        where:
        result << [true, false]
    }

    def "exists with ex"() {
        when:
        def response = storage.existsByRef(_ref)

        then:
        thrown(expectedEx)
        1 * locator.findStorage(_storageName) >> locatedStorage
        0 * _

        where:
        _storageName | _ref    | expectedEx               | locatedStorage
        storageName  | ref     | IllegalArgumentException | Mock(BlobStorage) {
            1 * existsByRef(ref) >> { throw new IllegalArgumentException() }
        }
        storageName  | ref     | NullPointerException     | null
        null         | blobKey | IllegalArgumentException | Mock(BlobStorage) {
            1 * existsByRef(blobKey) >> { throw new IllegalArgumentException() }
        }
        null         | blobKey | NullPointerException     | null
    }
//endregion
//region delete test
    def "delete"() {
        when:
        def response = storage.deleteByRef(ref)

        then:
        1 * locator.findStorage(storageName) >> Mock(BlobStorage) {
            1 * deleteByRef(ref) >> result
        }
        0 * _
        response == result

        where:
        result << [true, false]
    }

    def "delete default"() {
        when:
        def response = storage.deleteByRef(blobKey)

        then:
        1 * locator.findStorage(null) >> Mock(BlobStorage) {
            1 * deleteByRef(blobKey) >> result
        }
        0 * _
        response == result

        where:
        result << [true, false]
    }

    def "delete with ex"() {
        when:
        def response = storage.deleteByRef(_ref)

        then:
        thrown(expectedEx)
        1 * locator.findStorage(_storageName) >> locatedStorage
        0 * _

        where:
        _storageName | _ref    | expectedEx               | locatedStorage
        storageName  | ref     | IllegalArgumentException | Mock(BlobStorage) {
            1 * deleteByRef(ref) >> { throw new IllegalArgumentException() }
        }
        storageName  | ref     | NullPointerException     | null
        null         | blobKey | IllegalArgumentException | Mock(BlobStorage) {
            1 * deleteByRef(blobKey) >> { throw new IllegalArgumentException() }
        }
        null         | blobKey | NullPointerException     | null
    }
//endregion
//region get test
    def "get"() {
        when:
        def response = storage.getByRef(ref)

        then:
        1 * locator.findStorage(storageName) >> Mock(BlobStorage) {
            1 * getByRef(ref) >> blob
        }
        0 * _
        response.is(blob)

        where:
        blob << [null, blobBuilder.build()]
    }

    def "get default"() {
        when:
        def response = storage.getByRef(blobKey)

        then:
        1 * locator.findStorage(null) >> Mock(BlobStorage) {
            1 * getByRef(blobKey) >> blob
        }
        0 * _
        response.is(blob)

        where:
        blob << [null, blobBuilder.build()]
    }

    def "get with ex"() {
        when:
        def response = storage.getByRef(_ref)

        then:
        thrown(expectedEx)
        1 * locator.findStorage(_storageName) >> locatedStorage
        0 * _

        where:
        _storageName | _ref    | expectedEx               | locatedStorage
        storageName  | ref     | IllegalArgumentException | Mock(BlobStorage) {
            1 * getByRef(ref) >> { throw new IllegalArgumentException() }
        }
        storageName  | ref     | NullPointerException     | null
        null         | blobKey | IllegalArgumentException | Mock(BlobStorage) {
            1 * getByRef(blobKey) >> { throw new IllegalArgumentException() }
        }
        null         | blobKey | NullPointerException     | null
    }
//endregion
//region find test
    def "find"() {
        when:
        def response = storage.findByRef(ref)

        then:
        1 * locator.findStorage(storageName) >> Mock(BlobStorage) {
            1 * findByRef(ref) >> blob
        }
        0 * _
        response.is(blob)

        where:
        blob << [Optional.empty(), Optional.of(blobBuilder.build())]
    }

    def "find default"() {
        when:
        def response = storage.findByRef(blobKey)

        then:
        1 * locator.findStorage(null) >> Mock(BlobStorage) {
            1 * findByRef(blobKey) >> blob
        }
        0 * _
        response.is(blob)

        where:
        blob << [Optional.empty(), Optional.of(blobBuilder.build())]
    }

    def "find with ex"() {
        when:
        def response = storage.findByRef(_ref)

        then:
        thrown(expectedEx)
        1 * locator.findStorage(_storageName) >> locatedStorage
        0 * _

        where:
        _storageName | _ref    | expectedEx               | locatedStorage
        storageName  | ref     | IllegalArgumentException | Mock(BlobStorage) {
            1 * findByRef(ref) >> { throw new IllegalArgumentException() }
        }
        storageName  | ref     | NullPointerException     | null
        null         | blobKey | IllegalArgumentException | Mock(BlobStorage) {
            1 * findByRef(blobKey) >> { throw new IllegalArgumentException() }
        }
        null         | blobKey | NullPointerException     | null
    }
//endregion
}
