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

package io.github.rassafel.blobstorage.event.impl

import spock.lang.Specification

import io.github.rassafel.blobstorage.core.BlobStorage
import io.github.rassafel.blobstorage.event.BlobEvent
import io.github.rassafel.blobstorage.event.support.StubGenericBlobListener
import io.github.rassafel.blobstorage.event.type.DefaultBlobEvent
import io.github.rassafel.blobstorage.event.type.DeleteBlobEvent
import io.github.rassafel.blobstorage.event.type.HardDeleteBlobEvent
import io.github.rassafel.blobstorage.event.type.SoftDeleteBlobEvent

class BlobEventPublisherImplTest extends Specification {
    def rootListener = new StubGenericBlobListener(BlobEvent)
    def deleteListener = new StubGenericBlobListener(DeleteBlobEvent)
    def softDeleteListener = new StubGenericBlobListener(SoftDeleteBlobEvent)

    def publisher = new BlobEventPublisherImpl()

    void setup() {
        publisher.addBlobListener(rootListener)
        publisher.addBlobListener(deleteListener)
        publisher.addBlobListener(softDeleteListener)
    }

    BlobStorage storage = Mock(BlobStorage.class)

    def "public BlobEvent"() {
        given:
        def event = new DefaultBlobEvent(storage)
        when:
        publisher.publish(event)
        then:
        rootListener.events.size() == 1
        deleteListener.events.size() == 0
        softDeleteListener.events.size() == 0
    }

    def "public DeleteBlobEvent"() {
        given:
        def event = new DeleteBlobEvent(storage, "ref")
        when:
        publisher.publish(event)
        then:
        rootListener.events.size() == 1
        deleteListener.events.size() == 1
        softDeleteListener.events.size() == 0
    }

    def "public SoftDeleteBlobEvent"() {
        given:
        def event = new SoftDeleteBlobEvent(storage, "ref")
        when:
        publisher.publish(event)
        then:
        rootListener.events.size() == 1
        deleteListener.events.size() == 1
        softDeleteListener.events.size() == 1
    }

    def "public HardDeleteBlobEvent"() {
        given:
        def event = new HardDeleteBlobEvent(storage, "ref")
        when:
        publisher.publish(event)
        then:
        rootListener.events.size() == 1
        deleteListener.events.size() == 1
        softDeleteListener.events.size() == 0
    }

    def "twice publish BlobEvent"() {
        given:
        def event = new DefaultBlobEvent(storage)
        when:
        publisher.publish(event)
        publisher.publish(event)
        then:
        rootListener.events.size() == 2
        deleteListener.events.size() == 0
        softDeleteListener.events.size() == 0
    }
}
