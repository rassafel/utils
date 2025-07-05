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
 * Interface for back-off strategies that use state.
 */
public interface StateBackOff {
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
     * Evaluates the delay duration.
     *
     * @return The calculated delay in milliseconds, or a special value indicating stop if applicable.
     */
    long nextDelay();

    /**
     * Resets the backoff state to its initial condition. This method may be called at any time and is not
     * guaranteed to have any effect on the current calculation of the next delay. The implementation of this
     * method should ensure that it does not block or interfere with other threads.
     */
    void reset();
}
