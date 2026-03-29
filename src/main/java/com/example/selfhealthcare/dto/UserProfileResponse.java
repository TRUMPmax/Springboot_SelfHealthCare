package com.example.selfhealthcare.dto;

import com.example.selfhealthcare.domain.AlcoholUseStatus;
import com.example.selfhealthcare.domain.BloodType;
import com.example.selfhealthcare.domain.Gender;
import com.example.selfhealthcare.domain.SmokingStatus;
import com.example.selfhealthcare.domain.UserProfile;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Stream;

public record UserProfileResponse(
        Long id,
        String fullName,
        Gender gender,
        Integer age,
        LocalDate birthDate,
        BloodType bloodType,
        String phone,
        String email,
        String occupation,
        BigDecimal heightCm,
        BigDecimal weightKg,
        SmokingStatus smokingStatus,
        AlcoholUseStatus alcoholUseStatus,
        String familyHistory,
        String chronicDiseases,
        String allergies,
        String currentMedications,
        String surgeryHistory,
        String exerciseHabit,
        String careGoals,
        String emergencyContact,
        String emergencyContactPhone,
        String notes,
        int completionScore,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static UserProfileResponse from(UserProfile profile) {
        return new UserProfileResponse(
                profile.getId(),
                profile.getFullName(),
                profile.getGender(),
                profile.getAge(),
                profile.getBirthDate(),
                profile.getBloodType(),
                profile.getPhone(),
                profile.getEmail(),
                profile.getOccupation(),
                profile.getHeightCm(),
                profile.getWeightKg(),
                profile.getSmokingStatus(),
                profile.getAlcoholUseStatus(),
                profile.getFamilyHistory(),
                profile.getChronicDiseases(),
                profile.getAllergies(),
                profile.getCurrentMedications(),
                profile.getSurgeryHistory(),
                profile.getExerciseHabit(),
                profile.getCareGoals(),
                profile.getEmergencyContact(),
                profile.getEmergencyContactPhone(),
                profile.getNotes(),
                calculateCompletionScore(profile),
                profile.getCreatedAt(),
                profile.getUpdatedAt());
    }

    private static int calculateCompletionScore(UserProfile profile) {
        long completed = Stream.of(
                        profile.getFullName(),
                        profile.getGender(),
                        profile.getAge(),
                        profile.getBirthDate(),
                        profile.getBloodType(),
                        profile.getPhone(),
                        profile.getHeightCm(),
                        profile.getWeightKg(),
                        profile.getChronicDiseases(),
                        profile.getCurrentMedications())
                .filter(item -> item != null)
                .count();
        return (int) Math.round(completed * 100.0 / 10.0);
    }
}
