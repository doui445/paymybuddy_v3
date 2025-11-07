package com.paymybuddy.controller.web;

import com.paymybuddy.model.Transaction;
import com.paymybuddy.model.User;
import com.paymybuddy.model.dto.TransferRequestDTO;
import com.paymybuddy.service.TransactionService;
import com.paymybuddy.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class TransferController {

    private final UserService userService;
    private final TransactionService transactionService;

    @PostMapping("/transfer")
    public String makeTransfer(@AuthenticationPrincipal UserDetails principal,
                               @Valid @ModelAttribute("transfer") TransferRequestDTO transfer,
                               BindingResult result,
                               RedirectAttributes redirectAttributes) {

        User sender = userService.getUserByEmail(principal.getUsername()).orElse(null);
        if (sender == null) {
            redirectAttributes.addFlashAttribute("error", "Authenticated user not found.");
            return "redirect:/home";
        }

        User receiver = null;
        if (transfer.getConnectionEmail() != null) {
            receiver = userService.getUserByEmail(transfer.getConnectionEmail()).orElse(null);
        }
        if (receiver == null) {
            result.rejectValue("connectionEmail", "receiver.notfound", "Recipient not found");
        } else if (sender.getId().equals(receiver.getId())) {
            result.rejectValue("connectionEmail", "receiver.self", "You cannot send money to yourself");
        }

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.transfer", result);
            redirectAttributes.addFlashAttribute("transfer", transfer);
            return "redirect:/home";
        }

        try {
            Transaction transaction = Transaction.builder()
                    .sender(sender)
                    .receiver(receiver)
                    .amount(transfer.getAmount())
                    .description(transfer.getDescription())
                    .build();
            transactionService.saveTransaction(transaction);
            redirectAttributes.addFlashAttribute("success", "Transfer completed successfully.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/home";
    }
}


