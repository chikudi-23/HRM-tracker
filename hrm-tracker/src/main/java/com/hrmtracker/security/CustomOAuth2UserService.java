package com.hrmtracker.security;

import com.hrmtracker.entity.AuthProvider;
import com.hrmtracker.entity.Role;
import com.hrmtracker.entity.User;
import com.hrmtracker.repository.RoleRepository;
import com.hrmtracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            Role role = roleRepository.findByName("EMPLOYEE")
                    .orElseThrow(() -> new RuntimeException("Role 'EMPLOYEE' not found"));

            user = new User();
            user.setEmail(email);
            user.setFullName(name);
            user.setAuthProvider(AuthProvider.GOOGLE);
            user.setRole(role);

            userRepository.save(user);
        } else {
            // Update role if null or update auth provider if missing
            boolean changed = false;
            if (user.getRole() == null) {
                Role role = roleRepository.findByName("EMPLOYEE")
                        .orElseThrow(() -> new RuntimeException("Role 'EMPLOYEE' not found"));
                user.setRole(role);
                changed = true;
            }
            if (user.getAuthProvider() == null) {
                user.setAuthProvider(AuthProvider.GOOGLE);
                changed = true;
            }
            if (changed) {
                userRepository.save(user);
            }
        }
        final User finalUser = user;
        return new DefaultOAuth2User(
                Collections.singleton(() -> finalUser.getRole().getName()),
                attributes,
                "email"
        );


    }
}
