package com.paymybuddy.controller.api;

import com.paymybuddy.service.security.JWTService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/login")
@RequiredArgsConstructor
public class LoginController {
    public final JWTService jwtService;

    @PostMapping
    public String getToken(Authentication authentication) {
        return jwtService.generateToken(authentication);
    }
}
