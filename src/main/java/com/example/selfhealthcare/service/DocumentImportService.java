package com.example.selfhealthcare.service;

import com.example.selfhealthcare.domain.AlcoholUseStatus;
import com.example.selfhealthcare.domain.AppUser;
import com.example.selfhealthcare.domain.BloodType;
import com.example.selfhealthcare.domain.Gender;
import com.example.selfhealthcare.domain.SmokingStatus;
import com.example.selfhealthcare.domain.UserProfile;
import com.example.selfhealthcare.dto.HealthRecordRequest;
import com.example.selfhealthcare.dto.HealthRecordResponse;
import com.example.selfhealthcare.dto.ImportResultResponse;
import com.example.selfhealthcare.dto.UserProfileResponse;
import com.example.selfhealthcare.exception.ImportProcessingException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentImportService {

    private static final String DISCLAIMER = "OCR/PDF 识别结果已自动入档，请在页面核对关键信息后再作为正式健康资料使用。";
    private static final Pattern WHITESPACE = Pattern.compile("[\\t\\u00A0]+");

    private final AuthService authService;
    private final UserProfileService userProfileService;
    private final HealthRecordService healthRecordService;

    public DocumentImportService(
            AuthService authService,
            UserProfileService userProfileService,
            HealthRecordService healthRecordService) {
        this.authService = authService;
        this.userProfileService = userProfileService;
        this.healthRecordService = healthRecordService;
    }

    @Transactional
    public ImportResultResponse importDocument(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ImportProcessingException("上传文件不能为空");
        }

        AppUser currentUser = authService.requireAuthenticatedUser();
        ExtractionResult extraction = extractText(file);
        ParsedImportData parsed = parseDocument(extraction.text());
        parsed.warnings.addAll(extraction.warnings());

        if (!parsed.hasAnyData()) {
            throw new ImportProcessingException("未识别到可入档的健康信息，请更换更清晰的图片或可复制文本的 PDF");
        }

        UserProfile profile = userProfileService.findByUserId(currentUser.getId())
                .orElseGet(() -> userProfileService.createShellProfile(currentUser));
        mergeProfile(profile, currentUser, parsed);
        UserProfile savedProfile = userProfileService.save(profile);

        HealthRecordResponse archivedRecord = null;
        if (parsed.hasRecordData()) {
            archivedRecord = healthRecordService.createImportedRecord(currentUser, toHealthRecordRequest(file, parsed));
        } else {
            parsed.warnings.add("本次导入未解析出可入档的健康指标，因此没有生成新的健康记录。");
        }

        return new ImportResultResponse(
                file.getOriginalFilename(),
                detectFileType(file),
                extraction.method(),
                preview(extraction.text()),
                UserProfileResponse.from(savedProfile),
                archivedRecord,
                parsed.matchedFields,
                parsed.warnings.stream().distinct().toList(),
                DISCLAIMER);
    }

    private ExtractionResult extractText(MultipartFile file) {
        String fileName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase(Locale.ROOT);
        String fileType = detectFileType(file);
        try {
            if ("pdf".equals(fileType) || fileName.endsWith(".pdf")) {
                try (PDDocument document = Loader.loadPDF(file.getBytes())) {
                    String text = new PDFTextStripper().getText(document);
                    return new ExtractionResult(normalizeText(text), "pdf_text", List.of());
                }
            }
            if (isImageType(fileType, fileName)) {
                return extractImageText(file);
            }
            String plainText = normalizeText(new String(file.getBytes(), StandardCharsets.UTF_8));
            return new ExtractionResult(plainText, "plain_text", List.of("当前文件按文本内容解析。"));
        } catch (IOException exception) {
            throw new ImportProcessingException("读取上传文件失败，请重试", exception);
        }
    }

    private ExtractionResult extractImageText(MultipartFile file) throws IOException {
        Path tempFile = Files.createTempFile("health-import-", getSafeSuffix(file.getOriginalFilename()));
        Files.write(tempFile, file.getBytes());

        try {
            Process process = new ProcessBuilder("tesseract", tempFile.toString(), "stdout", "-l", "chi_sim+eng")
                    .redirectErrorStream(true)
                    .start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append('\n');
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new ImportProcessingException("图片 OCR 识别失败，请确认本机已安装 Tesseract OCR 并具备中文语言包");
            }

            return new ExtractionResult(normalizeText(output.toString()), "image_ocr_tesseract", List.of());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ImportProcessingException("图片 OCR 识别被中断，请重试", exception);
        } catch (IOException exception) {
            throw new ImportProcessingException("图片 OCR 依赖未就绪，请先在本机安装 Tesseract OCR 后再上传图片", exception);
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    private ParsedImportData parseDocument(String text) {
        ParsedImportData data = new ParsedImportData();
        data.fullName = matchText(text, "(?im)(?:姓名|Name)\\s*[:：]\\s*([^\\r\\n]+)");
        addMatchedField(data, data.fullName, "姓名");

        data.gender = parseGender(matchText(text, "(?im)(?:性别|Gender)\\s*[:：]\\s*([^\\r\\n]+)"));
        addMatchedField(data, data.gender, "性别");

        data.age = matchInteger(text, "(?im)(?:年龄|Age)\\s*[:：]\\s*(\\d{1,3})");
        addMatchedField(data, data.age, "年龄");

        data.birthDate = matchDate(text, "(?im)(?:出生日期|Birth(?:day| Date)?)\\s*[:：]\\s*([0-9./\\-]{8,12})");
        addMatchedField(data, data.birthDate, "出生日期");

        data.bloodType = parseBloodType(matchText(text, "(?im)(?:血型|Blood Type)\\s*[:：]\\s*([^\\r\\n]+)"));
        addMatchedField(data, data.bloodType, "血型");

        data.heightCm = matchDecimal(text, "(?im)(?:身高|Height)\\s*[:：]?\\s*([0-9]+(?:\\.[0-9]+)?)\\s*(?:cm|厘米)?");
        addMatchedField(data, data.heightCm, "身高");

        data.weightKg = matchDecimal(text, "(?im)(?:体重|Weight)\\s*[:：]?\\s*([0-9]+(?:\\.[0-9]+)?)\\s*(?:kg|千克)?");
        addMatchedField(data, data.weightKg, "体重");

        data.recordDate = firstNonNull(
                matchDate(text, "(?im)(?:检查日期|记录日期|体检日期|采集日期)\\s*[:：]\\s*([0-9./\\-]{8,12})"),
                matchDate(text, "(?im)(?:Date)\\s*[:：]\\s*([0-9./\\-]{8,12})"),
                LocalDate.now());
        addMatchedField(data, data.recordDate, "记录日期");

        Integer systolic = matchInteger(text, "(?im)(?:血压|Blood Pressure)\\s*[:：]?\\s*(\\d{2,3})\\s*[/／]\\s*(\\d{2,3})", 1);
        Integer diastolic = matchInteger(text, "(?im)(?:血压|Blood Pressure)\\s*[:：]?\\s*(\\d{2,3})\\s*[/／]\\s*(\\d{2,3})", 2);
        data.systolicPressure = systolic;
        data.diastolicPressure = diastolic;
        if (systolic != null || diastolic != null) {
            data.matchedFields.add("血压");
        }

        data.heartRate = matchInteger(text, "(?im)(?:心率|Heart Rate)\\s*[:：]?\\s*(\\d{2,3})");
        addMatchedField(data, data.heartRate, "心率");

        data.fastingBloodSugar = matchDecimal(text, "(?im)(?:空腹血糖|Fasting(?: Blood)? Glucose)\\s*[:：]?\\s*([0-9]+(?:\\.[0-9]+)?)");
        addMatchedField(data, data.fastingBloodSugar, "空腹血糖");

        data.postprandialBloodSugar = matchDecimal(
                text,
                "(?im)(?:餐后血糖|Post(?:prandial|[- ]Meal)(?: Blood)? Glucose)\\s*[:：]?\\s*([0-9]+(?:\\.[0-9]+)?)");
        addMatchedField(data, data.postprandialBloodSugar, "餐后血糖");

        data.bodyTemperature = matchDecimal(text, "(?im)(?:体温|Temperature)\\s*[:：]?\\s*([0-9]+(?:\\.[0-9]+)?)");
        addMatchedField(data, data.bodyTemperature, "体温");

        data.bloodOxygen = matchDecimal(text, "(?im)(?:血氧|Blood Oxygen|SpO2)\\s*[:：]?\\s*([0-9]+(?:\\.[0-9]+)?)");
        addMatchedField(data, data.bloodOxygen, "血氧");

        data.cholesterolTotal = matchDecimal(text, "(?im)(?:总胆固醇|Cholesterol)\\s*[:：]?\\s*([0-9]+(?:\\.[0-9]+)?)");
        addMatchedField(data, data.cholesterolTotal, "总胆固醇");

        data.sleepHours = matchDecimal(text, "(?im)(?:睡眠(?:时长)?|Sleep)\\s*[:：]?\\s*([0-9]+(?:\\.[0-9]+)?)");
        addMatchedField(data, data.sleepHours, "睡眠时长");

        data.exerciseMinutes = matchInteger(text, "(?im)(?:运动(?:时长)?|Exercise)\\s*[:：]?\\s*(\\d{1,4})");
        addMatchedField(data, data.exerciseMinutes, "运动时长");

        data.stepsCount = matchInteger(text, "(?im)(?:步数|Steps)\\s*[:：]?\\s*(\\d{1,6})");
        addMatchedField(data, data.stepsCount, "步数");

        data.waterIntakeMl = matchInteger(text, "(?im)(?:饮水量|Water(?: Intake)?)\\s*[:：]?\\s*(\\d{1,5})");
        addMatchedField(data, data.waterIntakeMl, "饮水量");

        data.stressLevel = matchInteger(text, "(?im)(?:压力(?:等级)?|Stress)\\s*[:：]?\\s*(\\d{1,2})");
        addMatchedField(data, data.stressLevel, "压力等级");

        data.moodScore = matchInteger(text, "(?im)(?:情绪(?:评分)?|Mood)\\s*[:：]?\\s*(\\d{1,2})");
        addMatchedField(data, data.moodScore, "情绪评分");

        data.familyHistory = matchText(text, "(?im)(?:家族病史|Family History)\\s*[:：]\\s*([^\\r\\n]+)");
        addMatchedField(data, data.familyHistory, "家族病史");

        data.chronicDiseases = matchText(text, "(?im)(?:慢性病史|Chronic Diseases?)\\s*[:：]\\s*([^\\r\\n]+)");
        addMatchedField(data, data.chronicDiseases, "慢性病史");

        data.allergies = matchText(text, "(?im)(?:过敏信息|Allerg(?:y|ies))\\s*[:：]\\s*([^\\r\\n]+)");
        addMatchedField(data, data.allergies, "过敏信息");

        data.currentMedications = matchText(text, "(?im)(?:当前用药|Medication(?:s)?|Current Medications?)\\s*[:：]\\s*([^\\r\\n]+)");
        addMatchedField(data, data.currentMedications, "当前用药");

        data.symptoms = matchText(text, "(?im)(?:症状|Symptoms?)\\s*[:：]\\s*([^\\r\\n]+)");
        addMatchedField(data, data.symptoms, "症状");

        data.notes = matchText(text, "(?im)(?:备注|Notes?)\\s*[:：]\\s*([^\\r\\n]+)");
        addMatchedField(data, data.notes, "备注");

        return data;
    }

    private void mergeProfile(UserProfile profile, AppUser currentUser, ParsedImportData parsed) {
        profile.setUser(currentUser);
        profile.setFullName(firstNonBlank(parsed.fullName, profile.getFullName(), currentUser.getDisplayName()));
        profile.setGender(firstNonNull(parsed.gender, profile.getGender()));
        profile.setAge(firstNonNull(parsed.age, profile.getAge()));
        profile.setBirthDate(firstNonNull(parsed.birthDate, profile.getBirthDate()));
        profile.setBloodType(firstNonNull(parsed.bloodType, profile.getBloodType()));
        profile.setHeightCm(firstNonNull(parsed.heightCm, profile.getHeightCm()));
        profile.setWeightKg(firstNonNull(parsed.weightKg, profile.getWeightKg()));
        profile.setFamilyHistory(firstNonBlank(parsed.familyHistory, profile.getFamilyHistory()));
        profile.setChronicDiseases(firstNonBlank(parsed.chronicDiseases, profile.getChronicDiseases()));
        profile.setAllergies(firstNonBlank(parsed.allergies, profile.getAllergies()));
        profile.setCurrentMedications(firstNonBlank(parsed.currentMedications, profile.getCurrentMedications()));
    }

    private HealthRecordRequest toHealthRecordRequest(MultipartFile file, ParsedImportData parsed) {
        String importNote = "导入来源：" + Objects.requireNonNullElse(file.getOriginalFilename(), "未知文件");
        String mergedNotes = firstNonBlank(parsed.notes, importNote);
        if (parsed.notes != null && !parsed.notes.equals(importNote)) {
            mergedNotes = parsed.notes + "；" + importNote;
        }

        return new HealthRecordRequest(
                parsed.recordDate,
                parsed.weightKg,
                parsed.waistCircumferenceCm,
                parsed.systolicPressure,
                parsed.diastolicPressure,
                parsed.heartRate,
                parsed.fastingBloodSugar,
                parsed.postprandialBloodSugar,
                parsed.bodyTemperature,
                parsed.bloodOxygen,
                parsed.cholesterolTotal,
                parsed.sleepHours,
                parsed.exerciseMinutes,
                parsed.stepsCount,
                parsed.waterIntakeMl,
                parsed.stressLevel,
                parsed.moodScore,
                parsed.symptoms,
                parsed.currentMedications,
                mergedNotes);
    }

    private String detectFileType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null) {
            return "unknown";
        }
        if (contentType.contains("pdf")) {
            return "pdf";
        }
        if (contentType.startsWith("image/")) {
            return "image";
        }
        if (contentType.startsWith("text/")) {
            return "text";
        }
        return contentType;
    }

    private boolean isImageType(String fileType, String fileName) {
        return "image".equals(fileType)
                || fileName.endsWith(".png")
                || fileName.endsWith(".jpg")
                || fileName.endsWith(".jpeg")
                || fileName.endsWith(".bmp");
    }

    private String getSafeSuffix(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return ".tmp";
        }
        return fileName.substring(fileName.lastIndexOf('.'));
    }

    private String normalizeText(String text) {
        if (text == null) {
            return "";
        }
        return WHITESPACE.matcher(text.replace('\u3000', ' ')).replaceAll(" ").trim();
    }

    private String preview(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        return text.length() > 1000 ? text.substring(0, 1000) + "..." : text;
    }

    private String matchText(String text, String regex) {
        Matcher matcher = Pattern.compile(regex).matcher(text);
        if (!matcher.find()) {
            return null;
        }
        String value = matcher.group(1).trim();
        return value.isEmpty() ? null : value;
    }

    private BigDecimal matchDecimal(String text, String regex) {
        String value = matchText(text, regex);
        if (value == null) {
            return null;
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private Integer matchInteger(String text, String regex) {
        return matchInteger(text, regex, 1);
    }

    private Integer matchInteger(String text, String regex, int groupIndex) {
        Matcher matcher = Pattern.compile(regex).matcher(text);
        if (!matcher.find()) {
            return null;
        }
        try {
            return Integer.parseInt(matcher.group(groupIndex));
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private LocalDate matchDate(String text, String regex) {
        String value = matchText(text, regex);
        if (value == null) {
            return null;
        }
        for (DateTimeFormatter formatter : List.of(
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("yyyy/M/d"),
                DateTimeFormatter.ofPattern("yyyy.M.d"))) {
            try {
                return LocalDate.parse(value, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }

    private Gender parseGender(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (normalized.contains("男") || normalized.contains("male")) {
            return Gender.MALE;
        }
        if (normalized.contains("女") || normalized.contains("female")) {
            return Gender.FEMALE;
        }
        return Gender.OTHER;
    }

    private BloodType parseBloodType(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT).replace("型", "");
        return switch (normalized) {
            case "A" -> BloodType.A;
            case "B" -> BloodType.B;
            case "AB" -> BloodType.AB;
            case "O" -> BloodType.O;
            default -> null;
        };
    }

    @SafeVarargs
    private <T> T firstNonNull(T... values) {
        for (T value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private void addMatchedField(ParsedImportData data, Object value, String fieldName) {
        if (value != null) {
            data.matchedFields.add(fieldName);
        }
    }

    private record ExtractionResult(String text, String method, List<String> warnings) {
    }

    private static final class ParsedImportData {
        private String fullName;
        private Gender gender;
        private Integer age;
        private LocalDate birthDate;
        private BloodType bloodType;
        private BigDecimal heightCm;
        private BigDecimal weightKg;
        private LocalDate recordDate;
        private BigDecimal waistCircumferenceCm;
        private Integer systolicPressure;
        private Integer diastolicPressure;
        private Integer heartRate;
        private BigDecimal fastingBloodSugar;
        private BigDecimal postprandialBloodSugar;
        private BigDecimal bodyTemperature;
        private BigDecimal bloodOxygen;
        private BigDecimal cholesterolTotal;
        private BigDecimal sleepHours;
        private Integer exerciseMinutes;
        private Integer stepsCount;
        private Integer waterIntakeMl;
        private Integer stressLevel;
        private Integer moodScore;
        private String familyHistory;
        private String chronicDiseases;
        private String allergies;
        private String currentMedications;
        private String symptoms;
        private String notes;
        private final List<String> matchedFields = new ArrayList<>();
        private final List<String> warnings = new ArrayList<>();

        private boolean hasAnyData() {
            return !matchedFields.isEmpty();
        }

        private boolean hasRecordData() {
            return StreamSupport.hasAny(
                    weightKg,
                    waistCircumferenceCm,
                    systolicPressure,
                    diastolicPressure,
                    heartRate,
                    fastingBloodSugar,
                    postprandialBloodSugar,
                    bodyTemperature,
                    bloodOxygen,
                    cholesterolTotal,
                    sleepHours,
                    exerciseMinutes,
                    stepsCount,
                    waterIntakeMl,
                    stressLevel,
                    moodScore,
                    symptoms,
                    currentMedications);
        }
    }

    private static final class StreamSupport {
        private static boolean hasAny(Object... values) {
            for (Object value : values) {
                if (value != null) {
                    return true;
                }
            }
            return false;
        }
    }
}
