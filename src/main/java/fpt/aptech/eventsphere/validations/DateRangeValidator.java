package fpt.aptech.eventsphere.validations;

import fpt.aptech.eventsphere.models.Activity;
import fpt.aptech.eventsphere.models.Events;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;

public class DateRangeValidator implements ConstraintValidator<ValidDateRange, Object> {

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value instanceof Events event) {
            if (event.getStartDate() == null || event.getEndDate() == null) {
                return true;
            }
            boolean valid = event.getEndDate().isAfter(event.getStartDate());
            if (!valid) {
                context.buildConstraintViolationWithTemplate("End date must be after start date")
                        .addPropertyNode("endDate").addConstraintViolation();
                return false;
            }
            if (event.getActivities() != null) {
                for (Activity activity : event.getActivities()) {
                    valid &= validateActivity(activity, event.getStartDate(), event.getEndDate(), context);
                }
            }
            return valid;
        } else if (value instanceof Activity activity) {
            if (activity.getStartTime() == null || activity.getEndTime() == null) {
                return true;
            }
            return activity.getEndTime().isAfter(activity.getStartTime());
        }
        return true;
    }

    private boolean validateActivity(Activity activity, LocalDateTime eventStart, LocalDateTime eventEnd, ConstraintValidatorContext context) {
        boolean valid = true;
        if (activity.getStartTime() != null && activity.getEndTime() != null) {
            if (!activity.getEndTime().isAfter(activity.getStartTime())) {
                valid = false;
                context.buildConstraintViolationWithTemplate("Activity end time must be after start time")
                        .addPropertyNode("endTime").addConstraintViolation();
            }
            if (activity.getStartTime().isBefore(eventStart) || activity.getEndTime().isAfter(eventEnd)) {
                valid = false;
                context.buildConstraintViolationWithTemplate("Activity times must be within event start and end dates")
                        .addPropertyNode("startTime").addConstraintViolation();
            }
        }
        return valid;
    }
}