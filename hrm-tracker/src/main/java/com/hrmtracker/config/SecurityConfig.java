package com.hrmtracker.config;

import com.hrmtracker.security.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // <-- Method-level security for @PreAuthorize
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Dev ke liye disable, prod me token use karo
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/register", "/login", "/css/**", "/js/**").permitAll()

                        // Announcements: GET open, write ops restricted
                        .requestMatchers(HttpMethod.GET, "/announcements").permitAll()
                        .requestMatchers(HttpMethod.POST, "/announcements").hasAnyRole("ADMIN", "HR")
                        .requestMatchers(HttpMethod.PUT, "/announcements/**").hasAnyRole("ADMIN", "HR")
                        .requestMatchers(HttpMethod.DELETE, "/announcements/**").hasAnyRole("ADMIN", "HR")

                        // Departments: (optional) restrict write ops to ADMIN/HR
                        .requestMatchers(HttpMethod.GET, "/api/departments").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/departments").hasAnyRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/departments/**").hasAnyRole("ADMIN")

                        // Everything else requires login
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout").permitAll()
                )
                .oauth2Login(oauth -> oauth
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .defaultSuccessUrl("/dashboard", true)
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
