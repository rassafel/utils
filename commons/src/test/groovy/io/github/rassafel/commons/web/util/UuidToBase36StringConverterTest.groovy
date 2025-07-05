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

package io.github.rassafel.commons.web.util

import spock.lang.Specification
import spock.lang.Subject


class UuidToBase36StringConverterTest extends Specification {
    @Subject
    def converter = new UuidToBase36StringConverter()

    def "convert to string"() {
        when:
        def actual = converter.convertToString(UUID.fromString(input))

        then:
        actual == expected

        where:
        input                                  | expected
        "7f900c7c-f1cb-4b7b-9bd9-90fa5cfbc4de" | "7jvf1n5a8rvxivfrl0e4wzen2"
    }

    def "convert from string"() {
        when:
        def actual = converter.convertFromString(input)

        then:
        actual == UUID.fromString(expected)

        where:
        input                       | expected
        "7jvf1n5a8rvxivfrl0e4wzen2" | "7f900c7c-f1cb-4b7b-9bd9-90fa5cfbc4de"
    }
}
