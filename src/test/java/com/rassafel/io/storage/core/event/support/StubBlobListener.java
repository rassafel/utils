package com.rassafel.io.storage.core.event.support;

import com.rassafel.io.storage.core.event.BlobEvent;
import com.rassafel.io.storage.core.event.BlobListener;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

//ToDo: fix
@Getter
public class StubBlobListener<T extends BlobEvent> implements BlobListener<T> {
    private final List<T> events = new ArrayList<>();

    @Override
    public void onBlobEvent(T event) {
        events.add(event);
    }
}
