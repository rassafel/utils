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

package com.rassafel.commons.backoff;

import java.time.Duration;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import org.springframework.util.Assert;

/**
 * Utility class for creating and managing backoff strategies.
 */
@UtilityClass
public final class BackOffs {
    /**
     * Wraps a given BackOff instance with StateBackOff functionality.
     *
     * @param backOff the BackOff instance to wrap
     * @return a StateBackOff instance that wraps the provided BackOff instance
     */
    public static StateBackOff wrap(BackOff backOff) {
        return new StateBackOffDelegate(backOff);
    }

    /**
     * Unwraps a given StateBackOff instance, returning the underlying BackOff instance.
     *
     * @param backOff the StateBackOff instance to unwrap
     * @return the underlying BackOff instance
     */
    private static BackOff unwrap(StateBackOff backOff) {
        if (backOff instanceof StateBackOffDelegate delegate) {
            return delegate.backOff;
        }
        throw new IllegalArgumentException();
    }

    /**
     * Creates a fixed BackOff instance with the specified delay in milliseconds.
     *
     * @param delay the fixed delay in milliseconds
     * @return a fixed BackOff instance
     */
    public static BackOff fixed(long delay) {
        return new FixedBackOff(delay);
    }

    /**
     * Creates a fixed BackOff instance with the specified delay.
     *
     * @param delay the fixed delay
     * @return a fixed BackOff instance
     */
    public static BackOff fixed(@NonNull Duration delay) {
        return fixed(delay.toMillis());
    }

    /**
     * Creates a StateBackOff instance with a fixed delay.
     *
     * @param delay the fixed delay in milliseconds
     * @return a StateBackOff instance with a fixed delay
     */
    public static StateBackOff fixedState(long delay) {
        return wrap(fixed(delay));
    }

    /**
     * Creates a StateBackOff instance with a fixed delay.
     *
     * @param delay the fixed delay
     * @return a StateBackOff instance with a fixed delay
     */
    public static StateBackOff fixedState(Duration delay) {
        return wrap(fixed(delay));
    }

    /**
     * Creates a BackOff instance that limits the delay to the specified maximum.
     *
     * @param maxDelay the maximum allowed delay in milliseconds
     * @param backOff  the underlying BackOff instance
     * @return a BackOff instance with limited delay
     */
    public static BackOff limitDelay(long maxDelay, BackOff backOff) {
        return new MaxDelayBackOff(maxDelay, backOff);
    }

    /**
     * Creates a BackOff instance that limits the delay to the specified maximum.
     *
     * @param maxDelay the maximum allowed delay
     * @param backOff  the underlying BackOff instance
     * @return a BackOff instance with limited delay
     */
    public static BackOff limitDelay(@NonNull Duration maxDelay, BackOff backOff) {
        return limitDelay(maxDelay.toMillis(), backOff);
    }

    /**
     * Creates a StateBackOff instance that limits the delay to the specified maximum.
     *
     * @param maxDelay the maximum allowed delay in milliseconds
     * @param backOff  the underlying BackOff instance
     * @return a StateBackOff instance with limited delay
     */
    public static StateBackOff limitDelayState(long maxDelay, StateBackOff backOff) {
        return wrap(limitDelay(maxDelay, unwrap(backOff)));
    }

    /**
     * Creates a StateBackOff instance that limits the delay to the specified maximum.
     *
     * @param maxDelay the maximum allowed delay
     * @param backOff  the underlying BackOff instance
     * @return a StateBackOff instance with limited delay
     */
    public static StateBackOff limitDelayState(Duration maxDelay, StateBackOff backOff) {
        return wrap(limitDelay(maxDelay, unwrap(backOff)));
    }

    /**
     * Creates a BackOff instance that returns an exponentially increasing delay.
     *
     * @param baseDelay the initial delay
     * @return a BackOff instance with exponential delay
     */
    public static BackOff exponential(@NonNull Duration baseDelay) {
        return exponential(baseDelay.toMillis());
    }

    /**
     * Creates a BackOff instance that returns an exponentially increasing delay.
     *
     * @param baseDelay the initial delay in milliseconds
     * @return a BackOff instance with exponential delay
     */
    public static BackOff exponential(long baseDelay) {
        return exponential(baseDelay, 1.5d);
    }

    /**
     * Creates a BackOff instance that returns an exponentially increasing delay.
     *
     * @param baseDelay  the initial delay
     * @param multiplier the multiplier for the exponential delay
     * @return a BackOff instance with exponential delay
     */
    public static BackOff exponential(@NonNull Duration baseDelay, double multiplier) {
        return exponential(baseDelay.toMillis(), multiplier);
    }

