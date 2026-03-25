package com.expensetracker.expense_tracker.service;

import com.expensetracker.expense_tracker.dto.UserExpenseSummaryDTO;
import com.expensetracker.expense_tracker.entity.*;
import com.expensetracker.expense_tracker.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final NotificationRepository notificationRepository;

    public GroupService(GroupRepository groupRepository,
            GroupMemberRepository groupMemberRepository,
            UserRepository userRepository,
            TransactionRepository transactionRepository,
            NotificationRepository notificationRepository
    ) {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.notificationRepository = notificationRepository;
    }

    private void createNotification(User user, String message){
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message);

        notificationRepository.save(notification);
    }

    public Group creategroup(String groupName, String username) {

        User creator = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found!"));

        Group group = new Group();
        group.setName(groupName);
        group.setCreatedBy(creator);

        Group savedGroup = groupRepository.save(group);

        GroupMember member = new GroupMember();
        member.setUser(creator);
        member.setGroup(savedGroup);
        member.setRole(GroupRole.ADMIN);

        groupMemberRepository.save(member);

        savedGroup.setMemberCount(1); // One member initially (creator)

        return savedGroup;
    }

    public GroupMember addMember(Long groupId, Long userId, String username) {

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        User loggedInUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("user not found"));

        GroupMember adminCheck = groupMemberRepository
                .findByUserIdAndGroupId(loggedInUser.getId(), groupId)
                .orElseThrow(() -> new RuntimeException("Not a member"));

        if (adminCheck.getRole() != GroupRole.ADMIN) {
            throw new RuntimeException("Only ADMIN can add members");
        }

        User userToAdd = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        GroupMember member = new GroupMember();
        member.setGroup(group);
        member.setUser(userToAdd);
        member.setRole(GroupRole.MEMBER);

        GroupMember savedMember = groupMemberRepository.save(member);

        createNotification(userToAdd,
                "You have been added to group: " + group.getName());

        // Update the group's member count for the response
        group.setMemberCount(groupMemberRepository.countByGroupId(groupId));

        return savedMember;
    }

    public List<GroupMember> getGroupMembers(Long groupId) {
        List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);
        long count = members.size();
        members.forEach(m -> m.getGroup().setMemberCount(count));
        return members;
    }

    public List<GroupMember> getUserMemberships(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<GroupMember> memberships = groupMemberRepository.findByUserId(user.getId());

        // Populate member count for each group
        for (GroupMember membership : memberships) {
            long count = groupMemberRepository.countByGroupId(membership.getGroup().getId());
            membership.getGroup().setMemberCount(count);
        }

        return memberships;
    }

    public List<Transaction> getGroupTransactions(Long groupId){

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        return transactionRepository.findByGroupId(groupId);
    }

    public long getGroupMemeberCount(Long groupId){

        groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        return groupMemberRepository.countByGroupId(groupId);
    }

    public UserExpenseSummaryDTO getUserExpenseSummary(Long groupId, String username){

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        groupMemberRepository.findByUserIdAndGroupId(user.getId(), groupId)
                .orElseThrow(() -> new RuntimeException("User is not a part of this group"));

        List<Transaction> transactions = transactionRepository.findByGroupId(groupId);

        BigDecimal totalExpense = transactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long memberCount = groupMemberRepository.countByGroupId(groupId);

        if(memberCount == 0){
            throw new RuntimeException("No members in group");
        }

        BigDecimal yourShare = transactions.stream()
                .filter(t -> t.getCreatedBy().getId().equals(user.getId()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new UserExpenseSummaryDTO(totalExpense, yourShare);
    }

    public String removeMember(Long groupId, Long userId, String username){

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        User loggedInUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        GroupMember adminCheck = groupMemberRepository
                .findByUserIdAndGroupId(loggedInUser.getId(), groupId)
                .orElseThrow(() -> new RuntimeException("Not a member"));

        if(adminCheck.getRole() != GroupRole.ADMIN){
            throw new RuntimeException("Only ADMIN can remove members");
        }

        GroupMember member = groupMemberRepository
                .findByUserIdAndGroupId(userId, groupId)
                .orElseThrow(() -> new RuntimeException("User not found in group"));

        if(member.getUser().getId().equals(loggedInUser.getId())){
            throw new RuntimeException("Admin cannot remove themselves");
        }

        createNotification(member.getUser(),
                "You have been removed from group: "+ group.getName());

        groupMemberRepository.delete(member);

        return "Member removed Successfully";
    }

    public String deleteGroup(Long groupId, String username) {

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        GroupMember membership = groupMemberRepository
                .findByUserIdAndGroupId(user.getId(), groupId)
                .orElseThrow(() -> new RuntimeException("Not a member"));

        if (membership.getRole() != GroupRole.ADMIN) {
            throw new RuntimeException("Only ADMIN can delete group");
        }

        // Get members for notification
        List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);

        sendGroupDeletedNotification(group, members);

        // 🔥 FIX STARTS HERE
        List<Transaction> transactions = transactionRepository.findByGroupId(groupId);
        transactionRepository.deleteAll(transactions);
        // 🔥 FIX ENDS HERE

        groupMemberRepository.deleteAll(members);

        groupRepository.delete(group);

        return "Group deleted successfully";
    }

    private void sendGroupDeletedNotification(Group group, List<GroupMember> members) {

        for (GroupMember member : members) {

            createNotification(
                    member.getUser(),
                    "Group '" + group.getName() + "' has been deleted"
            );
        }
    }

    public String leaveGroup(Long groupId, String username){

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        GroupMember membership = groupMemberRepository
                .findByUserIdAndGroupId(user.getId(), groupId)
                .orElseThrow(() -> new RuntimeException("You are not a part of this group"));

        if(membership.getRole() == GroupRole.ADMIN){

            long adminCount = groupMemberRepository.findByGroupId(groupId).stream()
                    .filter(m -> m.getRole() == GroupRole.ADMIN)
                    .count();

            if (adminCount <= 1){
                throw new RuntimeException("You are the last admin, assign another admin before leaving");
            }
        }

        groupMemberRepository.delete(membership);

        return "You have left the group successfully";
    }
}
