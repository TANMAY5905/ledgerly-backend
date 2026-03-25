package com.expensetracker.expense_tracker.specification;

import com.expensetracker.expense_tracker.entity.Transaction;
import com.expensetracker.expense_tracker.entity.TransactionTag;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TransactionSpecification {

        public static Specification<Transaction> filterTransactions(
                        Long userid,
                        String keyword,
                        Long groupId,
                        BigDecimal minAmount,
                        BigDecimal maxAmount,
                        List<Long> tagIds,
                        java.time.LocalDate date) {
                return (root, query, cb) -> {
                        List<Predicate> predicates = new ArrayList<>();

                        predicates.add(
                                        cb.equal(root.get("createdBy").get("id"), userid));

                        if (keyword != null && !keyword.trim().isEmpty()) {

                                String pattern = "%" + keyword.toLowerCase() + "%";

                                Predicate titleMatch = cb.like(
                                                cb.lower(root.get("title")),
                                                pattern);

                                Predicate notesMatch = cb.like(cb.lower(root.get("notes")),
                                                pattern);

                                List<Predicate> keywordPredicates = new ArrayList<>();
                                keywordPredicates.add(titleMatch);
                                keywordPredicates.add(notesMatch);

                                try {
                                        BigDecimal amountValue = new BigDecimal(keyword);
                                        Predicate amountMatch = cb.equal(root.get("amount"), amountValue);
                                        keywordPredicates.add(amountMatch);
                                } catch (NumberFormatException ignored) {
                                }

                                predicates.add(
                                                cb.or(keywordPredicates.toArray(new Predicate[0])));
                        }

                        if (groupId != null) {

                                predicates.add(
                                                cb.equal(root.get("group").get("id"), groupId));
                        }

                        if (minAmount != null) {
                                predicates.add(
                                                cb.greaterThanOrEqualTo(
                                                                root.get("amount"),
                                                                minAmount));
                        }

                        if (maxAmount != null) {

                                predicates.add(
                                                cb.lessThanOrEqualTo(
                                                                root.get("amount"),
                                                                maxAmount));
                        }

                        if (tagIds != null && !tagIds.isEmpty()) {
                                // Use a subquery to avoid join issues with pagination
                                jakarta.persistence.criteria.Subquery<Long> subquery = query.subquery(Long.class);
                                jakarta.persistence.criteria.Root<TransactionTag> ttRoot = subquery.from(TransactionTag.class);
                                subquery.select(ttRoot.get("transaction").get("id"))
                                                .where(ttRoot.get("tag").get("id").in(tagIds));
                                predicates.add(root.get("id").in(subquery));
                        }

                        if (date != null) {
                                java.time.LocalDateTime startOfDay = date.atStartOfDay();
                                java.time.LocalDateTime endOfDay = date.atTime(java.time.LocalTime.MAX);
                                predicates.add(cb.between(root.get("createdAt"), startOfDay, endOfDay));
                        }

                        return cb.and(predicates.toArray(new Predicate[0]));
                };
        }
}
