package com.expensetracker.expense_tracker.controller;

import com.expensetracker.expense_tracker.dto.LoginRequest;
import com.expensetracker.expense_tracker.dto.RegisterRequest;
import com.expensetracker.expense_tracker.dto.UserProfileDTO;
import com.expensetracker.expense_tracker.entity.User;
import com.expensetracker.expense_tracker.security.JwtService;
import com.expensetracker.expense_tracker.service.AuthService;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {


    private final JwtService jwtService;
    private final AuthService authService;

    public AuthController( AuthService authService,
            JwtService jwtService) {

        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public User register(@RequestBody RegisterRequest request) {

        return authService.register(request);
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest request) {

        User user = authService.login(request);
        String token = jwtService.generateToken(user.getUsername());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", new UserProfileDTO(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getEmail()
        ));

        return response;
    }

    @GetMapping("/me")
    public UserProfileDTO getMe(Principal principal) {
        if (principal == null) {
            throw new RuntimeException("Not authenticated");
        }
        return authService.getUserProfile(principal.getName());
    }

    @PostMapping("/reset-password")
    public Map<String, String> resetPassword(@RequestParam String token,
                                @RequestParam String newPassword){
        String message = authService.resetPassword(token, newPassword);
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        return response;
    }

    @PostMapping("/forgot-password")
    public Map<String, String> forgotPassword(@RequestParam String email){
        String link = authService.sendResetToken(email);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Reset link sent to your registered email");
        // For development/debugging purposes, we could put the link here if email isn't actually sent
        response.put("debug_link", link); 
        return response;
    }
}