    /**
     * Creates a BackOff instance that returns an exponentially increasing delay.
     *
     * @param baseDelay  the initial delay in milliseconds
     * @param multiplier the multiplier for the exponential delay
     * @return a BackOff instance with exponential delay
     */
    public static BackOff exponential(long baseDelay, double multiplier) {
        return new ExponentialBackOff(multiplier, baseDelay);
    }

    /**
     * Creates a StateBackOff instance that returns an exponentially increasing delay.
     *
     * @param baseDelay the initial delay
     * @return a StateBackOff instance with exponential delay
     */
    public static StateBackOff exponentialState(Duration baseDelay) {
        return wrap(exponential(baseDelay));
    }

    /**
     * Creates a StateBackOff instance that returns an exponentially increasing delay.
     *
     * @param baseDelay the initial delay in milliseconds
     * @return a StateBackOff instance with exponential delay
     */
    public static StateBackOff exponentialState(long baseDelay) {
        return wrap(exponential(baseDelay));
    }

    /**
     * Creates a StateBackOff instance that returns an exponentially increasing delay.
     *
     * @param baseDelay  the initial delay
     * @param multiplier the multiplier for the exponential delay
     * @return a StateBackOff instance with exponential delay
     */
    public static StateBackOff exponentialState(Duration baseDelay, double multiplier) {
        return wrap(exponential(baseDelay, multiplier));
    }

    /**
     * Creates a StateBackOff instance that returns an exponentially increasing delay.
     *
     * @param baseDelay  the initial delay in milliseconds
     * @param multiplier the multiplier for the exponential delay
     * @return a StateBackOff instance with exponential delay
     */
    public static StateBackOff exponentialState(long baseDelay, double multiplier) {
        return wrap(exponential(baseDelay, multiplier));
    }

    /**
     * Creates a BackOff instance that returns a delay between 0 and the specified maximum.
     *
     * @param max the maximum allowed delay
     * @return a BackOff instance that returns a random delay
     */
    public static BackOff randomDelay(@NonNull Duration max) {
        return randomDelay(max.toMillis());
    }

    /**
     * Creates a BackOff instance that introduces random delays up to the specified maximum.
     *
     * @param max the maximum allowed delay in milliseconds
     * @return a BackOff instance with random delay
     */
    public static BackOff randomDelay(long max) {
        return randomDelay(0, max);
    }

    /**
     * Creates a BackOff instance that introduces random delays up to the specified maximum.
     *
     * @param min the minimum allowed delay
     * @param max the maximum allowed delay
     * @return a BackOff instance with random delay
     */
    public static BackOff randomDelay(@NonNull Duration min, @NonNull Duration max) {
        return randomDelay(min.toMillis(), max.toMillis());
    }

    /**
     * Creates a BackOff instance that introduces random delays up to the specified maximum.
     *
     * @param min the minimum allowed delay in milliseconds
     * @param max the maximum allowed delay in milliseconds
     * @return a BackOff instance with random delay
     */
    public static BackOff randomDelay(long min, long max) {
        if (min == max) {
            return fixed(min);
        }
        return additionalRandom(min, max, fixed(0));
    }

    /**
     * Creates a StateBackOff instance that introduces random delays up to the specified maximum.
     *
     * @param max the maximum allowed delay
     * @return a StateBackOff instance with random delay
     */
    public static StateBackOff randomDelayState(Duration max) {
        return wrap(randomDelay(max));
    }

    /**
     * Creates a StateBackOff instance that introduces random delays up to the specified maximum.
     *
     * @param max the maximum allowed delay in milliseconds
     * @return a StateBackOff instance with random delay
     */
    public static StateBackOff randomDelayState(long max) {
        return wrap(randomDelay(max));
    }

    /**
     * Creates a StateBackOff instance that introduces random delays up to the specified maximum.
     *
     * @param min the minimum allowed delay
     * @param max the maximum allowed delay
     * @return a StateBackOff instance with random delay
     */
    public static StateBackOff randomDelayState(Duration min, Duration max) {
        return wrap(randomDelay(min, max));
    }

    /**
     * Creates a StateBackOff instance that introduces random delays up to the specified maximum.
     *
     * @param min the minimum allowed delay in milliseconds
     * @param max the maximum allowed delay in milliseconds
     * @return a StateBackOff instance with random delay
     */
    public static StateBackOff randomDelayState(long min, long max) {
        return wrap(randomDelay(min, max));
    }

