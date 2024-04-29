package com.rassafel.io.storage.core.impl;

import org.springframework.lang.Nullable;

/**
 * The key generator from name
 */
public interface KeyGenerator {
    String SEPARATOR = "/";

    /**
     * Generate key from name
     *
     * @param name source name
     * @return key from name
     */
    String createKey(@Nullable String name);
}
