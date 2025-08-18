package com.hrmtracker.controller;

import com.hrmtracker.dto.EmployeeDTO;
import com.hrmtracker.dto.HrDto;
import com.hrmtracker.entity.AuditLog;
import com.hrmtracker.entity.Department;
import com.hrmtracker.entity.User;
import com.hrmtracker.repository.AuditLogRepository;
import com.hrmtracker.repository.DepartmentRepository;
import com.hrmtracker.repository.UserRepository;
import com.hrmtracker.service.AuditLogService;
import com.hrmtracker.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final DepartmentRepository departmentRepository;
    private final AuditLogService auditLogService;


    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null || user.getRole() == null || user.getRole().getName() == null) {
            return "redirect:/login?error";
        }

        model.addAttribute("user", user);
        String roleName = user.getRole().getName();

        switch (roleName) {
            case "ADMIN":
                model.addAttribute("dashboardStats", dashboardService.getAdminDashboardStats());
                return "dashboard-admin";
            case "HR":
                model.addAttribute("dashboardStats", dashboardService.getHRDashboardStats());
                return "dashboard-hr";
            case "EMPLOYEE":
                model.addAttribute("dashboardStats", dashboardService.getEmployeeDashboardStats(email));
                return "dashboard-employee";
            default:
                return "redirect:/login?error";
        }
    }

    @GetMapping("/admin/audit-logs")
    public String auditLogs(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null || !"ADMIN".equals(user.getRole().getName())) {
            return "redirect:/access-denied";
        }

        model.addAttribute("user", user);
        List<AuditLog> logs = auditLogRepository.findAll(Sort.by(Sort.Direction.DESC, "timestamp"));
        model.addAttribute("logs", logs);
        return "audit-logs";
    }

    // Return JSON for Employees list
    @ResponseBody
    @GetMapping("/employees")
    public List<EmployeeDTO> getEmployees() {
        return dashboardService.getAllEmployees();
    }

    // Return JSON for HR list
    @ResponseBody
    @GetMapping("/hrs")
    public List<HrDto> getAllHRs() {
        return dashboardService.getAllHRs();
    }

    // Return JSON for departments
    @ResponseBody
    @GetMapping("/api/departments")
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    @ResponseBody
    @PostMapping("/api/departments")
    public Department createDepartment(@RequestBody Department department) {
        Department savedDept = departmentRepository.save(department);
        auditLogService.logAction("CREATE", "Department", null, savedDept.toString());
        return savedDept;
    }

    @ResponseBody
    @PutMapping("/api/departments/{id}")
    public Department updateDepartment(@PathVariable Long id, @RequestBody Department updatedDept) {
        Department oldDept = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found"));

        String oldValue = oldDept.toString();

        oldDept.setName(updatedDept.getName());
        oldDept.setDescription(updatedDept.getDescription());

        Department savedDept = departmentRepository.save(oldDept);

        auditLogService.logAction("UPDATE", "Department", oldValue, savedDept.toString());

        return savedDept;
    }
}
