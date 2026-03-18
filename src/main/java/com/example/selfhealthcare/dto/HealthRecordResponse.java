package com.example.selfhealthcare.dto;

import com.example.selfhealthcare.domain.HealthRecord;
import com.example.selfhealthcare.domain.RiskLevel;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record HealthRecordResponse(
        Long id,
        ProfileSummaryResponse profile,
        LocalDate recordDate,
        BigDecimal weightKg,
        BigDecimal waistCircumferenceCm,
        Integer systolicPressure,
        Integer diastolicPressure,
        Integer heartRate,
        BigDecimal fastingBloodSugar,
        BigDecimal postprandialBloodSugar,
        BigDecimal bodyTemperature,
        BigDecimal bloodOxygen,
        BigDecimal cholesterolTotal,
        BigDecimal bmi,
        BigDecimal sleepHours,
        Integer exerciseMinutes,
        Integer stepsCount,
        Integer waterIntakeMl,
        Integer stressLevel,
        Integer moodScore,
        String symptoms,
        String medicationTaken,
        RiskLevel riskLevel,
        Integer riskScore,
        String summary,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static HealthRecordResponse from(HealthRecord record) {
        return new HealthRecordResponse(
                record.getId(),
                ProfileSummaryResponse.from(record.getProfile()),
                record.getRecordDate(),
                record.getWeightKg(),
                record.getWaistCircumferenceCm(),
                record.getSystolicPressure(),
                record.getDiastolicPressure(),
                record.getHeartRate(),
                record.getFastingBloodSugar(),
                record.getPostprandialBloodSugar(),
                record.getBodyTemperature(),
                record.getBloodOxygen(),
                record.getCholesterolTotal(),
                record.getBmi(),
                record.getSleepHours(),
                record.getExerciseMinutes(),
                record.getStepsCount(),
                record.getWaterIntakeMl(),
                record.getStressLevel(),
                record.getMoodScore(),
                record.getSymptoms(),
                record.getMedicationTaken(),
                record.getRiskLevel(),
                record.getRiskScore(),
                record.getSummary(),
                record.getNotes(),
                record.getCreatedAt(),
                record.getUpdatedAt());
    }
}
