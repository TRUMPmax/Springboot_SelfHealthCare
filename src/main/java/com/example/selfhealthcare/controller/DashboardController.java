package com.example.selfhealthcare.controller;

import com.example.selfhealthcare.dto.DashboardSummaryResponse;
import com.example.selfhealthcare.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public DashboardSummaryResponse getSummary() {
        return dashboardService.getSummary();
    }
}
