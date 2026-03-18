package com.example.selfhealthcare.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "health_record")
public class HealthRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_id", nullable = false)
    private UserProfile profile;

    @Column(nullable = false)
    private LocalDate recordDate;

    @Column(precision = 6, scale = 2)
    private BigDecimal weightKg;

    @Column(precision = 6, scale = 2)
    private BigDecimal waistCircumferenceCm;

    private Integer systolicPressure;

    private Integer diastolicPressure;

    private Integer heartRate;

    @Column(precision = 6, scale = 2)
    private BigDecimal fastingBloodSugar;

    @Column(precision = 6, scale = 2)
    private BigDecimal postprandialBloodSugar;

    @Column(precision = 4, scale = 1)
    private BigDecimal bodyTemperature;

    @Column(precision = 5, scale = 2)
    private BigDecimal bloodOxygen;

    @Column(precision = 6, scale = 2)
    private BigDecimal cholesterolTotal;

    @Column(precision = 5, scale = 2)
    private BigDecimal bmi;

    @Column(precision = 4, scale = 1)
    private BigDecimal sleepHours;

    private Integer exerciseMinutes;

    private Integer stepsCount;

    private Integer waterIntakeMl;

    private Integer stressLevel;

    private Integer moodScore;

    @Column(length = 500)
    private String symptoms;

    @Column(length = 500)
    private String medicationTaken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RiskLevel riskLevel = RiskLevel.LOW;

    @Column(nullable = false)
    private Integer riskScore = 0;

    @Column(length = 500)
    private String summary;

    @Column(length = 1000)
    private String notes;

    public UserProfile getProfile() {
        return profile;
    }

    public void setProfile(UserProfile profile) {
        this.profile = profile;
    }

    public LocalDate getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(LocalDate recordDate) {
        this.recordDate = recordDate;
    }

    public BigDecimal getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(BigDecimal weightKg) {
        this.weightKg = weightKg;
    }

    public BigDecimal getWaistCircumferenceCm() {
        return waistCircumferenceCm;
    }

    public void setWaistCircumferenceCm(BigDecimal waistCircumferenceCm) {
        this.waistCircumferenceCm = waistCircumferenceCm;
    }

    public Integer getSystolicPressure() {
        return systolicPressure;
    }

    public void setSystolicPressure(Integer systolicPressure) {
        this.systolicPressure = systolicPressure;
    }

    public Integer getDiastolicPressure() {
        return diastolicPressure;
    }

    public void setDiastolicPressure(Integer diastolicPressure) {
        this.diastolicPressure = diastolicPressure;
    }

    public Integer getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(Integer heartRate) {
        this.heartRate = heartRate;
    }

    public BigDecimal getFastingBloodSugar() {
        return fastingBloodSugar;
    }

    public void setFastingBloodSugar(BigDecimal fastingBloodSugar) {
        this.fastingBloodSugar = fastingBloodSugar;
    }

    public BigDecimal getPostprandialBloodSugar() {
        return postprandialBloodSugar;
    }

    public void setPostprandialBloodSugar(BigDecimal postprandialBloodSugar) {
        this.postprandialBloodSugar = postprandialBloodSugar;
    }

    public BigDecimal getBodyTemperature() {
        return bodyTemperature;
    }

    public void setBodyTemperature(BigDecimal bodyTemperature) {
        this.bodyTemperature = bodyTemperature;
    }

    public BigDecimal getBloodOxygen() {
        return bloodOxygen;
    }

    public void setBloodOxygen(BigDecimal bloodOxygen) {
        this.bloodOxygen = bloodOxygen;
    }

    public BigDecimal getCholesterolTotal() {
        return cholesterolTotal;
    }

    public void setCholesterolTotal(BigDecimal cholesterolTotal) {
        this.cholesterolTotal = cholesterolTotal;
    }

    public BigDecimal getBmi() {
        return bmi;
    }

    public void setBmi(BigDecimal bmi) {
        this.bmi = bmi;
    }

    public BigDecimal getSleepHours() {
        return sleepHours;
    }

    public void setSleepHours(BigDecimal sleepHours) {
        this.sleepHours = sleepHours;
    }

    public Integer getExerciseMinutes() {
        return exerciseMinutes;
    }

    public void setExerciseMinutes(Integer exerciseMinutes) {
        this.exerciseMinutes = exerciseMinutes;
    }

    public Integer getStepsCount() {
        return stepsCount;
    }

    public void setStepsCount(Integer stepsCount) {
        this.stepsCount = stepsCount;
    }

    public Integer getWaterIntakeMl() {
        return waterIntakeMl;
    }

    public void setWaterIntakeMl(Integer waterIntakeMl) {
        this.waterIntakeMl = waterIntakeMl;
    }

    public Integer getStressLevel() {
        return stressLevel;
    }

    public void setStressLevel(Integer stressLevel) {
        this.stressLevel = stressLevel;
    }

    public Integer getMoodScore() {
        return moodScore;
    }

    public void setMoodScore(Integer moodScore) {
        this.moodScore = moodScore;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }

    public String getMedicationTaken() {
        return medicationTaken;
    }

    public void setMedicationTaken(String medicationTaken) {
        this.medicationTaken = medicationTaken;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }

    public Integer getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Integer riskScore) {
        this.riskScore = riskScore;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
