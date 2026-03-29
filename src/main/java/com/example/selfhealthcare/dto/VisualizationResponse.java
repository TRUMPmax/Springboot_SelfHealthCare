package com.example.selfhealthcare.dto;

import com.example.selfhealthcare.domain.RiskLevel;
import java.util.List;
import java.util.Map;

public record VisualizationResponse(
        UserProfileResponse profile,
        List<VisualizationSeriesResponse> series,
        Map<RiskLevel, Long> riskDistribution,
        List<HealthRecordResponse> latestRecords) {
}
