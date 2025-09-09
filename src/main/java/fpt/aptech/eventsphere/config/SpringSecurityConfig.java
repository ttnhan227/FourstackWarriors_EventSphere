package fpt.aptech.eventsphere.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SpringSecurityConfig {
    @Autowired
    private UserDetailsService userDetailsService;

    @Bean
    public static PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService);
    }

    @Bean
    public SecurityFilterChain fillterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(
                (auth) -> auth
                        .requestMatchers("/register/**").permitAll()
                        .requestMatchers("/index").permitAll()
                        .requestMatchers("/").permitAll()
                        .requestMatchers("/users").permitAll()
        )
                .formLogin(
                        (form) -> form
                                .loginPage("/login").defaultSuccessUrl("/users")
                                .loginProcessingUrl("/login").permitAll()
                        )
                .logout(
                        (logout) -> logout
                                .logoutUrl("/logout").logoutSuccessUrl("/logout").permitAll()
                        )
                .build();
    }
}
