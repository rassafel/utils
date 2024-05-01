package com.rassafel.io.storage.core.impl

import com.rassafel.io.storage.core.BlobStorage
import com.rassafel.io.storage.core.BlobStorageSpecification
import com.rassafel.io.storage.core.query.impl.*

class DefaultBlobStorageLocatorBlobStorageTest extends BlobStorageSpecification {
    BlobStorage defaultStorage = Mock()
    BlobStorage storage1 = Mock()
    BlobStorage storage2 = Mock()

    DefaultBlobStorageLocator locator = new DefaultBlobStorageLocator("default", [
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
            getStoredRef() == "default:$defaultRef"
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
            getStoredRef() == "default:$defaultRef"
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
            getStoredRef() == "storage1:$defaultRef"
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
        def response = storage.updateByRef(defaultRef, updateRequest)

        then:
        1 * defaultStorage.updateByRef(defaultRef, _) >> new DefaultUpdateAttributesResponse(defaultBlobObject)
        0 * _
        verifyAll(response.getStoredObject()) {
            it instanceof DefaultBlobStorageLocator.BlobStorageWrapper.StoredBlobObjectWrapper
            getStoredRef() == "default:$defaultRef"
        }
    }

    def "find default update"() {
        when:
        def storage = locator.findStorage(null)
        def response = storage.updateByRef(defaultRef, updateRequest)

        then:
        1 * defaultStorage.updateByRef(defaultRef, _) >> new DefaultUpdateAttributesResponse(defaultBlobObject)
        0 * _
        verifyAll(response.getStoredObject()) {
            it instanceof DefaultBlobStorageLocator.BlobStorageWrapper.StoredBlobObjectWrapper
            getStoredRef() == "default:$defaultRef"
        }
    }

    def "find storage1 update"() {
        when:
        def storage = locator.findStorage("storage1")
        def response = storage.updateByRef(defaultRef, updateRequest)

        then:
        1 * storage1.updateByRef(defaultRef, _) >> new DefaultUpdateAttributesResponse(defaultBlobObject)
        0 * _
        verifyAll(response.getStoredObject()) {
            it instanceof DefaultBlobStorageLocator.BlobStorageWrapper.StoredBlobObjectWrapper
            getStoredRef() == "storage1:$defaultRef"
        }
    }

    def "find unknown update"() {
        when:
        def storage = locator.findStorage("unknown")
        def response = storage.updateByRef(defaultRef, updateRequest)

        then:
        thrown(IllegalArgumentException)
        0 * _
    }
//endregion
//region get test
    def "get default get"() {
        when:
        def storage = locator.getDefaultStorage()
        def obj = storage.getByRef(defaultRef)

        then:
        1 * defaultStorage.getByRef(defaultRef) >> defaultBlobObject
        0 * _
        verifyAll(obj) {
            getStoredRef() == "default:$defaultRef"
        }
    }

    def "find default get"() {
        when:
        def storage = locator.findStorage(null)
        def obj = storage.getByRef(defaultRef)

        then:
        1 * defaultStorage.getByRef(defaultRef) >> defaultBlobObject
        0 * _
        verifyAll(obj) {
            getStoredRef() == "default:$defaultRef"
        }
    }

    def "find storage1 get"() {
        when:
        def storage = locator.findStorage("storage1")
        def obj = storage.getByRef(defaultRef)

        then:
        1 * storage1.getByRef(defaultRef) >> defaultBlobObject
        0 * _
        verifyAll(obj) {
            getStoredRef() == "storage1:$defaultRef"
        }
    }

    def "find unknown get"() {
        when:
        def storage = locator.findStorage("unknown")
        def obj = storage.getByRef(defaultRef)

        then:
        thrown(IllegalArgumentException)
        0 * _
    }
//endregion
//region find test
    def "get default find"() {
        when:
        def storage = locator.getDefaultStorage()
        def obj = storage.findByRef(defaultRef)

        then:
        1 * defaultStorage.findByRef(defaultRef) >> Optional.of(defaultBlobObject)
        0 * _
        obj.isPresent()
        verifyAll(obj.get()) {
            getStoredRef() == "default:$defaultRef"
        }
    }

    def "find default find"() {
        when:
        def storage = locator.findStorage(null)
        def obj = storage.findByRef(defaultRef)

        then:
        1 * defaultStorage.findByRef(defaultRef) >> Optional.of(defaultBlobObject)
        0 * _
        obj.isPresent()
        verifyAll(obj.get()) {
            getStoredRef() == "default:$defaultRef"
        }
    }

    def "find storage1 find"() {
        when:
        def storage = locator.findStorage("storage1")
        def obj = storage.findByRef(defaultRef)

        then:
        1 * storage1.findByRef(defaultRef) >> Optional.of(defaultBlobObject)
        0 * _
        obj.isPresent()
        verifyAll(obj.get()) {
            getStoredRef() == "storage1:$defaultRef"
        }
    }

    def "find unknown find"() {
        when:
        def storage = locator.findStorage("unknown")
        def obj = storage.findByRef(defaultRef)

        then:
        thrown(IllegalArgumentException)
        0 * _
    }
//endregion
//region exist test
    def "get default exists"() {
        when:
        def storage = locator.getDefaultStorage()
        def obj = storage.existsByRef(defaultRef)

        then:
        1 * defaultStorage.existsByRef(defaultRef) >> true
        0 * _
        obj
    }

    def "find default exists"() {
        when:
        def storage = locator.findStorage(null)
        def obj = storage.existsByRef(defaultRef)

        then:
        1 * defaultStorage.existsByRef(defaultRef) >> true
        0 * _
        obj
    }

    def "find storage1 exists"() {
        when:
        def storage = locator.findStorage("storage1")
        def obj = storage.existsByRef(defaultRef)

        then:
        1 * storage1.existsByRef(defaultRef) >> true
        0 * _
        obj
    }

    def "find unknown exists"() {
        when:
        def storage = locator.findStorage("unknown")
        def obj = storage.existsByRef(defaultRef)

        then:
        thrown(IllegalArgumentException)
        0 * _
    }
//endregion
//region delete test
    def "get default delete"() {
        when:
        def storage = locator.getDefaultStorage()
        def obj = storage.deleteByRef(defaultRef)

        then:
        1 * defaultStorage.deleteByRef(defaultRef) >> true
        0 * _
        obj
    }

    def "find default delete"() {
        when:
        def storage = locator.findStorage(null)
        def obj = storage.deleteByRef(defaultRef)

        then:
        1 * defaultStorage.deleteByRef(defaultRef) >> true
        0 * _
        obj
    }

    def "find storage1 delete"() {
        when:
        def storage = locator.findStorage("storage1")
        def obj = storage.deleteByRef(defaultRef)

        then:
        1 * storage1.deleteByRef(defaultRef) >> true
        0 * _
        obj
    }

    def "find unknown delete"() {
        when:
        def storage = locator.findStorage("unknown")
        def obj = storage.deleteByRef(defaultRef)

        then:
        thrown(IllegalArgumentException)
        0 * _
    }
//endregion
}
