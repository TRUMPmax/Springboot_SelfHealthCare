package com.example.selfhealthcare.dto;

import com.example.selfhealthcare.domain.RiskLevel;
import java.util.List;
import java.util.Map;

public record DashboardSummaryResponse(
        long totalProfiles,
        long totalRecords,
        long pendingAlerts,
        long highRiskRecords,
        Map<RiskLevel, Long> riskDistribution,
        List<HealthRecordResponse> recentRecords,
        List<HealthAlertResponse> recentAlerts) {
}
