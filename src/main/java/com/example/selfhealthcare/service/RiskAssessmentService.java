package com.example.selfhealthcare.service;

import com.example.selfhealthcare.domain.AlertSeverity;
import com.example.selfhealthcare.domain.HealthRecord;
import com.example.selfhealthcare.domain.RiskLevel;
import com.example.selfhealthcare.domain.UserProfile;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class RiskAssessmentService {

    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final BigDecimal BMI_WARNING = new BigDecimal("24.0");
    private static final BigDecimal BMI_HIGH = new BigDecimal("28.0");
    private static final BigDecimal TEMP_WARNING = new BigDecimal("37.3");
    private static final BigDecimal TEMP_CRITICAL = new BigDecimal("39.0");
    private static final BigDecimal GLUCOSE_WARNING = new BigDecimal("6.1");
    private static final BigDecimal GLUCOSE_HIGH = new BigDecimal("7.0");
    private static final BigDecimal GLUCOSE_CRITICAL = new BigDecimal("11.1");
    private static final BigDecimal OXYGEN_WARNING = new BigDecimal("95.0");
    private static final BigDecimal OXYGEN_CRITICAL = new BigDecimal("90.0");
    private static final BigDecimal CHOLESTEROL_WARNING = new BigDecimal("5.2");
    private static final BigDecimal CHOLESTEROL_HIGH = new BigDecimal("6.2");
    private static final BigDecimal SLEEP_WARNING = new BigDecimal("6.0");
    private static final BigDecimal SLEEP_CRITICAL = new BigDecimal("5.0");
    private static final BigDecimal POST_MEAL_GLUCOSE_WARNING = new BigDecimal("7.8");
    private static final BigDecimal POST_MEAL_GLUCOSE_HIGH = new BigDecimal("11.1");
    private static final BigDecimal WAIST_HIGH_MALE = new BigDecimal("90.0");
    private static final BigDecimal WAIST_HIGH_FEMALE = new BigDecimal("85.0");

    public AssessmentResult assess(UserProfile profile, HealthRecord record) {
        List<AlertDraft> alerts = new ArrayList<>();
        int score = 0;

        BigDecimal bmi = calculateBmi(record.getWeightKg(), profile.getHeightCm());
        if (bmi != null) {
            BigDecimal roundedBmi = bmi.setScale(2, RoundingMode.HALF_UP);
            if (roundedBmi.compareTo(BMI_HIGH) >= 0) {
                alerts.add(new AlertDraft(
                        "体重管理",
                        "肥胖风险升高",
                        "BMI " + roundedBmi,
                        "建议尽快调整饮食结构并增加有氧运动频率，必要时咨询营养或内分泌门诊。",
                        AlertSeverity.HIGH));
                score += 2;
            } else if (roundedBmi.compareTo(BMI_WARNING) >= 0) {
                alerts.add(new AlertDraft(
                        "体重管理",
                        "超重趋势",
                        "BMI " + roundedBmi,
                        "建议控制高糖高脂摄入，每周至少保持150分钟中等强度运动。",
                        AlertSeverity.MEDIUM));
                score += 1;
            }
            bmi = roundedBmi;
        }

        Integer systolic = record.getSystolicPressure();
        Integer diastolic = record.getDiastolicPressure();
        if (systolic != null || diastolic != null) {
            if ((systolic != null && systolic >= 180) || (diastolic != null && diastolic >= 120)) {
                alerts.add(new AlertDraft(
                        "血压风险",
                        "血压达到危急值",
                        describePressure(systolic, diastolic),
                        "存在高血压危象风险，建议立即复测并尽快前往医院评估。",
                        AlertSeverity.CRITICAL));
                score += 3;
            } else if ((systolic != null && systolic >= 140) || (diastolic != null && diastolic >= 90)) {
                alerts.add(new AlertDraft(
                        "血压风险",
                        "高血压风险较高",
                        describePressure(systolic, diastolic),
                        "建议连续监测近一周血压，减少盐分摄入并评估是否需要心血管专科随访。",
                        AlertSeverity.HIGH));
                score += 2;
            } else if ((systolic != null && systolic >= 130) || (diastolic != null && diastolic >= 85)) {
                alerts.add(new AlertDraft(
                        "血压风险",
                        "血压偏高",
                        describePressure(systolic, diastolic),
                        "建议控制熬夜和压力，减少高盐饮食，并继续观察血压变化。",
                        AlertSeverity.MEDIUM));
                score += 1;
            } else if ((systolic != null && systolic < 90) || (diastolic != null && diastolic < 60)) {
                alerts.add(new AlertDraft(
                        "血压风险",
                        "血压偏低",
                        describePressure(systolic, diastolic),
                        "建议结合头晕乏力等症状综合判断，必要时补液并到门诊复查。",
                        AlertSeverity.MEDIUM));
                score += 1;
            }
        }

        BigDecimal glucose = record.getFastingBloodSugar();
        if (glucose != null) {
            if (glucose.compareTo(GLUCOSE_CRITICAL) >= 0) {
                alerts.add(new AlertDraft(
                        "血糖风险",
                        "血糖显著升高",
                        "空腹血糖 " + glucose + " mmol/L",
                        "存在明显高血糖风险，建议尽快就医完善糖化血红蛋白和胰岛功能检查。",
                        AlertSeverity.CRITICAL));
                score += 3;
            } else if (glucose.compareTo(GLUCOSE_HIGH) >= 0) {
                alerts.add(new AlertDraft(
                        "血糖风险",
                        "糖尿病风险升高",
                        "空腹血糖 " + glucose + " mmol/L",
                        "建议连续复测空腹血糖，控制精制碳水摄入并预约内分泌门诊。",
                        AlertSeverity.HIGH));
                score += 2;
            } else if (glucose.compareTo(GLUCOSE_WARNING) >= 0) {
                alerts.add(new AlertDraft(
                        "血糖风险",
                        "血糖处于临界范围",
                        "空腹血糖 " + glucose + " mmol/L",
                        "建议改善饮食结构和运动习惯，按期复查空腹血糖。",
                        AlertSeverity.MEDIUM));
                score += 1;
            }
        }

        BigDecimal postMealGlucose = record.getPostprandialBloodSugar();
        if (postMealGlucose != null) {
            if (postMealGlucose.compareTo(POST_MEAL_GLUCOSE_HIGH) >= 0) {
                alerts.add(new AlertDraft(
                        "血糖风险",
                        "餐后血糖显著升高",
                        "餐后血糖 " + postMealGlucose + " mmol/L",
                        "建议结合饮食记录和空腹血糖进一步评估，必要时到内分泌门诊完善检查。",
                        AlertSeverity.HIGH));
                score += 2;
            } else if (postMealGlucose.compareTo(POST_MEAL_GLUCOSE_WARNING) >= 0) {
                alerts.add(new AlertDraft(
                        "血糖风险",
                        "餐后血糖偏高",
                        "餐后血糖 " + postMealGlucose + " mmol/L",
                        "建议减少高糖高油食物摄入，关注餐后散步和规律运动。",
                        AlertSeverity.MEDIUM));
                score += 1;
            }
        }

        Integer heartRate = record.getHeartRate();
        if (heartRate != null) {
            if (heartRate > 120 || heartRate < 45) {
                alerts.add(new AlertDraft(
                        "心率风险",
                        "心率异常明显",
                        "静息心率 " + heartRate + " 次/分",
                        "建议尽快复测，如伴胸闷心悸应及时就医排查心律失常。",
                        AlertSeverity.HIGH));
                score += 2;
            } else if (heartRate > 100 || heartRate < 50) {
                alerts.add(new AlertDraft(
                        "心率风险",
                        "心率偏离正常范围",
                        "静息心率 " + heartRate + " 次/分",
                        "建议保持休息后复测，结合睡眠、压力和运动状态综合判断。",
                        AlertSeverity.MEDIUM));
                score += 1;
            }
        }

        BigDecimal temperature = record.getBodyTemperature();
        if (temperature != null) {
            if (temperature.compareTo(TEMP_CRITICAL) >= 0) {
                alerts.add(new AlertDraft(
                        "感染风险",
                        "高热预警",
                        "体温 " + temperature + " °C",
                        "建议立即物理降温并尽快就医，排查感染或炎症反应。",
                        AlertSeverity.CRITICAL));
                score += 3;
            } else if (temperature.compareTo(TEMP_WARNING) >= 0) {
                alerts.add(new AlertDraft(
                        "感染风险",
                        "体温升高",
                        "体温 " + temperature + " °C",
                        "建议关注咽痛、咳嗽等症状，补充水分并继续监测体温变化。",
                        AlertSeverity.MEDIUM));
                score += 1;
            }
        }

        BigDecimal oxygen = record.getBloodOxygen();
        if (oxygen != null) {
            if (oxygen.compareTo(OXYGEN_CRITICAL) < 0) {
                alerts.add(new AlertDraft(
                        "呼吸风险",
                        "血氧偏低",
                        "血氧饱和度 " + oxygen + "%",
                        "建议尽快复测并观察呼吸困难情况，如持续下降需立即就医。",
                        AlertSeverity.CRITICAL));
                score += 3;
            } else if (oxygen.compareTo(OXYGEN_WARNING) < 0) {
                alerts.add(new AlertDraft(
                        "呼吸风险",
                        "血氧低于理想范围",
                        "血氧饱和度 " + oxygen + "%",
                        "建议减少剧烈活动，排查呼吸道不适并持续监测。",
                        AlertSeverity.HIGH));
                score += 2;
            }
        }

        BigDecimal cholesterol = record.getCholesterolTotal();
        if (cholesterol != null) {
            if (cholesterol.compareTo(CHOLESTEROL_HIGH) >= 0) {
                alerts.add(new AlertDraft(
                        "血脂风险",
                        "血脂异常风险较高",
                        "总胆固醇 " + cholesterol + " mmol/L",
                        "建议控制油脂和精制糖摄入，并评估是否需要进一步血脂谱检查。",
                        AlertSeverity.HIGH));
                score += 2;
            } else if (cholesterol.compareTo(CHOLESTEROL_WARNING) >= 0) {
                alerts.add(new AlertDraft(
                        "血脂风险",
                        "血脂偏高",
                        "总胆固醇 " + cholesterol + " mmol/L",
                        "建议优化饮食结构，增加膳食纤维和规律运动。",
                        AlertSeverity.MEDIUM));
                score += 1;
            }
        }

        BigDecimal waist = record.getWaistCircumferenceCm();
        BigDecimal waistThreshold = profile.getGender() == com.example.selfhealthcare.domain.Gender.MALE
                ? WAIST_HIGH_MALE
                : WAIST_HIGH_FEMALE;
        if (waist != null && waist.compareTo(waistThreshold) >= 0) {
            alerts.add(new AlertDraft(
                    "体重管理",
                    "中心性肥胖风险",
                    "腰围 " + waist + " cm",
                    "腰围提示内脏脂肪堆积风险上升，建议控制精制碳水和夜宵，并增加持续性运动。",
                    AlertSeverity.MEDIUM));
            score += 1;
        }

        BigDecimal sleepHours = record.getSleepHours();
        if (sleepHours != null) {
            if (sleepHours.compareTo(SLEEP_CRITICAL) < 0) {
                alerts.add(new AlertDraft(
                        "生活方式",
                        "睡眠严重不足",
                        "睡眠 " + sleepHours + " 小时",
                        "建议尽快调整作息，减少熬夜并评估持续疲劳原因。",
                        AlertSeverity.MEDIUM));
                score += 1;
            } else if (sleepHours.compareTo(SLEEP_WARNING) < 0) {
                alerts.add(new AlertDraft(
                        "生活方式",
                        "睡眠时间偏短",
                        "睡眠 " + sleepHours + " 小时",
                        "建议保持固定作息，减少睡前电子设备使用时间。",
                        AlertSeverity.LOW));
                score += 1;
            }
        }

        Integer stressLevel = record.getStressLevel();
        if (stressLevel != null && stressLevel >= 8) {
            alerts.add(new AlertDraft(
                    "生活方式",
                    "压力水平较高",
                    "压力等级 " + stressLevel + "/10",
                    "建议增加放松训练、户外活动或寻求专业心理支持。",
                    AlertSeverity.MEDIUM));
            score += 1;
        }

        Integer stepsCount = record.getStepsCount();
        if (stepsCount != null) {
            if (stepsCount < 1500) {
                alerts.add(new AlertDraft(
                        "生活方式",
                        "活动量明显不足",
                        "步数 " + stepsCount + " 步",
                        "建议逐步把每日步数提升到 5000 步以上，减少久坐时间。",
                        AlertSeverity.MEDIUM));
                score += 1;
            } else if (stepsCount < 3000) {
                alerts.add(new AlertDraft(
                        "生活方式",
                        "近期活动量偏低",
                        "步数 " + stepsCount + " 步",
                        "建议增加轻中度活动，例如饭后步行、拉伸或骑行。",
                        AlertSeverity.LOW));
                score += 1;
            }
        }

        Integer moodScore = record.getMoodScore();
        if (moodScore != null && moodScore <= 4) {
            alerts.add(new AlertDraft(
                    "心理状态",
                    "近期情绪状态偏低",
                    "情绪评分 " + moodScore + "/10",
                    "建议关注睡眠和压力来源，安排稳定作息，必要时寻求专业心理支持。",
                    AlertSeverity.MEDIUM));
            score += 1;
        }

        RiskLevel riskLevel = determineRiskLevel(alerts, score);
        String summary = buildSummary(alerts, riskLevel);
        return new AssessmentResult(riskLevel, score, bmi, summary, alerts);
    }

    private BigDecimal calculateBmi(BigDecimal weightKg, BigDecimal heightCm) {
        if (weightKg == null || heightCm == null || heightCm.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        BigDecimal heightMeter = heightCm.divide(HUNDRED, 4, RoundingMode.HALF_UP);
        return weightKg.divide(heightMeter.multiply(heightMeter), 2, RoundingMode.HALF_UP);
    }

    private RiskLevel determineRiskLevel(List<AlertDraft> alerts, int score) {
        AlertSeverity maxSeverity = alerts.stream()
                .map(AlertDraft::severity)
                .filter(Objects::nonNull)
                .max(Comparator.comparingInt(alert -> alert.toRiskLevel().getWeight()))
                .orElse(AlertSeverity.LOW);

        if (maxSeverity == AlertSeverity.CRITICAL) {
            return RiskLevel.CRITICAL;
        }
        if (maxSeverity == AlertSeverity.HIGH || score >= 5) {
            return RiskLevel.HIGH;
        }
        if (maxSeverity == AlertSeverity.MEDIUM || score >= 2) {
            return RiskLevel.MEDIUM;
        }
        return alerts.isEmpty() ? RiskLevel.LOW : RiskLevel.MEDIUM;
    }

    private String describePressure(Integer systolic, Integer diastolic) {
        if (systolic == null && diastolic == null) {
            return "血压数据缺失";
        }
        if (systolic == null) {
            return "舒张压 " + diastolic + " mmHg";
        }
        if (diastolic == null) {
            return "收缩压 " + systolic + " mmHg";
        }
        return systolic + "/" + diastolic + " mmHg";
    }

    private String buildSummary(List<AlertDraft> alerts, RiskLevel riskLevel) {
        if (alerts.isEmpty()) {
            return "本次健康指标整体平稳，建议保持规律作息、均衡饮食和持续记录。";
        }
        String topics = alerts.stream()
                .map(AlertDraft::category)
                .distinct()
                .limit(3)
                .reduce((left, right) -> left + "、" + right)
                .orElse("多项指标");
        return "系统识别为" + riskLevel.getLabel() + "，重点关注" + topics + "等问题，建议根据预警建议尽快干预。";
    }

    public record AssessmentResult(
            RiskLevel riskLevel,
            int riskScore,
            BigDecimal bmi,
            String summary,
            List<AlertDraft> alerts) {
    }

    public record AlertDraft(
            String category,
            String title,
            String indicator,
            String suggestion,
            AlertSeverity severity) {
    }
}
