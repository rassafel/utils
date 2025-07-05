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

package io.github.rassafel.blobstorage.event.support;

import io.github.rassafel.blobstorage.event.BlobEvent;
import io.github.rassafel.blobstorage.event.BlobListener;

/**
 * Generic blob listener.
 * Accepts all events filtered with {@link #supportsEventType(Class)}
 */
public interface GenericBlobListener extends BlobListener<BlobEvent> {
    /**
     * Checks if the event type is supported by this listener.
     *
     * @param clazz the class of the event type to check.
     * @return true if the event type is supported, false otherwise.
     */
    boolean supportsEventType(Class<? extends BlobEvent> clazz);
}
