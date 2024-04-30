package com.rassafel.io.storage.core.event.support;

import com.rassafel.io.storage.core.event.BlobEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Getter
public class StubGenericBlobListener implements GenericBlobListener {
    private final List<BlobEvent> events = new ArrayList<>();
    private final Class<? extends BlobEvent> clazz;

    @Override
    public void onBlobEvent(BlobEvent event) {
        events.add(event);
    }

    @Override
    public boolean supportsEventType(Class<? extends BlobEvent> clazz) {
        return this.clazz.isAssignableFrom(clazz);
    }
}
