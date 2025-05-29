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

package com.rassafel.blobstorage.core.impl.keygen

import spock.lang.Shared
import spock.lang.Specification

import java.time.*

class CalendarKeyGeneratorTest extends Specification {
    @Shared
    LocalDateTime now = LocalDateTime.of(LocalDate.of(2024, 4, 29), LocalTime.MIDNIGHT)
    @Shared
    Clock clock = Clock.fixed(now.toInstant(ZoneOffset.UTC), ZoneOffset.UTC)

    def gen = new CalendarKeyGenerator(clock)

    def "CreateKey"() {
        given:

        when:
        def actual = gen.createKey(name)

        then:
        actual == "2024/04/29"

        where:
        name << [*KeyGeneratorTestUtils.names, null]
    }
}
