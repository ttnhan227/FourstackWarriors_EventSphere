package fpt.aptech.eventsphere.services;

import fpt.aptech.eventsphere.models.Roles;
import fpt.aptech.eventsphere.models.Users;
import fpt.aptech.eventsphere.repositories.RoleRepository;
import fpt.aptech.eventsphere.repositories.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomSuccessHandler.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {
        logger.info("Authentication success handler called");
        logger.info("Authentication name: " + authentication.getName());

        // Handle Google OAuth2 user
        if (authentication.getPrincipal() instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            String email = oauth2User.getAttribute("email");
            String googleId = oauth2User.getAttribute("sub");

            logger.info("OAuth2 User - Email: " + email + ", Google ID: " + googleId);

            if (email != null && googleId != null) {
                handleGoogleUser(email, googleId);
            }
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        logger.info("User authorities: " + authorities);

        if (authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            logger.info("Redirecting to admin page");
            response.sendRedirect("/");
        } else if (authorities.contains(new SimpleGrantedAuthority("ROLE_PARTICIPANT"))) {
            logger.info("Redirecting to participant page");
            response.sendRedirect("/");
        } else if (authorities.contains(new SimpleGrantedAuthority("ROLE_ORGANIZER"))) {
            logger.info("Redirecting to organizer page");
            response.sendRedirect("/organizer/index");
        } else {
            logger.info("Redirecting to default page");
            response.sendRedirect("/");
        }
    }

    private void handleGoogleUser(String email, String googleId) {
        logger.info("Handling Google user: " + email);

        Optional<Users> existingUser = userRepository.findByEmailIgnoreCase(email);

        if (existingUser.isPresent()) {
            Users user = existingUser.get();
            logger.info("Existing user found: " + user.getEmail() + ", current Google ID: " + user.getGoogleId());

            if (user.getGoogleId() == null || !user.getGoogleId().equals(googleId)) {
                logger.info("Setting Google ID: " + googleId);
                user.setGoogleId(googleId);
                Users savedUser = userRepository.save(user);
                logger.info("User saved with Google ID: " + savedUser.getGoogleId());
            } else {
                logger.info("User already has correct Google ID: " + user.getGoogleId());
            }

            // Ensure user has at least PARTICIPANT role
            if (user.getRoles().isEmpty()) {
                Optional<Roles> participantRole = roleRepository.findByRoleName("PARTICIPANT");
                if (participantRole.isPresent()) {
                    user.getRoles().add(participantRole.get());
                    userRepository.save(user);
                    logger.info("Added PARTICIPANT role to existing user");
                }
            }
        } else {
            logger.info("Creating new user for Google login: " + email);
            Users newUser = new Users();
            newUser.setEmail(email);
            newUser.setGoogleId(googleId);
            newUser.setActive(true);
            newUser.setDeleted(false);

            // Set default role
            Optional<Roles> participantRole = roleRepository.findByRoleName("PARTICIPANT");
            if (participantRole.isPresent()) {
                newUser.setRoles(Collections.singletonList(participantRole.get()));
            }

            Users savedUser = userRepository.save(newUser);
            logger.info("Created new user with Google ID: " + savedUser.getGoogleId());
        }
    }
}
