package fpt.aptech.eventsphere.validations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = NoOffensiveLanguageValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface NoOffensiveLanguage {
    String message() default "Comments contain offensive language.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}