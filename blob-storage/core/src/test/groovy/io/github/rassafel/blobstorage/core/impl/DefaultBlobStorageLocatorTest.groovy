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

package io.github.rassafel.blobstorage.core.impl

import spock.lang.Shared
import spock.lang.Specification

import io.github.rassafel.blobstorage.core.BlobStorage

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
