package com.example.selfhealthcare.service;

import com.example.selfhealthcare.domain.AlertStatus;
import com.example.selfhealthcare.domain.HealthAlert;
import com.example.selfhealthcare.domain.HealthRecord;
import com.example.selfhealthcare.domain.RiskLevel;
import com.example.selfhealthcare.domain.UserProfile;
import com.example.selfhealthcare.dto.HealthRecordRequest;
import com.example.selfhealthcare.dto.HealthRecordResponse;
import com.example.selfhealthcare.exception.NotFoundException;
import com.example.selfhealthcare.repository.HealthAlertRepository;
import com.example.selfhealthcare.repository.HealthRecordRepository;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HealthRecordService {

    private final HealthRecordRepository healthRecordRepository;
    private final HealthAlertRepository healthAlertRepository;
    private final UserProfileService userProfileService;
    private final RiskAssessmentService riskAssessmentService;

    public HealthRecordService(
            HealthRecordRepository healthRecordRepository,
            HealthAlertRepository healthAlertRepository,
            UserProfileService userProfileService,
            RiskAssessmentService riskAssessmentService) {
        this.healthRecordRepository = healthRecordRepository;
        this.healthAlertRepository = healthAlertRepository;
        this.userProfileService = userProfileService;
        this.riskAssessmentService = riskAssessmentService;
    }

    @Transactional(readOnly = true)
    public List<HealthRecordResponse> listRecords(Long profileId, RiskLevel riskLevel) {
        return healthRecordRepository
                .findAll(Sort.by(Sort.Order.desc("recordDate"), Sort.Order.desc("createdAt")))
                .stream()
                .filter(record -> profileId == null || record.getProfile().getId().equals(profileId))
                .filter(record -> riskLevel == null || record.getRiskLevel() == riskLevel)
                .map(HealthRecordResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public HealthRecordResponse getRecord(Long id) {
        return HealthRecordResponse.from(findEntity(id));
    }

    @Transactional
    public HealthRecordResponse createRecord(HealthRecordRequest request) {
        HealthRecord record = new HealthRecord();
        return saveRecord(record, request);
    }

    @Transactional
    public HealthRecordResponse updateRecord(Long id, HealthRecordRequest request) {
        HealthRecord record = findEntity(id);
        return saveRecord(record, request);
    }

    @Transactional
    public void deleteRecord(Long id) {
        HealthRecord record = findEntity(id);
        healthAlertRepository.deleteByHealthRecordId(record.getId());
        healthRecordRepository.delete(record);
    }

    @Transactional(readOnly = true)
    public HealthRecord findEntity(Long id) {
        return healthRecordRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("未找到编号为 " + id + " 的健康记录"));
    }

    private HealthRecordResponse saveRecord(HealthRecord record, HealthRecordRequest request) {
        UserProfile profile = userProfileService.findEntity(request.profileId());
        apply(record, profile, request);

        RiskAssessmentService.AssessmentResult assessmentResult = riskAssessmentService.assess(profile, record);
        record.setBmi(assessmentResult.bmi());
        record.setRiskScore(assessmentResult.riskScore());
        record.setRiskLevel(assessmentResult.riskLevel());
        record.setSummary(assessmentResult.summary());

        HealthRecord savedRecord = healthRecordRepository.save(record);
        refreshAlerts(savedRecord, assessmentResult);

        return HealthRecordResponse.from(savedRecord);
    }

    private void refreshAlerts(HealthRecord record, RiskAssessmentService.AssessmentResult assessmentResult) {
        healthAlertRepository.deleteByHealthRecordId(record.getId());
        List<HealthAlert> alerts = assessmentResult.alerts().stream()
                .map(alertDraft -> {
                    HealthAlert alert = new HealthAlert();
                    alert.setProfile(record.getProfile());
                    alert.setHealthRecord(record);
                    alert.setObservedDate(record.getRecordDate());
                    alert.setCategory(alertDraft.category());
                    alert.setTitle(alertDraft.title());
                    alert.setIndicator(alertDraft.indicator());
                    alert.setSuggestion(alertDraft.suggestion());
                    alert.setSeverity(alertDraft.severity());
                    alert.setStatus(AlertStatus.PENDING);
                    return alert;
                })
                .toList();
        if (!alerts.isEmpty()) {
            healthAlertRepository.saveAll(alerts);
        }
    }

    private void apply(HealthRecord record, UserProfile profile, HealthRecordRequest request) {
        record.setProfile(profile);
        record.setRecordDate(request.recordDate());
        record.setWeightKg(request.weightKg());
        record.setWaistCircumferenceCm(request.waistCircumferenceCm());
        record.setSystolicPressure(request.systolicPressure());
        record.setDiastolicPressure(request.diastolicPressure());
        record.setHeartRate(request.heartRate());
        record.setFastingBloodSugar(request.fastingBloodSugar());
        record.setPostprandialBloodSugar(request.postprandialBloodSugar());
        record.setBodyTemperature(request.bodyTemperature());
        record.setBloodOxygen(request.bloodOxygen());
        record.setCholesterolTotal(request.cholesterolTotal());
        record.setSleepHours(request.sleepHours());
        record.setExerciseMinutes(request.exerciseMinutes());
        record.setStepsCount(request.stepsCount());
        record.setWaterIntakeMl(request.waterIntakeMl());
        record.setStressLevel(request.stressLevel());
        record.setMoodScore(request.moodScore());
        record.setSymptoms(normalize(request.symptoms()));
        record.setMedicationTaken(normalize(request.medicationTaken()));
        record.setNotes(normalize(request.notes()));
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
