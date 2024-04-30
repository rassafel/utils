package com.rassafel.io.storage.core.event.type;

import com.rassafel.io.storage.core.BlobStorage;
import lombok.Getter;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Restore soft deleted blob event
 */
@Getter
public class RestoreSoftDeletedBlobEvent extends DefaultBlobEvent {
    private final String ref;

    public RestoreSoftDeletedBlobEvent(BlobStorage storage, String ref) {
        super(storage);
        this.ref = ref;
    }

    public RestoreSoftDeletedBlobEvent(BlobStorage storage, Clock clock, String ref) {
        super(storage, clock);
        this.ref = ref;
    }

    public RestoreSoftDeletedBlobEvent(BlobStorage storage, LocalDateTime timestamp, String ref) {
        super(storage, timestamp);
        this.ref = ref;
    }
}
