package com.paymybuddy.controller.web;

import com.paymybuddy.model.User;
import com.paymybuddy.model.dto.UserRegistrationDTO;
import com.paymybuddy.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.util.StringUtils;

@Controller
@RequiredArgsConstructor
public class RegisterController {

    private final UserService userService;

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new UserRegistrationDTO());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") UserRegistrationDTO registrationDTO,
                               BindingResult result) {
        if (StringUtils.hasText(registrationDTO.getPassword())
                && StringUtils.hasText(registrationDTO.getConfirmPassword())
                && !registrationDTO.passwordsMatch()) {
            result.rejectValue("confirmPassword", "password.mismatch", "Passwords do not match");
        }

        if (StringUtils.hasText(registrationDTO.getEmail())
                && userService.getUserByEmail(registrationDTO.getEmail()).isPresent()) {
            result.rejectValue("email", "email.exists", "This email is already in use.");
        }

        if (result.hasErrors()) {
            return "register";
        }

        User user = new User();
        user.setUsername(registrationDTO.getUsername());
        user.setEmail(registrationDTO.getEmail());
        user.setPassword(registrationDTO.getPassword());

        userService.saveUser(user);
        return "redirect:/login?registered";
    }
}
