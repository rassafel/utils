package com.rassafel.io.storage.core.event.support;

import com.rassafel.commons.util.Assert;
import com.rassafel.io.storage.core.event.BlobEvent;
import com.rassafel.io.storage.core.event.BlobListener;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.ResolvableType;

/**
 * GenericBlobListener adapter from BlobListener
 */
public class GenericBlobListenerAdapter implements GenericBlobListener {
    private final BlobListener<BlobEvent> delegate;
    private final Class<? extends BlobEvent> declaredEventType;

    @SuppressWarnings("unchecked")
    public GenericBlobListenerAdapter(BlobListener<?> delegate) {
        Assert.notNull(delegate, "delegate cannot be null");
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
        return (Class<? extends BlobEvent>) ResolvableType.forClass(listenerType).as(BlobListener.class).getGeneric().getType();
    }
}
