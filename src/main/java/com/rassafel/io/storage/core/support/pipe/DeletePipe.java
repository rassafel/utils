package com.rassafel.io.storage.core.support.pipe;

import com.rassafel.commons.util.Assert;
import com.rassafel.io.storage.core.BlobStorage;
import com.rassafel.io.storage.core.StoreBlobException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;

import java.util.function.Function;

/**
 * Delete blob pipe.
 */
public class DeletePipe<Result> {
    private BlobStorage storage;
    private String ref;

    private Transformer<Result> transformer = Transformer.ignore();

    private ExceptionHandler<Result> exceptionHandler = ExceptionHandler.pass();

    public DeletePipe<Result> storage(BlobStorage storage) {
        Assert.notNull(storage, "storage cannot by null");
        this.storage = storage;
        return this;
    }

    public DeletePipe<Result> ref(String ref) {
        Assert.notNull(ref, "ref cannot by null");
        this.ref = ref;
        return this;
    }

    public DeletePipe<Result> handler(Transformer<Result> handler) {
        Assert.notNull(handler, "handler cannot by null");
        this.transformer = handler;
        return this;
    }

    public DeletePipe<Result> storage(ExceptionHandler<Result> exceptionHandler) {
        Assert.notNull(exceptionHandler, "exceptionHandler cannot by null");
        this.exceptionHandler = exceptionHandler;
        return this;
    }

    public Result delete() throws StoreBlobException {
        Assert.notNull(storage, "storage cannot be null");
        Assert.hasText(ref, "ref cannot be empty");
        val ctx = new Context(storage, ref);
        try {
            val result = storage.deleteByRef(ref);
            return transformer.transform(ctx, result);
        } catch (StoreBlobException ex) {
            return exceptionHandler.onException(ctx, ex);
        }
    }

    @AllArgsConstructor
    @Getter
    public static class Context {
        private final BlobStorage storage;
        private final String ref;
    }

    public interface Transformer<T> {
        static Transformer<String> ref() {
            return (ctx, result) -> ctx.getRef();
        }

        static Transformer<Boolean> result() {
            return (ctx, result) -> result;
        }

        static <T> Transformer<T> switchResult(Transformer<T> success,
                                               Transformer<T> fail) {
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
}
