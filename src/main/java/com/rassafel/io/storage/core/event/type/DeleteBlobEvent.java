package com.rassafel.io.storage.core.event.type;

import com.rassafel.io.storage.core.BlobStorage;
import lombok.Getter;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Delete blob event
 */
@Getter
public class DeleteBlobEvent extends DefaultBlobEvent {
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
