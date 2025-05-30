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

package com.rassafel.blobstorage.event.support

import spock.lang.Specification

import com.rassafel.blobstorage.event.BlobEvent
import com.rassafel.blobstorage.event.BlobListener
import com.rassafel.blobstorage.event.type.DeleteBlobEvent
import com.rassafel.blobstorage.event.type.UploadBlobEvent

class GenericBlobListenerAdapterTest extends Specification {
    def "BlobEvent adapter"() {
        when:
        def adapter = new GenericBlobListenerAdapter(listener)
        then:
        adapter.supportsEventType(BlobEvent.class)
        adapter.supportsEventType(UploadBlobEvent.class)
        adapter.supportsEventType(DeleteBlobEvent.class)

        where:
        listener << rootListeners()
    }

    def "DeleteBlobEvent adapter"() {
        when:
        def adapter = new GenericBlobListenerAdapter(listener)
        then:
        !adapter.supportsEventType(BlobEvent.class)
        !adapter.supportsEventType(UploadBlobEvent.class)
        adapter.supportsEventType(DeleteBlobEvent.class)

        where:
        listener << deleteListeners()
    }

    def rootListeners() {
        return [
                new BlobListener<BlobEvent>() {
                    @Override
                    void onBlobEvent(BlobEvent event) {
                    }
                },
                new StubBlobListener<BlobEvent>(),
                new StubRootBlobListener(),
                new StubGenericBlobListener(BlobEvent),
                new StubRootBlobListenerInterface() {
                    @Override
                    void onBlobEvent(BlobEvent event) {
                    }
                },
                new StubRootBlogListenerFromInterface(),
        ]
    }

    def deleteListeners() {
        return [
                new BlobListener<DeleteBlobEvent>() {
                    @Override
                    void onBlobEvent(DeleteBlobEvent event) {
                    }
                },
                // ToDo: fix
                //  new StubBlobListener<DeleteBlobEvent>(),
                new StubDeleteBlobListener(),
                new StubGenericBlobListener(DeleteBlobEvent),
                new StubDeleteBlobListenerInterface() {
                    @Override
                    void onBlobEvent(DeleteBlobEvent event) {
                    }
                },
                new StubDeleteBlogListenerFromInterface(),
        ]
    }
}
