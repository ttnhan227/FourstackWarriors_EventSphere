package fpt.aptech.eventsphere.services;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collection;

@Service
public class CustomSuccessHandler implements AuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        if (authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            response.sendRedirect("/");
        } else if (authorities.contains(new SimpleGrantedAuthority("ROLE_PARTICIPANT"))) {
            response.sendRedirect("/");
        } else if (authorities.contains(new SimpleGrantedAuthority("ROLE_ORGANIZER"))) {
            response.sendRedirect("/organizer/index");
        } else {
            response.sendRedirect("/access-denied");
        }
    }
}