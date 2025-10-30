package com.paymybuddy.repository;

import com.paymybuddy.model.Transaction;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends CrudRepository<Transaction, Integer> {

    List<Transaction> findBySenderId(Integer senderId);
    List<Transaction> findByReceiverId(Integer receiverId);
}
