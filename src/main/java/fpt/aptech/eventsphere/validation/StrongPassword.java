package fpt.aptech.eventsphere.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = StrongPasswordValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface StrongPassword {
    String message() default "Password must be at least 8 characters long and include uppercase letters, lowercase letters, numbers, and special characters";    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
