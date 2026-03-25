package com.expensetracker.expense_tracker.controller;

import com.expensetracker.expense_tracker.dto.DashboardResponseDTO;
import com.expensetracker.expense_tracker.dto.FilterType;
import com.expensetracker.expense_tracker.dto.GroupDashboardResponseDTO;
import com.expensetracker.expense_tracker.service.AnalyticsService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/dashboard")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService){
        this.analyticsService = analyticsService;
    }

    @GetMapping
    public DashboardResponseDTO getDashboard(
            Authentication authentication,
            @RequestParam(required = false) FilterType totalfilter,
            @RequestParam(required = false) FilterType dailyFilter,
            @RequestParam(required = false) FilterType tagFilter,
            @RequestParam(required = false) FilterType groupFilter,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate
    ){
        String username = authentication.getName();

        return analyticsService.getDashboard(
                username,
                totalfilter,
                dailyFilter,
                tagFilter,
                groupFilter,
                startDate,
                endDate);
    }

    @GetMapping("/group/{groupId}")
    public GroupDashboardResponseDTO getGroupDashboard(
            @PathVariable Long groupId
    ){
        return analyticsService.getGroupDashboard(groupId);
    }
}