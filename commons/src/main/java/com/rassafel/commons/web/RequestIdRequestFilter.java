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

import com.rassafel.commons.web.util.UuidToBase36StringConverter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class RequestIdRequestFilter extends OncePerRequestFilter {
    public static final String REQUEST_ID_ATTRIBUTE_NAME = RequestIdRequestFilter.class.getName() + ".requestId";
    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    public static final String REQUEST_ID_MDC_KEY = "requestId";
    private static final RequestIdGenerator DEFAULT_GENERATOR = RequestIdGenerator.uuidGen(new UuidToBase36StringConverter());
    private final RequestIdGenerator generator;

    public RequestIdRequestFilter() {
        this(DEFAULT_GENERATOR);
    }

    public RequestIdRequestFilter(RequestIdGenerator generator) {
        this.generator = generator;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        var requestId = generateRequestId();
        request.setAttribute(REQUEST_ID_ATTRIBUTE_NAME, requestId);
        response.addHeader(REQUEST_ID_HEADER, requestId);
        try (var mdcCloseable = MDC.putCloseable(REQUEST_ID_MDC_KEY, requestId)) {
            filterChain.doFilter(request, response);
        }
    }

    protected String generateRequestId() {
        return generator.get();
    }
}
