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

package io.github.rassafel.blobstorage.core.support.pipe;

import java.util.function.Function;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.util.Assert;

import io.github.rassafel.blobstorage.core.BlobStorage;
import io.github.rassafel.blobstorage.core.StoreBlobException;

/**
 * Delete blob pipe.
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class DeletePipe {
    private BlobStorage storage;
    private String ref;

    public DeletePipe storage(@NonNull BlobStorage storage) {
        this.storage = storage;
        return this;
    }

    public DeletePipe ref(String ref) {
        Assert.hasText(ref, "ref cannot be blank");
        this.ref = ref;
        return this;
    }

    public boolean delete() {
        return delete(Transformer.result(), ExceptionHandler.ignore());
    }

    public <Result> Result delete(Transformer<Result> transformer) {
        return delete(transformer, ExceptionHandler.ignore());
    }

    public <Result> Result delete(@NonNull Transformer<Result> transformer, @NonNull ExceptionHandler<Result> exceptionHandler) {
        Assert.notNull(storage, "storage cannot be null");
        Assert.hasText(ref, "ref cannot be blank");
        var ctx = new Context(storage, ref);
        try {
            var result = storage.deleteByRef(ref);
            return transformer.transform(ctx, result);
        } catch (StoreBlobException ex) {
            return exceptionHandler.onException(ctx, ex);
        }
    }

    public interface Transformer<T> {
        static Transformer<String> ref() {
            return (ctx, result) -> ctx.getRef();
        }

        static Transformer<Boolean> result() {
            return (ctx, result) -> result;
        }

        static <T> Transformer<T> switchResult(Transformer<T> success, Transformer<T> fail) {
            return (ctx, result) -> {
                if (result) return success.transform(ctx, true);
                return fail.transform(ctx, false);
            };
        }

        static <T> Transformer<T> ignore() {
            return (ctx, result) -> null;
        }

        T transform(Context ctx, boolean result);

        default <V> Transformer<V> andThen(Function<T, V> after) {
            return (ctx, result) -> after.apply(transform(ctx, result));
        }
    }

    public interface ExceptionHandler<T> {
        static ExceptionHandler<String> ref() {
            return (ctx, exception) -> ctx.getRef();
        }

        static <T> ExceptionHandler<T> ignore() {
            return (ctx, exception) -> null;
        }

        static <T> ExceptionHandler<T> pass() {
            return (ctx, exception) -> {
                throw exception;
            };
        }

        T onException(Context context, StoreBlobException exception);

        default <V> ExceptionHandler<V> andThen(Function<T, V> after) {
            return (ctx, response) -> after.apply(onException(ctx, response));
        }
    }

    @RequiredArgsConstructor
    @Getter
    public static class Context {
        private final BlobStorage storage;
        private final String ref;
    }
}
