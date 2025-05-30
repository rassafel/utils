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

package com.rassafel.blobstorage.event.support;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;

import com.rassafel.blobstorage.event.BlobEvent;

/**
 * Convert BlobEvent to Spring ApplicationEvent
 */
public class BlobEventToApplicationEventListener implements GenericBlobListener {
    private final ApplicationEventPublisher publisher;

    public BlobEventToApplicationEventListener(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void onBlobEvent(BlobEvent event) {
        publisher.publishEvent(event);
    }

    @Override
    public boolean supportsEventType(Class<? extends BlobEvent> clazz) {
        return BlobEvent.class.isAssignableFrom(clazz);
    }

    private static class ApplicationBlobEvent extends ApplicationEvent {
        public ApplicationBlobEvent(BlobEvent source) {
            super(source, clockFromEvent(source));
        }

        private static Clock clockFromEvent(BlobEvent event) {
            var timestamp = event.getTimestamp();
            var zoneId = ZoneId.systemDefault();
            if (timestamp == null) return Clock.system(zoneId);
            return Clock.fixed(Instant.from(timestamp), zoneId);
        }

        @Override
        public BlobEvent getSource() {
            return (BlobEvent) super.getSource();
        }
    }
}
