package com.hrmtracker.service.impl;

import com.hrmtracker.dto.UserRegistrationDto;
import com.hrmtracker.entity.AuthProvider;
import com.hrmtracker.entity.Department;
import com.hrmtracker.entity.User;
import com.hrmtracker.repository.DepartmentRepository;
import com.hrmtracker.repository.UserRepository;
import com.hrmtracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User registerUser(UserRegistrationDto dto) {
        Department department = null;

        // ✅ If department ID is provided, validate it
        if (dto.getDepartmentId() != null) {
            department = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Invalid Department ID: " + dto.getDepartmentId()));
        }

        // ✅ Build the User entity with or without department
        User user = User.builder()
                .fullName(dto.getFullName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .phone(dto.getPhone())
                .role(dto.getRole())
                .department(department) // can be null
                .active(true)
                .authProvider(AuthProvider.LOCAL)
                .build();

        return userRepository.save(user);
    }
}
