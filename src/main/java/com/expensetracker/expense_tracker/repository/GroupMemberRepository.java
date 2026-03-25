package com.expensetracker.expense_tracker.repository;

import com.expensetracker.expense_tracker.entity.Group;
import com.expensetracker.expense_tracker.entity.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    List<GroupMember> findByUserId(Long userId);

    List<GroupMember> findByGroupId(Long groupId);
    
    long countByGroupId(Long groupId);

    Optional<GroupMember> findByUserIdAndGroupId(Long userId, Long groupId);

    @Query("""
        SELECT gm.group
        FROM GroupMember gm
        WHERE gm.user.id = :userId
    """)
    List<Group> findGroupsByUserId(Long userId);
}
