package fpt.aptech.eventsphere.services;

import fpt.aptech.eventsphere.dto.ProfileDto;
import fpt.aptech.eventsphere.models.UserDetails;
import fpt.aptech.eventsphere.models.Users;
import fpt.aptech.eventsphere.repositories.ProfileRepository;
import fpt.aptech.eventsphere.repositories.UserDetailsRepository;
import fpt.aptech.eventsphere.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;

import fpt.aptech.eventsphere.exceptions.ProfilePictureUploadException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Transactional
public class ProfileServiceImpl implements ProfileService {
    private static final Logger logger = LoggerFactory.getLogger(ProfileServiceImpl.class);
    @Value("${app.upload.dir}/avatars/")
    private String uploadDir;
    
    private final UserRepository userRepository;
    private final UserDetailsRepository userDetailsRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;

    public ProfileServiceImpl(UserRepository userRepository,
                            UserDetailsRepository userDetailsRepository,
                            ProfileRepository profileRepository,
                            PasswordEncoder passwordEncoder,
                            UserDetailsService userDetailsService) {
        this.userRepository = userRepository;
        this.userDetailsRepository = userDetailsRepository;
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public ProfileDto getCurrentUserProfile() {
        Users user = getCurrentUser();
        UserDetails details = user.getUserDetails();
        
        ProfileDto profile = ProfileDto.fromEntity(user, details);
        int[] stats = getUserEventStats();
        profile.setTotalEventsRegistered(stats[0]);
        profile.setTotalEventsAttended(stats[1]);
        
        return profile;
    }

    @Override
    public ProfileDto updateProfile(ProfileDto profileDto) {
        Users user = getCurrentUser();
        UserDetails details = user.getUserDetails();
        
        if (details == null) {
            details = new UserDetails(user);
            user.setUserDetails(details);
        }
        
        // Update user details
        profileDto.updateEntity(details);
        
        // Save changes
        userDetailsRepository.save(details);
        
        return ProfileDto.fromEntity(user, details);
    }

    @Override
    public boolean changePassword(String currentPassword, String newPassword) {
        Users user = getCurrentUser();
        
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return false;
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return true;
    }

    @Override
    public String updateProfilePicture(MultipartFile imageFile) {
        if (imageFile.isEmpty()) {
            throw new IllegalArgumentException("Image file cannot be empty");
        }
        
        // Generate unique filename
        String originalFilename = imageFile.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String newFilename = UUID.randomUUID() + fileExtension;
        
        try {
            // Create upload path from configured directory
            Path uploadPath = Paths.get(uploadDir);
            logger.info("Upload directory resolved to: {}", uploadPath.toAbsolutePath());
            
            // Create upload directory if it doesn't exist
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                logger.info("Created upload directory: {}", uploadPath.toAbsolutePath());
            }
            
            // Save file
            Path filePath = uploadPath.resolve(newFilename);
            logger.info("Attempting to save file to: {}", filePath.toAbsolutePath());
            Files.copy(imageFile.getInputStream(), filePath);
            logger.info("File saved successfully to: {}", filePath.toAbsolutePath());
            
            // Get current user and details
            Users user = getCurrentUser();
            UserDetails details = user.getUserDetails();
            
            // Create user details if it doesn't exist
            if (details == null) {
                details = new UserDetails(user);
                user.setUserDetails(details);
            }
            
            // Delete old profile picture if exists
            String oldImagePath = details.getAvatar();
            if (oldImagePath != null && !oldImagePath.isEmpty()) {
                try {
                    String oldFilename = oldImagePath.substring(oldImagePath.lastIndexOf("/") + 1);
                    Path oldFilePath = uploadPath.resolve(oldFilename);
                    if (Files.exists(oldFilePath)) {
                        Files.delete(oldFilePath);
                    }
                } catch (IOException e) {
                    logger.warn("Failed to delete old profile picture", e);
                }
            }
            
            // Update user's profile picture
            String imageUrl = "/images/avatars/" + newFilename;
            details.setAvatar(imageUrl);
            userDetailsRepository.save(details);

            // Refresh the security context to reflect the updated avatar
            Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
            org.springframework.security.core.userdetails.UserDetails updatedUserDetails = userDetailsService.loadUserByUsername(user.getEmail());
            Authentication newAuth = new UsernamePasswordAuthenticationToken(updatedUserDetails, currentAuth.getCredentials(), updatedUserDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(newAuth);
            
            return imageUrl;
            
        } catch (IOException e) {
            logger.error("Failed to process profile picture upload", e);
            throw new ProfilePictureUploadException("Failed to process profile picture: " + e.getMessage(), e);
        }
    }

    @Override
    public int[] getUserEventStats() {
        Users user = getCurrentUser();
        int totalRegistrations = (int) profileRepository.countTotalRegistrations(user.getUserId());
        int attendedEvents = (int) profileRepository.countAttendedEvents(user.getUserId());
        return new int[]{totalRegistrations, attendedEvents};
    }
    
    private Users getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
