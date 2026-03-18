package com.example.selfhealthcare.config;

import com.example.selfhealthcare.domain.AlcoholUseStatus;
import com.example.selfhealthcare.domain.BloodType;
import com.example.selfhealthcare.domain.Gender;
import com.example.selfhealthcare.domain.SmokingStatus;
import com.example.selfhealthcare.dto.HealthRecordRequest;
import com.example.selfhealthcare.dto.UserProfileRequest;
import com.example.selfhealthcare.dto.UserProfileResponse;
import com.example.selfhealthcare.repository.UserProfileRepository;
import com.example.selfhealthcare.service.HealthRecordService;
import com.example.selfhealthcare.service.UserProfileService;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner loadDemoData(
            UserProfileRepository userProfileRepository,
            UserProfileService userProfileService,
            HealthRecordService healthRecordService) {
        return args -> {
            if (userProfileRepository.count() > 0) {
                return;
            }

            UserProfileResponse liNa = userProfileService.createProfile(new UserProfileRequest(
                    "李娜",
                    "本人",
                    Gender.FEMALE,
                    29,
                    LocalDate.of(1996, 5, 16),
                    BloodType.A,
                    "13800001111",
                    "lina@example.com",
                    "产品经理",
                    new BigDecimal("165.00"),
                    new BigDecimal("56.00"),
                    SmokingStatus.NEVER,
                    AlcoholUseStatus.OCCASIONAL,
                    "母亲有高血压史",
                    null,
                    "青霉素过敏",
                    null,
                    null,
                    "每周快走 2-3 次",
                    "缓解压力、稳定睡眠",
                    "李明",
                    "13912345678",
                    "日常久坐，需要控制压力"));

            UserProfileResponse wangQiang = userProfileService.createProfile(new UserProfileRequest(
                    "王强",
                    "父亲",
                    Gender.MALE,
                    45,
                    LocalDate.of(1981, 3, 8),
                    BloodType.O,
                    "13800002222",
                    "wangqiang@example.com",
                    "项目主管",
                    new BigDecimal("172.00"),
                    new BigDecimal("82.00"),
                    SmokingStatus.FORMER,
                    AlcoholUseStatus.WEEKLY,
                    "父亲有糖尿病史",
                    "脂肪肝",
                    null,
                    "护肝药物间断使用",
                    null,
                    "工作日活动量偏少",
                    "控制血糖和体重",
                    "王芳",
                    "13888886666",
                    "工作强度大，经常熬夜"));

            UserProfileResponse zhangMin = userProfileService.createProfile(new UserProfileRequest(
                    "张敏",
                    "母亲",
                    Gender.FEMALE,
                    61,
                    LocalDate.of(1965, 11, 22),
                    BloodType.B,
                    "13800003333",
                    "zhangmin@example.com",
                    "退休",
                    new BigDecimal("158.00"),
                    new BigDecimal("67.00"),
                    SmokingStatus.NEVER,
                    AlcoholUseStatus.NEVER,
                    "家族中有高脂血症",
                    "高血压",
                    null,
                    "降压药每日服用",
                    "阑尾手术史",
                    "每天散步 30 分钟",
                    "稳定血压和血脂",
                    "陈涛",
                    "13777774444",
                    "建议持续跟踪血压和血脂"));

            healthRecordService.createRecord(new HealthRecordRequest(
                    liNa.id(),
                    LocalDate.now().minusDays(6),
                    new BigDecimal("56.50"),
                    new BigDecimal("71.00"),
                    118,
                    78,
                    74,
                    new BigDecimal("5.10"),
                    new BigDecimal("6.60"),
                    new BigDecimal("36.5"),
                    new BigDecimal("98.0"),
                    new BigDecimal("4.70"),
                    new BigDecimal("7.5"),
                    45,
                    7800,
                    1800,
                    4,
                    7,
                    null,
                    null,
                    "状态稳定"));

            healthRecordService.createRecord(new HealthRecordRequest(
                    liNa.id(),
                    LocalDate.now().minusDays(1),
                    new BigDecimal("57.00"),
                    new BigDecimal("72.00"),
                    122,
                    80,
                    76,
                    new BigDecimal("5.30"),
                    new BigDecimal("6.90"),
                    new BigDecimal("36.6"),
                    new BigDecimal("98.0"),
                    new BigDecimal("4.80"),
                    new BigDecimal("7.2"),
                    40,
                    6500,
                    1700,
                    5,
                    7,
                    "偶有肩颈酸胀",
                    null,
                    "整体较稳定，但睡眠偏少"));

            healthRecordService.createRecord(new HealthRecordRequest(
                    liNa.id(),
                    LocalDate.now(),
                    new BigDecimal("58.00"),
                    new BigDecimal("73.50"),
                    138,
                    88,
                    92,
                    new BigDecimal("6.40"),
                    new BigDecimal("8.60"),
                    new BigDecimal("36.9"),
                    new BigDecimal("97.0"),
                    new BigDecimal("5.40"),
                    new BigDecimal("6.0"),
                    20,
                    3200,
                    1200,
                    8,
                    5,
                    "乏力、睡眠浅",
                    null,
                    "近期加班较多，压力明显升高"));

            healthRecordService.createRecord(new HealthRecordRequest(
                    wangQiang.id(),
                    LocalDate.now().minusDays(4),
                    new BigDecimal("81.50"),
                    new BigDecimal("93.00"),
                    146,
                    92,
                    102,
                    new BigDecimal("7.20"),
                    new BigDecimal("10.20"),
                    new BigDecimal("36.8"),
                    new BigDecimal("96.0"),
                    new BigDecimal("6.50"),
                    new BigDecimal("5.8"),
                    20,
                    2800,
                    1400,
                    7,
                    5,
                    "晨起口干",
                    "间断服用保健品",
                    "近期出差较多"));

            healthRecordService.createRecord(new HealthRecordRequest(
                    wangQiang.id(),
                    LocalDate.now().minusDays(2),
                    new BigDecimal("82.00"),
                    new BigDecimal("94.00"),
                    152,
                    96,
                    108,
                    new BigDecimal("7.80"),
                    new BigDecimal("11.40"),
                    new BigDecimal("37.1"),
                    new BigDecimal("96.0"),
                    new BigDecimal("6.80"),
                    new BigDecimal("5.5"),
                    15,
                    2200,
                    1300,
                    9,
                    4,
                    "晨起口渴明显",
                    "护肝药物",
                    "近期饮食油腻，晨起口渴明显"));

            healthRecordService.createRecord(new HealthRecordRequest(
                    zhangMin.id(),
                    LocalDate.now().minusDays(3),
                    new BigDecimal("67.00"),
                    new BigDecimal("88.00"),
                    168,
                    102,
                    84,
                    new BigDecimal("6.90"),
                    new BigDecimal("9.80"),
                    new BigDecimal("36.8"),
                    new BigDecimal("94.0"),
                    new BigDecimal("6.60"),
                    new BigDecimal("6.8"),
                    35,
                    5200,
                    1600,
                    6,
                    6,
                    "餐后偶有头晕",
                    "按时服用降压药",
                    "餐后偶有头晕，建议持续观察"));

            healthRecordService.createRecord(new HealthRecordRequest(
                    zhangMin.id(),
                    LocalDate.now(),
                    new BigDecimal("66.00"),
                    new BigDecimal("87.50"),
                    181,
                    118,
                    88,
                    new BigDecimal("7.30"),
                    new BigDecimal("11.20"),
                    new BigDecimal("37.5"),
                    new BigDecimal("92.0"),
                    new BigDecimal("6.90"),
                    new BigDecimal("5.2"),
                    10,
                    1900,
                    1100,
                    7,
                    5,
                    "头晕、轻微气短",
                    "按时服用降压药",
                    "血压和血氧波动明显，需要重点关注"));
        };
    }
}
