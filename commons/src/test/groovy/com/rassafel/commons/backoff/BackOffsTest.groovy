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

package com.rassafel.commons.backoff

import spock.lang.Specification


class BackOffsTest extends Specification {
    private static List<Long> generate(BackOff backOff, int count) {
        def longs = new ArrayList<Long>(count)
        for (int i = 0; i < count; i++) {
            longs.add(backOff.evaluateDelay(i))
        }
        return longs
    }

    private static List<Long> generate(StateBackOff backOff, int count) {
        def longs = new ArrayList<Long>(count)
        for (int i = 0; i < count; i++) {
            longs.add(backOff.nextDelay())
        }
        return longs
    }

    def "fixed"() {
        given:
        def backOff = BackOffs.fixed(1)

        when:
        def actual = generate(backOff, 20)

        then:
        actual.every { it == 1L }
    }

    def "fixed state"() {
        given:
        def backOff = BackOffs.fixedState(1)

        when:
        def actual = generate(backOff, 20)

        then:
        actual.every { it == 1L }
    }

    def "limit delay"() {
        given:
        def backOff = BackOffs.fixed(1000)
        backOff = BackOffs.limitDelay(1L, backOff)

        when:
        def actual = generate(backOff, 20)

        then:
        actual.every { it == 1L }
    }

    def "limit delay state"() {
        given:
        def backOff = BackOffs.fixedState(1000)
        backOff = BackOffs.limitDelayState(1L, backOff)

        when:
        def actual = generate(backOff, 20)

        then:
        actual.every { it == 1L }
    }

    def "random delay"() {
        given:
        def backOff = BackOffs.randomDelay(1, 4)

        when:
        def actual = generate(backOff, 20)

        then:
        actual.every { it >= 1L }
        actual.every { it <= 4L }
    }

    def "random delay state"() {
        given:
        def backOff = BackOffs.randomDelayState(1, 4)

        when:
        def actual = generate(backOff, 20)

        then:
        actual.every { it >= 1L }
        actual.every { it <= 4L }
    }

    def "exponential"() {
        given:
        def backOff = BackOffs.exponential(1, 2)

        when:
        def actual = generate(backOff, 8)

        then:
        actual == [1L, 2L, 4L, 8L, 16L, 32L, 64L, 128L]
    }

    def "exponential state"() {
        given:
        def backOff = BackOffs.exponentialState(1, 2)

        when:
        def actual = generate(backOff, 8)

        then:
        actual == [1L, 2L, 4L, 8L, 16L, 32L, 64L, 128L]
    }

    def "additional random"() {
        given:
        var backOff = BackOffs.fixed(10)
        backOff = BackOffs.additionalRandom(-2, 2, backOff)

        when:
        def actual = generate(backOff, 20)

        then:
        actual.every { it >= 8L }
        actual.every { it <= 12L }
    }

    def "additional random state"() {
        given:
        var backOff = BackOffs.fixedState(10)
        backOff = BackOffs.additionalRandomState(-2, 2, backOff)

        when:
        def actual = generate(backOff, 20)

        then:
        actual.every { it >= 8L }
        actual.every { it <= 12L }
    }

    def "limit attempts"() {
        given:
        def backOff = BackOffs.fixed(1)
        backOff = BackOffs.limitAttempts(5, backOff)

        when:
        def actual = generate(backOff, 10)

        then:
        actual == [
                1L, 1L, 1L, 1L, 1L,
                BackOff.STOP, BackOff.STOP, BackOff.STOP, BackOff.STOP, BackOff.STOP
        ]
    }

    def "limit attempts state"() {
        given:
        def backOff = BackOffs.fixedState(1)
        backOff = BackOffs.limitAttemptsState(5, backOff)

        when:
        def actual = generate(backOff, 10)

        then:
        actual == [
                1L, 1L, 1L, 1L, 1L,
                BackOff.STOP, BackOff.STOP, BackOff.STOP, BackOff.STOP, BackOff.STOP
        ]
    }

    def "backoff stream"() {
        given:
        def backOff = BackOffs.fixed(1)
        backOff = BackOffs.limitAttempts(5, backOff)

        when:
        def actual = BackOffs.stream(backOff).limit(6).toList()

        then:
        actual.size() == 5
    }
}
