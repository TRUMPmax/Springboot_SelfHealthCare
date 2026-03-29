package com.example.selfhealthcare.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record HealthRecordRequest(
        @NotNull(message = "记录日期不能为空")
        @PastOrPresent(message = "记录日期不能晚于今天")
        LocalDate recordDate,
        @DecimalMin(value = "20.0", message = "体重不能小于20千克")
        @DecimalMax(value = "300.0", message = "体重不能大于300千克")
        BigDecimal weightKg,
        @DecimalMin(value = "40.0", message = "腰围不能小于40厘米")
        @DecimalMax(value = "200.0", message = "腰围不能大于200厘米")
        BigDecimal waistCircumferenceCm,
        @Min(value = 40, message = "收缩压不能低于40")
        @Max(value = 250, message = "收缩压不能高于250")
        Integer systolicPressure,
        @Min(value = 30, message = "舒张压不能低于30")
        @Max(value = 180, message = "舒张压不能高于180")
        Integer diastolicPressure,
        @Min(value = 30, message = "心率不能低于30")
        @Max(value = 220, message = "心率不能高于220")
        Integer heartRate,
        @DecimalMin(value = "2.0", message = "空腹血糖不能低于2.0")
        @DecimalMax(value = "30.0", message = "空腹血糖不能高于30.0")
        BigDecimal fastingBloodSugar,
        @DecimalMin(value = "2.0", message = "餐后血糖不能低于2.0")
        @DecimalMax(value = "30.0", message = "餐后血糖不能高于30.0")
        BigDecimal postprandialBloodSugar,
        @DecimalMin(value = "34.0", message = "体温不能低于34.0")
        @DecimalMax(value = "43.0", message = "体温不能高于43.0")
        BigDecimal bodyTemperature,
        @DecimalMin(value = "70.0", message = "血氧不能低于70")
        @DecimalMax(value = "100.0", message = "血氧不能高于100")
        BigDecimal bloodOxygen,
        @DecimalMin(value = "2.0", message = "总胆固醇不能低于2.0")
        @DecimalMax(value = "15.0", message = "总胆固醇不能高于15.0")
        BigDecimal cholesterolTotal,
        @DecimalMin(value = "0.0", message = "睡眠时长不能小于0")
        @DecimalMax(value = "24.0", message = "睡眠时长不能大于24")
        BigDecimal sleepHours,
        @Min(value = 0, message = "运动时长不能小于0")
        @Max(value = 1440, message = "运动时长不能大于1440")
        Integer exerciseMinutes,
        @Min(value = 0, message = "步数不能小于0")
        @Max(value = 100000, message = "步数不能大于100000")
        Integer stepsCount,
        @Min(value = 0, message = "饮水量不能小于0")
        @Max(value = 10000, message = "饮水量不能大于10000")
        Integer waterIntakeMl,
        @Min(value = 1, message = "压力等级不能小于1")
        @Max(value = 10, message = "压力等级不能大于10")
        Integer stressLevel,
        @Min(value = 1, message = "情绪评分不能小于1")
        @Max(value = 10, message = "情绪评分不能大于10")
        Integer moodScore,
        @Size(max = 500, message = "症状描述长度不能超过500个字符")
        String symptoms,
        @Size(max = 500, message = "用药记录长度不能超过500个字符")
        String medicationTaken,
        @Size(max = 1000, message = "备注长度不能超过1000个字符")
        String notes) {
}
