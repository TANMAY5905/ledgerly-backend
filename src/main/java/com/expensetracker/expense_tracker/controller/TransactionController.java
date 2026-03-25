package com.expensetracker.expense_tracker.controller;

import com.expensetracker.expense_tracker.entity.Transaction;
import com.expensetracker.expense_tracker.service.TransactionService;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

        private final TransactionService transactionservice;

        public TransactionController(TransactionService transactionservice) {
                this.transactionservice = transactionservice;
        }

        // add new transactions
        @PostMapping("/add")
        public Transaction addTransaction(
                        Authentication authentication,
                        @RequestParam(required = false) Long groupId,
                        @RequestParam String title,
                        @RequestParam BigDecimal amount,
                        @RequestParam(required = false) String note,
                        @RequestParam(required = false) String date,
                        @RequestParam(required = true) List<Long> tagIds) {

                String username = authentication.getName();

                return transactionservice.addTransaction(
                                username,
                                groupId,
                                title,
                                amount,
                                note,
                                date,
                                tagIds);
        }

        // get user transactions
        @GetMapping("/user")
        public Page<Transaction> getUserTransactions(Authentication authentication,
                        @RequestParam(required = false) String keyword,
                        @RequestParam(required = false) Long groupId,
                        @RequestParam(required = false) BigDecimal minAmount,
                        @RequestParam(required = false) BigDecimal maxAmount,
                        @RequestParam(required = false) List<Long> tagIds,
                        @RequestParam(required = false) String date,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "desc") String sortDir) {

                String username = authentication.getName();

                System.out.println("DEBUG: Fetching transactions for user: " + username);
                System.out.println("DEBUG: Filters - keyword: " + keyword + ", tagIds: " + tagIds + ", date: " + date);

                return transactionservice.getUserTransactions(
                                username,
                                keyword,
                                groupId,
                                minAmount,
                                maxAmount,
                                tagIds,
                                date,
                                page,
                                size,
                                sortBy,
                                sortDir);
        }

        // update a transaction
        @PutMapping("/{id}")
        public Transaction updateTransaction(
                        @PathVariable Long id,
                        Authentication authentication,
                        @RequestParam(required = false) String title,
                        @RequestParam(required = false) BigDecimal amount,
                        @RequestParam(required = false) String note,
                        @RequestParam(required = false) Long groupId,
                        @RequestParam(required = false) String date,
                        @RequestParam(required = false) List<Long> tagIds) {
                String username = authentication.getName();

                return transactionservice.updateTransaction(
                                id,
                                username,
                                title,
                                amount,
                                note,
                                groupId,
                                date,
                                tagIds);
        }

        @DeleteMapping("/{id}")
        public String deleteTransaction(
                        @PathVariable Long id,
                        Authentication authentication) {
                String username = authentication.getName();

                transactionservice.deleteTransaction(
                                id,
                                username);

                return "Transaction delete successfully";
        }

        @GetMapping("/bulk/template")
        public void downloadTemplate(HttpServletResponse response) throws IOException {
                transactionservice.downloadTemplate(response);
        }

        @PostMapping("/bulk-upload")
        public String bulkUpload(
                Authentication authentication,
                @RequestParam("file")MultipartFile file
                ) {

                String username = authentication.getName();

                transactionservice.bulkUpload(file, username);

                return "Bulk upload successfull";
        }
}
