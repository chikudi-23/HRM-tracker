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

        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isEmpty()) {
            // Create new user
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setFullName(name);
            newUser.setPassword(""); // No password for OAuth2 users
            newUser.setAuthProvider(AuthProvider.valueOf("GOOGLE")); // ✅ Correct


            // Set default role as EMPLOYEE
            Role defaultRole = roleRepository.findByName("EMPLOYEE")
                    .orElseThrow(() -> new RuntimeException("Default role not found"));

            newUser.setRole(defaultRole);

            userRepository.save(newUser);
        }

        // Redirect to dashboard after login
        response.sendRedirect("/dashboard");
    }
}
