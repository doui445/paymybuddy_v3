package com.paymybuddy.controller.api;

import com.paymybuddy.service.security.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/login")
@RequiredArgsConstructor
public class AuthenticationController {

    public final TokenService tokenService;

    /**
     * Create - Generate a new token
     * @param authentication An object authentication
     * @return A String containing the token object generated
     */
    @PostMapping
    public String getToken(Authentication authentication) {
        return tokenService.generateToken(authentication);
    }
}
