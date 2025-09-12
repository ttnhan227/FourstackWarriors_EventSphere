package fpt.aptech.eventsphere.controllers;

import fpt.aptech.eventsphere.dto.UserRegistrationDto;
import fpt.aptech.eventsphere.models.Roles;
import fpt.aptech.eventsphere.models.UserDetails;
import fpt.aptech.eventsphere.models.Users;
import fpt.aptech.eventsphere.repositories.RoleRepository;
import fpt.aptech.eventsphere.repositories.UserDetailsRepository;
import fpt.aptech.eventsphere.repositories.UserRepository;
import fpt.aptech.eventsphere.services.EmailService;
import fpt.aptech.eventsphere.validations.StrongPassword;
import jakarta.validation.Valid;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final UserDetailsRepository userDetailsRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public AuthController(UserRepository userRepository,
                          UserDetailsRepository userDetailsRepository,
                          RoleRepository roleRepository,
                          PasswordEncoder passwordEncoder,
                          EmailService emailService) {
        this.userRepository = userRepository;
        this.userDetailsRepository = userDetailsRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }



    @GetMapping("/login")
    public String showLoginForm(@RequestParam(value = "error", required = false) String error,
                                @RequestParam(value = "logout", required = false) String logout,
                                Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth.getPrincipal() instanceof String)) {
            return "redirect:/";
        }

        if (error != null) {
            model.addAttribute("error", "Invalid email or password. Please try again.");
        }

        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully.");
        }

        return "auth/login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(@RequestParam(value = "oauth2user", required = false) boolean isOAuth2User,
                                     @RequestParam(value = "email", required = false) String emailParam,
                                     Authentication authentication,
                                     Model model) {
        UserRegistrationDto userDto = new UserRegistrationDto();
        
        if (isOAuth2User) {
            String email = emailParam;
            
            // If email not in URL, try to get from authentication
            if ((email == null || email.isEmpty()) && authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();
                
                if (principal instanceof org.springframework.security.oauth2.core.user.DefaultOAuth2User) {
                    org.springframework.security.oauth2.core.user.DefaultOAuth2User oauth2User = 
                        (org.springframework.security.oauth2.core.user.DefaultOAuth2User) principal;
                    email = oauth2User.getAttribute("email");
                } else if (principal instanceof org.springframework.security.core.userdetails.User user) {
                    email = user.getUsername();
                }
            }
            
            if (email != null && !email.isEmpty()) {
                userDto.setEmail(email);
                model.addAttribute("isOAuth2User", true);
            } else {
                // If we still don't have an email, redirect to login with error
                return "redirect:/auth/login?error=oauth_failed";
            }
        } else {
            model.addAttribute("isOAuth2User", false);
        }
        
        model.addAttribute("user", userDto);
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUserAccount(
            @Valid @ModelAttribute("user") UserRegistrationDto registrationDto,
            @RequestParam(value = "isOAuth2User", required = false, defaultValue = "false") boolean isOAuth2User,
            BindingResult result,
            Model model) {

        // Check if passwords match
        if (!registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.user", "Passwords do not match");
        }
        
        // Check password requirements
        if (registrationDto.getPassword() == null || registrationDto.getPassword().length() < 8) {
            result.rejectValue("password", "error.user", "Password must be at least 8 characters long");
        }

        // Check if email already exists
        Optional<Users> existingUser = userRepository.findByEmailIgnoreCase(registrationDto.getEmail());
        if (existingUser.isPresent() && !isOAuth2User) {
            result.rejectValue("email", "error.user", "An account already exists with this email");
        }

        if (result.hasErrors()) {
            model.addAttribute("user", registrationDto);
            model.addAttribute("isOAuth2User", isOAuth2User);
            return "auth/register";
        }

        Users user;
        if (isOAuth2User && existingUser.isPresent()) {
            // For OAuth users, update existing user
            user = existingUser.get();
            // Update password if it was changed
            if (registrationDto.getPassword() != null && !registrationDto.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
            }
        } else {
            // For new users, create a new user
            user = new Users();
            user.setEmail(registrationDto.getEmail());
            user.setActive(true);
            user.setDeleted(false);
            
            // Set password
            user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));

            // Assign default role (PARTICIPANT)
            Roles role = roleRepository.findByRoleName("PARTICIPANT")
                    .orElseGet(() -> {
                        Roles newRole = new Roles();
                        newRole.setRoleName("PARTICIPANT");
                        return roleRepository.save(newRole);
                    });
            user.getRoles().add(role);
        }

        Users savedUser = userRepository.save(user);

        // Create or update user details
        UserDetails userDetails = user.getUserDetails();
        if (userDetails == null) {
            userDetails = new UserDetails();
            userDetails.setUser(savedUser);
        }
        userDetails.setFullName(registrationDto.getFullName());
        userDetails.setPhone(registrationDto.getPhone());
        userDetailsRepository.save(userDetails);

        // Redirect to login with success message
        return "redirect:/auth/login?registered";
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email, Model model) {
        userRepository.findByEmailIgnoreCase(email).ifPresent(user -> {
            String token = UUID.randomUUID().toString();
            user.setResetToken(token);
            userRepository.save(user);

            // In a real application, you would send an email with the reset link
            // emailService.sendPasswordResetEmail(user.getEmail(), token);
        });

        model.addAttribute("message", "If an account exists with this email, a password reset link has been sent.");
        return "auth/forgot-password";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        userRepository.findByResetToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid reset token"));

        model.addAttribute("token", token);
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(
            @RequestParam("token") String token,
            @RequestParam("password") @StrongPassword String password) {

        Users user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid reset token"));

        user.setPassword(passwordEncoder.encode(password));
        user.setResetToken(null);
        userRepository.save(user);

        return "redirect:/auth/login?resetSuccess";
    }
}
