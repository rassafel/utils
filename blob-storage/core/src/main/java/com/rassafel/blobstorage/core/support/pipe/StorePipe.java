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

package com.rassafel.blobstorage.core.support.pipe;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.util.Assert;

import com.rassafel.blobstorage.core.BlobStorage;
import com.rassafel.blobstorage.core.StoreBlobException;
import com.rassafel.blobstorage.core.query.StoreBlobRequest;
import com.rassafel.blobstorage.core.query.StoreBlobResponse;
import com.rassafel.blobstorage.core.query.impl.DefaultStoreBlobRequest;

/**
 * Store blob pipe
 *
 * @param <Source> blob source
 */
public abstract class StorePipe<Source, Pipe extends StorePipe<Source, Pipe>> {
    protected Source source;
    protected BlobStorage storage;
    protected boolean closeStream;
    protected StoreBlobRequest.Builder<?, ?> requestBuilder = DefaultStoreBlobRequest.builder();

    public Pipe source(@NonNull Source source) {
        this.source = source;
        return self();
    }

    public Pipe request(@NonNull StoreBlobRequest request) {
        requestBuilder = request.toBuilder();
        return self();
    }

    public Pipe mutateRequest(@NonNull Consumer<StoreBlobRequest.Builder<?, ?>> mutator) {
        mutator.accept(requestBuilder);
        return self();
    }

    public Pipe closeStream(boolean closeStream) {
        this.closeStream = closeStream;
        return self();
    }

    public StoreBlobResponse store() {
        return store(Transformer.response(), ExceptionHandler.ignore());
    }

    public <Result> Result store(Transformer<Result> transformer) {
        return store(transformer, ExceptionHandler.ignore());
    }

    public <Result> Result store(@NonNull Transformer<Result> transformer, @NonNull ExceptionHandler<Result> exceptionHandler) {
        Assert.notNull(storage, "storage cannot be null");
        Assert.notNull(source, "source cannot be null");
        var request = requestBuilder.build();
        var ctx = new Context(storage, request, this::openSourceStream);
        StoreBlobException exception;
        InputStream in = null;
        try {
            in = openSourceStream();
            var response = storage.store(in, request);
            return transformer.transform(ctx, response);
        } catch (StoreBlobException ex) {
            exception = ex;
        } catch (IOException e) {
            exception = new StoreBlobException(e);
        } finally {
            if (closeStream && in != null) {
                IOUtils.closeQuietly(in);
            }
        }
        return exceptionHandler.onException(ctx, exception);
    }

    protected abstract Pipe self();

    protected abstract InputStream openSourceStream() throws IOException;

    public interface Transformer<T> {
        static Transformer<StoreBlobResponse> response() {
            return (ctx, response) -> response;
        }

        static <T> Transformer<T> ignore() {
            return (ctx, response) -> null;
        }

        static <T> Transformer<T> predicate(
                Predicate<StoreBlobResponse> predicate,
                Transformer<T> trueTransformer,
                Transformer<T> falseTransformer) {
            return (ctx, response) -> {
                if (predicate.test(response)) return trueTransformer.transform(ctx, response);
                return falseTransformer.transform(ctx, response);
            };
        }

        static <T> Transformer<T> skipNull(Transformer<T> transformer) {
            return predicate(Objects::nonNull, transformer, (ctx, response) -> null);
        }

        T transform(Context context, StoreBlobResponse response);

        default <V> Transformer<V> andThen(Function<T, V> after) {
            return (ctx, response) -> after.apply(transform(ctx, response));
        }
    }

    public interface ExceptionHandler<T> {
        static <T> ExceptionHandler<T> ignore() {
            return (ctx, exception) -> null;
        }

        static <T> ExceptionHandler<T> pass() {
            return (ctx, exception) -> {
                throw exception;
            };
        }

        T onException(Context ctx, StoreBlobException exception);

        default <V> ExceptionHandler<V> andThen(Function<T, V> after) {
            return (ctx, response) -> after.apply(onException(ctx, response));
        }
    }

    @RequiredArgsConstructor
    @Getter
    public static class Context {
        private final BlobStorage storage;
        private final StoreBlobRequest request;
        private final InputStreamProvider provider;

        public interface InputStreamProvider {
            InputStream get() throws IOException;
        }
    }
}
