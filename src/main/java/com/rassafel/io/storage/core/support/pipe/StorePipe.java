package com.rassafel.io.storage.core.support.pipe;

import com.rassafel.commons.util.Assert;
import com.rassafel.io.storage.core.BlobStorage;
import com.rassafel.io.storage.core.StoreBlobException;
import com.rassafel.io.storage.core.query.StoreBlobRequest;
import com.rassafel.io.storage.core.query.StoreBlobResponse;
import com.rassafel.io.storage.core.query.impl.DefaultStoreBlobRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Store blob pipe
 *
 * @param <Result> return value
 * @param <Source> blob source
 */
public abstract class StorePipe<Result, Source> {
    protected Source source;
    protected BlobStorage storage;
    protected StoreBlobRequest.Builder requestBuilder = DefaultStoreBlobRequest.builder();

    protected Transformer<Result> transformer = Transformer.ignore();

    protected ExceptionHandler<Result> exceptionHandler = ExceptionHandler.pass();

    public StorePipe<Result, Source> source(Source source) {
        Assert.notNull(source, "source cannot by null");
        this.source = source;
        return this;
    }

    public StorePipe<Result, Source> target(BlobStorage fileStorage) {
        Assert.notNull(storage, "storage cannot by null");
        this.storage = fileStorage;
        return this;
    }

    public StorePipe<Result, Source> transformer(Transformer<Result> transformer) {
        Assert.notNull(transformer, "transformer cannot by null");
        this.transformer = transformer;
        return this;
    }

    public StorePipe<Result, Source> onException(ExceptionHandler<Result> exceptionHandler) {
        Assert.notNull(exceptionHandler, "exceptionHandler cannot by null");
        this.exceptionHandler = exceptionHandler;
        return this;
    }

    public StorePipe<Result, Source> request(StoreBlobRequest request) {
        Assert.notNull(request, "request cannot be null");
        requestBuilder = request.toBuilder();
        return this;
    }

    public StorePipe<Result, Source> mutateRequest(Consumer<StoreBlobRequest.Builder> mutator) {
        Assert.notNull(mutator, "mutator cannot be null");
        mutator.accept(requestBuilder);
        return this;
    }

    public Result store() {
        Assert.notNull(storage, "storage cannot be null");
        Assert.notNull(source, "source cannot be null");
        val request = requestBuilder.build();
        val ctx = new Context(storage, request, this::openSourceStream);
        StoreBlobException exception;
        try (val in = openSourceStream()) {
            val response = storage.store(in, request);
            return transformer.transform(ctx, response);
        } catch (StoreBlobException ex) {
            exception = ex;
        } catch (IOException e) {
            exception = new StoreBlobException(e);
        }
        return exceptionHandler.onException(ctx, exception);
    }

    @AllArgsConstructor
    @Getter
    public static class Context {
        private final BlobStorage storage;
        private final StoreBlobRequest request;
        private final InputStreamProvider provider;

        interface InputStreamProvider {
            InputStream get() throws IOException;
        }
    }

    protected abstract InputStream openSourceStream() throws IOException;

    public interface Transformer<T> {
        static Transformer<StoreBlobResponse> response() {
            return (ctx, response) -> response;
        }

        static <T> Transformer<T> ignore() {
            return (ctx, response) -> null;
        }

        static <T> Transformer<T> predicate(Predicate<StoreBlobResponse> predicate,
                                            Transformer<T> trueTransformer,
                                            Transformer<T> falseTransformer) {
            return (ctx, response) -> {
                if (predicate.test(response))
                    return trueTransformer.transform(ctx, response);
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
}
