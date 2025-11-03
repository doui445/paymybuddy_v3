package com.paymybuddy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymybuddy.controller.api.TransactionController;
import com.paymybuddy.model.Transaction;
import com.paymybuddy.model.User;
import com.paymybuddy.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class TransactionControllerTest {

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionController transactionController;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper; // For serialize/deserialize JSON

    private Transaction transaction;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(transactionController).build();
        objectMapper = new ObjectMapper();

        User user1 = User.builder()
                .id(1)
                .username("steveLander")
                .build();
        User user2 = User.builder()
                .id(2)
                .username("doui445")
                .build();

        transaction = Transaction.builder()
                .id(1)
                .sender(user1)
                .receiver(user2)
                .description("Payment")
                .amount(BigDecimal.valueOf(25.5))
                .build();
    }

    @Test
    @DisplayName("Create Transaction - Success")
    void givenTransaction_whenCreateTransaction_thenReturnCreatedTransaction() throws Exception {
        given(transactionService.saveTransaction(any(Transaction.class))).willReturn(transaction);

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transaction)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(25.5))
                .andExpect(jsonPath("$.description").value("Payment"));
    }

    @Test
    @DisplayName("Get All Transactions - Success")
    void givenTransactions_whenGetTransactions_thenReturnTransactionList() {
        given(transactionService.getTransactions()).willReturn(List.of(transaction));

        ResponseEntity<Iterable<Transaction>> result = transactionController.getTransactions();

        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody()).isEqualTo(List.of(transaction));
    }

    @Test
    @DisplayName("Get Transaction By ID - Success")
    void givenTransactionId_whenGetTransactionById_thenReturnTransaction() throws Exception {
        given(transactionService.getTransactionById(anyInt())).willReturn(Optional.of(transaction));

        mockMvc.perform(get("/api/transactions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(25.5))
                .andExpect(jsonPath("$.description").value("Payment"));
    }

    @Test
    @DisplayName("Get Transaction By ID - Not Found")
    void givenNonExistingTransactionId_whenGetTransactionById_thenReturnNotFound() throws Exception {
        given(transactionService.getTransactionById(anyInt())).willReturn(Optional.empty());

        mockMvc.perform(get("/api/transactions/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Get Transactions By Sender ID - Success")
    void givenSenderId_whenGetTransactionsBySenderId_thenReturnTransactionList() throws Exception {
        given(transactionService.getTransactionsBySenderId(1)).willReturn(Collections.singletonList(transaction));

        mockMvc.perform(get("/api/transactions/sender/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].amount").value(25.5));
    }

    @Test
    @DisplayName("Get Transactions By Sender ID - Not Found")
    void testGetTransactionsBySenderIdEmpty() throws Exception {
        given(transactionService.getTransactionsBySenderId(1)).willReturn(List.of());

        mockMvc.perform(get("/api/transactions/sender/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Update Transaction - Success")
    void givenUpdatedTransaction_whenUpdateTransaction_thenReturnUpdatedTransaction() throws Exception {
        Transaction transaction1 = transaction;
        transaction1.setAmount(BigDecimal.valueOf(100.0));

        Transaction updateDetails = transaction = Transaction.builder()
                .amount(BigDecimal.valueOf(100.0))
                .build();

        given(transactionService.getTransactionById(1)).willReturn(Optional.of(transaction));
        given(transactionService.saveTransaction(any(Transaction.class))).willReturn(transaction1);

        mockMvc.perform(put("/api/transactions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(100.0));
    }

    @Test
    @DisplayName("Update Transaction - Not Found")
    void givenNonExistingTransaction_whenUpdateTransaction_thenReturnNotFound() throws Exception {
        Transaction updateDetails = transaction = Transaction.builder()
                .amount(BigDecimal.valueOf(100.0))
                .build();

        given(transactionService.getTransactionById(anyInt())).willReturn(Optional.empty());

        mockMvc.perform(put("/api/transactions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDetails)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Delete Transaction - Success")
    void givenTransactionId_whenDeleteTransaction_thenReturnOk() throws Exception {
        willDoNothing().given(transactionService).deleteTransactionById(1);

        mockMvc.perform(delete("/api/transactions/1"))
                .andExpect(status().isNoContent());

        verify(transactionService, times(1)).deleteTransactionById(1);
    }
}
