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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

import io.github.rassafel.blobstorage.core.impl.KeyGenerator;

/**
 * The delegating key generator. Returns joined generated key
 * separated by {@link KeyGenerator#SEPARATOR}.
 */
@RequiredArgsConstructor
public class DelegatingKeyGenerator implements KeyGenerator {
    private final List<KeyGenerator> generators = new ArrayList<>();

    public DelegatingKeyGenerator(Collection<KeyGenerator> generators) {
        generators.forEach(this::addGenerator);
    }

    /**
     * Add generator to generators
     *
     * @param generator generator to add
     */
    public void addGenerator(@NonNull KeyGenerator generator) {
        generators.add(generator);
    }

    @Override
    public String createKey(@Nullable String name) {
        return generators.stream()
                .map(e -> e.createKey(name))
                .collect(Collectors.joining(SEPARATOR));
    }
}
