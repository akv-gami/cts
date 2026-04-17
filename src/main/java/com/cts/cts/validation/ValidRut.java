package com.cts.cts.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = RutValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidRut {
    String message() default "El RUT ingresado no es válido";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}