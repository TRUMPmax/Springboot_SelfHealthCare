package com.example.selfhealthcare.service;

import com.example.selfhealthcare.domain.AppUser;
import com.example.selfhealthcare.domain.HealthRecord;
import com.example.selfhealthcare.domain.RiskLevel;
import com.example.selfhealthcare.dto.HealthRecordResponse;
import com.example.selfhealthcare.dto.TrendPointResponse;
import com.example.selfhealthcare.dto.UserProfileResponse;
import com.example.selfhealthcare.dto.VisualizationResponse;
import com.example.selfhealthcare.dto.VisualizationSeriesResponse;
import com.example.selfhealthcare.repository.HealthRecordRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VisualizationService {

    private final AuthService authService;
    private final UserProfileService userProfileService;
    private final HealthRecordRepository healthRecordRepository;

    public VisualizationService(
            AuthService authService,
            UserProfileService userProfileService,
            HealthRecordRepository healthRecordRepository) {
        this.authService = authService;
        this.userProfileService = userProfileService;
        this.healthRecordRepository = healthRecordRepository;
    }

    @Transactional(readOnly = true)
    public VisualizationResponse getVisualization() {
        AppUser currentUser = authService.requireAuthenticatedUser();
        List<HealthRecord> allRecords = healthRecordRepository.findByUserIdOrderByRecordDateAscCreatedAtAsc(currentUser.getId());
        if (allRecords.size() > 20) {
            allRecords = new ArrayList<>(allRecords.subList(allRecords.size() - 20, allRecords.size()));
        }

        Map<RiskLevel, Long> distribution = new EnumMap<>(RiskLevel.class);
        Arrays.stream(RiskLevel.values()).forEach(level -> distribution.put(level, 0L));
        allRecords.forEach(record -> distribution.compute(record.getRiskLevel(), (key, value) -> value == null ? 1L : value + 1));

        return new VisualizationResponse(
                userProfileService.findByUserId(currentUser.getId()).map(UserProfileResponse::from).orElse(null),
                List.of(
                        buildSeries("weight", "体重", "kg", allRecords, HealthRecord::getWeightKg),
                        buildSeries("systolic", "收缩压", "mmHg", allRecords, record -> toDecimal(record.getSystolicPressure())),
                        buildSeries("glucose", "空腹血糖", "mmol/L", allRecords, HealthRecord::getFastingBloodSugar),
                        buildSeries("sleep", "睡眠时长", "h", allRecords, HealthRecord::getSleepHours),
                        buildSeries("exercise", "运动时长", "min", allRecords, record -> toDecimal(record.getExerciseMinutes())),
                        buildSeries("risk", "风险分数", "分", allRecords, record -> toDecimal(record.getRiskScore())))
                        .stream()
                        .filter(Objects::nonNull)
                        .toList(),
                distribution,
                healthRecordRepository.findTop10ByUserIdOrderByRecordDateDescCreatedAtDesc(currentUser.getId()).stream()
                        .map(HealthRecordResponse::from)
                        .toList());
    }

    private VisualizationSeriesResponse buildSeries(
            String metricCode,
            String metricName,
            String unit,
            List<HealthRecord> records,
            Function<HealthRecord, BigDecimal> extractor) {
        List<TrendPointResponse> points = records.stream()
                .map(record -> {
                    BigDecimal value = extractor.apply(record);
                    return value == null ? null : new TrendPointResponse(record.getRecordDate(), scale(value));
                })
                .filter(Objects::nonNull)
                .toList();

        if (points.isEmpty()) {
            return null;
        }

        BigDecimal latest = points.getLast().value();
        BigDecimal average = points.stream()
                .map(TrendPointResponse::value)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(points.size()), 2, RoundingMode.HALF_UP);

        return new VisualizationSeriesResponse(metricCode, metricName, unit, latest, average, points);
    }

    private BigDecimal toDecimal(Integer value) {
        return value == null ? null : BigDecimal.valueOf(value);
    }

    private BigDecimal scale(BigDecimal value) {
        return value == null ? null : value.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros();
    }
}
