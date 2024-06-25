package com.rassafel.io.storage.core.support.wrapper;

import com.rassafel.io.storage.core.StoredBlobObject;

import java.io.IOException;
import java.io.InputStream;

/**
 * Delegation StoredBlobObject.
 */
public class DefaultDelegationStoredBlobObject extends DefaultDelegationBlobObject implements StoredBlobObject {
    public DefaultDelegationStoredBlobObject(StoredBlobObject delegate) {
        super(delegate);
    }

    @Override
    protected StoredBlobObject getDelegate() {
        return (StoredBlobObject) super.getDelegate();
    }

    @Override
    public InputStream toInputStream() throws IOException {
        return getDelegate().toInputStream();
    }
}
