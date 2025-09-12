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

        // Debug: Print all available attributes
        logger.info("OAuth2 User Attributes: " + oauth2User.getAttributes());

        String email = oauth2User.getAttribute("email");
        String googleId = oauth2User.getAttribute("sub");

        // Try alternative attribute names if sub is null
        if (googleId == null) {
            googleId = oauth2User.getAttribute("id");
        }
        if (googleId == null) {
            googleId = oauth2User.getAttribute("googleId");
        }

        // Debug: Print the values
        logger.info("Email: " + email);
        logger.info("Google ID (sub): " + oauth2User.getAttribute("sub"));
        logger.info("Google ID (id): " + oauth2User.getAttribute("id"));
        logger.info("Google ID (googleId): " + oauth2User.getAttribute("googleId"));
        logger.info("Final Google ID used: " + googleId);

        Optional<Users> existingUser = userRepository.findByEmailIgnoreCase(email);
        logger.info("Looking for user with email: " + email);
        logger.info("User found: " + existingUser.isPresent());
        if (existingUser.isPresent()) {
            logger.info("Found user email in DB: " + existingUser.get().getEmail());
            logger.info("Email match: " + email.equals(existingUser.get().getEmail()));
        }

        Users user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
            logger.info("Existing user found: " + user.getEmail() + ", current Google ID: " + user.getGoogleId());
            if (user.getGoogleId() == null) {
                logger.info("Setting Google ID: " + googleId);
                user.setGoogleId(googleId);
                Users savedUser = userRepository.save(user);
                logger.info("User saved with Google ID: " + savedUser.getGoogleId());
            } else {
                logger.info("User already has Google ID: " + user.getGoogleId());
            }
            // Ensure user has at least PARTICIPANT role
            if (user.getRoles().isEmpty()) {
                Optional<Roles> participantRole = roleRepository.findByRoleName("PARTICIPANT");
                if (participantRole.isPresent()) {
                    user.getRoles().add(participantRole.get());
                    userRepository.save(user);
                }
            }
        } else {
            user = new Users();
            user.setEmail(email);
            user.setGoogleId(googleId);
            user.setActive(true);
            user.setDeleted(false);
            // set default role
            Optional<Roles> participantRole = roleRepository.findByRoleName("PARTICIPANT");
            if (participantRole.isPresent()) {
                user.setRoles(Collections.singletonList(participantRole.get()));
            }
            userRepository.save(user);
        }

        Collection<? extends GrantedAuthority> authorities = mapRolesToAuthorities(user);

        return new DefaultOAuth2User(authorities, oauth2User.getAttributes(), "email");
    }

    private Collection<? extends GrantedAuthority> mapRolesToAuthorities(Users user) {
        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRoleName()))
                .collect(Collectors.toList());
    }
}
