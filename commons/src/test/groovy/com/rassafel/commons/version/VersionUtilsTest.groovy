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

package com.rassafel.commons.version

import spock.lang.Specification


class VersionUtilsTest extends Specification {
    def "check version of versionable is matched"() {
        given:
        def expectedVersion = 10L
        def entity = new VersionableEntity()
        entity.version = expectedVersion

        when:
        VersionUtils.checkVersion(entity, expectedVersion)

        then:
        noExceptionThrown()
    }

    def "check version of versionable is not matched"() {
        given:
        def expectedVersion = 10L
        def entity = new VersionableEntity()
        entity.version = 9L

        when:
        VersionUtils.checkVersion(entity, expectedVersion)

        then:
        thrown(IllegalArgumentException)
    }

    def "check version of versionable is matched, entity is persistable"() {
        given:
        def expectedVersion = 10L
        def entity = new PersistableVersionableEntity()
        entity.id = UUID.fromString("9710df64-175d-4675-98de-581c771bd822")
        entity.version = expectedVersion

        when:
        VersionUtils.checkVersion(entity, expectedVersion)

        then:
        noExceptionThrown()
    }

    def "check version of versionable is not matched, entity is persistable"() {
        given:
        def expectedVersion = 10L
        def entity = new PersistableVersionableEntity()
        entity.id = UUID.fromString("20cdcbf2-940d-435a-8fbe-2d5fef81b17f")
        entity.version = 9L

        when:
        VersionUtils.checkVersion(entity, expectedVersion)

        then:
        thrown(IllegalArgumentException)
    }
}
