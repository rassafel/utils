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
import com.rassafel.blobstorage.core.query.impl.DefaultStoreBlobRequest
import com.rassafel.blobstorage.core.query.impl.DefaultStoreBlobResponse
import com.rassafel.blobstorage.core.query.impl.DefaultUpdateAttributesRequest
import com.rassafel.blobstorage.core.query.impl.DefaultUpdateAttributesResponse
import spock.lang.Specification

import static com.rassafel.blobstorage.BlobStorageTestUtils.toInputStream

class DefaultBlobStorageLocatorBlobStorageTest extends Specification {
    BlobStorage defaultStorage = Mock()
    BlobStorage storage1 = Mock()
    BlobStorage storage2 = Mock()

    DefaultBlobStorageLocator locator = new DefaultBlobStorageLocator("default", [
        "default" : defaultStorage,
        "storage1": storage1,
        "storage2": storage2,
    ])

    def ref = "/static/test"
    def defaultRef = "default${DefaultBlobStorageLocator.DEFAULT_DELIMITER}$ref"
    def storage1Ref = "storage1${DefaultBlobStorageLocator.DEFAULT_DELIMITER}$ref"

    TestStoredBlobObject defaultBlobObject = TestStoredBlobObject.builder()
        .originalName("test.txt")
        .attribute("X-Meta", "Value1")
        .contentType("text/plain")
        .storedRef(ref)
        .build()

//region store test
    def storeRequest = DefaultStoreBlobRequest.builder()
        .originalName("test.txt")
        .attribute("X-Meta", "Value1")
        .build()

    def "get default store"() {
        when:
        def storage = locator.getDefaultStorage()
        def response = storage.store(toInputStream("Test body"), storeRequest)

        then:
        1 * defaultStorage.store(_, _) >> new DefaultStoreBlobResponse(defaultBlobObject)
        0 * storage1.store(_, _)
        0 * storage2.store(_, _)
        verifyAll(response.getStoredObject()) {
            it instanceof DefaultBlobStorageLocator.BlobStorageWrapper.StoredBlobObjectWrapper
            getStoredRef() == "default${DefaultBlobStorageLocator.DEFAULT_DELIMITER}$ref"
        }
    }

    def "find default store"() {
        when:
        def storage = locator.findStorage(null)
        def response = storage.store(toInputStream("Test body"), storeRequest)

        then:
        1 * defaultStorage.store(_, _) >> new DefaultStoreBlobResponse(defaultBlobObject)
        0 * storage1.store(_, _)
        0 * storage2.store(_, _)
        verifyAll(response.getStoredObject()) {
            it instanceof DefaultBlobStorageLocator.BlobStorageWrapper.StoredBlobObjectWrapper
            getStoredRef() == defaultRef
        }
    }

    def "find storage1 store"() {
        when:
        def storage = locator.findStorage("storage1")
        def response = storage.store(toInputStream("Test body"), storeRequest)

        then:
        1 * storage1.store(_, _) >> new DefaultStoreBlobResponse(defaultBlobObject)
        0 * defaultStorage.store(_, _)
        0 * storage2.store(_, _)
        verifyAll(response.getStoredObject()) {
            it instanceof DefaultBlobStorageLocator.BlobStorageWrapper.StoredBlobObjectWrapper
            getStoredRef() == storage1Ref
        }
    }

    def "find unknown store"() {
        when:
        def storage = locator.findStorage("unknown")
        def response = storage.store(toInputStream("Test body"), storeRequest)

        then:
        thrown(UnsupportedOperationException)
        0 * storage1.store(_, _)
        0 * defaultStorage.store(_, _)
        0 * storage2.store(_, _)
    }
//endregion
//region update test
    def updateRequest = DefaultUpdateAttributesRequest.builder()
        .attribute("X-Test", "Value")
        .build()

    def "get default update"() {
        when:
        def storage = locator.getDefaultStorage()
        def response = storage.updateByRef(ref, updateRequest)

        then:
        1 * defaultStorage.updateByRef(ref, _) >> new DefaultUpdateAttributesResponse(defaultBlobObject)
        0 * _
        verifyAll(response.getStoredObject()) {
            it instanceof DefaultBlobStorageLocator.BlobStorageWrapper.StoredBlobObjectWrapper
            getStoredRef() == defaultRef
        }
    }

    def "find default update"() {
        when:
        def storage = locator.findStorage(null)
        def response = storage.updateByRef(ref, updateRequest)

        then:
        1 * defaultStorage.updateByRef(ref, _) >> new DefaultUpdateAttributesResponse(defaultBlobObject)
        0 * _
        verifyAll(response.getStoredObject()) {
            it instanceof DefaultBlobStorageLocator.BlobStorageWrapper.StoredBlobObjectWrapper
            getStoredRef() == defaultRef
        }
    }

