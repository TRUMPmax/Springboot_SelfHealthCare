package com.example.selfhealthcare.config;

import com.example.selfhealthcare.domain.AlcoholUseStatus;
import com.example.selfhealthcare.domain.BloodType;
import com.example.selfhealthcare.domain.Gender;
import com.example.selfhealthcare.domain.SmokingStatus;
import com.example.selfhealthcare.dto.HealthRecordRequest;
import com.example.selfhealthcare.dto.UserProfileRequest;
import com.example.selfhealthcare.dto.UserProfileResponse;
import com.example.selfhealthcare.repository.HealthRecordRepository;
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
            HealthRecordRepository healthRecordRepository,
            UserProfileService userProfileService,
            HealthRecordService healthRecordService) {
        return args -> {
            LocalDate today = LocalDate.now();

            UserProfileResponse liNa = ensureProfile(
                    userProfileRepository,
                    userProfileService,
                    new UserProfileRequest(
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
                            "每周快走 2-3 次，工作日久坐较多",
                            "缓解压力、改善睡眠、稳定血糖",
                            "李明",
                            "13912345678",
                            "近期加班较多，适合演示压力增大带来的趋势变化"));

            UserProfileResponse wangQiang = ensureProfile(
                    userProfileRepository,
                    userProfileService,
                    new UserProfileRequest(
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
                            "控制血糖、减重、稳定血压",
                            "王芳",
                            "13888886666",
                            "适合演示高血压、高血糖和生活方式风险"));

            UserProfileResponse zhangMin = ensureProfile(
                    userProfileRepository,
                    userProfileService,
                    new UserProfileRequest(
                            "张敏",
                            "母亲",
                            Gender.FEMALE,
                            61,
                            LocalDate.of(1965, 11, 22),
                            BloodType.B,
                            "13800003333",
                            "zhangmin@example.com",
                            "退休教师",
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
                            "适合演示服药后指标逐步改善的趋势"));

            UserProfileResponse chenChen = ensureProfile(
                    userProfileRepository,
                    userProfileService,
                    new UserProfileRequest(
                            "陈晨",
                            "配偶",
                            Gender.FEMALE,
                            31,
                            LocalDate.of(1994, 8, 4),
                            BloodType.AB,
                            "13800004444",
                            "chenchen@example.com",
                            "品牌策划",
                            new BigDecimal("168.00"),
                            new BigDecimal("62.00"),
                            SmokingStatus.NEVER,
                            AlcoholUseStatus.OCCASIONAL,
                            null,
                            null,
                            "花粉季节轻度过敏",
                            null,
                            null,
                            "每周瑜伽 2 次，周末慢跑",
                            "保持体脂和睡眠质量",
                            "李娜",
                            "13800001111",
                            "适合演示低风险、稳定型成员档案"));

            UserProfileResponse liLe = ensureProfile(
                    userProfileRepository,
                    userProfileService,
                    new UserProfileRequest(
                            "李乐",
                            "儿子",
                            Gender.MALE,
                            12,
                            LocalDate.of(2014, 6, 18),
                            BloodType.O,
                            null,
                            null,
                            "初中学生",
                            new BigDecimal("154.00"),
                            new BigDecimal("49.00"),
                            SmokingStatus.NEVER,
                            AlcoholUseStatus.NEVER,
                            "父亲有脂肪肝",
                            null,
                            null,
                            null,
                            null,
                            "课余打羽毛球，但最近运动减少",
                            "改善作息，减少久坐，提高活动量",
                            "李娜",
                            "13800001111",
                            "适合演示家庭成员共同管理场景"));

            seedRecordIfMissing(
                    healthRecordRepository,
                    healthRecordService,
                    new HealthRecordRequest(
                            liNa.id(),
                            today.minusDays(12),
                            new BigDecimal("56.20"),
                            new BigDecimal("70.00"),
                            116,
                            76,
                            72,
                            new BigDecimal("5.00"),
                            new BigDecimal("6.50"),
                            new BigDecimal("36.5"),
                            new BigDecimal("98.0"),
                            new BigDecimal("4.60"),
                            new BigDecimal("7.6"),
                            50,
                            8200,
                            1900,
                            3,
                            8,
                            null,
                            null,
                            "状态平稳，睡眠和运动都比较规律"));

            seedRecordIfMissing(
                    healthRecordRepository,
                    healthRecordService,
                    new HealthRecordRequest(
                            liNa.id(),
                            today.minusDays(7),
                            new BigDecimal("56.80"),
                            new BigDecimal("71.00"),
                            121,
                            79,
                            76,
                            new BigDecimal("5.40"),
                            new BigDecimal("7.10"),
                            new BigDecimal("36.6"),
                            new BigDecimal("98.0"),
                            new BigDecimal("4.80"),
                            new BigDecimal("6.8"),
                            40,
                            7200,
                            1800,
                            5,
                            7,
                            "偶有肩颈紧张",
                            null,
                            "项目进入赶工阶段，开始出现轻度疲劳"));

            seedRecordIfMissing(
                    healthRecordRepository,
                    healthRecordService,
                    new HealthRecordRequest(
                            liNa.id(),
                            today.minusDays(3),
                            new BigDecimal("57.50"),
                            new BigDecimal("72.50"),
                            132,
                            85,
                            84,
                            new BigDecimal("5.90"),
                            new BigDecimal("7.90"),
                            new BigDecimal("36.8"),
                            new BigDecimal("97.0"),
                            new BigDecimal("5.00"),
                            new BigDecimal("6.0"),
                            30,
                            5200,
                            1500,
                            7,
                            6,
                            "轻度乏力、入睡慢",
                            null,
                            "适合演示压力升高和睡眠变差的趋势"));

            seedRecordIfMissing(
                    healthRecordRepository,
                    healthRecordService,
                    new HealthRecordRequest(
                            liNa.id(),
                            today,
                            new BigDecimal("58.10"),
                            new BigDecimal("73.50"),
                            138,
                            88,
                            92,
                            new BigDecimal("6.40"),
                            new BigDecimal("8.60"),
                            new BigDecimal("36.9"),
                            new BigDecimal("97.0"),
                            new BigDecimal("5.40"),
                            new BigDecimal("5.7"),
                            20,
                            3200,
                            1200,
                            8,
                            5,
                            "乏力、浅睡眠",
                            null,
                            "最近连续加班，多个指标同步走高"));

            seedRecordIfMissing(
                    healthRecordRepository,
                    healthRecordService,
                    new HealthRecordRequest(
                            wangQiang.id(),
                            today.minusDays(11),
                            new BigDecimal("81.50"),
                            new BigDecimal("92.00"),
                            142,
                            90,
                            96,
                            new BigDecimal("6.80"),
                            new BigDecimal("9.80"),
                            new BigDecimal("36.7"),
                            new BigDecimal("96.0"),
                            new BigDecimal("6.20"),
                            new BigDecimal("6.2"),
                            30,
                            4200,
                            1700,
                            6,
                            6,
                            "晨起口干",
                            "偶尔服用护肝药",
                            "适合演示代谢异常的早期阶段"));

            seedRecordIfMissing(
                    healthRecordRepository,
                    healthRecordService,
                    new HealthRecordRequest(
                            wangQiang.id(),
                            today.minusDays(6),
                            new BigDecimal("82.20"),
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
                            "饭后困倦明显",
                            "护肝药物间断使用",
                            "出差增多，作息和饮食都不规律"));

            seedRecordIfMissing(
                    healthRecordRepository,
                    healthRecordService,
                    new HealthRecordRequest(
                            wangQiang.id(),
                            today.minusDays(2),
                            new BigDecimal("83.00"),
                            new BigDecimal("94.50"),
                            152,
                            96,
                            108,
                            new BigDecimal("7.80"),
                            new BigDecimal("11.40"),
                            new BigDecimal("37.1"),
                            new BigDecimal("95.0"),
                            new BigDecimal("6.80"),
                            new BigDecimal("5.5"),
                            15,
                            2200,
                            1300,
                            9,
                            4,
                            "晨起口渴明显",
                            "护肝药物",
                            "适合演示高风险预警较为集中的成员"));

            seedRecordIfMissing(
                    healthRecordRepository,
                    healthRecordService,
                    new HealthRecordRequest(
                            wangQiang.id(),
                            today,
                            new BigDecimal("83.50"),
                            new BigDecimal("95.00"),
                            158,
                            98,
                            110,
                            new BigDecimal("8.10"),
                            new BigDecimal("11.90"),
                            new BigDecimal("37.2"),
                            new BigDecimal("95.0"),
                            new BigDecimal("7.10"),
                            new BigDecimal("5.1"),
                            10,
                            1800,
                            1200,
                            8,
                            4,
                            "头胀、口干、容易疲劳",
                            "护肝药物",
                            "最近聚餐和熬夜较多，适合演示高危档案"));

            seedRecordIfMissing(
                    healthRecordRepository,
                    healthRecordService,
                    new HealthRecordRequest(
                            zhangMin.id(),
                            today.minusDays(10),
                            new BigDecimal("67.00"),
                            new BigDecimal("89.00"),
                            176,
                            108,
                            88,
                            new BigDecimal("7.40"),
                            new BigDecimal("11.20"),
                            new BigDecimal("37.2"),
                            new BigDecimal("92.0"),
                            new BigDecimal("6.90"),
                            new BigDecimal("5.5"),
                            10,
                            1800,
                            1100,
                            7,
                            5,
                            "头晕、偶有胸闷",
                            "按时服用降压药",
                            "作为改善前的基线数据"));

            seedRecordIfMissing(
                    healthRecordRepository,
                    healthRecordService,
                    new HealthRecordRequest(
                            zhangMin.id(),
                            today.minusDays(5),
                            new BigDecimal("66.80"),
                            new BigDecimal("88.50"),
                            168,
                            102,
                            84,
                            new BigDecimal("6.90"),
                            new BigDecimal("10.10"),
                            new BigDecimal("36.8"),
                            new BigDecimal("94.0"),
                            new BigDecimal("6.60"),
                            new BigDecimal("6.0"),
                            20,
                            3200,
                            1300,
                            6,
                            6,
                            "饭后偶有头晕",
                            "按时服用降压药",
                            "服药和散步后，部分指标开始改善"));

            seedRecordIfMissing(
                    healthRecordRepository,
                    healthRecordService,
                    new HealthRecordRequest(
                            zhangMin.id(),
                            today.minusDays(2),
                            new BigDecimal("66.50"),
                            new BigDecimal("88.00"),
                            160,
                            96,
                            80,
                            new BigDecimal("6.60"),
                            new BigDecimal("9.40"),
                            new BigDecimal("36.7"),
                            new BigDecimal("95.0"),
                            new BigDecimal("6.20"),
                            new BigDecimal("6.5"),
                            35,
                            5200,
                            1500,
                            5,
                            6,
                            "症状有所减轻",
                            "按时服用降压药",
                            "适合演示趋势改善和建议调整"));

            seedRecordIfMissing(
                    healthRecordRepository,
                    healthRecordService,
                    new HealthRecordRequest(
                            zhangMin.id(),
                            today,
                            new BigDecimal("66.10"),
                            new BigDecimal("87.50"),
                            154,
                            94,
                            78,
                            new BigDecimal("6.40"),
                            new BigDecimal("9.00"),
                            new BigDecimal("36.6"),
                            new BigDecimal("96.0"),
                            new BigDecimal("6.00"),
                            new BigDecimal("6.7"),
                            40,
                            6100,
                            1700,
                            4,
                            7,
                            "头晕明显减少",
                            "按时服用降压药",
                            "适合展示高风险向中风险缓慢回落"));

            seedRecordIfMissing(
                    healthRecordRepository,
                    healthRecordService,
                    new HealthRecordRequest(
                            chenChen.id(),
                            today.minusDays(9),
                            new BigDecimal("62.00"),
                            new BigDecimal("74.00"),
                            118,
                            76,
                            70,
                            new BigDecimal("4.90"),
                            new BigDecimal("6.40"),
                            new BigDecimal("36.5"),
                            new BigDecimal("99.0"),
                            new BigDecimal("4.40"),
                            new BigDecimal("7.3"),
                            45,
                            9000,
                            2000,
                            4,
                            8,
                            null,
                            null,
                            "适合演示低风险成员和稳定趋势"));

            seedRecordIfMissing(
                    healthRecordRepository,
                    healthRecordService,
                    new HealthRecordRequest(
                            chenChen.id(),
                            today.minusDays(4),
                            new BigDecimal("61.80"),
                            new BigDecimal("73.50"),
                            116,
                            74,
                            68,
                            new BigDecimal("4.80"),
                            new BigDecimal("6.20"),
                            new BigDecimal("36.5"),
                            new BigDecimal("99.0"),
                            new BigDecimal("4.30"),
                            new BigDecimal("7.6"),
                            50,
                            10200,
                            2100,
                            3,
                            8,
                            null,
                            null,
                            "近期作息规律，睡眠和运动都比较稳定"));

            seedRecordIfMissing(
                    healthRecordRepository,
                    healthRecordService,
                    new HealthRecordRequest(
                            chenChen.id(),
                            today,
                            new BigDecimal("61.60"),
                            new BigDecimal("73.00"),
                            114,
                            72,
                            69,
                            new BigDecimal("4.90"),
                            new BigDecimal("6.30"),
                            new BigDecimal("36.6"),
                            new BigDecimal("99.0"),
                            new BigDecimal("4.30"),
                            new BigDecimal("7.4"),
                            60,
                            11000,
                            2200,
                            3,
                            9,
                            "无明显不适",
                            null,
                            "适合展示低风险、建议较少的成员画像"));

            seedRecordIfMissing(
                    healthRecordRepository,
                    healthRecordService,
                    new HealthRecordRequest(
                            liLe.id(),
                            today.minusDays(8),
                            new BigDecimal("49.00"),
                            new BigDecimal("71.00"),
                            112,
                            68,
                            82,
                            new BigDecimal("4.80"),
                            new BigDecimal("6.50"),
                            new BigDecimal("36.6"),
                            new BigDecimal("99.0"),
                            new BigDecimal("4.50"),
                            new BigDecimal("8.2"),
                            60,
                            12000,
                            1700,
                            3,
                            9,
                            null,
                            null,
                            "适合演示家庭中青少年档案的录入"));

            seedRecordIfMissing(
                    healthRecordRepository,
                    healthRecordService,
                    new HealthRecordRequest(
                            liLe.id(),
                            today.minusDays(3),
                            new BigDecimal("49.50"),
                            new BigDecimal("72.00"),
                            116,
                            70,
                            84,
                            new BigDecimal("5.00"),
                            new BigDecimal("6.80"),
                            new BigDecimal("36.7"),
                            new BigDecimal("99.0"),
                            new BigDecimal("4.70"),
                            new BigDecimal("7.6"),
                            40,
                            7800,
                            1500,
                            4,
                            8,
                            "近视用眼后有眼疲劳",
                            null,
                            "最近作业增多，运动时长下降"));

            seedRecordIfMissing(
                    healthRecordRepository,
                    healthRecordService,
                    new HealthRecordRequest(
                            liLe.id(),
                            today,
                            new BigDecimal("50.20"),
                            new BigDecimal("73.00"),
                            118,
                            72,
                            86,
                            new BigDecimal("5.10"),
                            new BigDecimal("7.00"),
                            new BigDecimal("36.7"),
                            new BigDecimal("98.0"),
                            new BigDecimal("4.80"),
                            new BigDecimal("7.1"),
                            30,
                            5200,
                            1400,
                            5,
                            7,
                            "眼疲劳、久坐后肩背发紧",
                            null,
                            "适合演示趋势里运动减少、睡眠变短的家庭管理场景"));
        };
    }

    private UserProfileResponse ensureProfile(
            UserProfileRepository userProfileRepository,
            UserProfileService userProfileService,
            UserProfileRequest request) {
        return userProfileRepository.findByFullName(request.fullName())
                .map(UserProfileResponse::from)
                .orElseGet(() -> userProfileService.createProfile(request));
    }

    private void seedRecordIfMissing(
            HealthRecordRepository healthRecordRepository,
            HealthRecordService healthRecordService,
            HealthRecordRequest request) {
        if (healthRecordRepository.existsByProfileIdAndRecordDate(request.profileId(), request.recordDate())) {
            return;
        }
        healthRecordService.createRecord(request);
    }
}
