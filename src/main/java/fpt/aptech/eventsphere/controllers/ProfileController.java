package fpt.aptech.eventsphere.controllers;

import fpt.aptech.eventsphere.dto.ProfileDto;
import fpt.aptech.eventsphere.services.ProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import fpt.aptech.eventsphere.exceptions.ProfilePictureUploadException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/profile")
@PreAuthorize("isAuthenticated()")
public class ProfileController {
    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);
    
    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("")
    public String viewProfile(Model model) {
        try {
            ProfileDto profile = profileService.getCurrentUserProfile();
            model.addAttribute("profile", profile);
            return "profile/view";
        } catch (Exception e) {
            logger.error("Error loading profile", e);
            model.addAttribute("error", "Error loading profile: " + e.getMessage());
            return "error/error";
        }
    }

    @GetMapping("/edit")
    public String editProfileForm(Model model) {
        try {
            ProfileDto profile = profileService.getCurrentUserProfile();
            model.addAttribute("profile", profile);
            return "profile/edit";
        } catch (Exception e) {
            logger.error("Error loading edit profile form", e);
            model.addAttribute("error", "Error loading edit form: " + e.getMessage());
            return "error/error";
        }
    }

    @PostMapping("/update")
    public String updateProfile(@Valid @ModelAttribute("profile") ProfileDto profileDto,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "profile/edit";
        }
        
        try {
            profileService.updateProfile(profileDto);
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
            return "redirect:/profile";
        } catch (Exception e) {
            logger.error("Error updating profile", e);
            redirectAttributes.addFlashAttribute("error", "Error updating profile: " + e.getMessage());
            return "redirect:/profile/edit";
        }
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam("currentPassword") String currentPassword,
                               @RequestParam("newPassword") String newPassword,
                               RedirectAttributes redirectAttributes) {
        try {
            if (profileService.changePassword(currentPassword, newPassword)) {
                redirectAttributes.addFlashAttribute("success", "Password changed successfully!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Current password is incorrect");
            }
            return "redirect:/profile";
        } catch (Exception e) {
            logger.error("Error changing password", e);
            redirectAttributes.addFlashAttribute("error", "Error changing password: " + e.getMessage());
            return "redirect:/profile";
        }
    }

    @PostMapping("/upload-picture")
    public String uploadProfilePicture(@RequestParam("imageFile") MultipartFile imageFile,
                                     RedirectAttributes redirectAttributes) {
        try {
            String imageUrl = profileService.updateProfilePicture(imageFile);
            redirectAttributes.addFlashAttribute("success", "Profile picture updated successfully!");
            redirectAttributes.addFlashAttribute("imageUrl", imageUrl);
        } catch (ProfilePictureUploadException e) {
            logger.error("Error uploading profile picture", e);
            redirectAttributes.addFlashAttribute("error", "Error uploading profile picture: " + e.getMessage());
        } catch (Exception e) {
            logger.error("An unexpected error occurred during profile picture upload", e);
            redirectAttributes.addFlashAttribute("error", "An unexpected error occurred: " + e.getMessage());
        }
        return "redirect:/profile";
    }
}
