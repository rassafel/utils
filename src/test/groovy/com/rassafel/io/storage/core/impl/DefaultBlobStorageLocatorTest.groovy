package com.rassafel.io.storage.core.impl

import com.rassafel.io.storage.core.BlobStorage
import spock.lang.Shared
import spock.lang.Specification

class DefaultBlobStorageLocatorTest extends Specification {
    @Shared
    BlobStorage defaultStorage = Mock()
    @Shared
    BlobStorage storage1 = Mock()
    @Shared
    BlobStorage storage2 = Mock()

    @Shared
    DefaultBlobStorageLocator locator = new DefaultBlobStorageLocator("default", [
        "default" : defaultStorage,
        "storage1": storage1,
        "storage2": storage2,
    ])


    def "default lookup"() {
        when:
        def storage = locator.getDefaultStorage()

        then:
        with(storage) {
            it instanceof DefaultBlobStorageLocator.BlobStorageWrapper &&
                it.getDelegate().is(defaultStorage)
        }
    }

    def "by name lookup"() {
        when:
        def storage = locator.findStorage(null)

        then:
        with(storage) {
            it instanceof DefaultBlobStorageLocator.BlobStorageWrapper &&
                it.getDelegate().is(defaultStorage)
        }

        where:
        storageName | expectedStorage
        null        | defaultStorage
        "default"   | defaultStorage
        "storage1"  | storage1
        "storage2"  | storage2
    }

    def "by unknown name lookup"() {
        when:
        def storage = locator.findStorage("unknown")

        then:
        with(storage) {
            it instanceof DefaultBlobStorageLocator.UnmappedBlobStorage
        }
    }

    def "by unknown name lookup and default match override"() {
        given:
        locator.setDefaultMatchedStorage(overrideStorage)
        when:
        def storage = locator.findStorage("unknown")

        then:
        storage.is(overrideStorage)

        where:
        overrideStorage << [null, defaultStorage, storage1]
    }
}

