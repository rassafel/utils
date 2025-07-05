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

package io.github.rassafel.blobstorage.event.support;

import lombok.Getter;
import lombok.NonNull;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.GenericTypeResolver;

import io.github.rassafel.blobstorage.event.BlobEvent;
import io.github.rassafel.blobstorage.event.BlobListener;

/**
 * GenericBlobListener adapter from BlobListener
 */
@Getter
public class GenericBlobListenerAdapter implements GenericBlobListener {
    private final BlobListener<BlobEvent> delegate;
    private final Class<? extends BlobEvent> declaredEventType;

    @SuppressWarnings("unchecked")
    public GenericBlobListenerAdapter(@NonNull BlobListener<?> delegate) {
        this.delegate = (BlobListener<BlobEvent>) delegate;
        this.declaredEventType = resolveType(AopUtils.getTargetClass(delegate));
    }

    @Override
    public void onBlobEvent(BlobEvent event) {
        delegate.onBlobEvent(event);
    }

    @Override
    public boolean supportsEventType(Class<? extends BlobEvent> clazz) {
        if (delegate instanceof GenericBlobListener genericListener) {
            return genericListener.supportsEventType(clazz);
        }
        return declaredEventType.isAssignableFrom(clazz);
    }

    private Class<? extends BlobEvent> resolveType(Class<?> listenerType) {
        return (Class<? extends BlobEvent>) GenericTypeResolver.resolveTypeArgument(listenerType, BlobListener.class);
//        return (Class<? extends BlobEvent>)  GenericTypeResolver.resolveTypeArguments(listenerType, BlobListener.class)[0];
//        return (Class<? extends BlobEvent>) ResolvableType.forClass(listenerType).as(BlobListener.class).getGeneric().getType();
    }
}
