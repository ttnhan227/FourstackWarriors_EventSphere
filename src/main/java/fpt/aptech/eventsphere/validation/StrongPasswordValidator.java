package fpt.aptech.eventsphere.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

    @Override
    public void initialize(StrongPassword constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.trim().isEmpty()) {
            return false;
        }

        // Kiểm tra độ dài tối thiểu
        if (password.length() < 8) {
            return false;
        }

        // Kiểm tra có chữ hoa
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);

        // Kiểm tra có chữ thường
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);

        // Kiểm tra có số
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);

        // Kiểm tra có ký tự đặc biệt
        boolean hasSpecial = password.chars()
                .anyMatch(ch -> "!@#$%^&*()_+-=[]{}|;:,.<>?".indexOf(ch) >= 0);

        return hasUpper && hasLower && hasDigit && hasSpecial;
    }
}

