package fpt.aptech.eventsphere.dto;

import fpt.aptech.eventsphere.validations.StrongPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

public class UserRegistrationDto {
    @NotEmpty(message = "Full name is required")
    private String fullName;
    
    @NotEmpty(message = "Email is required")
    @Email(message = "Please provide a valid email")
    private String email;
    
    @NotEmpty(message = "Phone number is required")
    private String phone;
    
    @StrongPassword
    @NotEmpty(message = "Password is required")
    private String password;
    
    @NotEmpty(message = "Password confirmation is required")
    private String confirmPassword;

    // Getters and Setters
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
