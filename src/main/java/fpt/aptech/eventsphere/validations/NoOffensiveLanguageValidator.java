package fpt.aptech.eventsphere.validations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.List;

public class NoOffensiveLanguageValidator implements ConstraintValidator<NoOffensiveLanguage, String> {
    private final List<String> bannedWords = Arrays.asList("badword1", "badword2");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true;
        String lower = value.toLowerCase();
        return bannedWords.stream().noneMatch(lower::contains);
    }
}
