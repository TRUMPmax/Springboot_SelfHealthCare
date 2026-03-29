package com.example.selfhealthcare.service;

import com.example.selfhealthcare.domain.AlcoholUseStatus;
import com.example.selfhealthcare.domain.AppUser;
import com.example.selfhealthcare.domain.BloodType;
import com.example.selfhealthcare.domain.Gender;
import com.example.selfhealthcare.domain.HealthRecord;
import com.example.selfhealthcare.domain.RiskLevel;
import com.example.selfhealthcare.domain.SmokingStatus;
import com.example.selfhealthcare.domain.UserProfile;
import com.example.selfhealthcare.dto.HealthAlertResponse;
import com.example.selfhealthcare.dto.HealthRecordResponse;
import com.example.selfhealthcare.dto.ProfileDetailResponse;
import com.example.selfhealthcare.dto.TrendMetricResponse;
import com.example.selfhealthcare.dto.TrendPointResponse;
import com.example.selfhealthcare.dto.UserProfileResponse;
import com.example.selfhealthcare.exception.NotFoundException;
import com.example.selfhealthcare.repository.HealthAlertRepository;
import com.example.selfhealthcare.repository.HealthRecordRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileInsightService {

    private final AuthService authService;
    private final UserProfileService userProfileService;
    private final HealthRecordRepository healthRecordRepository;
    private final HealthAlertRepository healthAlertRepository;

    public ProfileInsightService(
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
    public ProfileDetailResponse getCurrentProfileDetail() {
        AppUser currentUser = authService.requireAuthenticatedUser();
        Optional<UserProfile> profile = userProfileService.findByUserId(currentUser.getId());
        List<HealthRecord> records = healthRecordRepository.findTop12ByUserIdOrderByRecordDateDescCreatedAtDesc(currentUser.getId());

        HealthRecord latestRecord = records.isEmpty() ? null : records.getFirst();
        List<TrendMetricResponse> trends = buildTrends(records);
        List<String> suggestions = buildPersonalizedSuggestions(profile.orElse(null), records);

        return new ProfileDetailResponse(
                profile.map(UserProfileResponse::from).orElse(null),
                latestRecord == null ? null : HealthRecordResponse.from(latestRecord),
                trends,
                suggestions,
                healthRecordRepository.findTop10ByUserIdOrderByRecordDateDescCreatedAtDesc(currentUser.getId()).stream()
                        .map(HealthRecordResponse::from)
                        .toList(),
                healthAlertRepository.findTop10ByUserIdOrderByObservedDateDescCreatedAtDesc(currentUser.getId()).stream()
                        .map(HealthAlertResponse::from)
                        .toList());
    }

    @Transactional(readOnly = true)
    public ConsultationContext buildConsultationContext(Long focusRecordId, String question) {
        AppUser currentUser = authService.requireAuthenticatedUser();
        ProfileDetailResponse detail = getCurrentProfileDetail();
        HealthRecordResponse focusRecord = resolveFocusRecord(detail, focusRecordId);

        String profileName = detail.profile() == null ? currentUser.getDisplayName() : detail.profile().fullName();
        StringBuilder digest = new StringBuilder();
        digest.append(profileName);
        if (focusRecord != null && focusRecord.riskLevel() != null) {
            digest.append("，最近风险等级为").append(focusRecord.riskLevel().getLabel());
        }

        StringBuilder context = new StringBuilder();
        context.append("【个人档案】\n")
                .append("姓名：").append(profileName).append('\n');

        if (detail.profile() != null) {
            context.append("性别：").append(orDash(labelOf(detail.profile().gender()))).append('\n')
                    .append("年龄：").append(orDash(detail.profile().age())).append('\n')
                    .append("血型：").append(orDash(labelOf(detail.profile().bloodType()))).append('\n')
                    .append("职业：").append(orDash(detail.profile().occupation())).append('\n')
                    .append("吸烟情况：").append(orDash(labelOf(detail.profile().smokingStatus()))).append('\n')
                    .append("饮酒情况：").append(orDash(labelOf(detail.profile().alcoholUseStatus()))).append('\n')
                    .append("慢性病史：").append(orDash(detail.profile().chronicDiseases())).append('\n')
                    .append("家族病史：").append(orDash(detail.profile().familyHistory())).append('\n')
                    .append("过敏信息：").append(orDash(detail.profile().allergies())).append('\n')
                    .append("当前用药：").append(orDash(detail.profile().currentMedications())).append('\n')
                    .append("健康目标：").append(orDash(detail.profile().careGoals())).append('\n');
        } else {
            context.append("备注：当前账号尚未完善个人档案，仅基于健康记录进行分析。\n");
        }

        if (focusRecord != null) {
            context.append("\n【重点健康记录】\n")
                    .append("日期：").append(focusRecord.recordDate()).append('\n')
                    .append("血压：").append(orDash(focusRecord.systolicPressure())).append("/")
                    .append(orDash(focusRecord.diastolicPressure())).append(" mmHg\n")
                    .append("空腹血糖：").append(orDash(focusRecord.fastingBloodSugar())).append('\n')
                    .append("餐后血糖：").append(orDash(focusRecord.postprandialBloodSugar())).append('\n')
                    .append("心率：").append(orDash(focusRecord.heartRate())).append('\n')
                    .append("体重：").append(orDash(focusRecord.weightKg())).append('\n')
                    .append("BMI：").append(orDash(focusRecord.bmi())).append('\n')
                    .append("睡眠：").append(orDash(focusRecord.sleepHours())).append('\n')
                    .append("运动：").append(orDash(focusRecord.exerciseMinutes())).append(" 分钟\n")
                    .append("步数：").append(orDash(focusRecord.stepsCount())).append('\n')
                    .append("压力：").append(orDash(focusRecord.stressLevel())).append("/10\n")
                    .append("情绪：").append(orDash(focusRecord.moodScore())).append("/10\n")
                    .append("症状：").append(orDash(focusRecord.symptoms())).append('\n')
                    .append("风险等级：").append(orDash(labelOf(focusRecord.riskLevel()))).append('\n')
                    .append("系统摘要：").append(orDash(focusRecord.summary())).append('\n');
        }

        if (!detail.trends().isEmpty()) {
            context.append("\n【近期趋势】\n");
            detail.trends().forEach(trend -> context.append(trend.metricName())
                    .append("：最新").append(orDash(trend.latestValue()))
                    .append(trend.unit() == null ? "" : trend.unit())
                    .append("，变化").append(orDash(trend.changeValue()))
                    .append(trend.unit() == null ? "" : trend.unit())
                    .append("，趋势").append(trend.direction())
                    .append("，解读：").append(trend.interpretation())
                    .append('\n'));
        }

        if (!detail.personalizedSuggestions().isEmpty()) {
            context.append("\n【系统建议】\n");
            detail.personalizedSuggestions().forEach(item -> context.append("- ").append(item).append('\n'));
        }

        if (question != null && !question.isBlank()) {
            context.append("\n【用户问题】\n").append(question.trim()).append('\n');
        }

        return new ConsultationContext(context.toString(), digest.toString());
    }

    private HealthRecordResponse resolveFocusRecord(ProfileDetailResponse detail, Long focusRecordId) {
        if (focusRecordId == null) {
            return detail.latestRecord();
        }

        return detail.recentRecords().stream()
                .filter(record -> record.id().equals(focusRecordId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("未找到指定的健康记录"));
    }

    private List<TrendMetricResponse> buildTrends(List<HealthRecord> recordsDesc) {
        if (recordsDesc.isEmpty()) {
            return List.of();
        }

        List<TrendMetricResponse> trends = new ArrayList<>();
        trends.add(buildMetric(recordsDesc, "weight", "体重", "kg", HealthRecord::getWeightKg,
                (latest, change) -> "结合饮食和活动量，继续观察近期体重变化。"));
        trends.add(buildMetric(recordsDesc, "blood-pressure", "收缩压", "mmHg",
                record -> toDecimal(record.getSystolicPressure()),
                (latest, change) -> latest.compareTo(new BigDecimal("140")) >= 0
                        ? "收缩压偏高，建议连续监测并控制盐分摄入。"
                        : "收缩压波动相对平稳，可持续跟踪。"));
        trends.add(buildMetric(recordsDesc, "glucose-fasting", "空腹血糖", "mmol/L",
                HealthRecord::getFastingBloodSugar,
                (latest, change) -> latest.compareTo(new BigDecimal("6.1")) >= 0
                        ? "空腹血糖已进入重点关注范围。"
                        : "空腹血糖目前相对平稳。"));
        trends.add(buildMetric(recordsDesc, "sleep", "睡眠时长", "h",
                HealthRecord::getSleepHours,
                (latest, change) -> latest.compareTo(new BigDecimal("6.0")) < 0
                        ? "睡眠不足会明显影响恢复和代谢。"
                        : "近期睡眠时长相对稳定。"));
        trends.add(buildMetric(recordsDesc, "exercise", "运动时长", "min",
                record -> toDecimal(record.getExerciseMinutes()),
                (latest, change) -> latest.compareTo(new BigDecimal("30")) < 0
                        ? "运动时长偏低，可逐步提升活动量。"
                        : "运动时长保持得不错，可继续坚持。"));
        trends.add(buildMetric(recordsDesc, "risk-score", "风险分数", "分",
                record -> toDecimal(record.getRiskScore()),
                (latest, change) -> latest.compareTo(new BigDecimal("5")) >= 0
                        ? "风险分数偏高，建议尽快针对异常指标进行干预。"
                        : "总体风险仍处于可跟踪范围内。"));

        return trends.stream().filter(Objects::nonNull).toList();
    }

    private TrendMetricResponse buildMetric(
            List<HealthRecord> recordsDesc,
            String metricCode,
            String metricName,
            String unit,
            Function<HealthRecord, BigDecimal> extractor,
            InterpretationBuilder interpretationBuilder) {
        List<HealthRecord> recordsAsc = new ArrayList<>(recordsDesc);
        Collections.reverse(recordsAsc);

        List<TrendPointResponse> points = recordsAsc.stream()
                .map(record -> {
                    BigDecimal value = extractor.apply(record);
                    return value == null ? null : new TrendPointResponse(record.getRecordDate(), value);
                })
                .filter(Objects::nonNull)
                .toList();

        if (points.isEmpty()) {
            return null;
        }

        BigDecimal latestValue = points.getLast().value();
        BigDecimal previousValue = points.size() >= 2 ? points.get(points.size() - 2).value() : null;
        BigDecimal changeValue = previousValue == null ? BigDecimal.ZERO : latestValue.subtract(previousValue);
        String direction = previousValue == null || changeValue.abs().compareTo(new BigDecimal("0.01")) < 0
                ? "稳定"
                : changeValue.signum() > 0 ? "上升" : "下降";

        return new TrendMetricResponse(
                metricCode,
                metricName,
                unit,
                scale(latestValue),
                scale(previousValue),
                scale(changeValue),
                direction,
                interpretationBuilder.build(scale(latestValue), scale(changeValue)),
                points.stream()
                        .map(point -> new TrendPointResponse(point.recordDate(), scale(point.value())))
                        .toList());
    }

    private List<String> buildPersonalizedSuggestions(UserProfile profile, List<HealthRecord> recordsDesc) {
        List<String> suggestions = new ArrayList<>();
        if (recordsDesc.isEmpty()) {
            suggestions.add("建议先完成至少3次健康记录，系统才能更准确地识别趋势变化并生成个性化建议。");
        } else {
            HealthRecord latest = recordsDesc.getFirst();
            if (latest.getRiskLevel() == RiskLevel.HIGH || latest.getRiskLevel() == RiskLevel.CRITICAL) {
                suggestions.add("最近一次记录属于高风险区间，建议优先处理血压、血糖、血氧等异常指标，必要时尽快线下就医。");
            }
            if (isRising(recordsDesc, HealthRecord::getFastingBloodSugar, new BigDecimal("0.5"))) {
                suggestions.add("空腹血糖较之前有上升趋势，建议减少高糖主食和夜宵，并保留近一周的饮食与血糖记录。");
            }
            if (isRising(recordsDesc, record -> toDecimal(record.getSystolicPressure()), new BigDecimal("8"))) {
                suggestions.add("收缩压近期有上升趋势，建议连续晨起和晚间测量血压，减少盐分摄入并避免熬夜。");
            }
            if (isRising(recordsDesc, HealthRecord::getWeightKg, new BigDecimal("1.5"))) {
                suggestions.add("近期体重上升较明显，可结合腰围、步数和饮食记录评估是否需要强化体重管理。");
            }
            if (average(recordsDesc, HealthRecord::getSleepHours).compareTo(new BigDecimal("6.0")) < 0) {
                suggestions.add("最近睡眠均值偏低，建议固定入睡时间，减少睡前电子设备使用，优先改善恢复质量。");
            }
            if (average(recordsDesc, record -> toDecimal(record.getExerciseMinutes())).compareTo(new BigDecimal("30")) < 0) {
                suggestions.add("最近运动时长不足，建议从每周3次、每次20到30分钟的中等强度运动开始逐步恢复。");
            }
            if (average(recordsDesc, record -> toDecimal(record.getStepsCount())).compareTo(new BigDecimal("4000")) < 0) {
                suggestions.add("步数偏低，建议通过饭后步行和减少久坐来提升日常活动量。");
            }
            if (average(recordsDesc, record -> toDecimal(record.getMoodScore())).compareTo(new BigDecimal("5")) <= 0) {
                suggestions.add("近期情绪评分偏低，建议结合压力来源、睡眠和活动量一起调整，如持续低落可考虑寻求专业支持。");
            }
        }

        if (profile != null) {
            if (profile.getSmokingStatus() == SmokingStatus.CURRENT || profile.getSmokingStatus() == SmokingStatus.OCCASIONAL) {
                suggestions.add("吸烟会增加心血管和呼吸系统风险，若近期指标波动较大，建议把控烟作为优先干预目标。");
            }
            if (profile.getAlcoholUseStatus() == AlcoholUseStatus.FREQUENT || profile.getAlcoholUseStatus() == AlcoholUseStatus.WEEKLY) {
                suggestions.add("饮酒频率偏高时，建议额外关注血压、睡眠与血糖波动，并控制饮酒总量。");
            }
            if (profile.getChronicDiseases() != null && !profile.getChronicDiseases().isBlank()) {
                suggestions.add("已登记慢性病史，建议把复查计划、用药记录和异常症状一并纳入长期跟踪。");
            }
        }

        if (suggestions.isEmpty()) {
            suggestions.add("当前整体状态较平稳，建议继续保持规律记录，这样系统才能识别更细微的长期变化。");
        }

        return suggestions.stream().distinct().limit(6).toList();
    }

    private boolean isRising(List<HealthRecord> recordsDesc, Function<HealthRecord, BigDecimal> extractor, BigDecimal threshold) {
        if (recordsDesc.size() < 2) {
            return false;
        }
        BigDecimal latest = extractor.apply(recordsDesc.getFirst());
        BigDecimal previous = extractor.apply(recordsDesc.get(1));
        if (latest == null || previous == null) {
            return false;
        }
        return latest.subtract(previous).compareTo(threshold) >= 0;
    }

    private BigDecimal average(List<HealthRecord> recordsDesc, Function<HealthRecord, BigDecimal> extractor) {
        List<BigDecimal> values = recordsDesc.stream()
                .limit(5)
                .map(extractor)
                .filter(Objects::nonNull)
                .toList();
        if (values.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal sum = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(values.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal toDecimal(Integer value) {
        return value == null ? null : BigDecimal.valueOf(value);
    }

    private BigDecimal scale(BigDecimal value) {
        return value == null ? null : value.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros();
    }

    private String orDash(Object value) {
        return value == null ? "-" : String.valueOf(value);
    }

    private String labelOf(Object enumValue) {
        if (enumValue instanceof RiskLevel value) {
            return value.getLabel();
        }
        if (enumValue instanceof Gender value) {
            return value.getLabel();
        }
        if (enumValue instanceof BloodType value) {
            return value.getLabel();
        }
        if (enumValue instanceof SmokingStatus value) {
            return value.getLabel();
        }
        if (enumValue instanceof AlcoholUseStatus value) {
            return value.getLabel();
        }
        return enumValue == null ? null : enumValue.toString();
    }

    public record ConsultationContext(String promptContext, String contextDigest) {
    }

    @FunctionalInterface
    private interface InterpretationBuilder {
        String build(BigDecimal latest, BigDecimal change);
    }
}
