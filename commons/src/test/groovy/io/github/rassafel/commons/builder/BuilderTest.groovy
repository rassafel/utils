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

package io.github.rassafel.commons.builder

import spock.lang.Specification


class BuilderTest extends Specification {
    def "create"() {
        when:
        def actual = TestChildBody.builder()
                .body("Test 1")
                .version(1)
                .build()

        then:
        actual != null
        actual.version == 1
        actual.body == "Test 1"
    }

    def "mutate builder"() {
        when:
        def actual = TestChildBody.builder().version(2)
                .applyMutation(b -> b.body("Test 2"))
                .build()

        then:
        actual != null
        actual.version == 2
        actual.body == "Test 2"
    }

    def "mutate built object"() {
        given:
        def initial = TestChildBody.builder().version(3)
                .applyMutation(b -> b.body("Test 3"))
                .build()

        when:
        def actual = initial.toBuilder().version(4)
                .applyMutation(b -> b.body("Test 4"))
                .build()

        then:
        actual != null
        actual.version == 4
        actual.body == "Test 4"

        and:
        initial != null
        initial.version == 3
        initial.body == "Test 3"
    }

    def "copy"() {
        given:
        def initial = TestChildBody.builder().version(8)
                .applyMutation(b -> b.body("Test 8"))
                .build()

        when:
        def actual = initial.copy()

        then:
        actual != initial
        actual != null
        actual.version == 8
        actual.body == "Test 8"
    }

    def "copy mutate"() {
        given:
        def initial = TestChildBody.builder().version(5)
                .applyMutation(b -> b.body("Test 5"))
                .build()

        when:
        def actual = initial.copy(e -> {
        })

        then:
        actual != initial
        actual != null
        actual.version == 5
        actual.body == "Test 5"
    }

    def "copy builder"() {
        given:
        def initial = TestChildBody.builder().version(6)
                .applyMutation(b -> b.body("Test 6"))
        def item1 = initial.copy().build()

        when:
        def item2 = initial.body("Test 7")
                .version(7).build()

        then:
        item1 != item2
        item1 != null
        item1.version == 6
        item1.body == "Test 6"

        and:
        item2 != null
        item2.version == 7
        item2.body == "Test 7"
    }
}
