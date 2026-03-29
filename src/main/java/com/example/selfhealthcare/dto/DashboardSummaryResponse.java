package com.example.selfhealthcare.dto;

import com.example.selfhealthcare.domain.RiskLevel;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record DashboardSummaryResponse(
        String displayName,
        boolean profileExists,
        int profileCompletionScore,
        long totalRecords,
        long pendingAlerts,
        long highRiskRecords,
        RiskLevel latestRiskLevel,
        LocalDate lastRecordDate,
        Map<RiskLevel, Long> riskDistribution,
        List<HealthRecordResponse> recentRecords,
        List<HealthAlertResponse> recentAlerts) {
}
