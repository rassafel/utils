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

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.support.RequestContextUtils;

import java.util.Locale;

@Slf4j
@RequiredArgsConstructor
@Setter
public abstract class ApplicationExceptionAdvice implements MessageSourceAware {
    public static final String TITLE_MESSAGE_PREFIX = "application.exception.title.";
    public static final String DETAIL_MESSAGE_PREFIX = "application.exception.detail.";

    public static final ExceptionStatusResolver DEFAULT_STATUS_CONVERTER = exception -> {
        var type = exception.getType();
        if (ExceptionCode.DEFAULT_TYPE.equals(type)) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        if ("system".equalsIgnoreCase(type)) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        if ("input".equalsIgnoreCase(type)) {
            return HttpStatus.BAD_REQUEST;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    };

    private final String titleMessagePrefix;
    private final String detailMessagePrefix;
    private final ExceptionStatusResolver typeToStatusConverter;
    @Nullable
    private MessageSource messageSource;

    public ApplicationExceptionAdvice() {
        this(TITLE_MESSAGE_PREFIX, DETAIL_MESSAGE_PREFIX, DEFAULT_STATUS_CONVERTER);
    }

    public ApplicationExceptionAdvice(
        String titleMessagePrefix,
        String detailMessagePrefix
    ) {
        this(titleMessagePrefix, detailMessagePrefix, DEFAULT_STATUS_CONVERTER);
    }

    @ExceptionHandler
    public ProblemDetail handleThrowable(Throwable throwable,
                                         HttpServletRequest request) throws Throwable {
        var ex = findApplicationException(throwable);
        if (ex == null) throw throwable;
        var locale = ObjectUtils.defaultIfNull(RequestContextUtils.getLocale(request), Locale.getDefault());
        log.debug("Caught application exception", ex);
        var code = ex.getCode();
        var detail = ProblemDetail.forStatus(typeToStatusConverter.resolve(ex));
        detail.setProperty("code", code);
        var titleCode = titleMessagePrefix + code;
        var detailCode = detailMessagePrefix + code;
        if (messageSource == null) {
            detail.setTitle(titleCode);
            detail.setDetail(detailCode);
        } else {
            detail.setTitle(messageSource.getMessage(titleCode, null, titleCode, locale));
            detail.setDetail(messageSource.getMessage(detailCode, null, detailCode, locale));
        }
        ex.getDetails().forEach(detail::setProperty);
        return detail;
    }

    @Nullable
    protected ApplicationException findApplicationException(Throwable ex) {
        for (var throwable : ExceptionUtils.getThrowableList(ex)) {
            if (throwable instanceof ApplicationException appEx) {
                return appEx;
            }
            for (var suppressed : throwable.getSuppressed()) {
                if (suppressed instanceof ApplicationException appEx) {
                    return appEx;
                }
            }
        }
        return null;
    }
}
