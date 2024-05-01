package com.rassafel.io.storage.core.support.wrapper;

import com.rassafel.io.storage.core.BlobObject;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Delegation BlobObject
 */
@RequiredArgsConstructor
public class DefaultDelegationBlobObject implements BlobObject {

    private final BlobObject delegate;

    protected BlobObject getDelegate() {
        return delegate;
    }

    public String getOriginalName() {
        return getDelegate().getOriginalName();
    }

    public String getStoredRef() {
        return getDelegate().getStoredRef();
    }

    public String getContentType() {
        return getDelegate().getContentType();
    }

    public LocalDateTime getUploadedAt() {
        return getDelegate().getUploadedAt();
    }

    public LocalDateTime getLastModifiedAt() {
        return getDelegate().getLastModifiedAt();
    }

    public long getSize() {
        return getDelegate().getSize();
    }

    public Map<String, String> getAttributes() {
        return getDelegate().getAttributes();
    }

    public String getAttribute(String key) {
        return getDelegate().getAttribute(key);
    }
}
