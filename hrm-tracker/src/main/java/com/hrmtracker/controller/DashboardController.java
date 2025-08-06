package com.hrmtracker.controller;

import com.hrmtracker.entity.Role;
import com.hrmtracker.entity.User;
import com.hrmtracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final UserRepository userRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            return "redirect:/login?error";
        }

        model.addAttribute("user", user);

        Role role = user.getRole();
        if (role == null || role.getName() == null) {
            return "redirect:/login?error";
        }

        switch (role.getName()) {
            case "ADMIN":
                return "dashboard-admin";
            case "HR":
                return "dashboard-hr";
            case "EMPLOYEE":
                return "dashboard-employee";
            default:
                return "redirect:/login?error";
        }
    }
}
