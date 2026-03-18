package com.example.selfhealthcare.dto;

import java.util.List;

public record ProfileDetailResponse(
        UserProfileResponse profile,
        HealthRecordResponse latestRecord,
        List<TrendMetricResponse> trends,
        List<String> personalizedSuggestions,
        List<HealthRecordResponse> recentRecords,
        List<HealthAlertResponse> recentAlerts) {
}
