package com.rassafel.io.storage.core.event.support;

import com.rassafel.io.storage.core.event.type.DeleteBlobEvent;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class StubDeleteBlogListenerFromInterface implements StubDeleteBlobListenerInterface {
    private final List<DeleteBlobEvent> events = new ArrayList<>();

    @Override
    public void onBlobEvent(DeleteBlobEvent event) {
        events.add(event);
    }
}
