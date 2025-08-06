package com.hrmtracker.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/login")
    public String loginPage() {
        return "login"; // resources/templates/login.html
    }

}
