/*
 * Copyright 2024-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rassafel.blobstorage.event.support;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

import com.rassafel.blobstorage.event.type.DeleteBlobEvent;

@Getter
public class StubDeleteBlogListenerFromInterface implements StubDeleteBlobListenerInterface {
    private final List<DeleteBlobEvent> events = new ArrayList<>();

    @Override
    public void onBlobEvent(DeleteBlobEvent event) {
        events.add(event);
    }
}
