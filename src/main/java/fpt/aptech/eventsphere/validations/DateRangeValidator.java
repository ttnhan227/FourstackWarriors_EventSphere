package fpt.aptech.eventsphere.validations;

import fpt.aptech.eventsphere.models.Events;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DateRangeValidator implements ConstraintValidator<ValidDateRange, Events> {
    @Override
    public boolean isValid(Events event, ConstraintValidatorContext context) {
        if (event == null) {
            return true;
        }

        if (event.getStartDate() == null || event.getEndDate() == null) {
            return true;
        }

        if (!event.getEndDate().isAfter(event.getStartDate())) {
            context.disableDefaultConstraintViolation();

            // Attach error to endDate field
            context.buildConstraintViolationWithTemplate(
                            "End date must be after start date"
                    )
                    .addPropertyNode("endDate")
                    .addConstraintViolation();

            return false;
        }

        return true;
    }
}
