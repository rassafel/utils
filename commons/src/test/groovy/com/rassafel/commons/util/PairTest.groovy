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

package com.rassafel.commons.util

import spock.lang.Specification


class PairTest extends Specification {
    def "should create a pair with two elements"() {
        when:
        def actual = Pair.of(1, "Test")

        then:
        actual.left == 1
        actual.right == "Test"
    }

    def "should create a pair with left element"() {
        when:
        def actual = Pair.<Integer, String> left(1)

        then:
        actual.left == 1
        actual.right == null
    }

    def "should create a pair with right element"() {
        when:
        def actual = Pair.<Integer, String> right("Test")

        then:
        actual.left == null
        actual.right == "Test"
    }

    def "should create a pair with left element then provide right element"() {
        given:
        def pair = Pair.<Integer, String> left(1)

        when:
        def actual = pair.withRight("Test")

        then:
        actual.left == 1
        actual.right == "Test"

        pair.left == 1
        pair.right == null
    }

    def "should create a pair with right element then provide left element"() {
        given:
        def pair = Pair.<Integer, String> right("Test")

        when:
        def actual = pair.withLeft(1)

        then:
        actual.left == 1
        actual.right == "Test"

        pair.left == null
        pair.right == "Test"
    }

    def "should map left element of a pair"() {
        given:
        def pair = Pair.of(1, 1)

        when:
        def actual = pair.mapLeft { it + "" }

        then:
        actual.left == "1"
        actual.right == 1

        pair.left == 1
        pair.right == 1
    }

    def "should map right element of a pair"() {
        given:
        def pair = Pair.of(1, 1)

        when:
        def actual = pair.mapRight { it + "" }

        then:
        actual.left == 1
        actual.right == "1"

        pair.left == 1
        pair.right == 1
    }

    def "should swap left and right elements of a pair"() {
        given:
        def pair = Pair.of(1, "Test")

        when:
        def actual = pair.swap()

        then:
        actual.left == "Test"
        actual.right == 1

        pair.left == 1
        pair.right == "Test"
    }

    def "should create array from a pair"() {
        given:
        def pair = Pair.of(1, "Test")

        when:
        def actual = pair.toArray()

        then:
        actual[0] == 1
        actual[1] == "Test"

        pair.left == 1
        pair.right == "Test"
    }

    def "should create list from a pair"() {
        given:
        def pair = Pair.of(1, "Test")

        when:
        def actual = pair.toList()

        then:
        actual[0] == 1
        actual[1] == "Test"

        pair.left == 1
        pair.right == "Test"
    }

    def "should create list with merged type"() {
        given:
        def pair = Pair.of(1, 2d)

        when:
        def actual = Pair.toList(pair)

        then:
        actual[0].intValue() == 1
        actual[1].intValue() == 2
    }
}
