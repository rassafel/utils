package com.rassafel.io.storage.core.event.type;

import com.rassafel.io.storage.core.BlobStorage;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Soft delete blob event
 */
public class SoftDeleteBlobEvent extends DeleteBlobEvent {
    public SoftDeleteBlobEvent(BlobStorage storage, String ref) {
        super(storage, ref);
    }

    public SoftDeleteBlobEvent(BlobStorage storage, Clock clock, String ref) {
        super(storage, clock, ref);
    }

    public SoftDeleteBlobEvent(BlobStorage storage, LocalDateTime timestamp, String ref) {
        super(storage, timestamp, ref);
    }
}
