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

package com.rassafel.commons.spel;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.StringValueResolver;

import java.lang.reflect.Method;

public class SpelResolver implements EmbeddedValueResolverAware {
    private static final String PLACEHOLDER_SPEL_REGEX = "^[$#]\\{.+}$";
    private static final String METHOD_SPEL_REGEX = "^#.+$";

    private final SpelExpressionParser expressionParser;
    private final ParameterNameDiscoverer parameterNameDiscoverer;
    private StringValueResolver stringValueResolver;

    public SpelResolver(SpelExpressionParser expressionParser, ParameterNameDiscoverer parameterNameDiscoverer) {
        this.expressionParser = expressionParser;
        this.parameterNameDiscoverer = parameterNameDiscoverer;
    }

    @Override
    public void setEmbeddedValueResolver(StringValueResolver resolver) {
        stringValueResolver = resolver;
    }

    public String resolve(Method method, Object[] args, String key, String expression) {
        if (StringUtils.isBlank(expression)) return expression;
        if (expression.matches(PLACEHOLDER_SPEL_REGEX) && stringValueResolver != null) {
            return stringValueResolver.resolveStringValue(expression);
        }
        if (expression.matches(METHOD_SPEL_REGEX)) {
            var rootObject = SpelRootObject.of(method, args, key);
            var context = new MethodBasedEvaluationContext(rootObject, method, args, parameterNameDiscoverer);
            return expressionParser.parseExpression(expression).getValue(context, String.class);
        }
        return expression;
    }

    @Data
    public static class SpelRootObject {
        private final String className;
        private final String methodName;
        private final Object[] args;
        private final String key;

        public static SpelRootObject of(Method method, Object[] args, String key) {
            return new SpelRootObject(method.getDeclaringClass().getName(), method.getName(), args, key);
        }
    }
}
