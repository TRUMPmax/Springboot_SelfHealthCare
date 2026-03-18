package com.example.selfhealthcare.service;

import com.example.selfhealthcare.domain.UserProfile;
import com.example.selfhealthcare.dto.UserProfileRequest;
import com.example.selfhealthcare.dto.UserProfileResponse;
import com.example.selfhealthcare.exception.NotFoundException;
import com.example.selfhealthcare.repository.HealthAlertRepository;
import com.example.selfhealthcare.repository.HealthRecordRepository;
import com.example.selfhealthcare.repository.UserProfileRepository;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final HealthRecordRepository healthRecordRepository;
    private final HealthAlertRepository healthAlertRepository;

    public UserProfileService(
            UserProfileRepository userProfileRepository,
            HealthRecordRepository healthRecordRepository,
            HealthAlertRepository healthAlertRepository) {
        this.userProfileRepository = userProfileRepository;
        this.healthRecordRepository = healthRecordRepository;
        this.healthAlertRepository = healthAlertRepository;
    }

    @Transactional(readOnly = true)
    public List<UserProfileResponse> listProfiles() {
        return userProfileRepository.findAll(Sort.by(Sort.Order.desc("updatedAt"))).stream()
                .map(UserProfileResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long id) {
        return UserProfileResponse.from(findEntity(id));
    }

    @Transactional
    public UserProfileResponse createProfile(UserProfileRequest request) {
        UserProfile profile = new UserProfile();
        apply(profile, request);
        return UserProfileResponse.from(userProfileRepository.save(profile));
    }

    @Transactional
    public UserProfileResponse updateProfile(Long id, UserProfileRequest request) {
        UserProfile profile = findEntity(id);
        apply(profile, request);
        return UserProfileResponse.from(userProfileRepository.save(profile));
    }

    @Transactional
    public void deleteProfile(Long id) {
        UserProfile profile = findEntity(id);
        healthAlertRepository.deleteByProfileId(profile.getId());
        healthRecordRepository.deleteByProfileId(profile.getId());
        userProfileRepository.delete(profile);
    }

    @Transactional(readOnly = true)
    public UserProfile findEntity(Long id) {
        return userProfileRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("未找到编号为 " + id + " 的健康档案"));
    }

    private void apply(UserProfile profile, UserProfileRequest request) {
        profile.setFullName(normalize(request.fullName()));
        profile.setRelationToUser(normalize(request.relationToUser()));
        profile.setGender(request.gender());
        profile.setAge(request.age());
        profile.setBirthDate(request.birthDate());
        profile.setBloodType(request.bloodType());
        profile.setPhone(normalize(request.phone()));
        profile.setEmail(normalize(request.email()));
        profile.setOccupation(normalize(request.occupation()));
        profile.setHeightCm(request.heightCm());
        profile.setWeightKg(request.weightKg());
        profile.setSmokingStatus(request.smokingStatus());
        profile.setAlcoholUseStatus(request.alcoholUseStatus());
        profile.setFamilyHistory(normalize(request.familyHistory()));
        profile.setChronicDiseases(normalize(request.chronicDiseases()));
        profile.setAllergies(normalize(request.allergies()));
        profile.setCurrentMedications(normalize(request.currentMedications()));
        profile.setSurgeryHistory(normalize(request.surgeryHistory()));
        profile.setExerciseHabit(normalize(request.exerciseHabit()));
        profile.setCareGoals(normalize(request.careGoals()));
        profile.setEmergencyContact(normalize(request.emergencyContact()));
        profile.setEmergencyContactPhone(normalize(request.emergencyContactPhone()));
        profile.setNotes(normalize(request.notes()));
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
