package com.example.selfhealthcare.service;

import com.example.selfhealthcare.domain.AlertStatus;
import com.example.selfhealthcare.domain.RiskLevel;
import com.example.selfhealthcare.dto.DashboardSummaryResponse;
import com.example.selfhealthcare.dto.HealthAlertResponse;
import com.example.selfhealthcare.dto.HealthRecordResponse;
import com.example.selfhealthcare.repository.HealthAlertRepository;
import com.example.selfhealthcare.repository.HealthRecordRepository;
import com.example.selfhealthcare.repository.UserProfileRepository;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private final UserProfileRepository userProfileRepository;
    private final HealthRecordRepository healthRecordRepository;
    private final HealthAlertRepository healthAlertRepository;

    public DashboardService(
            UserProfileRepository userProfileRepository,
            HealthRecordRepository healthRecordRepository,
            HealthAlertRepository healthAlertRepository) {
        this.userProfileRepository = userProfileRepository;
        this.healthRecordRepository = healthRecordRepository;
        this.healthAlertRepository = healthAlertRepository;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary() {
        Map<RiskLevel, Long> distribution = new EnumMap<>(RiskLevel.class);
        Arrays.stream(RiskLevel.values()).forEach(level -> distribution.put(level, 0L));
        healthRecordRepository.findAll().forEach(record ->
                distribution.compute(record.getRiskLevel(), (key, value) -> value == null ? 1L : value + 1));

        List<HealthRecordResponse> recentRecords = healthRecordRepository.findTop5ByOrderByRecordDateDescCreatedAtDesc()
                .stream()
                .map(HealthRecordResponse::from)
                .toList();

        List<HealthAlertResponse> recentAlerts = healthAlertRepository.findTop6ByOrderByCreatedAtDesc()
                .stream()
                .map(HealthAlertResponse::from)
                .toList();

        return new DashboardSummaryResponse(
                userProfileRepository.count(),
                healthRecordRepository.count(),
                healthAlertRepository.countByStatus(AlertStatus.PENDING),
                healthRecordRepository.countByRiskLevelIn(List.of(RiskLevel.HIGH, RiskLevel.CRITICAL)),
                distribution,
                recentRecords,
                recentAlerts);
    }
}
