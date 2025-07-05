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

package io.github.rassafel.commons.text

import spock.lang.Specification


class EntityToStringStyleTest extends Specification {
    def "empty entity"() {
        given:
        def input = new EntityWithToStringStyle()

        when:
        def actual = input.toString()

        then:
        actual == "EntityWithToStringStyle[isNew=true;name=null;version=null]"
    }

    def "empty entity with id"() {
        given:
        def input = new EntityWithToStringStyle()
        input.id = UUID.fromString("108664a4-1c5e-4d9b-8cd4-79fc28aadd9f")

        when:
        def actual = input.toString()

        then:
        actual == "EntityWithToStringStyle[id=\"108664a4-1c5e-4d9b-8cd4-79fc28aadd9f\";name=null;version=null]"
    }

    def "full entity"() {
        given:
        def input = new EntityWithToStringStyle()
        input.id = UUID.fromString("108664a4-1c5e-4d9b-8cd4-79fc28aadd9f")
        input.name = "FWILgqM"
        input.version = 10

        when:
        def actual = input.toString()

        then:
        actual == "EntityWithToStringStyle[id=\"108664a4-1c5e-4d9b-8cd4-79fc28aadd9f\";name=\"FWILgqM\";version=10]"
    }
}
