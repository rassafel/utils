package com.rassafel.io.storage.core.impl.keygen;

import com.rassafel.io.storage.core.impl.KeyGenerator;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.Nullable;

/**
 * The key generator return static name.
 */
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class StaticKeyGenerator implements KeyGenerator {
    private String key = "";

    @Override
    public String createKey(@Nullable String name) {
        return key;
    }
}
