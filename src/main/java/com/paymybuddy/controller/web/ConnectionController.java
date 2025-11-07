package com.paymybuddy.controller.web;

import com.paymybuddy.model.User;
import com.paymybuddy.model.dto.AddConnectionDTO;
import com.paymybuddy.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/add-relation")
@RequiredArgsConstructor
public class ConnectionController {

    private final UserService userService;

    @GetMapping
    public String addRelationPage(@AuthenticationPrincipal UserDetails principal, Model model) {
        if (!model.containsAttribute("addConnection")) {
            model.addAttribute("addConnection", new AddConnectionDTO());
        }
        return "add-relation";
    }

    @PostMapping
    public String addConnection(@AuthenticationPrincipal UserDetails principal,
                                @Valid @ModelAttribute("addConnection") AddConnectionDTO addConnection,
                                BindingResult result,
                                RedirectAttributes redirectAttributes) {

        User currentUser = userService.getUserByEmail(principal.getUsername()).orElse(null);
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Authenticated user not found.");
            return "redirect:/add-relation";
        }

        // Check if user exists
        User targetUser = userService.getUserByEmail(addConnection.getEmail()).orElse(null);
        if (targetUser == null) {
            result.rejectValue("email", "user.notfound", "No user found with this email.");
        } else if (currentUser.getId().equals(targetUser.getId())) {
            result.rejectValue("email", "user.self", "You cannot add yourself as a connection.");
        } else if (currentUser.getConnections().contains(targetUser)) {
            result.rejectValue("email", "connection.exists", "This connection already exists.");
        }

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.addConnection", result);
            redirectAttributes.addFlashAttribute("addConnection", addConnection);
            return "redirect:/add-relation";
        }

        // At this point, targetUser cannot be null as we checked above
        if (targetUser != null) {
            try {
                userService.connectUsers(currentUser.getId(), targetUser.getId());
                redirectAttributes.addFlashAttribute("success", "Connection added successfully.");
            } catch (Exception ex) {
                redirectAttributes.addFlashAttribute("error", "Error adding connection: " + ex.getMessage());
            }
        }

        return "redirect:/add-relation";
    }
}

