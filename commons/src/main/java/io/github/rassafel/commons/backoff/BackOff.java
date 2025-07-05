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

package io.github.rassafel.commons.backoff;

import java.time.Duration;

import org.springframework.lang.Nullable;

/**
 * Interface for back-off strategies.
 */
public interface BackOff {
    long STOP = -1;

    /**
     * Determines whether a delay indicates that back-off should be stopped.
     *
     * @param delay the delay value to check
     * @return true if the delay indicates back-off should stop, false otherwise
     */
    static boolean isStop(long delay) {
        return STOP == delay;
    }

    /**
     * Converts a delay value to a {@link Duration}. If the input delay is equal to {@code STOP}, this method returns null.
     *
     * @param delay the delay value to convert
     * @return a {@link Duration} instance, or null if the input delay is {@code STOP}
     */
    @Nullable
    static Duration toDuration(long delay) {
        if (isStop(delay)) {
            return null;
        }
        return Duration.ofMillis(delay);
    }

    /**
     * Evaluates the delay duration based on the given attempt number.
     *
     * @param attempt The current attempt number.
     * @return The calculated delay in milliseconds, or a special value indicating stop if applicable.
     */
    long evaluateDelay(long attempt);
}
