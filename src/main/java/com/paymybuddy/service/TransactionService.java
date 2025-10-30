package com.paymybuddy.service;

import com.paymybuddy.model.Transaction;

import java.util.List;
import java.util.Optional;

public interface TransactionService {

    Iterable<Transaction> getTransactions();

    Optional<Transaction> getTransactionById(Integer id);

    List<Transaction> getTransactionsBySenderId(Integer senderId);

    List<Transaction> getTransactionsByReceiverId(Integer receiverId);

    Transaction saveTransaction(Transaction transaction);

    void deleteTransactionById(Integer id);
}
