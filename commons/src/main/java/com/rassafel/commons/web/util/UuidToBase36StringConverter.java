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

package com.rassafel.commons.web.util;

import java.math.BigInteger;
import java.util.UUID;

public class UuidToBase36StringConverter implements UuidToStringConverter {
    @Override
    public String apply(UUID uuid) {
        return convertToString(uuid);
    }

    public String convertToString(UUID id) {
        var hex = id.toString().replace("-", "");
        return new BigInteger(hex, 16).toString(36);
    }

    public UUID convertFromString(String x) {
        var hex = new BigInteger(x, 36).toString(16);
        return UUID.fromString(hex);
    }
}
