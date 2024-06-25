package com.rassafel.commons.web;

import com.rassafel.commons.web.util.UuidToBase36StringConverter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

public class RequestIdRequestFilter extends OncePerRequestFilter {
    public static final String REQUEST_ID_ATTRIBUTE_NAME = RequestIdRequestFilter.class.getName() + ".requestId";
    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    private final RequestIdGenerator generator;

    public RequestIdRequestFilter() {
        this(new RequestIdGenerator() {
            private final UuidToBase36StringConverter converter = new UuidToBase36StringConverter();

            @Override
            public String get() {
                return converter.convertToString(UUID.randomUUID());
            }
        });
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
        try (var mdcCloseable = MDC.putCloseable("requestId", requestId)) {
            filterChain.doFilter(request, response);
        }
    }

    protected String generateRequestId() {
        return generator.get();
    }
}
