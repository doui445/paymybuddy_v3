package com.paymybuddy.controller.web;

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
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransferController.class)
@Import(SpringSecurityConfig.class)
class TransferControllerTest {

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
    @WithMockUser(username = "sender@example.com")
    @DisplayName("POST /transfer should save transaction")
    void postTransfer_success() throws Exception {
        User sender = new User();
        sender.setId(1);
        sender.setEmail("sender@example.com");
        User receiver = new User();
        receiver.setId(2);
        receiver.setEmail("rcv@example.com");

        Mockito.when(userService.getUserByEmail("sender@example.com")).thenReturn(Optional.of(sender));
        Mockito.when(userService.getUserByEmail("rcv@example.com")).thenReturn(Optional.of(receiver));

        mockMvc.perform(post("/transfer")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("connectionEmail", "rcv@example.com")
                        .param("amount", "25.50")
                        .param("description", "Dinner"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));

    }

    @Test
    @WithMockUser(username = "sender@example.com")
    @DisplayName("POST /transfer should redirect with error when sender not found")
    void postTransfer_senderMissing() throws Exception {
        Mockito.when(userService.getUserByEmail("sender@example.com")).thenReturn(Optional.empty());

        mockMvc.perform(post("/transfer")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("connectionEmail", "rcv@example.com")
                        .param("amount", "10.00")
                        .param("description", "Test"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"))
                .andExpect(flash().attributeExists("error"));
    }

    @Test
    @WithMockUser(username = "sender@example.com")
    @DisplayName("POST /transfer should reject when receiver not found")
    void postTransfer_receiverMissing() throws Exception {
        User sender = new User();
        sender.setId(1);
        sender.setEmail("sender@example.com");
        Mockito.when(userService.getUserByEmail("sender@example.com")).thenReturn(Optional.of(sender));
        Mockito.when(userService.getUserByEmail("missing@example.com")).thenReturn(Optional.empty());

        mockMvc.perform(post("/transfer")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("connectionEmail", "missing@example.com")
                        .param("amount", "10.00")
                        .param("description", "Test"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"))
                .andExpect(flash().attributeExists("org.springframework.validation.BindingResult.transfer"))
                .andExpect(flash().attributeExists("transfer"));
    }

    @Test
    @WithMockUser(username = "sender@example.com")
    @DisplayName("POST /transfer should reject when trying to send to self")
    void postTransfer_self() throws Exception {
        User sender = new User();
        sender.setId(1);
        sender.setEmail("sender@example.com");
        Mockito.when(userService.getUserByEmail("sender@example.com")).thenReturn(Optional.of(sender));
        Mockito.when(userService.getUserByEmail("sender@example.com")).thenReturn(Optional.of(sender));

        mockMvc.perform(post("/transfer")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("connectionEmail", "sender@example.com")
                        .param("amount", "10.00")
                        .param("description", "Test"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"))
                .andExpect(flash().attributeExists("org.springframework.validation.BindingResult.transfer"))
                .andExpect(flash().attributeExists("transfer"));
    }

    @Test
    @WithMockUser(username = "sender@example.com")
    @DisplayName("POST /transfer should flash error when service throws")
    void postTransfer_serviceThrows() throws Exception {
        User sender = new User();
        sender.setId(1);
        sender.setEmail("sender@example.com");
        User receiver = new User();
        receiver.setId(2);
        receiver.setEmail("rcv@example.com");

        Mockito.when(userService.getUserByEmail("sender@example.com")).thenReturn(Optional.of(sender));
        Mockito.when(userService.getUserByEmail("rcv@example.com")).thenReturn(Optional.of(receiver));
        Mockito.doThrow(new IllegalArgumentException("Insufficient funds")).when(transactionService).saveTransaction(any());

        mockMvc.perform(post("/transfer")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("connectionEmail", "rcv@example.com")
                        .param("amount", "5.00")
                        .param("description", "Test"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"))
                .andExpect(flash().attributeExists("error"));
    }
}

