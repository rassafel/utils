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

package io.github.rassafel.blobstorage.core.impl.keygen

import spock.lang.Shared
import spock.lang.Specification

class DelegatingKeyGeneratorTest extends Specification {
    def "Empty generators"() {
        given:
        def gen = new DelegatingKeyGenerator()

        when:
        def actual = gen.createKey(name)

        then:
        actual == ""

        where:
        name << [*KeyGeneratorTestUtils.names, null]
    }

    @Shared
    String key = "static"
    @Shared
    StaticKeyGenerator staticKeyGenerator = new StaticKeyGenerator(key)

    def "Single generator"() {
        given:
        def gen = new DelegatingKeyGenerator()
        gen.addGenerator(staticKeyGenerator)

        when:
        def actual = gen.createKey(name)

        then:
        actual == key

        where:
        name << [*KeyGeneratorTestUtils.names, null]
    }

    def "Two generators"() {
        given:
        def gen = new DelegatingKeyGenerator()
        gen.addGenerator(staticKeyGenerator)
        gen.addGenerator(staticKeyGenerator)

        when:
        def actual = gen.createKey(name)

        then:
        actual == "$key/$key"

        where:
        name << [*KeyGeneratorTestUtils.names, null]
    }
}
