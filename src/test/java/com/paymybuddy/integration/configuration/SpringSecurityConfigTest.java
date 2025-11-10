package com.paymybuddy.integration.configuration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) // Setup a web environment
@AutoConfigureMockMvc // Setup mockMVC
class SpringSecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("Access Protected Resource - Authorized")
    @WithMockUser(username = "john") // Simulate authenticated user
    void givenAuthentication_whenAccessProtectedResource_thenAuthorized() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk()); // StatusOK is returned because the user is authenticated
    }

    @Test
    @DisplayName("Access Protected Resource - Unauthorized")
    void givenNoAuthentication_whenAccessProtectedResource_thenUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users")) // Try to get an authenticated endpoint without jwt
                .andExpect(status().isUnauthorized()); // Verify status is unauthorized
    }
}