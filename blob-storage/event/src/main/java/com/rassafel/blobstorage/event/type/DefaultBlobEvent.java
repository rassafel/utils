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

package com.rassafel.blobstorage.event.type;

import java.time.Clock;
import java.time.LocalDateTime;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import com.rassafel.blobstorage.core.BlobStorage;
import com.rassafel.blobstorage.event.BlobEvent;

/**
 * Default blob event
 */
@RequiredArgsConstructor
public class DefaultBlobEvent implements BlobEvent {
    @NonNull
    private final BlobStorage storage;
    @NonNull
    private final LocalDateTime timestamp;

    public DefaultBlobEvent(BlobStorage storage) {
        this(storage, LocalDateTime.now());
    }

    public DefaultBlobEvent(BlobStorage storage, Clock clock) {
        this(storage, LocalDateTime.now(clock));
    }

    @Override
    public BlobStorage getStorage() {
        return storage;
    }

    @Override
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
