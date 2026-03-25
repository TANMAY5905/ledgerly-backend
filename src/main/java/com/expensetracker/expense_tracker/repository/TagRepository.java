package com.expensetracker.expense_tracker.repository;

import com.expensetracker.expense_tracker.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByName(String name);

    Optional<Tag> findByNameAndCreatedById(String name, Long userId);
    List<Tag> findByCreatedById(Long userId);

    Optional<Tag> findByNameIgnoreCase(String name);
}
