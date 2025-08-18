package com.hrmtracker.service.impl;

import com.hrmtracker.dto.DashboardStatsDTO;
import com.hrmtracker.dto.EmployeeDTO;
import com.hrmtracker.dto.HrDto;
import com.hrmtracker.dto.UserRegistrationDto;
import com.hrmtracker.entity.AuthProvider;
import com.hrmtracker.entity.Department;
import com.hrmtracker.entity.Role;
import com.hrmtracker.entity.User;
import com.hrmtracker.repository.*;
import com.hrmtracker.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService, UserDetailsService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final AttendanceRepository attendanceRepository;
    private final LeaveRepository leaveRepository;
    private final AnnouncementRepository announcementRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    // ==================== Dashboard Methods ====================

    @Override
    public DashboardStatsDTO getAdminDashboardStats() {
        return DashboardStatsDTO.builder()
                .totalEmployees(userRepository.countByRoleName("EMPLOYEE"))
                .totalDepartments(departmentRepository.count())
                .totalHRs(userRepository.countByRoleName("HR"))
                .pendingLeaves(leaveRepository.countByStatus("PENDING"))
                .approvedLeaves(leaveRepository.countByStatus("APPROVED"))
                .rejectedLeaves(leaveRepository.countByStatus("REJECTED"))
                .totalAttendanceToday(attendanceRepository.countByDate(LocalDate.now()))
                .totalAnnouncements(announcementRepository.count())
                .build();
    }

    @Override
    public DashboardStatsDTO getHRDashboardStats() {
        return DashboardStatsDTO.builder()
                .totalEmployees(userRepository.countByRoleName("EMPLOYEE"))
                .pendingLeaves(leaveRepository.countByStatus("PENDING"))
                .approvedLeaves(leaveRepository.countByStatus("APPROVED"))
                .rejectedLeaves(leaveRepository.countByStatus("REJECTED"))
                .totalAttendanceToday(attendanceRepository.countByDate(LocalDate.now()))
                .totalAnnouncements(announcementRepository.count())
                .build();
    }

    @Override
    public DashboardStatsDTO getEmployeeDashboardStats(String email) {
        return DashboardStatsDTO.builder()
                .totalAttendanceToday(attendanceRepository.countByDate(LocalDate.now()))
                .totalAnnouncements(announcementRepository.count())
                .build();
    }

    // ==================== Department Methods ====================

    @Override
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    // ==================== User Methods ====================

    @Override
    public User registerUser(UserRegistrationDto dto) {
        Department department = null;
        Role role = null;

        if (dto.getDepartmentId() != null) {
            department = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Invalid Department ID: " + dto.getDepartmentId()));
        }
        if (dto.getRoleId() != null) {
            role = roleRepository.findById(dto.getRoleId())
                    .orElseThrow(() -> new RuntimeException("Invalid Role ID: " + dto.getRoleId()));
        }

        User user = User.builder()
                .fullName(dto.getFullName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .phone(dto.getPhone())
                .role(role)
                .department(department)
                .authProvider(AuthProvider.LOCAL)
                .build();

        return userRepository.save(user);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    @Override
    public void updateUserProfile(String email, User updatedData) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFullName(updatedData.getFullName());
        user.setPhone(updatedData.getPhone());

        if (updatedData.getPassword() != null && !updatedData.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(updatedData.getPassword()));
        }

        if (updatedData.getDepartment() != null && updatedData.getDepartment().getId() != null) {
            Department department = departmentRepository.findById(updatedData.getDepartment().getId())
                    .orElseThrow(() -> new RuntimeException("Invalid Department ID: " + updatedData.getDepartment().getId()));
            user.setDepartment(department);
        } else {
            user.setDepartment(null);
        }

        userRepository.save(user);
    }

    @Override
    public void updateUserProfileFields(String email, String fullName, String phone, String password, Long departmentId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFullName(fullName);
        user.setPhone(phone);

        if (password != null && !password.isBlank()) {
            user.setPassword(passwordEncoder.encode(password));
        }

        if (departmentId != null) {
            Department department = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new RuntimeException("Invalid Department ID: " + departmentId));
            user.setDepartment(department);
        } else {
            user.setDepartment(null);
        }

        userRepository.save(user);
    }

    @Override
    public List<EmployeeDTO> getAllEmployees() {
        String employeeRoleName = "EMPLOYEE";
        List<User> users = userRepository.findAllByRoleNameWithDepartment(employeeRoleName);

        return users.stream()
                .map(u -> new EmployeeDTO(
                        u.getFullName(),
                        u.getEmail(),
                        u.getPhone(),
                        u.getDepartment() != null ? u.getDepartment().getName() : "N/A",
                        u.getAuthProvider() != null ? u.getAuthProvider().name() : "N/A",
                        u.getIdProofPath(),
                        u.getResumePath()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<HrDto> getAllHRs() {
        Role hrRole = roleRepository.findByName("HR")
                .orElseThrow(() -> new RuntimeException("Role not found: HR"));

        List<User> hrUsers = userRepository.findByRole(hrRole);

        return hrUsers.stream()
                .map(user -> new HrDto(
                        user.getFullName(),
                        user.getEmail(),
                        user.getPhone(),
                        user.getDepartment() != null ? user.getDepartment().getName() : null,
                        user.getRole() != null ? user.getRole().getName() : null,
                        user.getIdProofPath(),
                        user.getResumePath()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public void updateIdProof(String email, String path) {
        User user = userRepository.findByEmail(email).orElseThrow();
        user.setIdProofPath(path);
        userRepository.save(user);
    }

    @Override
    public void updateResume(String email, String path) {
        User user = userRepository.findByEmail(email).orElseThrow();
        user.setResumePath(path);
        userRepository.save(user);
    }

    // ==================== Security Method (UserDetailsService) ====================

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        if (user.getRole() == null || user.getRole().getName() == null) {
            throw new UsernameNotFoundException("User has no role assigned.");
        }

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName()))
        );
    }
}
