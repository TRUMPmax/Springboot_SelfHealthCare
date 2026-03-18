package com.example.selfhealthcare.dto;

import com.example.selfhealthcare.domain.AlcoholUseStatus;
import com.example.selfhealthcare.domain.BloodType;
import com.example.selfhealthcare.domain.Gender;
import com.example.selfhealthcare.domain.SmokingStatus;
import com.example.selfhealthcare.domain.UserProfile;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record UserProfileResponse(
        Long id,
        String fullName,
        String relationToUser,
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
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static UserProfileResponse from(UserProfile profile) {
        return new UserProfileResponse(
                profile.getId(),
                profile.getFullName(),
                profile.getRelationToUser(),
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
                profile.getCreatedAt(),
                profile.getUpdatedAt());
    }
}
