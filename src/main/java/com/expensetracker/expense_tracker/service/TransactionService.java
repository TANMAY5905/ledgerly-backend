package com.expensetracker.expense_tracker.service;

import com.expensetracker.expense_tracker.entity.*;
import com.expensetracker.expense_tracker.repository.*;
import com.expensetracker.expense_tracker.specification.TransactionSpecification;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class TransactionService {

        private final TransactionRepository transactionRepository;
        private final UserRepository userRepository;
        private final GroupRepository groupRepository;
        private final TagRepository tagRepository;
        private final TransactionTagRepository transactionTagRepository;
        private final GroupMemberRepository groupMemberRepository;

        public TransactionService(TransactionRepository transactionRepository,
                                  UserRepository userRepository,
                                  GroupRepository groupRepository,
                                  TagRepository tagRepository,
                                  TransactionTagRepository transactionTagRepository,
                                  GroupMemberRepository groupMemberRepository) {

                this.transactionRepository = transactionRepository;
                this.userRepository = userRepository;
                this.groupRepository = groupRepository;
                this.tagRepository = tagRepository;
                this.transactionTagRepository = transactionTagRepository;
                this.groupMemberRepository = groupMemberRepository; // ✅ ADDED
        }

        public Transaction addTransaction(String username,
                                          Long groupId,
                                          String title,
                                          BigDecimal amount,
                                          String note,
                                          String date,
                                          List<Long> tagIds) {

                User user = userRepository.findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("User not found!"));

                Transaction transaction = new Transaction();

                transaction.setCreatedBy(user);
                transaction.setTitle(title);
                transaction.setAmount(amount);
                transaction.setNotes(note);

                if (date != null && !date.isEmpty()) {
                        try {
                                transaction.setCreatedAt(java.time.LocalDateTime.parse(date, java.time.format.DateTimeFormatter.ISO_DATE_TIME));
                        } catch (Exception e) {
                                try {
                                        transaction.setCreatedAt(java.time.LocalDate.parse(date).atStartOfDay());
                                } catch (Exception ex) {
                                        transaction.setCreatedAt(java.time.LocalDateTime.now());
                                }
                        }
                } else {
                        transaction.setCreatedAt(java.time.LocalDateTime.now());
                }

                if (groupId != null) {
                        Group group = groupRepository.findById(groupId)
                                .orElseThrow(() -> new RuntimeException("Group not found"));

                        transaction.setGroup(group);
                }

                Transaction savedTransaction = transactionRepository.save(transaction);

                if (tagIds != null) {
                        for (Long tagId : tagIds) {
                                Tag tag = tagRepository.findById(tagId)
                                        .orElseThrow(() -> new RuntimeException("Tag not found"));

                                TransactionTag transactionTag = new TransactionTag();
                                transactionTag.setTransaction(savedTransaction);
                                transactionTag.setTag(tag);

                                transactionTagRepository.save(transactionTag);
                        }
                }
                return savedTransaction;
        }

        public Page<Transaction> getUserTransactions(String username,
                                                     String keyword,
                                                     Long groupId,
                                                     BigDecimal minAmount,
                                                     BigDecimal maxAmount,
                                                     List<Long> tagIds,
                                                     String date,
                                                     int page,
                                                     int size,
                                                     String sortBy,
                                                     String sortDir) {

                User user = userRepository.findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("User not found"));

                Sort sort = sortDir.equalsIgnoreCase("asc") ?
                        Sort.by(sortBy).ascending() :
                        Sort.by(sortBy).descending();

                Pageable pageable = PageRequest.of(page, size, sort);

                java.time.LocalDate localDate = null;
                if (date != null && !date.isEmpty()) {
                        try {
                                if (date.contains("T")) {
                                    // Handle ISO format like 2026-03-25T10:00:00Z or without offset
                                    try {
                                        localDate = java.time.OffsetDateTime.parse(date).toLocalDate();
                                    } catch (Exception e) {
                                        localDate = java.time.LocalDateTime.parse(date).toLocalDate();
                                    }
                                } else {
                                    localDate = java.time.LocalDate.parse(date);
                                }
                        } catch (Exception ignored) {}
                }

                Specification<Transaction> spec = TransactionSpecification.filterTransactions(
                        user.getId(),
                        keyword,
                        groupId,
                        minAmount,
                        maxAmount,
                        tagIds,
                        localDate);

                return transactionRepository.findAll(spec, pageable);
        }

        public Transaction updateTransaction(Long id,
                                             String username,
                                             String title,
                                             BigDecimal amount,
                                             String note,
                                             Long groupId,
                                             String date,
                                             List<Long> tagIds) {

                User user = userRepository.findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("User not found"));

                Transaction transaction = transactionRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Transaction not found"));

                if (!transaction.getCreatedBy().getId().equals(user.getId())) {
                        throw new RuntimeException("Unauthorized");
                }

                if (title != null) transaction.setTitle(title);
                if (amount != null) transaction.setAmount(amount);
                if (note != null) transaction.setNotes(note);

                if (date != null && !date.isEmpty()) {
                        try {
                                transaction.setCreatedAt(java.time.LocalDateTime.parse(date, java.time.format.DateTimeFormatter.ISO_DATE_TIME));
                        } catch (Exception e) {
                                try {
                                        transaction.setCreatedAt(java.time.LocalDate.parse(date).atStartOfDay());
                                } catch (Exception ignored) {}
                        }
                }

                if (groupId != null) {
                        Group group = groupRepository.findById(groupId)
                                .orElseThrow(() -> new RuntimeException("Group not found"));
                        transaction.setGroup(group);
                } else {
                        transaction.setGroup(null);
                }

                if (tagIds != null) {
                        transactionTagRepository.deleteByTransactionId(transaction.getId());

                        for (Long tagId : tagIds) {
                                Tag tag = tagRepository.findById(tagId)
                                        .orElseThrow(() -> new RuntimeException("Tag not found"));

                                TransactionTag transactionTag = new TransactionTag();
                                transactionTag.setTransaction(transaction);
                                transactionTag.setTag(tag);

                                transactionTagRepository.save(transactionTag);
                        }
                }

                return transactionRepository.save(transaction);
        }

        public void deleteTransaction(Long id, String username) {

                User user = userRepository.findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("User not found"));

                Transaction transaction = transactionRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Transaction not found"));

                if (!transaction.getCreatedBy().getId().equals(user.getId())) {
                        throw new RuntimeException("Unauthorized");
                }

                transactionTagRepository.deleteByTransactionId(transaction.getId());
                transactionRepository.delete(transaction);
        }

        // BULK UPLOAD
        @Transactional(rollbackFor = Exception.class)
        public void bulkUpload(MultipartFile file, String username) {

                User user = userRepository.findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("User not found"));

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {

                        String line;
                        boolean isFirstLine = true;

                        while ((line = reader.readLine()) != null) {

                                if (isFirstLine) {
                                        isFirstLine = false;
                                        continue;
                                }

                                String[] data = line.split(",");

                                if (data.length < 6) {
                                        throw new RuntimeException("Invalid CSV format at line: " + line);
                                }

                                String title = data[0].trim();
                                BigDecimal amount = new BigDecimal(data[1].trim());
                                String note = data[2].trim();
                                String date = data[3].trim();
                                String groupName = data[4].trim();
                                String tagsRaw = data[5].trim();

                                // ✅ FIXED GROUP LOGIC
                                // ✅ FIXED GROUP LOGIC
                                Group group = null;

                                if (!groupName.isEmpty()) {

                                        group = groupRepository.findAll().stream()
                                                .filter(g -> g.getName().equalsIgnoreCase(groupName))
                                                .findFirst()
                                                .orElseThrow(() ->
                                                        new RuntimeException("Invalid group: " + groupName + " (Upload Failed)")
                                                );

                                        // ✅ CORRECT MEMBERSHIP CHECK
                                        boolean isMember = groupMemberRepository
                                                .findByUserIdAndGroupId(user.getId(), group.getId())
                                                .isPresent();

                                        if (!isMember) {
                                                throw new RuntimeException("You are not a member of group: " + groupName);
                                        }
                                }

                                // 🟢 CREATE TRANSACTION
                                Transaction transaction = new Transaction();
                                transaction.setTitle(title);
                                transaction.setAmount(amount);
                                transaction.setNotes(note);
                                transaction.setCreatedBy(user);

                                // DATE PARSE
                                if (!date.isEmpty()) {
                                        try {
                                                transaction.setCreatedAt(
                                                        java.time.LocalDateTime.parse(date, java.time.format.DateTimeFormatter.ISO_DATE_TIME)
                                                );
                                        } catch (Exception e) {
                                                try {
                                                        transaction.setCreatedAt(
                                                                java.time.LocalDate.parse(date).atStartOfDay()
                                                        );
                                                } catch (Exception ex) {
                                                        transaction.setCreatedAt(java.time.LocalDateTime.now());
                                                }
                                        }
                                }

                                if (group != null) {
                                        transaction.setGroup(group);
                                }

                                Transaction savedTransaction = transactionRepository.save(transaction);

                                // 🟣 TAGS
                                if (!tagsRaw.isEmpty()) {

                                        String[] tags = tagsRaw.split(";");

                                        for (String rawTag : tags) {

                                                String tagName = rawTag.trim();
                                                if (tagName.isEmpty()) continue;

                                                Tag tag = tagRepository
                                                        .findByNameIgnoreCase(tagName)
                                                        .orElseGet(() -> {
                                                                Tag newTag = new Tag();
                                                                newTag.setName(tagName);
                                                                newTag.setCreatedBy(user);
                                                                return tagRepository.save(newTag);
                                                        });

                                                TransactionTag transactionTag = new TransactionTag();
                                                transactionTag.setTransaction(savedTransaction);
                                                transactionTag.setTag(tag);

                                                transactionTagRepository.save(transactionTag);
                                        }
                                }
                        }

                } catch (Exception e) {
                        throw new RuntimeException("Bulk upload failed: " + e.getMessage());
                }
        }

        public void downloadTemplate(HttpServletResponse response) throws IOException {

                response.setContentType("text/csv");
                response.setHeader("Content-Disposition", "attachment; filename=transactions_template.csv");

                PrintWriter writer = response.getWriter();

                writer.println("title,amount,note,date,groupName,tags");
                writer.println("Lunch,200,Office lunch,2026-03-22,Friends,food;daily");

                writer.flush();
        }
}