    /**
     * Creates a BackOff instance that introduces random additional delays up to the specified maximum.
     *
     * @param max     the maximum allowed additional delay
     * @param backOff the underlying BackOff instance
     * @return a BackOff instance with random additional delay
     */
    public static BackOff additionalRandom(@NonNull Duration max, BackOff backOff) {
        return additionalRandom(max.toMillis(), backOff);
    }

    /**
     * Creates a BackOff instance that introduces random additional delays up to the specified maximum.
     *
     * @param max     the maximum allowed additional delay in milliseconds
     * @param backOff the underlying BackOff instance
     * @return a BackOff instance with random additional delay
     */
    public static BackOff additionalRandom(long max, BackOff backOff) {
        return additionalRandom(0, max, backOff);
    }

    /**
     * Creates a BackOff instance that introduces random additional delays up to the specified maximum.
     *
     * @param min     the minimum allowed additional delay
     * @param max     the maximum allowed additional delay
     * @param backOff the underlying BackOff instance
     * @return a BackOff instance with random additional delay
     */
    public static BackOff additionalRandom(@NonNull Duration min, @NonNull Duration max, BackOff backOff) {
        return additionalRandom(min.toMillis(), max.toMillis(), backOff);
    }

    /**
     * Creates a BackOff instance that introduces random additional delays up to the specified maximum.
     *
     * @param min     the minimum allowed additional delay in milliseconds
     * @param max     the maximum allowed additional delay in milliseconds
     * @param backOff the underlying BackOff instance
     * @return a BackOff instance with random additional delay
     */
    public static BackOff additionalRandom(long min, long max, BackOff backOff) {
        if (min == max) return backOff;
        return new AdditionalRandomBackOff(min, max, backOff);
    }

    /**
     * Creates a StateBackOff instance that introduces random additional delays up to the specified maximum.
     *
     * @param max     the maximum allowed additional delay
     * @param backOff the underlying BackOff instance
     * @return a StateBackOff instance with random additional delay
     */
    public static StateBackOff additionalRandomState(Duration max, StateBackOff backOff) {
        return wrap(additionalRandom(max, unwrap(backOff)));
    }

    /**
     * Creates a StateBackOff instance that introduces random additional delays up to the specified maximum.
     *
     * @param max     the maximum allowed additional delay in milliseconds
     * @param backOff the underlying BackOff instance
     * @return a StateBackOff instance with random additional delay
     */
    public static StateBackOff additionalRandomState(long max, StateBackOff backOff) {
        return wrap(additionalRandom(max, unwrap(backOff)));
    }

    /**
     * Creates a StateBackOff instance that introduces random additional delays up to the specified maximum.
     *
     * @param min     the minimum allowed additional delay
     * @param max     the maximum allowed additional delay
     * @param backOff the underlying BackOff instance
     * @return a StateBackOff instance with random additional delay
     */
    public static StateBackOff additionalRandomState(Duration min, Duration max, StateBackOff backOff) {
        return wrap(additionalRandom(min, max, unwrap(backOff)));
    }

    /**
     * Creates a StateBackOff instance that introduces random additional delays up to the specified maximum.
     *
     * @param min     the minimum allowed additional delay in milliseconds
     * @param max     the maximum allowed additional delay in milliseconds
     * @param backOff the underlying BackOff instance
     * @return a StateBackOff instance with random additional delay
     */
    public static StateBackOff additionalRandomState(long min, long max, StateBackOff backOff) {
        return wrap(additionalRandom(min, max, unwrap(backOff)));
    }

    /**
     * Creates a BackOff instance that limits the number of attempts to the specified maximum.
     *
     * @param maxAttempts the maximum allowed number of attempts
     * @param backOff     the underlying BackOff instance
     * @return a BackOff instance with limited attempts
     */
    public static BackOff limitAttempts(long maxAttempts, BackOff backOff) {
        return new MaxAttemptBackOff(maxAttempts, backOff);
    }

    /**
     * Creates a StateBackOff instance that limits the number of attempts to the specified maximum.
     *
     * @param maxAttempts the maximum allowed number of attempts
     * @param backOff     the underlying BackOff instance
     * @return a StateBackOff instance with limited attempts
     */
    public static StateBackOff limitAttemptsState(long maxAttempts, StateBackOff backOff) {
        return wrap(limitAttempts(maxAttempts, unwrap(backOff)));
    }

