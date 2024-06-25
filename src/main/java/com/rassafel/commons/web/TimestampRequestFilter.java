package com.rassafel.commons.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class TimestampRequestFilter extends OncePerRequestFilter {
    public static final String TIMESTAMP_ATTRIBUTE_NAME = TimestampRequestFilter.class.getName() + ".timestamp";
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
        filterChain.doFilter(request, response);
    }

    protected String createTimestamp() {
        var now = OffsetDateTime.now(clock);
        return formatter.format(now);
    }
}
