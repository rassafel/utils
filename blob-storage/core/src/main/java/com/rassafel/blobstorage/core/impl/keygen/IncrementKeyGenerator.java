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

package com.rassafel.blobstorage.core.impl.keygen;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.lang.Nullable;

import com.rassafel.blobstorage.core.impl.KeyGenerator;

/**
 * The number sequence based key generator. Ignores source name.
 */
public class IncrementKeyGenerator implements KeyGenerator {
    private final long increment;
    private final AtomicLong key = new AtomicLong();

    public IncrementKeyGenerator() {
        this(1);
    }

    public IncrementKeyGenerator(long key) {
        this(key, 1L);
    }

    public IncrementKeyGenerator(long key, long increment) {
        this.key.set(key);
        this.increment = increment;
    }

    @Override
    public String createKey(@Nullable String name) {
        var value = key.getAndAdd(increment);
        return String.valueOf(value);
    }
}
