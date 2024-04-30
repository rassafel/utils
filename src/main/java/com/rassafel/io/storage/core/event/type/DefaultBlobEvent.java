package com.rassafel.io.storage.core.event.type;

import com.rassafel.io.storage.core.BlobStorage;
import com.rassafel.io.storage.core.event.BlobEvent;
import lombok.Getter;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Default blob event
 */
@Getter
public class DefaultBlobEvent implements BlobEvent {
    private final BlobStorage storage;
    private final LocalDateTime timestamp;

    public DefaultBlobEvent(BlobStorage storage) {
        this(storage, LocalDateTime.now());
    }

    public DefaultBlobEvent(BlobStorage storage, Clock clock) {
        this(storage, LocalDateTime.now(clock));
    }

    public DefaultBlobEvent(BlobStorage storage, LocalDateTime timestamp) {
        this.storage = storage;
        this.timestamp = timestamp;
    }
}
