package com.paymybuddy.controller.web;

import com.paymybuddy.model.Transaction;
import com.paymybuddy.model.User;
import com.paymybuddy.service.TransactionService;
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
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PageController.class)
@Import(SpringSecurityConfig.class)
class PageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private TransactionService transactionService;

    @TestConfiguration
    static class MvcTestConfig {
        @Bean
        UserService userService() {
            return Mockito.mock(UserService.class);
        }
        @Bean
        TransactionService transactionService() {
            return Mockito.mock(TransactionService.class);
        }
        @Bean
        UserDetailsServiceImpl userDetailsServiceImpl() {
            return Mockito.mock(UserDetailsServiceImpl.class);
        }
    }

    @Test
    @DisplayName("GET /login should return login view")
    void loginPage_shouldRenderLoginView() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    @WithMockUser(username = "alice@example.com")
    @DisplayName("GET /home should render home with user data when user exists")
    void homePage_shouldRenderHomeWithUserData() throws Exception {
        User user = new User();
        user.setId(1);
        user.setEmail("alice@example.com");
        user.setUsername("alice");

        List<Transaction> txs = Collections.emptyList();

        Mockito.when(userService.getUserByEmail("alice@example.com")).thenReturn(Optional.of(user));
        Mockito.when(transactionService.getTransactionsBySenderId(1)).thenReturn(txs);

        mockMvc.perform(get("/home"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attribute("username", is("alice@example.com")))
                .andExpect(model().attributeExists("connections"))
                .andExpect(model().attributeExists("transactions"))
                .andExpect(model().attributeExists("transfer"));
    }

    @Test
    @WithMockUser(username = "bob@example.com")
    @DisplayName("GET /home should render empty lists when user not found")
    void homePage_shouldRenderEmptyWhenUserNotFound() throws Exception {
        Mockito.when(userService.getUserByEmail("bob@example.com")).thenReturn(Optional.empty());
        Mockito.when(transactionService.getTransactionsBySenderId(Mockito.anyInt())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/home"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attribute("username", is("bob@example.com")))
                .andExpect(model().attribute("connections", empty()))
                .andExpect(model().attribute("transactions", empty()))
                .andExpect(model().attributeExists("transfer"));
    }

    @Test
    @WithMockUser(username = "charlie@example.com")
    @DisplayName("GET /profile should render profile with user in model")
    void profilePage_shouldRenderProfile() throws Exception {
        User user = new User();
        user.setId(7);
        user.setEmail("charlie@example.com");
        user.setUsername("charlie");

        Mockito.when(userService.getUserByEmail("charlie@example.com")).thenReturn(Optional.of(user));

        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("user"));
    }
}
