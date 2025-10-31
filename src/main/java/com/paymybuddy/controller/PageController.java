package com.paymybuddy.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping({"/", "/home"})
    public String homePage(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("username", user.getUsername());
        return "home";
    }
}
