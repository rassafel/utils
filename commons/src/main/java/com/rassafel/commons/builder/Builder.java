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

package com.rassafel.commons.builder;

import java.util.function.Consumer;

/**
 * A mutable object that can be used to create an immutable object of type T.
 *
 * @param <B> the builder type (this)
 * @param <O> the type that the builder will build
 */
public interface Builder<O, B extends Builder<O, B>> {
    /**
     * An immutable object that is created from the properties that have been set on the builder.
     *
     * @return an instance of T
     */
    O build();

    /**
     * A convenience operator that takes something that will mutate the builder in some way and allows inclusion of it
     * in chaining operations.
     *
     * @param mutator the function that mutates the builder
     * @return B the mutated builder instance
     */
    @SuppressWarnings("unchecked")
    default B applyMutation(Consumer<? super B> mutator) {
        mutator.accept((B) this);
        return (B) this;
    }
}
