package com.hrmtracker.service;

import com.hrmtracker.dto.DashboardStatsDTO;
import com.hrmtracker.dto.EmployeeDTO;
import com.hrmtracker.dto.HrDto;
import com.hrmtracker.dto.UserRegistrationDto;
import com.hrmtracker.entity.Department;
import com.hrmtracker.entity.User;

import java.util.List;

public interface DashboardService {

    // Dashboard-related
    DashboardStatsDTO getAdminDashboardStats();
    DashboardStatsDTO getHRDashboardStats();
    DashboardStatsDTO getEmployeeDashboardStats(String employeeEmail);

    // Department-related
    List<Department> getAllDepartments();

    // User-related
    User registerUser(UserRegistrationDto dto);
    User findByEmail(String email);
    void updateUserProfile(String email, User updatedData);
    List<EmployeeDTO> getAllEmployees();
    void updateUserProfileFields(String email, String fullName, String phone, String password, Long departmentId);
    List<HrDto> getAllHRs();
    void updateIdProof(String email, String path);
    void updateResume(String email, String path);
}
