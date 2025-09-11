package fpt.aptech.eventsphere.services;

import fpt.aptech.eventsphere.dto.ProfileDto;
import fpt.aptech.eventsphere.models.UserDetails;
import fpt.aptech.eventsphere.models.Users;
import org.springframework.web.multipart.MultipartFile;

public interface ProfileService {
    /**
     * Get the current user's profile information
     * @return ProfileDto containing user profile data
     */
    ProfileDto getCurrentUserProfile();
    
    /**
     * Update the current user's profile information
     * @param profileDto The updated profile data
     * @return The updated profile DTO
     */
    ProfileDto updateProfile(ProfileDto profileDto);
    
    /**
     * Change the current user's password
     * @param currentPassword The current password for verification
     * @param newPassword The new password to set
     * @return true if password was changed successfully
     */
    boolean changePassword(String currentPassword, String newPassword);
    
    /**
     * Update the user's profile picture
     * @param imageFile The uploaded image file
     * @return The URL of the uploaded profile picture
     */
    String updateProfilePicture(MultipartFile imageFile);
    
    /**
     * Get the user's registration statistics
     * @return Array containing [totalRegistrations, attendedEvents]
     */
    int[] getUserEventStats();
}
