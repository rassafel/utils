package com.rassafel.io.storage.core.event.type;

import com.rassafel.io.storage.core.BlobStorage;
import lombok.Getter;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Upload blob event
 */
@Getter
public class UploadBlobEvent extends DefaultBlobEvent {
    private final String ref;

    public UploadBlobEvent(BlobStorage storage, String ref) {
        super(storage);
        this.ref = ref;
    }

    public UploadBlobEvent(BlobStorage storage, Clock clock, String ref) {
        super(storage, clock);
        this.ref = ref;
    }

    public UploadBlobEvent(BlobStorage storage, LocalDateTime timestamp, String ref) {
        super(storage, timestamp);
        this.ref = ref;
    }
}
