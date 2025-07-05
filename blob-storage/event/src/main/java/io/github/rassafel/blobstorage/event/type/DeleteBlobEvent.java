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

package io.github.rassafel.blobstorage.event.type;

import java.time.Clock;
import java.time.LocalDateTime;

import lombok.Getter;

import io.github.rassafel.blobstorage.core.BlobStorage;

/**
 * Delete blob event
 */
@Getter
public class DeleteBlobEvent extends DefaultBlobEvent {
    /**
     * Reference to the deleted blob
     */
    private final String ref;

    public DeleteBlobEvent(BlobStorage storage, String ref) {
        super(storage);
        this.ref = ref;
    }

    public DeleteBlobEvent(BlobStorage storage, Clock clock, String ref) {
        super(storage, clock);
        this.ref = ref;
    }

    public DeleteBlobEvent(BlobStorage storage, LocalDateTime timestamp, String ref) {
        super(storage, timestamp);
        this.ref = ref;
    }
}
