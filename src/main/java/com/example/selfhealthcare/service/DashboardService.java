package com.example.selfhealthcare.service;

import com.example.selfhealthcare.domain.AlertStatus;
import com.example.selfhealthcare.domain.AppUser;
import com.example.selfhealthcare.domain.HealthRecord;
import com.example.selfhealthcare.domain.RiskLevel;
import com.example.selfhealthcare.dto.DashboardSummaryResponse;
import com.example.selfhealthcare.dto.HealthAlertResponse;
import com.example.selfhealthcare.dto.HealthRecordResponse;
import com.example.selfhealthcare.dto.UserProfileResponse;
import com.example.selfhealthcare.repository.HealthAlertRepository;
import com.example.selfhealthcare.repository.HealthRecordRepository;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private final AuthService authService;
    private final UserProfileService userProfileService;
    private final HealthRecordRepository healthRecordRepository;
    private final HealthAlertRepository healthAlertRepository;

    public DashboardService(
            AuthService authService,
            UserProfileService userProfileService,
            HealthRecordRepository healthRecordRepository,
            HealthAlertRepository healthAlertRepository) {
        this.authService = authService;
        this.userProfileService = userProfileService;
        this.healthRecordRepository = healthRecordRepository;
        this.healthAlertRepository = healthAlertRepository;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary() {
        AppUser currentUser = authService.requireAuthenticatedUser();
        Long userId = currentUser.getId();

        Map<RiskLevel, Long> distribution = new EnumMap<>(RiskLevel.class);
        Arrays.stream(RiskLevel.values()).forEach(level -> distribution.put(level, 0L));
        healthRecordRepository.findByUserIdOrderByRecordDateAscCreatedAtAsc(userId).forEach(record ->
                distribution.compute(record.getRiskLevel(), (key, value) -> value == null ? 1L : value + 1));

        List<HealthRecord> recentRecordEntities = healthRecordRepository.findTop5ByUserIdOrderByRecordDateDescCreatedAtDesc(userId);
        List<HealthRecordResponse> recentRecords = recentRecordEntities.stream()
                .map(HealthRecordResponse::from)
                .toList();

        List<HealthAlertResponse> recentAlerts = healthAlertRepository.findTop6ByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(HealthAlertResponse::from)
                .toList();

        UserProfileResponse profile = userProfileService.findByUserId(userId)
                .map(UserProfileResponse::from)
                .orElse(null);

        HealthRecord latestRecord = recentRecordEntities.isEmpty() ? null : recentRecordEntities.getFirst();

        return new DashboardSummaryResponse(
                currentUser.getDisplayName(),
                profile != null,
                profile == null ? 0 : profile.completionScore(),
                healthRecordRepository.countByUserId(userId),
                healthAlertRepository.countByUserIdAndStatus(userId, AlertStatus.PENDING),
                healthRecordRepository.countByUserIdAndRiskLevelIn(userId, List.of(RiskLevel.HIGH, RiskLevel.CRITICAL)),
                latestRecord == null ? null : latestRecord.getRiskLevel(),
                latestRecord == null ? null : latestRecord.getRecordDate(),
                distribution,
                recentRecords,
                recentAlerts);
    }
}
