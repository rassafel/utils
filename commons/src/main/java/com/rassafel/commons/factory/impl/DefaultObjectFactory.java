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

package com.rassafel.commons.factory.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import lombok.NonNull;

import com.rassafel.commons.factory.ObjectFactory;
import com.rassafel.commons.factory.ObjectInitializer;

public class DefaultObjectFactory implements ObjectFactory {
    protected final List<ObjectInitializer> initializers = new ArrayList<>();

    public boolean hasInitializer(Class<? extends ObjectInitializer> clazz) {
        return findInitializer(clazz) != -1;
    }

    public void addInitializer(ObjectInitializer initializer) {
        checkInitializer(initializer);
        initializers.add(initializer);
    }

    public void addInitializerBefore(ObjectInitializer initializer, @NonNull Class<? extends ObjectInitializer> clazz) {
        checkInitializer(initializer);
        var index = findInitializer(clazz);
        if (index == -1) {
            throw new IllegalArgumentException("clazz must be found in initializers list");
        }
        initializers.add(index, initializer);
    }

    public void addInitializerAfter(ObjectInitializer initializer, @NonNull Class<? extends ObjectInitializer> clazz) {
        checkInitializer(initializer);
        var index = findInitializer(clazz);
        if (index == -1) {
            throw new IllegalArgumentException("clazz must be found in initializers list");
        }
        initializers.add(index + 1, initializer);
    }

    protected int findInitializer(Class<? extends ObjectInitializer> clazz) {
        var size = initializers.size();
        for (int i = 0; i < size; i++) {
            var initializer = initializers.get(i);
            if (clazz.equals(initializer.getClass())) {
                return i;
            }
        }
        return -1;
    }

    protected void checkInitializer(@NonNull ObjectInitializer initializer) {
        if (hasInitializer(initializer.getClass())) {
            throw new IllegalArgumentException("initializer already registered");
        }
    }

    @Override
    public <T> T create(Class<T> clazz) {
        try {
            var obj = clazz.getDeclaredConstructor().newInstance();

            for (var initializer : initializers) {
                if (initializer.supported(clazz)) {
                    initializer.initObject(obj);
                }
            }

            return obj;
        } catch (InvocationTargetException
                 | InstantiationException
                 | IllegalAccessException
                 | NoSuchMethodException e) {
            throw new RuntimeException("Unable to create object instance", e);
        }
    }
}
