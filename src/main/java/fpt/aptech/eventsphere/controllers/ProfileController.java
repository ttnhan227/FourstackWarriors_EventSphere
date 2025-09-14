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
import fpt.aptech.eventsphere.models.Bookmark;
import fpt.aptech.eventsphere.repositories.BookmarkRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import fpt.aptech.eventsphere.models.Users;

@Controller
@RequestMapping("/profile")
@PreAuthorize("isAuthenticated()")
public class ProfileController {
    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);
    
    private final ProfileService profileService;
    private final BookmarkRepository bookmarkRepository;
    private static final int PAGE_SIZE = 10;

    public ProfileController(ProfileService profileService, BookmarkRepository bookmarkRepository) {
        this.profileService = profileService;
        this.bookmarkRepository = bookmarkRepository;
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

    @GetMapping("/bookmarks")
    public String viewBookmarks(
            @AuthenticationPrincipal Users user,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {
        try {
            Pageable pageable = PageRequest.of(page, PAGE_SIZE);
            Page<Bookmark> bookmarksPage = bookmarkRepository.findByUser(user, pageable);
            
            model.addAttribute("bookmarks", bookmarksPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", bookmarksPage.getTotalPages());
            
            return "profile/bookmarks";
            
        } catch (Exception e) {
            logger.error("Error loading bookmarks", e);
            model.addAttribute("error", "Error loading bookmarks: " + e.getMessage());
            return "error/error";
        }
    }
    
    @PostMapping("/bookmarks/remove/{bookmarkId}")
    public String removeBookmark(
            @PathVariable("bookmarkId") Long bookmarkId,
            @AuthenticationPrincipal Users user,
            RedirectAttributes redirectAttributes) {
        try {
            // Verify the bookmark belongs to the current user before deleting
            bookmarkRepository.findById(bookmarkId).ifPresent(bookmark -> {
                if (bookmark.getUser().getUserId() == user.getUserId()) {
                    bookmarkRepository.delete(bookmark);
                    redirectAttributes.addFlashAttribute("success", "Bookmark removed successfully");
                } else {
                    redirectAttributes.addFlashAttribute("error", "You don't have permission to remove this bookmark");
                }
            });
            
            return "redirect:/profile/bookmarks";
            
        } catch (Exception e) {
            logger.error("Error removing bookmark", e);
            redirectAttributes.addFlashAttribute("error", "Error removing bookmark: " + e.getMessage());
            return "redirect:/profile/bookmarks";
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
