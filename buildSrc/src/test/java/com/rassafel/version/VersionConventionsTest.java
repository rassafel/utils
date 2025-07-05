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

package com.rassafel.version;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class VersionConventionsTest {
    private final VersionConventions conventions = new VersionConventions();

    @ParameterizedTest
    @ValueSource(strings = {
            "unspecified",
            "master-SNAPSHOT",
            "main-SNAPSHOT",
            "develop-SNAPSHOT",
    })
    void applyWhenHasMatchedVersion(String version) {
        assertThat(conventions.needReplaceVersion(version))
                .isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "1.0.0",
            "1.0.0-SNAPSHOT",
    })
    void applyWhenNoMatchedVersion(String version) {
        assertThat(conventions.needReplaceVersion(version))
                .isFalse();
    }
}