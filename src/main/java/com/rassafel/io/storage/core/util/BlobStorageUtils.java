package com.rassafel.io.storage.core.util;

import com.rassafel.commons.util.Assert;
import com.rassafel.io.storage.core.BlobStorage;
import com.rassafel.io.storage.core.NotFoundBlobException;
import com.rassafel.io.storage.core.StoreBlobException;
import com.rassafel.io.storage.core.StoredBlobObject;
import com.rassafel.io.storage.core.query.StoreBlobRequest;
import com.rassafel.io.storage.core.query.StoreBlobResponse;
import com.rassafel.io.storage.core.query.impl.DefaultStoreBlobRequest;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
@UtilityClass
public class BlobStorageUtils {
    public StoreBlobResponse copy(String ref, BlobStorage from, BlobStorage to) {
        return copy(ref, from, to, builder -> {
        });
    }

    public StoreBlobResponse copy(String ref, BlobStorage from, BlobStorage to,
                                  Consumer<StoreBlobRequest.Builder> mutator) {
        return copy(ref, from, to, (StoredBlobObject obj) -> DefaultStoreBlobRequest.builder(obj)
            .applyMutation(mutator::accept)
            .build());
    }

    public StoreBlobResponse copy(String ref, BlobStorage from, BlobStorage to,
                                  Function<StoredBlobObject, StoreBlobRequest> mapper) {
        try {
            return copyInner(ref, from, to, mapper);
        } catch (IOException e) {
            log.debug("Fail move blob, ref: {}; from: {}; to: {}", ref, from, to, e);
            throw new StoreBlobException("Fail move blob", e);
        }
    }

    public StoreBlobResponse move(String ref, BlobStorage from, BlobStorage to) {
        return move(ref, from, to, builder -> {
        });
    }

    public StoreBlobResponse move(String ref, BlobStorage from, BlobStorage to,
                                  Consumer<StoreBlobRequest.Builder> mutator) {
        return move(ref, from, to, (StoredBlobObject obj) -> DefaultStoreBlobRequest.builder(obj)
            .applyMutation(mutator::accept)
            .build());
    }

    public StoreBlobResponse move(String ref, BlobStorage from, BlobStorage to,
                                  Function<StoredBlobObject, StoreBlobRequest> mapper) {
        try {
            val copy = copyInner(ref, from, to, mapper);
            from.deleteByRef(ref);
            return copy;
        } catch (IOException e) {
            log.debug("Fail move blob, ref: {}; from: {}; to: {}", ref, from, to, e);
            throw new StoreBlobException("Fail move blob", e);
        }
    }

    private StoreBlobResponse copyInner(String ref, BlobStorage from, BlobStorage to,
                                        Function<StoredBlobObject, StoreBlobRequest> mapper) throws IOException {
        Assert.notNull(from, "from cannot be null");
        Assert.notNull(to, "to cannot be null");
        val obj = from.findByRef(ref)
            .orElseThrow(() -> new NotFoundBlobException(ref));
        try (InputStream inputStream = obj.toInputStream()) {
            return to.store(inputStream, mapper.apply(obj));
        }
    }
}
