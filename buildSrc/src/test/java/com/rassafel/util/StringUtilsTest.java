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

package com.rassafel.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class StringUtilsTest {
    @ParameterizedTest
    @CsvSource({
            "0.0.1-SNAPSHOT,-SNAPSHOT,0.0.1",
            "0.0.1,-SNAPSHOT,0.0.1",
            "-SNAPSHOT-0.0.1,-SNAPSHOT,''",
    })
    void substringBefore(String input, String search, String expect) {
        var actual = StringUtils.substringBefore(input, search);

        assertThat(actual)
                .isEqualTo(expect);
    }
}