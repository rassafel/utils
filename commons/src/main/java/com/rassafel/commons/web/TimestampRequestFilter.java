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

package com.rassafel.commons.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class TimestampRequestFilter extends OncePerRequestFilter {
    public static final String TIMESTAMP_ATTRIBUTE_NAME = TimestampRequestFilter.class.getName() + ".timestamp";
    public static final String TIMESTAMP_MDC_KEY = "requestTimestamp";
    protected static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private final DateTimeFormatter formatter;
    private final Clock clock;

    public TimestampRequestFilter() {
        this(Clock.systemUTC(), DEFAULT_FORMATTER);
    }

    public TimestampRequestFilter(Clock clock, DateTimeFormatter formatter) {
        this.clock = clock;
        this.formatter = formatter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        var timestamp = createTimestamp();
        request.setAttribute(TIMESTAMP_ATTRIBUTE_NAME, timestamp);
        try (var mdcCloseable = MDC.putCloseable(TIMESTAMP_MDC_KEY, timestamp)) {
            filterChain.doFilter(request, response);
        }
    }

    protected String createTimestamp() {
        var now = OffsetDateTime.now(clock);
        return formatter.format(now);
    }
}
