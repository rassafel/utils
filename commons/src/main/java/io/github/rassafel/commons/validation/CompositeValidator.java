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

package io.github.rassafel.commons.validation;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Composite validator that combines multiple validators.
 */
@RequiredArgsConstructor
public class CompositeValidator implements Validator {
    private final List<Validator> validators = new LinkedList<>();

    public CompositeValidator(@NonNull Collection<Validator> validators) {
        this.validators.addAll(validators);
    }

    public void registerValidator(@NonNull Validator validator) {
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
