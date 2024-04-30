package com.rassafel.io.storage.core.event.impl

import com.rassafel.io.storage.core.BlobStorage
import com.rassafel.io.storage.core.event.BlobEvent
import com.rassafel.io.storage.core.event.support.StubGenericBlobListener
import com.rassafel.io.storage.core.event.type.DefaultBlobEvent
import com.rassafel.io.storage.core.event.type.DeleteBlobEvent
import com.rassafel.io.storage.core.event.type.HardDeleteBlobEvent
import com.rassafel.io.storage.core.event.type.SoftDeleteBlobEvent
import spock.lang.Specification

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
