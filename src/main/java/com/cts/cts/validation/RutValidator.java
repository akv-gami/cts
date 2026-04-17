package com.cts.cts.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RutValidator implements ConstraintValidator<ValidRut, String> {

    @Override
    public boolean isValid(String rut, ConstraintValidatorContext context) {
        if (rut == null || rut.isBlank()) return true;

        String clean = rut.replaceAll("[^0-9kK]", "");

        if (clean.length() < 8 || clean.length() > 9) return false;

        String body = clean.substring(0, clean.length() - 1);
        char dvChar = Character.toLowerCase(clean.charAt(clean.length() - 1));

        int sum = 0;
        int multiplier = 2;

        for (int i = body.length() - 1; i >= 0; i--) {
            sum += multiplier * Character.getNumericValue(body.charAt(i));
            multiplier = multiplier == 7 ? 2 : multiplier + 1;
        }

        int remainder = 11 - (sum % 11);
        char expected;
        if (remainder == 11) {
            expected = '0';
        } else if (remainder == 10) {
            expected = 'k';
        } else {
            expected = Character.forDigit(remainder, 10);
        }

        return dvChar == expected;
    }
}