package com.hrmtracker.controller;

import com.hrmtracker.dto.UserRegistrationDto;
import com.hrmtracker.entity.User;
import com.hrmtracker.entity.Role;
import com.hrmtracker.repository.RoleRepository;
import com.hrmtracker.repository.UserRepository;
import com.hrmtracker.repository.DepartmentRepository;

import com.hrmtracker.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class AuthController {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;



    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserRegistrationDto());

        List<Role> roles = roleRepository.findAll();
        model.addAttribute("roles", roles);

        model.addAttribute("departments", departmentRepository.findAll());

        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute UserRegistrationDto userDto) {
        userService.registerUser(userDto);
        return "redirect:/login?success";
    }

  //  @PostMapping("/register")
   // public String processRegistration(@ModelAttribute User user) {
     //   userRepository.save(user);
       // return "redirect:/login?success";
    //}
}
