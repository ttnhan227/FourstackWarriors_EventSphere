package fpt.aptech.eventsphere.services;

import fpt.aptech.eventsphere.models.Users;
import fpt.aptech.eventsphere.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        if (!user.isActive()) {
            throw new UsernameNotFoundException("User account is not activated: " + email);
        }

        if (user.isDeleted()) {
            throw new UsernameNotFoundException("User account is deleted: " + email);
        }

        return User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(mapRolesToAuthorities(user))
                .build();
    }

    private Collection<? extends GrantedAuthority> mapRolesToAuthorities(Users user) {
        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRoleName()))
                .collect(Collectors.toList());
    }
}