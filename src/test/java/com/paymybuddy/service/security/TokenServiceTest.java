package com.paymybuddy.service.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class TokenServiceTest {

    @Mock
    private JwtEncoder jwtEncoder;

    @InjectMocks
    private TokenServiceImpl tokenService;

    private AutoCloseable mocks;

    private Authentication authentication;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);

        authentication = mock(Authentication.class);
    }

    @AfterEach
    public void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    @DisplayName("Generate Token - Success")
    void givenAuthentication_whenGenerateToken_thenReturnJwtToken() {
        Map<String, Object> headers = new HashMap<>();
        headers.put("typ", "JWT"); // Example header

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "test_user"); // Subject (username)

        Instant now = Instant.now();

        given(authentication.getName()).willReturn("test_user");
        given(jwtEncoder.encode(any())).willReturn(new Jwt("token", now, now.plusSeconds(3600), headers, claims));

        String token = tokenService.generateToken(authentication);

        assertThat(token).isNotBlank();
    }
}