    def "find storage1 update"() {
        when:
        def storage = locator.findStorage("storage1")
        def response = storage.updateByRef(ref, updateRequest)

        then:
        1 * storage1.updateByRef(ref, _) >> new DefaultUpdateAttributesResponse(defaultBlobObject)
        0 * _
        verifyAll(response.getStoredObject()) {
            it instanceof DefaultBlobStorageLocator.BlobStorageWrapper.StoredBlobObjectWrapper
            getStoredRef() == storage1Ref
        }
    }

    def "find unknown update"() {
        when:
        def storage = locator.findStorage("unknown")
        def response = storage.updateByRef(ref, updateRequest)

        then:
        thrown(IllegalArgumentException)
        0 * _
    }
//endregion
//region get test
    def "get default get"() {
        when:
        def storage = locator.getDefaultStorage()
        def obj = storage.getByRef(ref)

        then:
        1 * defaultStorage.getByRef(ref) >> defaultBlobObject
        0 * _
        verifyAll(obj) {
            getStoredRef() == defaultRef
        }
    }

    def "find default get"() {
        when:
        def storage = locator.findStorage(null)
        def obj = storage.getByRef(ref)

        then:
        1 * defaultStorage.getByRef(ref) >> defaultBlobObject
        0 * _
        verifyAll(obj) {
            getStoredRef() == defaultRef
        }
    }

    def "find storage1 get"() {
        when:
        def storage = locator.findStorage("storage1")
        def obj = storage.getByRef(ref)

        then:
        1 * storage1.getByRef(ref) >> defaultBlobObject
        0 * _
        verifyAll(obj) {
            getStoredRef() == storage1Ref
        }
    }

    def "find unknown get"() {
        when:
        def storage = locator.findStorage("unknown")
        def obj = storage.getByRef(ref)

        then:
        thrown(IllegalArgumentException)
        0 * _
    }
//endregion
//region find test
    def "get default find"() {
        when:
        def storage = locator.getDefaultStorage()
        def obj = storage.findByRef(ref)

        then:
        1 * defaultStorage.findByRef(ref) >> Optional.of(defaultBlobObject)
        0 * _
        obj.isPresent()
        verifyAll(obj.get()) {
            getStoredRef() == defaultRef
        }
    }

    def "find default find"() {
        when:
        def storage = locator.findStorage(null)
        def obj = storage.findByRef(ref)

        then:
        1 * defaultStorage.findByRef(ref) >> Optional.of(defaultBlobObject)
        0 * _
        obj.isPresent()
        verifyAll(obj.get()) {
            getStoredRef() == defaultRef
        }
    }

    def "find storage1 find"() {
        when:
        def storage = locator.findStorage("storage1")
        def obj = storage.findByRef(ref)

        then:
        1 * storage1.findByRef(ref) >> Optional.of(defaultBlobObject)
        0 * _
        obj.isPresent()
        verifyAll(obj.get()) {
            getStoredRef() == storage1Ref
        }
    }

    def "find unknown find"() {
        when:
        def storage = locator.findStorage("unknown")
        def obj = storage.findByRef(ref)

        then:
        thrown(IllegalArgumentException)
        0 * _
    }
//endregion
//region exist test
    def "get default exists"() {
        when:
        def storage = locator.getDefaultStorage()
        def obj = storage.existsByRef(ref)

        then:
        1 * defaultStorage.existsByRef(ref) >> true
        0 * _
        obj
    }

    def "find default exists"() {
        when:
        def storage = locator.findStorage(null)
        def obj = storage.existsByRef(ref)

        then:
        1 * defaultStorage.existsByRef(ref) >> true
        0 * _
        obj
    }

    def "find storage1 exists"() {
        when:
        def storage = locator.findStorage("storage1")
        def obj = storage.existsByRef(ref)

        then:
        1 * storage1.existsByRef(ref) >> true
        0 * _
        obj
    }

    def "find unknown exists"() {
        when:
        def storage = locator.findStorage("unknown")
        def obj = storage.existsByRef(ref)

        then:
        thrown(IllegalArgumentException)
        0 * _
    }
//endregion
//region delete test
    def "get default delete"() {
        when:
        def storage = locator.getDefaultStorage()
        def obj = storage.deleteByRef(ref)

        then:
        1 * defaultStorage.deleteByRef(ref) >> true
        0 * _
        obj
    }

    def "find default delete"() {
        when:
        def storage = locator.findStorage(null)
        def obj = storage.deleteByRef(ref)

        then:
        1 * defaultStorage.deleteByRef(ref) >> true
        0 * _
        obj
    }

    def "find storage1 delete"() {
        when:
        def storage = locator.findStorage("storage1")
        def obj = storage.deleteByRef(ref)

        then:
        1 * storage1.deleteByRef(ref) >> true
        0 * _
        obj
    }

    def "find unknown delete"() {
        when:
        def storage = locator.findStorage("unknown")
        def obj = storage.deleteByRef(ref)

        then:
        thrown(IllegalArgumentException)
        0 * _
    }
//endregion
}
