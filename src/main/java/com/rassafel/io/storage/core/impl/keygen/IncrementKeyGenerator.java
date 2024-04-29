package com.rassafel.io.storage.core.impl.keygen;

import com.rassafel.io.storage.core.impl.KeyGenerator;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.val;
import org.springframework.lang.Nullable;

/**
 * The number sequence based key generator. Ignores source name.
 */
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class IncrementKeyGenerator implements KeyGenerator {
    private long key = 1;
    private long increment = 1;

    @Override
    public String createKey(@Nullable String name) {
        val value = String.valueOf(key);
        key += increment;
        return value;
    }
}
