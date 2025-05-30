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

import java.util.*;
import java.util.stream.Stream;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import com.rassafel.commons.spel.SpelResolver;

/**
 * Aspect that handles exceptions and returns the appropriate error code based on the exception type.
 */
@Slf4j
@RequiredArgsConstructor
@Aspect
@Component
public class ExceptionCodeAspect {
    @NonNull
    private final SpelResolver spelResolver;

    @AfterThrowing(value = "@annotation(exceptionCodes)", throwing = "ex")
    public void handleException(JoinPoint jp, Throwable ex, ExceptionCode.List exceptionCodes) throws Throwable {
        doHandleException(jp, ex, exceptionCodes.value());
    }

    @AfterThrowing(value = "@annotation(exceptionCode)", throwing = "ex")
    public void handleException(JoinPoint jp, Throwable ex, ExceptionCode exceptionCode) throws Throwable {
        doHandleException(jp, ex, exceptionCode);
    }

    private void doHandleException(JoinPoint jp, Throwable ex, ExceptionCode... exceptionCodes) throws Throwable {
        log.atDebug().addArgument(() -> getMethodName(jp)).log("Handle Exception on {}");
        if (hasApplicationException(ex)) {
            throw ex;
        }
        var code = foundMatched(ex, exceptionCodes);
        if (code != null) {
            var applicationException = createException(jp, code);
            log.atDebug()
                    .addArgument(applicationException::toCode)
                    .log("Add found ApplicationException to suppressed, code: {}");
            ex.addSuppressed(applicationException);
        }
        throw ex;
    }

    private String getMethodName(JoinPoint jp) {
        var method = ((MethodSignature) jp.getSignature()).getMethod();
        var className = method.getDeclaringClass().getName();
        var methodName = method.getName();
        return className + "." + methodName;
    }

    private boolean hasApplicationException(Throwable ex) {
        if (ex instanceof ApplicationException) {
            log.debug("Current exception is ApplicationException");
            return true;
        }
        if (Stream.of(ex.getSuppressed()).anyMatch(ApplicationException.class::isInstance)) {
            log.debug("Found suppressed ApplicationException");
            return true;
        }
        log.debug("ApplicationException not found");
        return false;
    }

    @Nullable
    private ExceptionCode foundMatched(Throwable ex, ExceptionCode[] codes) {
        var parents = new ArrayList<Class<?>>();
        parents.add(ex.getClass());
        parents.addAll(ClassUtils.getAllSuperclasses(ex.getClass()));
        var found = Arrays.stream(codes)
                .map(code -> Pair.of(evaluateDeep(code, parents), code))
                .filter(p -> Objects.nonNull(p.getKey()))
                .min(Map.Entry.comparingByKey())
                .orElse(null);
        if (found == null) {
            log.debug("Not found matched exception code");
            return null;
        }
        var exceptionCode = found.getValue();
        log.debug("Best matched exception code, type: {}; value: {}; matched class: {}",
                exceptionCode.type(), exceptionCode.value(), parents.get(found.getKey()));
        return exceptionCode;
    }

    @Nullable
    private Integer evaluateDeep(ExceptionCode code, List<Class<?>> classes) {
        return Arrays.stream(code.on())
                .flatMap(ex -> {
                    var index = classes.indexOf(ex);
                    if (index == -1) return Stream.empty();
                    log.trace("Found matched exception code, type: {}; value: {}; matched class: {}; deep: {}",
                            code.type(), code.value(), classes.get(index), index);
                    return Stream.of(index);
                })
                .min(Comparator.naturalOrder())
                .orElse(null);
    }

    private ApplicationException createException(JoinPoint jp, ExceptionCode code) {
        var method = ((MethodSignature) jp.getSignature()).getMethod();
        var args = jp.getArgs();
        var exception = new ApplicationException(code.value(), code.type());
        for (var detail : code.details()) {
            var resolved = spelResolver.resolve(method, args, detail.key(), detail.value());
            log.trace("Resolved detail, key: {}; value: {}",
                    detail.key(), resolved);
            exception.addDetail(detail.key(), resolved);
        }
        return exception;
    }
}
