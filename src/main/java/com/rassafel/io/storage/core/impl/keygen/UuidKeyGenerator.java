package com.rassafel.io.storage.core.impl.keygen;

import com.rassafel.io.storage.core.impl.KeyGenerator;
import org.springframework.lang.Nullable;

import java.util.UUID;

/**
 * The UUID based key generator. Ignores source name
 */
public class UuidKeyGenerator implements KeyGenerator {
    @Override
    public String createKey(@Nullable String name) {
        return UUID.randomUUID().toString();
    }
}
