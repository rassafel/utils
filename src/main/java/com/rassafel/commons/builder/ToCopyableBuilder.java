package com.rassafel.commons.builder;

import java.util.function.Consumer;

/**
 * Implementors of this interface provide a way to get from an instance of T to a
 * {@link CopyableBuilder}. This allows
 * modification of an otherwise immutable object using the source object as a base.
 *
 * @param <T> the type that the builder will build (this)
 * @param <B> the builder type
 */
public interface ToCopyableBuilder<B extends CopyableBuilder<B, T>, T extends ToCopyableBuilder<B, T>> {
    /**
     * Take this object and create a builder that contains all of the current property values of this object.
     *
     * @return a builder for type T
     */
    B toBuilder();

    /**
     * A convenience method for calling {@link #toBuilder()}, updating the returned builder and then calling
     * {@link CopyableBuilder#build()}.
     * This is useful for making small modifications to the existing object.
     *
     * @param modifier A function that mutates this immutable object using the provided builder.
     * @return A new copy of this object with the requested modifications.
     */
    default T copy(Consumer<? super B> modifier) {
        return toBuilder().applyMutation(modifier::accept).build();
    }
}
