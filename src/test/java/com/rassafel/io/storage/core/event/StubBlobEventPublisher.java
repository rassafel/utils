package com.rassafel.io.storage.core.event;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class StubBlobEventPublisher implements BlobEventPublisher {
    private final List<BlobEvent> events = new ArrayList<>();

    @Override
    public void publish(BlobEvent event) {
        events.add(event);
    }
}
