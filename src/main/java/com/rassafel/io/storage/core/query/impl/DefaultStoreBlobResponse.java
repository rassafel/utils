package com.rassafel.io.storage.core.query.impl;

import com.rassafel.io.storage.core.query.StoreBlobResponse;
import com.rassafel.io.storage.core.StoredBlobObject;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class DefaultStoreBlobResponse implements StoreBlobResponse {
    private final StoredBlobObject storedObject;
}
