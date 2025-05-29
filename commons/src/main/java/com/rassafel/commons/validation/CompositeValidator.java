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

package com.rassafel.commons.validation;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class CompositeValidator implements Validator {
    private final List<Validator> validators = new LinkedList<>();

    public CompositeValidator(Collection<Validator> validators) {
        this.validators.addAll(validators);
    }

    public void registerValidator(Validator validator) {
        org.springframework.util.Assert.notNull(validator, "validator cannot be null");
        validators.add(validator);
    }

    @Override
    public boolean supports(Class<?> clazz) {
        for (var validator : validators) {
            if (validator.supports(clazz)) return true;
        }
        return false;
    }

    @Override
    public void validate(Object target, Errors errors) {
        var clazz = target.getClass();
        for (var validator : validators) {
            if (validator.supports(clazz)) {
                validator.validate(target, errors);
            }
        }
    }
}
