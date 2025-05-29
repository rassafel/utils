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


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

interface SystemComponent {
    @ExceptionCode(value = "1", on = IllegalArgumentException.class)
    void single();

    @ExceptionCode(value = "1", on = RuntimeException.class)
    @ExceptionCode(value = "2", type = "INPUT", on = IllegalArgumentException.class)
    void inheritance();

    @ExceptionCode(value = "1", on = RuntimeException.class, details = {
        @ExceptionCodeDetail(key = "simple", value = "simple"),
        @ExceptionCodeDetail(key = "simpleArgument", value = "#id"),
        @ExceptionCodeDetail(key = "complexArgument", value = "#complex.id"),
    })
    void detail(String id, ComplexObject complex);

    void withoutAnnotation();

    @RequiredArgsConstructor
    @AllArgsConstructor
    @Data
    class ComplexObject {
        private String id;
    }
}
