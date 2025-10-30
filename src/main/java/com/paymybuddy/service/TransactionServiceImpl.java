package com.paymybuddy.service;

import com.paymybuddy.model.Transaction;
import com.paymybuddy.model.User;
import com.paymybuddy.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

    @Override
    public Iterable<Transaction> getTransactions() {
        return transactionRepository.findAll();
    }

    @Override
    public Optional<Transaction> getTransactionById(Integer id) {
        return transactionRepository.findById(id);
    }

    @Override
    public List<Transaction> getTransactionsBySenderId(Integer userId) {
        return transactionRepository.findBySenderId(userId);
    }

    @Override
    public List<Transaction> getTransactionsByReceiverId(Integer receiverId) {
        return transactionRepository.findByReceiverId(receiverId);
    }

    @Override
    public Transaction saveTransaction(Transaction transaction) {
        User sender = transaction.getSender();
        User receiver = transaction.getReceiver();

        if (!sender.getConnections().contains(receiver)) {
            throw new IllegalArgumentException("You can only send money to your connections.");
        }

        return transactionRepository.save(transaction);
    }

    @Override
    public void deleteTransactionById(Integer id) {
        transactionRepository.deleteById(id);
    }
}
