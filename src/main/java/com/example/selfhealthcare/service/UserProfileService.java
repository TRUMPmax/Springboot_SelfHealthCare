package com.example.selfhealthcare.service;

import com.example.selfhealthcare.domain.AppUser;
import com.example.selfhealthcare.domain.UserProfile;
import com.example.selfhealthcare.dto.UserProfileRequest;
import com.example.selfhealthcare.dto.UserProfileResponse;
import com.example.selfhealthcare.exception.NotFoundException;
import com.example.selfhealthcare.repository.UserProfileRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final AuthService authService;

    public UserProfileService(UserProfileRepository userProfileRepository, AuthService authService) {
        this.userProfileRepository = userProfileRepository;
        this.authService = authService;
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentProfile() {
        return UserProfileResponse.from(getCurrentProfileEntity());
    }

    @Transactional(readOnly = true)
    public Optional<UserProfile> findCurrentProfileOptional() {
        AppUser currentUser = authService.requireAuthenticatedUser();
        return userProfileRepository.findByUserId(currentUser.getId());
    }

    @Transactional(readOnly = true)
    public Optional<UserProfile> findByUserId(Long userId) {
        return userProfileRepository.findByUserId(userId);
    }

    @Transactional
    public UserProfileResponse saveCurrentProfile(UserProfileRequest request) {
        AppUser currentUser = authService.requireAuthenticatedUser();
        UserProfile profile = userProfileRepository.findByUserId(currentUser.getId())
                .orElseGet(() -> {
                    UserProfile entity = new UserProfile();
                    entity.setUser(currentUser);
                    return entity;
                });
        apply(profile, request);
        return UserProfileResponse.from(userProfileRepository.save(profile));
    }

    @Transactional
    public UserProfile save(UserProfile profile) {
        return userProfileRepository.save(profile);
    }

    @Transactional
    public UserProfile createShellProfile(AppUser user) {
        return userProfileRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    UserProfile profile = new UserProfile();
                    profile.setUser(user);
                    profile.setFullName(user.getDisplayName());
                    return userProfileRepository.save(profile);
                });
    }

    @Transactional(readOnly = true)
    public UserProfile getCurrentProfileEntity() {
        AppUser currentUser = authService.requireAuthenticatedUser();
        return userProfileRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new NotFoundException("当前账号尚未创建个人档案"));
    }

    private void apply(UserProfile profile, UserProfileRequest request) {
        profile.setFullName(normalize(request.fullName()));
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
