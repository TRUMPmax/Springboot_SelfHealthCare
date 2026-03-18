package com.example.selfhealthcare.dto;

import com.example.selfhealthcare.domain.AlertSeverity;
import com.example.selfhealthcare.domain.AlertStatus;
import com.example.selfhealthcare.domain.HealthAlert;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record HealthAlertResponse(
        Long id,
        ProfileSummaryResponse profile,
        Long healthRecordId,
        LocalDate observedDate,
        String category,
        String title,
        String indicator,
        String suggestion,
        AlertSeverity severity,
        AlertStatus status,
        String handledNote,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static HealthAlertResponse from(HealthAlert alert) {
        return new HealthAlertResponse(
                alert.getId(),
                ProfileSummaryResponse.from(alert.getProfile()),
                alert.getHealthRecord().getId(),
                alert.getObservedDate(),
                alert.getCategory(),
                alert.getTitle(),
                alert.getIndicator(),
                alert.getSuggestion(),
                alert.getSeverity(),
                alert.getStatus(),
                alert.getHandledNote(),
                alert.getCreatedAt(),
                alert.getUpdatedAt());
    }
}
