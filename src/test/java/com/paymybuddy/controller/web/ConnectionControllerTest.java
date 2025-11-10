package com.paymybuddy.controller.web;

import com.paymybuddy.model.User;
import com.paymybuddy.service.UserService;
import com.paymybuddy.configuration.SpringSecurityConfig;
import com.paymybuddy.service.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ConnectionController.class)
@Import(SpringSecurityConfig.class)
class ConnectionControllerTest {

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
    @WithMockUser(username = "me@example.com")
    @DisplayName("GET /add-relation should render form with empty DTO")
    void getAddRelation_shouldRenderForm() throws Exception {
        mockMvc.perform(get("/add-relation"))
                .andExpect(status().isOk())
                .andExpect(view().name("add-relation"))
                .andExpect(model().attributeExists("addConnection"))
                .andExpect(model().attribute("addConnection", notNullValue()));
    }

    @Test
    @WithMockUser(username = "me@example.com")
    @DisplayName("POST /add-relation should redirect with error when current user not found")
    void postAddRelation_currentUserMissing() throws Exception {
        Mockito.when(userService.getUserByEmail("me@example.com")).thenReturn(Optional.empty());

        mockMvc.perform(post("/add-relation")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("email", "target@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/add-relation"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    @WithMockUser(username = "me@example.com")
    @DisplayName("POST /add-relation should reject when target user not found")
    void postAddRelation_targetMissing() throws Exception {
        User me = new User();
        me.setId(1);
        me.setEmail("me@example.com");

        Mockito.when(userService.getUserByEmail("me@example.com")).thenReturn(Optional.of(me));
        Mockito.when(userService.getUserByEmail("unknown@example.com")).thenReturn(Optional.empty());

        mockMvc.perform(post("/add-relation")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("email", "unknown@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/add-relation"))
                .andExpect(flash().attributeExists("org.springframework.validation.BindingResult.addConnection"))
                .andExpect(flash().attributeExists("addConnection"));
    }

    @Test
    @WithMockUser(username = "me@example.com")
    @DisplayName("POST /add-relation should reject when adding self")
    void postAddRelation_selfAdd() throws Exception {
        User me = new User();
        me.setId(1);
        me.setEmail("me@example.com");

        Mockito.when(userService.getUserByEmail("me@example.com")).thenReturn(Optional.of(me));
        Mockito.when(userService.getUserByEmail("me@example.com")).thenReturn(Optional.of(me));

        mockMvc.perform(post("/add-relation")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("email", "me@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/add-relation"))
                .andExpect(flash().attributeExists("org.springframework.validation.BindingResult.addConnection"))
                .andExpect(flash().attributeExists("addConnection"));
    }

    @Test
    @WithMockUser(username = "me@example.com")
    @DisplayName("POST /add-relation should reject when connection already exists")
    void postAddRelation_alreadyConnected() throws Exception {
        User me = new User();
        me.setId(1);
        me.setEmail("me@example.com");

        User target = new User();
        target.setId(2);
        target.setEmail("friend@example.com");

        me.getConnections().add(target);

        Mockito.when(userService.getUserByEmail("me@example.com")).thenReturn(Optional.of(me));
        Mockito.when(userService.getUserByEmail("friend@example.com")).thenReturn(Optional.of(target));

        mockMvc.perform(post("/add-relation")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("email", "friend@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/add-relation"))
                .andExpect(flash().attributeExists("org.springframework.validation.BindingResult.addConnection"))
                .andExpect(flash().attributeExists("addConnection"));
    }

    @Test
    @WithMockUser(username = "me@example.com")
    @DisplayName("POST /add-relation should connect users and flash success")
    void postAddRelation_success() throws Exception {
        User me = new User();
        me.setId(1);
        me.setEmail("me@example.com");

        User target = new User();
        target.setId(2);
        target.setEmail("friend@example.com");

        Mockito.when(userService.getUserByEmail("me@example.com")).thenReturn(Optional.of(me));
        Mockito.when(userService.getUserByEmail("friend@example.com")).thenReturn(Optional.of(target));

        mockMvc.perform(post("/add-relation")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("email", "friend@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/add-relation"))
                .andExpect(flash().attributeExists("success"));

        verify(userService).connectUsers(eq(1), eq(2));
    }

    @Test
    @WithMockUser(username = "me@example.com")
    @DisplayName("POST /add-relation should flash error when service throws")
    void postAddRelation_serviceThrows() throws Exception {
        User me = new User();
        me.setId(1);
        me.setEmail("me@example.com");

        User target = new User();
        target.setId(2);
        target.setEmail("friend@example.com");

        Mockito.when(userService.getUserByEmail("me@example.com")).thenReturn(Optional.of(me));
        Mockito.when(userService.getUserByEmail("friend@example.com")).thenReturn(Optional.of(target));
        Mockito.doThrow(new RuntimeException("boom")).when(userService).connectUsers(Mockito.anyInt(), Mockito.anyInt());

        mockMvc.perform(post("/add-relation")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("email", "friend@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/add-relation"))
                .andExpect(flash().attributeExists("error"));
    }
}

