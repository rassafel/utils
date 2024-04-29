package com.rassafel.io.storage.core.impl.keygen;

import com.rassafel.commons.util.Assert;
import com.rassafel.io.storage.core.impl.KeyGenerator;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The delegating key generator. Returns joined generated key
 * separated by {@link KeyGenerator#SEPARATOR}.
 */
public class DelegatingKeyGenerator implements KeyGenerator {
    private final List<KeyGenerator> generators = new ArrayList<>();

    public DelegatingKeyGenerator() {
    }

    public DelegatingKeyGenerator(Collection<KeyGenerator> generators) {
        generators.forEach(this::addGenerator);
    }

    /**
     * Add generator to generators
     *
     * @param generator generator to add
     */
    public void addGenerator(KeyGenerator generator) {
        Assert.notNull(generator, "generator cannot be null");
        generators.add(generator);
    }

    @Override
    public String createKey(@Nullable String name) {
        return generators.stream().map(e -> e.createKey(name))
            .collect(Collectors.joining(SEPARATOR));
    }
}
