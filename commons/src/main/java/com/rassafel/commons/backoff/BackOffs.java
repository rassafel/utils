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


import lombok.experimental.UtilityClass;
import org.springframework.util.Assert;

import java.time.Duration;

@UtilityClass
public final class BackOffs {
    public static BackOff fixed(Duration delay) {
        Assert.notNull(delay, "delay must not be null");
        return fixed(delay.toMillis());
    }

    public static BackOff fixed(long delay) {
        return new FixedBackOff(delay);
    }

    public static BackOff limitDelay(Duration maxDelay, BackOff backOff) {
        Assert.notNull(maxDelay, "maxDelay must not be null");
        return limitDelay(maxDelay.toMillis(), backOff);
    }

    public static BackOff limitDelay(long maxDelay, BackOff backOff) {
        return new MaxDelayBackOff(maxDelay, backOff);
    }

    public static BackOff randomDelay(Duration max) {
        Assert.notNull(max, "max must not be null");
        return randomDelay(max.toMillis());
    }

    public static BackOff randomDelay(long max) {
        return randomDelay(0, max);
    }

    public static BackOff randomDelay(Duration min, Duration max) {
        Assert.notNull(min, "min must not be null");
        Assert.notNull(max, "max must not be null");
        return randomDelay(min.toMillis(), max.toMillis());
    }

    public static BackOff randomDelay(long min, long max) {
        if (min == max) return fixed(min);
        return new RandomBackOff(min, max);
    }

    public static BackOff exponential(Duration baseDelay) {
        Assert.notNull(baseDelay, "baseDelay must not be null");
        return exponential(baseDelay.toMillis());
    }

    public static BackOff exponential(long baseDelay) {
        return exponential(baseDelay, 1.5d);
    }

    public static BackOff exponential(Duration baseDelay, double multiplier) {
        Assert.notNull(baseDelay, "baseDelay must not be null");
        return exponential(baseDelay.toMillis(), multiplier);
    }

    public static BackOff exponential(long baseDelay, double multiplier) {
        return new ExponentialBackOff(multiplier, baseDelay);
    }

    public static BackOff additionalRandom(Duration max, BackOff backOff) {
        Assert.notNull(max, "max must not be null");
        return additionalRandom(max.toMillis(), backOff);
    }

    public static BackOff additionalRandom(long max, BackOff backOff) {
        return additionalRandom(0, max, backOff);
    }

    public static BackOff additionalRandom(Duration min, Duration max, BackOff backOff) {
        Assert.notNull(min, "min must not be null");
        Assert.notNull(max, "max must not be null");
        return additionalRandom(min.toMillis(), max.toMillis(), backOff);
    }

    public static BackOff additionalRandom(long min, long max, BackOff backOff) {
        if (min == max) return backOff;
        return new AdditionalRandomBackOff(min, max, backOff);
    }

    public static BackOff limitAttempts(long maxAttempts, BackOff backOff) {
        return new MaxAttemptBackOff(maxAttempts, backOff);
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

    private static final class RandomBackOff implements BackOff {
        private final long minDelay;
        private final long maxDelay;

        private RandomBackOff(long minDelay, long maxDelay) {
            Assert.isTrue(minDelay >= 0, "minDelay must be zero or positive");
            Assert.isTrue(minDelay < maxDelay, "minDelay must be less then maxDelay");
            this.minDelay = minDelay;
            this.maxDelay = maxDelay;
        }

        @Override
        public long evaluateDelay(long attempt) {
            var delay = Math.round(Math.random() * (maxDelay - minDelay));
            return minDelay + delay;
        }
    }

    private static final class MaxDelayBackOff implements BackOff {
        private final long maxDelay;
        private final BackOff delegate;

        private MaxDelayBackOff(long maxDelay, BackOff delegate) {
            Assert.isTrue(maxDelay >= 0, "maxDelay must be zero or positive");
            Assert.notNull(delegate, "delegate must not be null");
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
        private final long maxDelay;
        private final BackOff delegate;

        private AdditionalRandomBackOff(long minDelay, long maxDelay, BackOff delegate) {
            Assert.notNull(delegate, "delegate must not be null");
            Assert.isTrue(minDelay < maxDelay, "minDelay must be less then maxDelay");
            this.minDelay = minDelay;
            this.maxDelay = maxDelay;
            this.delegate = delegate;
        }

        @Override
        public long evaluateDelay(long attempt) {
            var delay = delegate.evaluateDelay(attempt);
            var randomDelay = Math.round(Math.random() * (maxDelay - minDelay)) + minDelay;
            delay = delay + randomDelay;
            if (delay <= 0) return Long.MAX_VALUE;
            return delay;
        }
    }

    private static final class MaxAttemptBackOff implements BackOff {
        private final long maxAttempts;
        private final BackOff delegate;

        private MaxAttemptBackOff(long maxAttempts, BackOff delegate) {
            Assert.isTrue(maxAttempts > 0, "maxAttempts must be positive");
            Assert.notNull(delegate, "delegate must not be null");
            this.maxAttempts = maxAttempts;
            this.delegate = delegate;
        }

        @Override
        public long evaluateDelay(long attempt) {
            if (attempt >= maxAttempts) return STOP;
            return delegate.evaluateDelay(attempt);
        }
    }
}

