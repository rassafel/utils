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

import java.io.Serial;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import com.rassafel.commons.util.StreamUtils;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.lang.Nullable;

/**
 * Represents an application exception.
 */
@RequiredArgsConstructor
@Getter
public class ApplicationException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 8473641975620247606L;
    @NonNull
    private final String code;
    @NonNull
    private final String type;
    private final Map<String, Object> details = new LinkedHashMap<>();

    public void setDetails(@Nullable Map<String, Object> details) {
        this.details.clear();
        if (details != null) {
            this.details.putAll(details);
        }
    }

    public void addDetail(@NonNull String key, @Nullable Object value) {
        details.put(key, value);
    }

    public String toCode() {
        return "%s - %s".formatted(type, code);
    }

    @Nullable
    public static ApplicationException findApplicationException(Throwable ex) {
        return ExceptionUtils.getThrowableList(ex).stream()
                .flatMap(e -> Stream.concat(Stream.of(e), Stream.of(e.getSuppressed())))
                .flatMap(StreamUtils.filterAndCast(ApplicationException.class))
                .findFirst()
                .orElse(null);
    }
}
