package com.rassafel.io.storage.core.event.type;

import com.rassafel.io.storage.core.BlobStorage;
import lombok.Getter;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Update attributes blob event
 */
@Getter
public class UpdateAttributesBlobEvent extends DefaultBlobEvent {
    private final String ref;

    public UpdateAttributesBlobEvent(BlobStorage storage, String ref) {
        super(storage);
        this.ref = ref;
    }

    public UpdateAttributesBlobEvent(BlobStorage storage, Clock clock, String ref) {
        super(storage, clock);
        this.ref = ref;
    }

    public UpdateAttributesBlobEvent(BlobStorage storage, LocalDateTime timestamp, String ref) {
        super(storage, timestamp);
        this.ref = ref;
    }
}
