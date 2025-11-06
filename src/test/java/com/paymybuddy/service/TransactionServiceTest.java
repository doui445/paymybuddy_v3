package com.paymybuddy.service;

import com.paymybuddy.model.Transaction;
import com.paymybuddy.model.User;
import com.paymybuddy.repository.TransactionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

public class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private AutoCloseable mocks;

    private Transaction transaction;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);

        User sender = User.builder().id(1).username("alice").build();
        User receiver = User.builder().id(2).username("bob").build();

        sender.addConnection(receiver);

        transaction = Transaction.builder()
                .id(1)
                .amount(new BigDecimal("100.00"))
                .description("Payment to friend")
                .sender(sender)
                .receiver(receiver)
                .build();
    }

    @AfterEach
    public void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    @DisplayName("getTransactionById should return transaction when exists")
    void testGetTransactionByIdFound() {
        given(transactionRepository.findById(1)).willReturn(Optional.of(transaction));

        Optional<Transaction> result = transactionService.getTransactionById(1);

        assertThat(result).isPresent();
        assertThat(result.get().getAmount()).isEqualByComparingTo("100.00");
        verify(transactionRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("getTransactionById should return empty when transaction does not exist")
    void testGetTransactionByIdNotFound() {
        given(transactionRepository.findById(2)).willReturn(Optional.empty());

        Optional<Transaction> result = transactionService.getTransactionById(2);

        assertThat(result).isEmpty();
        verify(transactionRepository, times(1)).findById(2);
    }

    @Test
    @DisplayName("getTransactionsBySenderId should return list of transactions")
    void testGetTransactionsBySenderId() {
        List<Transaction> transactions = Collections.singletonList(transaction);

        given(transactionRepository.findBySenderId(1)).willReturn(transactions);

        List<Transaction> result = transactionService.getTransactionsBySenderId(1);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getDescription()).isEqualTo("Payment to friend");
        verify(transactionRepository, times(1)).findBySenderId(1);
    }

    @Test
    @DisplayName("saveTransaction should save and return transaction")
    void testSaveTransaction() {
        given(transactionRepository.save(transaction)).willReturn(transaction);

        Transaction saved = transactionService.saveTransaction(transaction);

        assertThat(saved).isNotNull();
        assertThat(saved.getDescription()).isEqualTo("Payment to friend");
        verify(transactionRepository, times(1)).save(transaction);
    }

    @Test
    @DisplayName("deleteTransactionById should delete transaction by id")
    void testDeleteTransactionById() {
        transactionService.deleteTransactionById(1);

        verify(transactionRepository, times(1)).deleteById(1);
    }
}
