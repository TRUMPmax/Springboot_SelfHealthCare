package com.example.selfhealthcare.dto;

import com.example.selfhealthcare.domain.AlcoholUseStatus;
import com.example.selfhealthcare.domain.BloodType;
import com.example.selfhealthcare.domain.Gender;
import com.example.selfhealthcare.domain.SmokingStatus;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record UserProfileRequest(
        @NotBlank(message = "姓名不能为空")
        @Size(max = 50, message = "姓名长度不能超过50个字符")
        String fullName,
        Gender gender,
        @Min(value = 1, message = "年龄不能小于1")
        @Max(value = 120, message = "年龄不能大于120")
        Integer age,
        @PastOrPresent(message = "出生日期不能晚于今天")
        LocalDate birthDate,
        BloodType bloodType,
        @Size(max = 30, message = "手机号长度不能超过30个字符")
        String phone,
        @Email(message = "邮箱格式不正确")
        @Size(max = 100, message = "邮箱长度不能超过100个字符")
        String email,
        @Size(max = 100, message = "职业长度不能超过100个字符")
        String occupation,
        @DecimalMin(value = "50.0", message = "身高不能小于50厘米")
        @DecimalMax(value = "250.0", message = "身高不能大于250厘米")
        BigDecimal heightCm,
        @DecimalMin(value = "20.0", message = "体重不能小于20千克")
        @DecimalMax(value = "300.0", message = "体重不能大于300千克")
        BigDecimal weightKg,
        SmokingStatus smokingStatus,
        AlcoholUseStatus alcoholUseStatus,
        @Size(max = 500, message = "家族病史长度不能超过500个字符")
        String familyHistory,
        @Size(max = 500, message = "慢性病史长度不能超过500个字符")
        String chronicDiseases,
        @Size(max = 500, message = "过敏信息长度不能超过500个字符")
        String allergies,
        @Size(max = 500, message = "当前用药长度不能超过500个字符")
        String currentMedications,
        @Size(max = 500, message = "手术史长度不能超过500个字符")
        String surgeryHistory,
        @Size(max = 500, message = "运动习惯长度不能超过500个字符")
        String exerciseHabit,
        @Size(max = 500, message = "健康目标长度不能超过500个字符")
        String careGoals,
        @Size(max = 100, message = "紧急联系人长度不能超过100个字符")
        String emergencyContact,
        @Size(max = 30, message = "紧急联系人电话长度不能超过30个字符")
        String emergencyContactPhone,
        @Size(max = 1000, message = "备注长度不能超过1000个字符")
        String notes) {
}
