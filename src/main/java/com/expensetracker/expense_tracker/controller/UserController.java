package com.expensetracker.expense_tracker.controller;

import com.expensetracker.expense_tracker.dto.UserProfileDTO;
import com.expensetracker.expense_tracker.entity.User;
import com.expensetracker.expense_tracker.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @GetMapping("/search")
    public List<User> searchUsers(@RequestParam String query){
        return userService.searchUsers(query);
    }

    @GetMapping("/profile")
    public UserProfileDTO getProfile(Authentication authentication){
        String username = authentication.getName();
        return userService.getUserByUsername(username);
    }
}
