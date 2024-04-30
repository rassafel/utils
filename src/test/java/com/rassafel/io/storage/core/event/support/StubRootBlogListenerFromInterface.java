package com.rassafel.io.storage.core.event.support;

import com.rassafel.io.storage.core.event.BlobEvent;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class StubRootBlogListenerFromInterface implements StubRootBlobListenerInterface {
    private final List<BlobEvent> events = new ArrayList<>();

    @Override
    public void onBlobEvent(BlobEvent event) {
        events.add(event);
    }
}
