package com.expensetracker.expense_tracker.controller;

import com.expensetracker.expense_tracker.dto.UserExpenseSummaryDTO;
import com.expensetracker.expense_tracker.entity.*;
import com.expensetracker.expense_tracker.repository.NotificationRepository;
import com.expensetracker.expense_tracker.repository.UserRepository;
import com.expensetracker.expense_tracker.service.GroupService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/groups")
public class GroupController {

    private final GroupService groupService;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    public GroupController(GroupService groupService,
            UserRepository userRepository,
                           NotificationRepository notificationRepository) {
        this.groupService = groupService;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
    }

    @PostMapping("/create")
    public Group createGroup(@RequestParam String name,
            Authentication authentication) {

        String username = authentication.getName();
        return groupService.creategroup(name, username);
    }

    @PostMapping("/add-member")
    public GroupMember addMember(@RequestParam Long groupId,
                                 @RequestParam Long userId,
                                 Authentication authentication) {

        String username = authentication.getName();
        return groupService.addMember(groupId, userId, username);
    }

    @GetMapping("/{groupId}/members")
    public List<GroupMember> getMembers(@PathVariable Long groupId) {
        return groupService.getGroupMembers(groupId);
    }

//    get user groups
    @GetMapping("/user")
    public List<GroupMember> getUserGroups(Authentication authentication) {

        String username = authentication.getName();

        return groupService.getUserMemberships(username);
    }

    @GetMapping("/{groupId}/transactions")
    public List<Transaction> getGroupTransactions(@PathVariable Long groupId){
        return groupService.getGroupTransactions(groupId);
    }

    @GetMapping("/{groupId}/member-count")
    public long getMemberCount(@PathVariable Long groupId){
        return groupService.getGroupMemeberCount(groupId);
    }

    @GetMapping("/{groupId}/my-share")
    public UserExpenseSummaryDTO getMyShare(
            @PathVariable Long groupId,
            Authentication authentication
    ) {
        String username = authentication.getName();

        return groupService.getUserExpenseSummary(groupId, username);
    }

    @DeleteMapping("/remove-member")
    public String removeMember(
            @RequestParam Long groupId,
            @RequestParam Long userId,
            Authentication authentication
    ){
        String username = authentication.getName();

        return groupService.removeMember(groupId, userId, username);
    }

    @DeleteMapping("/delete")
    public String deleteGroup(
            @RequestParam Long groupId,
            Authentication authentication
    ){

        String username = authentication.getName();

        return groupService.deleteGroup(groupId, username);
    }

    @DeleteMapping("/leave")
    public String leaveGroup(
            @RequestParam Long groupId,
            Authentication authentication
    ){

        String username = authentication.getName();
        return groupService.leaveGroup(groupId, username);
    }

    @GetMapping("/notifications")
    public List<Notification> getNotifications(Authentication authentication){
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return notificationRepository.findByUserId(user.getId());
    }

    @PutMapping("/notifications/{id}/read")
    public String markAsRead(@PathVariable Long id) {

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        notification.setRead(true);
        notificationRepository.save(notification);

        return "Marked as read";
    }
}
