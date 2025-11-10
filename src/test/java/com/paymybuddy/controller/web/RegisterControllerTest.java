package com.paymybuddy.controller.web;

import com.paymybuddy.model.User;
import com.paymybuddy.service.UserService;
import com.paymybuddy.configuration.SpringSecurityConfig;
import com.paymybuddy.service.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RegisterController.class)
@Import(SpringSecurityConfig.class)
class RegisterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @TestConfiguration
    static class MvcTestConfig {
        @Bean
        UserService userService() {
            return Mockito.mock(UserService.class);
        }
        @Bean
        UserDetailsServiceImpl userDetailsServiceImpl() {
            return Mockito.mock(UserDetailsServiceImpl.class);
        }
    }

    @Test
    @DisplayName("GET /register should show form with empty DTO")
    void getRegister_shouldShowForm() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attribute("user", notNullValue()));
    }

    @Test
    @DisplayName("POST /register should reject when passwords mismatch")
    void postRegister_shouldRejectPasswordMismatch() throws Exception {
        Mockito.when(userService.getUserByEmail("test@example.com")).thenReturn(Optional.empty());

        mockMvc.perform(post("/register")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("username", "john")
                        .param("email", "test@example.com")
                        .param("password", "abc123")
                        .param("confirmPassword", "zzz999"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeHasFieldErrors("user", "confirmPassword"));
    }

    @Test
    @DisplayName("POST /register should reject when email already exists")
    void postRegister_shouldRejectExistingEmail() throws Exception {
        Mockito.when(userService.getUserByEmail("exist@example.com")).thenReturn(Optional.of(new User()));

        mockMvc.perform(post("/register")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("username", "alice")
                        .param("email", "exist@example.com")
                        .param("password", "abc123")
                        .param("confirmPassword", "abc123"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeHasFieldErrors("user", "email"));
    }

    @Test
    @DisplayName("POST /register should create user and redirect to login")
    void postRegister_shouldCreateUser() throws Exception {
        Mockito.when(userService.getUserByEmail("ok@example.com")).thenReturn(Optional.empty());

        mockMvc.perform(post("/register")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("username", "okuser")
                        .param("email", "ok@example.com")
                        .param("password", "abc123")
                        .param("confirmPassword", "abc123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userService).saveUser(captor.capture());
        User saved = captor.getValue();
        // minimal assertions
        assert saved.getEmail().equals("ok@example.com");
        assert saved.getUsername().equals("okuser");
    }
}
