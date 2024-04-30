package com.rassafel.io.storage.core.event;

import com.rassafel.io.storage.core.event.support.NoOpBlobEventPublisher;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class StubBlobEventPublisher extends NoOpBlobEventPublisher {
    private final List<BlobEvent> events = new ArrayList<>();

    @Override
    public void publish(BlobEvent event) {
        events.add(event);
    }
}
