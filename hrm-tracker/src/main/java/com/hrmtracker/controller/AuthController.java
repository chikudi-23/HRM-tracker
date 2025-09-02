package com.hrmtracker.controller;

import com.hrmtracker.dto.UserRegistrationDto;
import com.hrmtracker.repository.RoleRepository;
import com.hrmtracker.repository.DepartmentRepository;
import com.hrmtracker.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor   // ✅ Auto-wires all final fields
public class AuthController {

    private final DashboardService dashboardService;
    private final RoleRepository roleRepository;          // ✅ Inject RoleRepository
    private final DepartmentRepository departmentRepository;  // ✅ Inject DepartmentRepository

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserRegistrationDto());

        // ✅ Load roles and departments from DB
        model.addAttribute("roles", roleRepository.findAll());
        model.addAttribute("departments", departmentRepository.findAll());

        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") UserRegistrationDto userDto) {
        dashboardService.registerUser(userDto);
        return "redirect:/login?success";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

}
