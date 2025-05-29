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

package com.rassafel.commons.exception;

import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public class ApplicationException extends RuntimeException {
    private final String code;
    private final String type;
    private final Map<String, Object> details = new LinkedHashMap<>();

    public ApplicationException(String code, String type) {
        this.code = code;
        this.type = type;
    }

    public void setDetails(Map<String, Object> details) {
        this.details.clear();
        if (details != null) {
            this.details.putAll(details);
        }
    }

    public void addDetail(String key, Object value) {
        details.put(key, value);
    }

    public String toCode() {
        return "%s - %s".formatted(type, code);
    }
}
