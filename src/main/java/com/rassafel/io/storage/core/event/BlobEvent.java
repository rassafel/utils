package com.rassafel.io.storage.core.event;

import com.rassafel.io.storage.core.BlobStorage;

import java.time.LocalDateTime;

/**
 *
 */
public interface BlobEvent {
    BlobStorage getStorage();

    LocalDateTime getTimestamp();
}
