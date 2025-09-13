package fpt.aptech.eventsphere.services;

import fpt.aptech.eventsphere.models.Users;
import fpt.aptech.eventsphere.repositories.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
@RequiredArgsConstructor
public class CustomSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomSuccessHandler.class);

    private final UserRepository userRepository;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request,
                                      HttpServletResponse response,
                                      Authentication authentication)
            throws IOException, ServletException {
        logger.debug("Authentication success - Starting authentication success handler");

        Users user = null;
        boolean isOAuth2User = authentication.getPrincipal() instanceof OAuth2User;
        logger.info("Is OAuth2 user: " + isOAuth2User);

        try {
            if (isOAuth2User) {
                // Handle OAuth2 login (Google)
                OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
                String email = oauth2User.getAttribute("email");
                logger.info("OAuth2 user email: " + email);

                if (email != null) {
                    // Find the user in the database
                    user = userRepository.findByEmailIgnoreCase(email).orElse(null);
                    
                    if (user != null) {
                        logger.info("Found user in database: " + user.getEmail());
                        
                        // Get the user's roles from the database
                        List<GrantedAuthority> updatedAuthorities = user.getRoles().stream()
                                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRoleName()))
                                .collect(Collectors.toList());
                        
                        logger.info("User roles: " + updatedAuthorities);
                        
                        // Create a new authentication with the updated authorities
                        OAuth2User principal = new DefaultOAuth2User(
                                updatedAuthorities,
                                oauth2User.getAttributes(),
                                "email");
                        
                        if (authentication instanceof OAuth2AuthenticationToken) {
                            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
                            OAuth2AuthenticationToken newAuth = new OAuth2AuthenticationToken(
                                    principal,
                                    updatedAuthorities,
                                    oauthToken.getAuthorizedClientRegistrationId());
                            
                            // Update the security context
                            SecurityContextHolder.getContext().setAuthentication(newAuth);
                            authentication = newAuth;
                            
                            logger.info("Updated security context with roles: " + updatedAuthorities);
                        }
                    } else {
                        logger.warn("User not found in database for email: " + email);
                    }
                }
            } else {
                // Handle form login
                String username = authentication.getName();
                logger.info("Form login for username: " + username);
                user = userRepository.findByEmailIgnoreCase(username).orElse(null);
            }

            // Check if user exists and has completed profile
            if (user != null && user.getUserDetails() == null) {
                logger.info("User needs to complete profile");
                response.sendRedirect("/auth/register?oauth2user=true&email=" + 
                    java.net.URLEncoder.encode(user.getEmail(), "UTF-8"));
                return;
            }

            // Log the authorities for debugging
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            logger.info("Final authorities in authentication: " + authorities);

            // Redirect based on role
            if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                logger.info("Redirecting to admin dashboard");
                response.sendRedirect("/admin/index");
            } else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ORGANIZER"))) {
                logger.info("Redirecting to organizer dashboard");
                response.sendRedirect("/organizer/index");
            } else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_PARTICIPANT"))) {
                logger.info("Redirecting to participant dashboard");
                response.sendRedirect("/participant/dashboard");
            } else {
                logger.info("No specific role found, redirecting to home");
                response.sendRedirect("/");
            }
            
        } catch (Exception e) {
            logger.error("Error in authentication success handler", e);
            response.sendRedirect("/auth/login?error");
        }
    }

    // Google user handling is now done in the onAuthenticationSuccess method
}
