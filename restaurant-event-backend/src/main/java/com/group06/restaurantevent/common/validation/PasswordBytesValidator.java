package com.group06.restaurantevent.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordBytesValidator implements ConstraintValidator<PasswordBytes, String> {
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null
                || value.getBytes(java.nio.charset.StandardCharsets.UTF_8).length <= 72;
    }
}
