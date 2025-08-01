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

package io.github.rassafel.blobstorage.core.impl.keygen;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

import io.github.rassafel.blobstorage.core.impl.KeyGenerator;

/**
 * The key generator return static name.
 */
@RequiredArgsConstructor
public class StaticKeyGenerator implements KeyGenerator {
    @NonNull
    private final String key;

    public StaticKeyGenerator() {
        this("");
    }

    @Override
    public String createKey(@Nullable String name) {
        return key;
    }
}