    /**
     * Creates an Iterable that provides the delays for each attempt in the BackOff instance.
     *
     * @param backOff the BackOff instance to use for delays
     * @return an Iterable that provides the delays for each attempt in the BackOff instance.
     */
    public static Iterable<Long> iterator(@NonNull BackOff backOff) {
        return () -> new Iterator<>() {
            private final StateBackOff stateBackOff = wrap(backOff);
            private long next = stateBackOff.nextDelay();

            @Override
            public boolean hasNext() {
                return !StateBackOff.isStop(next);
            }

            @Override
            public Long next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                final var current = next;
                next = stateBackOff.nextDelay();
                return current;
            }
        };
    }

    /**
     * Creates a Spliterator that provides the delays for each attempt in the BackOff instance.
     *
     * @param backOff the BackOff instance to use for delays
     * @return a Spliterator that provides the delays for each attempt in the BackOff instance.
     */
    public static Spliterator<Long> spliterator(BackOff backOff) {
        return Spliterators.spliteratorUnknownSize(iterator(backOff).iterator(), Spliterator.NONNULL);
    }

    /**
     * Creates a Stream that provides the delays for each attempt in the BackOff instance.
     *
     * @param backOff the BackOff instance to use for delays
     * @return a Stream that provides the delays for each attempt in the BackOff instance.
     */
    public static Stream<Long> stream(BackOff backOff) {
        return StreamSupport.stream(spliterator(backOff), false);
    }

    private static final class FixedBackOff implements BackOff {
        private final long delay;

        private FixedBackOff(long delay) {
            Assert.isTrue(delay >= 0, "delay must be zero or positive");
            this.delay = delay;
        }

        @Override
        public long evaluateDelay(long attempt) {
            return delay;
        }
    }

    private static final class MaxDelayBackOff implements BackOff {
        private final long maxDelay;
        @NonNull
        private final BackOff delegate;

        private MaxDelayBackOff(long maxDelay, BackOff delegate) {
            Assert.isTrue(maxDelay >= 0, "maxDelay must be zero or positive");
            this.maxDelay = maxDelay;
            this.delegate = delegate;
        }

        @Override
        public long evaluateDelay(long attempt) {
            var delay = delegate.evaluateDelay(attempt);
            return Math.min(delay, maxDelay);
        }
    }

    private static final class ExponentialBackOff implements BackOff {
        private final double multiplier;
        private final long baseDelay;

        private ExponentialBackOff(double multiplier, long baseDelay) {
            Assert.isTrue(baseDelay >= 0, "baseDelay must be zero or positive");
            Assert.isTrue(multiplier > 1, "multiplier must be greater then 1");
            this.multiplier = multiplier;
            this.baseDelay = baseDelay;
        }

        @Override
        public long evaluateDelay(long attempt) {
            var delay = Math.round(baseDelay * Math.pow(multiplier, attempt));
            if (delay <= 0) return Long.MAX_VALUE;
            return delay;
        }
    }

    private static final class AdditionalRandomBackOff implements BackOff {
        private final long minDelay;
        private final long delayDiff;
        @NonNull
        private final BackOff delegate;

        private AdditionalRandomBackOff(long minDelay, long maxDelay, BackOff delegate) {
            Assert.isTrue(minDelay < maxDelay, "minDelay must be less then maxDelay");
            this.minDelay = minDelay;
            this.delegate = delegate;
            this.delayDiff = Math.subtractExact(maxDelay, minDelay);
        }

        @Override
        public long evaluateDelay(long attempt) {
            var delay = delegate.evaluateDelay(attempt);
            var randomDelay = Math.addExact(Math.round(Math.random() * delayDiff), minDelay);
            delay += randomDelay;
            if (delay <= 0) return Long.MAX_VALUE;
            return delay;
        }
    }

    private static final class MaxAttemptBackOff implements BackOff {
        private final long maxAttempts;
        @NonNull
        private final BackOff delegate;

        private MaxAttemptBackOff(long maxAttempts, BackOff delegate) {
            Assert.isTrue(maxAttempts > 0, "maxAttempts must be positive");
            this.maxAttempts = maxAttempts;
            this.delegate = delegate;
        }

        @Override
        public long evaluateDelay(long attempt) {
            if (attempt >= maxAttempts) return STOP;
            return delegate.evaluateDelay(attempt);
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class StateBackOffDelegate implements StateBackOff {
        @NonNull
        private final BackOff backOff;
        private final AtomicLong attempt = new AtomicLong();

        @Override
        public long nextDelay() {
            return backOff.evaluateDelay(attempt.getAndIncrement());
        }

        @Override
        public void reset() {
            attempt.set(0);
        }
    }
}
