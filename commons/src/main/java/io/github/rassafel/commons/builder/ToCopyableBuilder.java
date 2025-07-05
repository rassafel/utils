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

package io.github.rassafel.commons.builder;

import java.util.function.Consumer;

/**
 * Implementors of this interface provide a way to get from an instance of T to a {@link CopyableBuilder}. This allows
 * modification of an otherwise immutable object using the source object as a base.
 *
 * @param <T> the type that the builder will build (this)
 * @param <B> the builder type
 */
public interface ToCopyableBuilder<T extends ToCopyableBuilder<T, B>, B extends CopyableBuilder<T, B>> {
    /**
     * Take this object and create a builder that contains all the current property values of this object.
     *
     * @return a builder for type T
     */
    B toBuilder();

    default T copy() {
        return toBuilder().build();
    }

    /**
     * A convenience method for calling {@link #toBuilder()}, updating the returned builder and then calling
     * {@link CopyableBuilder#build()}. This is useful for making small modifications to the existing object.
     *
     * @param modifier A function that mutates this immutable object using the provided builder.
     * @return A new copy of this object with the requested modifications.
     */
    default T copy(Consumer<? super B> modifier) {
        return toBuilder().applyMutation(modifier::accept).build();
    }
}
