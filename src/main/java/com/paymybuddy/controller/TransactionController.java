package com.paymybuddy.controller;

import com.paymybuddy.model.Transaction;
import com.paymybuddy.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * Create - Add a new transaction
     * @param transaction An object transaction
     * @return A ResponseEntity containing the transaction object saved
     */
    @PostMapping
    public ResponseEntity<Transaction> createTransaction(@RequestBody Transaction transaction) {
        Transaction savedTransaction = transactionService.saveTransaction(transaction);
        URI location = URI.create("/api/transactions/" + savedTransaction.getId());
        return ResponseEntity.created(location).body(savedTransaction);
    }

    /**
     * Read - Get all transactions
     * @return - A ResponseEntity containing an Iterable object of Transaction fulfilled
     */
    @GetMapping
    public ResponseEntity<Iterable<Transaction>> getTransactions() {
        return ResponseEntity.ok(transactionService.getTransactions());
    }

    /**
     * Read - Get one transaction
     * @param id The id of the transaction
     * @return A ResponseEntity containing a Transaction object fulfilled
     */
    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable Integer id) {
        return transactionService.getTransactionById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Read - Get all sent transactions of a user
     * @param senderId The id of the transactions sender
     * @return A ResponseEntity containing a List of Transaction objects fulfilled
     */
    @GetMapping("/sender/{senderId}")
    public ResponseEntity<List<Transaction>> getTransactionsBySenderId(@PathVariable Integer senderId) {
        List<Transaction> transactions = transactionService.getTransactionsBySenderId(senderId);
        if (transactions.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(transactions);
    }

    /**
     * Read - Get all received transactions of a user
     * @param receiverId The id of the transactions receiver
     * @return A ResponseEntity containing a List of Transaction objects fulfilled
     */
    @GetMapping("/receiver/{receiverId}")
    public ResponseEntity<List<Transaction>> getTransactionsByReceiverId(@PathVariable Integer receiverId) {
        List<Transaction> transactions = transactionService.getTransactionsByReceiverId(receiverId);
        if (transactions.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(transactions);
    }

    /**
     * Update - Update an existing transaction
     * @param id - The id of the transaction to update
     * @param transactionDetails - The transaction object updated
     * @return A ResponseEntity containing the transaction object updated
     */
    @PutMapping("/{id}")
    public ResponseEntity<Transaction> updateTransaction(
            @PathVariable Integer id,
            @RequestBody Transaction transactionDetails) {

        return transactionService.getTransactionById(id)
                .map(existingTransaction -> {
                    existingTransaction.setAmount(transactionDetails.getAmount());
                    existingTransaction.setDescription(transactionDetails.getDescription());
                    existingTransaction.setSender(transactionDetails.getSender());
                    existingTransaction.setReceiver(transactionDetails.getReceiver());

                    Transaction updated = transactionService.saveTransaction(existingTransaction);
                    return ResponseEntity.ok(updated);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Delete - Delete a transaction
     * @param id - The id of the transaction to delete
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Integer id) {
        transactionService.deleteTransactionById(id);
        return ResponseEntity.noContent().build();
    }
}
