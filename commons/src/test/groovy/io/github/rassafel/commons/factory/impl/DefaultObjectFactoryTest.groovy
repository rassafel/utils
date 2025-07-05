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

package io.github.rassafel.commons.factory.impl

import spock.lang.Specification

import io.github.rassafel.commons.factory.*

class DefaultObjectFactoryTest extends Specification {
    def factory = new DefaultObjectFactory()

    def "empty initializers test"() {
        when:
        def actual = factory.create(CustomDataClass)

        then:
        actual instanceof CustomDataClass
        actual.name == null
    }

    def "with initializer"() {
        given:
        factory.addInitializer(new CustomDataClassInitializer())
        factory.addInitializer(new AssignedCustomDataClassInitializer())

        when:
        def actual = factory.create(CustomDataClass)

        then:
        actual instanceof CustomDataClass
        actual.name == "Default name"
    }

    def "child with initializer"() {
        given:
        factory.addInitializer(new CustomDataClassInitializer())
        factory.addInitializer(new AssignedCustomDataClassInitializer())

        when:
        def actual = factory.create(AssignedCustomDataClass)

        then:
        actual instanceof AssignedCustomDataClass
        actual.name == "Default name"
        actual.description == "Default description"
    }

    def "class without empty constructor"() {
        when:
        def actual = factory.create(DataClassWithoutEmptyConstructor)

        then:
        thrown(RuntimeException)
    }
}
