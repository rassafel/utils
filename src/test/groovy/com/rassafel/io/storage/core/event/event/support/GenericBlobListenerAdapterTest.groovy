package com.rassafel.io.storage.core.event.event.support

import com.rassafel.io.storage.core.event.*
import com.rassafel.io.storage.core.event.support.GenericBlobListenerAdapter
import com.rassafel.io.storage.core.event.support.StubDeleteBlobListenerInterface
import com.rassafel.io.storage.core.event.support.StubDeleteBlogListenerFromInterface
import com.rassafel.io.storage.core.event.support.StubGenericBlobListener
import com.rassafel.io.storage.core.event.support.StubRootBlobListenerInterface
import com.rassafel.io.storage.core.event.support.StubRootBlogListenerFromInterface
import com.rassafel.io.storage.core.event.type.DeleteBlobEvent
import com.rassafel.io.storage.core.event.type.UploadBlobEvent
import spock.lang.Specification

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
//            ToDo: fix
//            new StubBlobListener<BlobEvent>(),
//            new StubRootBlobListener(),
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
//            ToDo: fix
//            new StubBlobListener<DeleteBlobEvent>(),
//            new StubDeleteBlobListener(),
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
