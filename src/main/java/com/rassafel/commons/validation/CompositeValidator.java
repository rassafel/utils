package com.rassafel.commons.validation;

import com.rassafel.commons.util.Assert;
import lombok.NoArgsConstructor;
import lombok.val;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

@NoArgsConstructor
public class CompositeValidator implements Validator {
    private final List<Validator> validators = new LinkedList<>();

    public CompositeValidator(Collection<Validator> validators) {
        this.validators.addAll(validators);
    }

    public void registerValidator(Validator validator) {
        Assert.notNull(validator, "validator cannot be null");
        validators.add(validator);
    }

    @Override
    public boolean supports(Class<?> clazz) {
        for (val validator : validators) {
            if (validator.supports(clazz)) return true;
        }
        return false;
    }

    @Override
    public void validate(Object target, Errors errors) {
        val clazz = target.getClass();
        for (val validator : validators) {
            if (validator.supports(clazz)) {
                validator.validate(target, errors);
            }
        }
    }
}
