package com.expensetracker.expense_tracker.repository;

import com.expensetracker.expense_tracker.entity.Group;
import com.expensetracker.expense_tracker.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long> {
    Optional<Group> findByName(String name);
}
