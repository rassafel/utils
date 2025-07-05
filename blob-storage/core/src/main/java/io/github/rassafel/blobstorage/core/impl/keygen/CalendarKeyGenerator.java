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

package io.github.rassafel.blobstorage.core.impl.keygen;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;

import io.github.rassafel.blobstorage.core.impl.KeyGenerator;

import static java.time.temporal.ChronoField.*;

/**
 * The calendar based key generator. Ignores source name.
 */
@RequiredArgsConstructor
public class CalendarKeyGenerator implements KeyGenerator {
    /**
     * Date formatter that formats a date, such as '2024/04/29'.
     */
    protected static final DateTimeFormatter DEFAULT_FORMATTER = new DateTimeFormatterBuilder()
            .appendValue(YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
            .appendLiteral(SEPARATOR)
            .appendValue(MONTH_OF_YEAR, 2)
            .appendLiteral(SEPARATOR)
            .appendValue(DAY_OF_MONTH, 2)
            .toFormatter();

    @NonNull
    private final Clock clock;
    @NonNull
    private final DateTimeFormatter formatter;

    public CalendarKeyGenerator() {
        this(Clock.systemUTC());
    }

    public CalendarKeyGenerator(Clock clock) {
        this(clock, DEFAULT_FORMATTER);
    }

    @Override
    public String createKey(@Nullable String name) {
        var date = LocalDate.now(clock);
        return formatter.format(date);
    }
}
