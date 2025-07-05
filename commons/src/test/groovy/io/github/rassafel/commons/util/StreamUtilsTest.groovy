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

package io.github.rassafel.commons.util

import java.util.function.Function

import spock.lang.Specification

class StreamUtilsTest extends Specification {
    def "emptyIfNull with null array"() {
        when:
        def actual = StreamUtils.emptyIfNull((Integer[]) null)

        then:
        def list = actual.toList()
        list.size() == 0
    }

    def "emptyIfNull with empty array"() {
        when:
        def actual = StreamUtils.emptyIfNull(new Integer[0])

        then:
        def list = actual.toList()
        list.size() == 0
    }

    def "emptyIfNull with not empty array"() {
        when:
        def actual = StreamUtils.emptyIfNull(new Integer[]{1})

        then:
        def list = actual.toList()
        list.size() == 1
        list.first() == 1
    }

    def "emptyIfNull with null collection"() {
        when:
        def actual = StreamUtils.emptyIfNull((List<Integer>) null)

        then:
        def list = actual.toList()
        list.size() == 0
    }

    def "emptyIfNull with empty collection"() {
        when:
        def actual = StreamUtils.emptyIfNull(List.<Integer> of())

        then:
        def list = actual.toList()
        list.size() == 0
    }

    def "emptyIfNull with not empty collection"() {
        when:
        def actual = StreamUtils.emptyIfNull(List.<Integer> of(1))

        then:
        def list = actual.toList()
        list.size() == 1
        list.first() == 1
    }

    def "distinctByKey"() {
        given:
        def input = [
                new IntegerWrapper(1),
                new IntegerWrapper(1),
                new IntegerWrapper(2),
                new IntegerWrapper(3),
        ]

        when:
        def actual = input.stream()
                .filter(StreamUtils.distinctByKey(w -> w.value))

        then:
        def list = actual.toList()
        list.size() == 3
        list.collect { it.value }.toList() == [1, 2, 3]
    }

    def "distinctByKey pass null"() {
        given:
        var input = [
                new IntegerWrapper(1),
                new IntegerWrapper(1),
                null,
                new IntegerWrapper(3),
        ]

        when:
        def actual = input.stream()
                .filter(StreamUtils.distinctByKey(w -> w.value))

        then:
        def list = actual.toList()
        list.size() == 3
        list.collect { it?.value }.toList() == [1, null, 3]
    }

    def "distinctByKey skip null"() {
        given:
        var input = [
                new IntegerWrapper(1),
                new IntegerWrapper(1),
                null,
                new IntegerWrapper(3),
        ]

        when:
        def actual = input.stream()
                .filter(StreamUtils.distinctByKey(w -> w.value, false))

        then:
        def list = actual.toList()
        list.size() == 2
        list.collect { it.value }.toList() == [1, 3]
    }

    def "mapIfNotNull null"() {
        given:
        def spy = Spy(Function)
        def mappedFunction = StreamUtils.mapIfNotNull(spy)

        when:
        var actual = mappedFunction.apply(null)

        then:
        actual == null
        0 * spy.apply(_)
    }

    def "mapIfNotNull not null"() {
        given:
        def mappedFunction = StreamUtils.mapIfNotNull(v -> v)

        when:
        var actual = mappedFunction.apply("")

        then:
        actual == ""
    }

    def "filterAndCast empty"() {
        given:
        var input = []

        when:
        def actual = input.stream()
                .flatMap(StreamUtils.filterAndCast(Integer))
                .toList()

        then:
        actual.isEmpty()
    }

    def "filterAndCast null"() {
        given:
        def input = [null, 1]

        when:
        def actual = input.stream()
                .flatMap(StreamUtils.filterAndCast(Integer))
                .toList()

        then:
        actual.size() == 1
        actual[0] == 1
    }

    def "filterAndCast"() {
        given:
        def input = [1, "Test"]

        when:
        def actual = input.stream()
                .flatMap(StreamUtils.filterAndCast(Integer))
                .toList()

        then:
        actual.size() == 1
        actual[0] == 1
    }

    def "filterAndCast upcast"() {
        given:
        def input = ["Test", 1]

        when:
        def actual = input.stream()
                .flatMap(StreamUtils.filterAndCast(Object))
                .toList()

        then:
        actual.size() == 2
        actual[0] == "Test"
        actual[1] == 1
    }

    def "exactlyOne empty list throws exception"() {
        given:
        def input = []

        when:
        def actual = input.stream().collect(StreamUtils.exactlyOne())

        then:
        thrown IllegalArgumentException
    }

    def "exactlyOne exactly one element"() {
        given:
        def input = [1]

        when:
        def actual = input.stream().collect(StreamUtils.exactlyOne())

        then:
        actual == 1
    }

    def "exactlyOne more than one element throws exception"() {
        given:
        def input = [1, 2]

        when:
        def actual = input.stream().collect(StreamUtils.exactlyOne())

        then:
        thrown IllegalArgumentException
    }

    private static class IntegerWrapper {
        Integer value

        IntegerWrapper(Integer value) {
            this.value = value
        }
    }

    def "collect stats from empty"() {
        given:
        def input = [] as List<Integer>

        when:
        def actual = input.stream().collect(StreamUtils.stats())

        then:
        actual.count == 0
        actual.min == null
        actual.max == null
    }

    def "collect stats from one"() {
        given:
        def input = [1]

        when:
        def actual = input.stream().collect(StreamUtils.stats())

        then:
        actual.count == 1
        actual.min == 1
        actual.max == 1
    }

    def "collect stats from multiple"() {
        given:
        def input = [3, 2, 4]

        when:
        def actual = input.stream().collect(StreamUtils.stats())

        then:
        actual.count == 3
        actual.min == 2
        actual.max == 4
    }

    def "collect stats from multiple with comparator"() {
        given:
        def input = ["Test", "", "Longest"]
        def comparator = Comparator.comparingInt(String::length)

        when:
        def actual = input.stream().collect(StreamUtils.stats(comparator))

        then:
        actual.count == 3
        actual.min == ""
        actual.max == "Longest"
    }

    def "collect stats from multiple with null"() {
        given:
        def input = [1, null, 2]
        def comparator = Comparator.<Integer> nullsFirst(Comparator.naturalOrder())

        when:
        def actual = input.stream().collect(StreamUtils.stats(comparator))

        then:
        actual.count == 3
        actual.min == null
        actual.max == 2
    }
}
