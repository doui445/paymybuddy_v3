package com.paymybuddy.controller.web;

import com.paymybuddy.model.User;
import com.paymybuddy.service.TransactionService;
import com.paymybuddy.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class PageController {

    private final UserService userService;
    private final TransactionService transactionService;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping({"/", "/home"})
    public String homePage(@AuthenticationPrincipal UserDetails principal, Model model) {
        model.addAttribute("username", principal.getUsername());

        User user = userService.getUserByEmail(principal.getUsername()).orElse(null);
        if (user != null) {
            model.addAttribute("connections", user.getConnections());
            model.addAttribute("transactions", transactionService.getTransactionsBySenderId(user.getId()));
        } else {
            model.addAttribute("connections", java.util.Collections.emptyList());
            model.addAttribute("transactions", java.util.Collections.emptyList());
        }
        // Prepare an empty transfer object so field values can be preserved on redirect errors
        if (!model.containsAttribute("transfer")) {
            model.addAttribute("transfer", new com.paymybuddy.model.dto.TransferRequestDTO());
        }
        return "home";
    }
}
