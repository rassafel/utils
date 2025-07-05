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

package io.github.rassafel.commons.exception;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for exception code detail
 */
@Target(value = {ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface ExceptionCodeDetail {
    /**
     * Detail key
     */
    String key();

    /**
     * Detail value
     * <p>
     * Can be a SpEL.
     * <p>
     * Examples:
     * <ul>
     * <li>{@code string} - return constant value</li>
     * </ul>
     * SpEL examples:
     * <ul>
     * <li>{@code #root.className} - return class name</li>
     * <li>{@code #root.methodName} - return method name</li>
     * <li>{@code #root.key} - return {@link ExceptionCodeDetail#key()}</li>
     * <li>{@code #root.args} - return array with values of method arguments</li>
     * <li>{@code #root.argsNames} - return array with names of method arguments, can be null</li>
     * <li>{@code #root.args[X]} - return value of the method argument, where X is index of argument</li>
     * <li>{@code #root.argsNames[X]} - return name of the method argument, where X is index of argument, can be null</li>
     * <li>{@code #pX} - return value of the method parameter, where X is index of parameter</li>
     * <li>{@code #aX} - return value of the method parameter, where X is index of parameter</li>
     * <li>{@code #id} - return value of the method parameter with name "id"</li>
     * <li>{@code #p0.id} - return value of the field with name "id from the first method parameter</li>
     * <li>{@code #obj.id} - return value of the field with name "id from the method parameter with name "obj"</li>
     * </ul>
     */
    String value();
}
