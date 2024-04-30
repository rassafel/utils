package com.rassafel.io.storage.core.query.impl;

import com.rassafel.io.storage.core.StoredBlobObject;
import com.rassafel.io.storage.core.query.UpdateAttributesResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *
 */
@AllArgsConstructor
@Getter
public class DefaultUpdateAttributesResponse implements UpdateAttributesResponse {
    private final StoredBlobObject storedObject;
}
