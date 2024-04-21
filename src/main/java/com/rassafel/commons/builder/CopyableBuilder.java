package com.rassafel.commons.builder;

/**
 * A special type of {@link Builder} that can be used when the built type
 * implements {@link ToCopyableBuilder}.
 */
public interface CopyableBuilder<B extends CopyableBuilder<B, T>, T extends ToCopyableBuilder<B, T>> extends Builder<B, T> {
    default B copy() {
        return build().toBuilder();
    }
}
