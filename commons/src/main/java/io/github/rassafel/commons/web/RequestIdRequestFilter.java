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

package io.github.rassafel.commons.web;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import io.github.rassafel.commons.web.util.UuidToBase36StringConverter;

/**
 * A filter that generates a request ID and adds it to the MDC (Mapped Diagnostic Context).
 * The request ID is then available in the MDC for logging purposes.
 */
@RequiredArgsConstructor
public class RequestIdRequestFilter extends OncePerRequestFilter {
    public static final String REQUEST_ID_ATTRIBUTE_NAME = RequestIdRequestFilter.class.getName() + ".requestId";
    public static final String REQUEST_ID_MDC_KEY = "requestId";
    public static final String DEFAULT_REQUEST_ID_HEADER = "X-Request-Id";
    public static final RequestIdGenerator DEFAULT_GENERATOR = RequestIdGenerator.uuidGen(new UuidToBase36StringConverter());
    @NonNull
    protected final RequestIdGenerator generator;
    @NonNull
    protected final String requestIdHeader;

    public RequestIdRequestFilter() {
        this(DEFAULT_GENERATOR, DEFAULT_REQUEST_ID_HEADER);
    }

    public RequestIdRequestFilter(RequestIdGenerator generator) {
        this(generator, DEFAULT_REQUEST_ID_HEADER);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        var requestId = generateRequestId();
        request.setAttribute(REQUEST_ID_ATTRIBUTE_NAME, requestId);
        response.addHeader(requestIdHeader, requestId);
        try (var mdcCloseable = MDC.putCloseable(REQUEST_ID_MDC_KEY, requestId)) {
            filterChain.doFilter(request, response);
        }
    }

    protected String generateRequestId() {
        return generator.get();
    }
}
