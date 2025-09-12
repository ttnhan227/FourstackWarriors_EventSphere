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
        logger.debug("Authentication success");

        Users user = null;

        // Handle Google OAuth2 user
        if (authentication.getPrincipal() instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            String email = oauth2User.getAttribute("email");
            String googleId = oauth2User.getAttribute("sub");


            if (email != null && googleId != null) {
                user = handleGoogleUser(email, googleId);
            }
        } else {
            // For form login
            String username = authentication.getName();
            user = userRepository.findByEmailIgnoreCase(username).orElse(null);
        }

        // Check if user exists and has completed profile
        if (user != null && user.getUserDetails() == null) {
            String email = user.getEmail();
            response.sendRedirect("/auth/register?oauth2user=true&email=" + java.net.URLEncoder.encode(email, "UTF-8"));
            return;
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        if (authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            response.sendRedirect("/");
        } else if (authorities.contains(new SimpleGrantedAuthority("ROLE_ORGANIZER"))) {
            response.sendRedirect("/organizer/index");
        } else {
            response.sendRedirect("/");
        }
    }

    private Users handleGoogleUser(String email, String googleId) {

        Optional<Users> existingUser = userRepository.findByEmailIgnoreCase(email);
        Users user;

        if (existingUser.isPresent()) {
            user = existingUser.get();
            if (user.getGoogleId() == null || !user.getGoogleId().equals(googleId)) {
                user.setGoogleId(googleId);
                user = userRepository.save(user);
            }

            // Ensure user has at least PARTICIPANT role
            if (user.getRoles().isEmpty()) {
                Optional<Roles> participantRole = roleRepository.findByRoleName("PARTICIPANT");
                if (participantRole.isPresent()) {
                    user.getRoles().add(participantRole.get());
                    user = userRepository.save(user);
                }
            }
        } else {
            user = new Users();
            user.setEmail(email);
            user.setGoogleId(googleId);
            user.setActive(true);
            user.setDeleted(false);

            // Set default role
            Optional<Roles> participantRole = roleRepository.findByRoleName("PARTICIPANT");
            if (participantRole.isPresent()) {
                user.setRoles(Collections.singletonList(participantRole.get()));
            }

            user = userRepository.save(user);
        }
        
        return user;
    }
}
