package fpt.aptech.eventsphere.services;

import fpt.aptech.eventsphere.models.Roles;
import fpt.aptech.eventsphere.models.Users;
import fpt.aptech.eventsphere.repositories.RoleRepository;
import fpt.aptech.eventsphere.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        logger.info("CustomOAuth2UserService.loadUser called");
        OAuth2User oauth2User = super.loadUser(userRequest);

        // Get email and googleId from OAuth2 user
        String email = oauth2User.getAttribute("email");
        String googleId = oauth2User.getAttribute("sub");

        // Debug: Print all available attributes
        logger.info("OAuth2 User Attributes: " + oauth2User.getAttributes());
        logger.info("Email from OAuth2: " + email);
        logger.info("Google ID (sub): " + googleId);

        // Find or create user in database
        Optional<Users> existingUser = userRepository.findByEmailIgnoreCase(email);
        Users user;

        if (existingUser.isPresent()) {
            // Update existing user
            user = existingUser.get();
            logger.info("Found existing user: " + user.getEmail());
            
            // Update Google ID if not set
            if (user.getGoogleId() == null) {
                user.setGoogleId(googleId);
                user = userRepository.save(user);
                logger.info("Updated user with Google ID: " + user.getGoogleId());
            }
            
            // Ensure user has at least PARTICIPANT role
            if (user.getRoles().isEmpty()) {
                Roles participantRole = roleRepository.findByRoleName("PARTICIPANT")
                    .orElseThrow(() -> new IllegalStateException("PARTICIPANT role not found"));
                user.getRoles().add(participantRole);
                user = userRepository.save(user);
                logger.info("Added PARTICIPANT role to user: " + user.getEmail());
            }
        } else {
            // Create new user
            logger.info("Creating new user for email: " + email);
            user = new Users();
            user.setEmail(email);
            user.setGoogleId(googleId);
            user.setActive(true);
            user.setDeleted(false);
            
            // Set default role
            Roles participantRole = roleRepository.findByRoleName("PARTICIPANT")
                .orElseThrow(() -> new IllegalStateException("PARTICIPANT role not found"));
            user.setRoles(Collections.singletonList(participantRole));
            
            user = userRepository.save(user);
            logger.info("Created new user with PARTICIPANT role: " + user.getEmail());
        }

        // Map user roles to authorities
        Collection<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRoleName()))
                .collect(Collectors.toList());

        logger.info("Mapped authorities for " + user.getEmail() + ": " + authorities);

        // Create a new attributes map that includes the original OAuth2 attributes
        Map<String, Object> attributes = new HashMap<>(oauth2User.getAttributes());
        
        // Add roles to the attributes for Thymeleaf access
        List<String> roleNames = user.getRoles().stream()
                .map(role -> "ROLE_" + role.getRoleName())
                .collect(Collectors.toList());
        
        attributes.put("roles", roleNames);
        
        // Create and return the OAuth2User with the updated authorities
        return new DefaultOAuth2User(authorities, attributes, "email");
    }

    // Role mapping is now done inline where needed
}
