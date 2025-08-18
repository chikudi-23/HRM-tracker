package com.hrmtracker.security;

import com.hrmtracker.entity.AuthProvider;
import com.hrmtracker.entity.Role;
import com.hrmtracker.entity.User;
import com.hrmtracker.repository.UserRepository;
import com.hrmtracker.repository.RoleRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        if (email == null) {
            throw new RuntimeException("OAuth2 provider did not return an email.");
        }

        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setFullName(name);
            newUser.setPassword(""); // No password for OAuth users
            newUser.setAuthProvider(AuthProvider.GOOGLE);

            Role defaultRole = roleRepository.findByName("EMPLOYEE")
                    .orElseThrow(() -> new RuntimeException("Default role 'EMPLOYEE' not found"));

            newUser.setRole(defaultRole);
            userRepository.save(newUser);
        } else {
            // Optionally, update role if missing or authProvider not GOOGLE
            User existingUser = optionalUser.get();
            if (existingUser.getRole() == null) {
                Role defaultRole = roleRepository.findByName("EMPLOYEE")
                        .orElseThrow(() -> new RuntimeException("Default role 'EMPLOYEE' not found"));
                existingUser.setRole(defaultRole);
                userRepository.save(existingUser);
            }
            if (existingUser.getAuthProvider() == null) {
                existingUser.setAuthProvider(AuthProvider.GOOGLE);
                userRepository.save(existingUser);
            }
        }

        response.sendRedirect("/dashboard");
    }
}
