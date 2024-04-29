package com.rassafel.io.storage.core.impl.keygen;

import com.rassafel.io.storage.core.impl.KeyGenerator;
import org.springframework.lang.Nullable;

import java.util.Objects;

/**
 * The key generator return name.
 */
public class NoOpKeyGenerator implements KeyGenerator {
    private static final NoOpKeyGenerator INSTANCE = new NoOpKeyGenerator();

    public static KeyGenerator getInstance() {
        return INSTANCE;
    }

    @Override
    public String createKey(@Nullable String name) {
        return Objects.toString(name, "null");
    }
}
