package com.rassafel.io.storage.core.event.type;

import com.rassafel.io.storage.core.BlobStorage;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Hard delete blob event
 */
public class HardDeleteBlobEvent extends DeleteBlobEvent {
    public HardDeleteBlobEvent(BlobStorage storage, String ref) {
        super(storage, ref);
    }

    public HardDeleteBlobEvent(BlobStorage storage, Clock clock, String ref) {
        super(storage, clock, ref);
    }

    public HardDeleteBlobEvent(BlobStorage storage, LocalDateTime timestamp, String ref) {
        super(storage, timestamp, ref);
    }
}
