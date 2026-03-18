package com.example.selfhealthcare.controller;

import com.example.selfhealthcare.dto.AiConsultationRequest;
import com.example.selfhealthcare.dto.AiConsultationResponse;
import com.example.selfhealthcare.dto.ProfileDetailResponse;
import com.example.selfhealthcare.dto.UserProfileRequest;
import com.example.selfhealthcare.dto.UserProfileResponse;
import com.example.selfhealthcare.service.DeepSeekConsultationService;
import com.example.selfhealthcare.service.ProfileInsightService;
import com.example.selfhealthcare.service.UserProfileService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profiles")
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final ProfileInsightService profileInsightService;
    private final DeepSeekConsultationService deepSeekConsultationService;

    public UserProfileController(
            UserProfileService userProfileService,
            ProfileInsightService profileInsightService,
            DeepSeekConsultationService deepSeekConsultationService) {
        this.userProfileService = userProfileService;
        this.profileInsightService = profileInsightService;
        this.deepSeekConsultationService = deepSeekConsultationService;
    }

    @GetMapping
    public List<UserProfileResponse> listProfiles() {
        return userProfileService.listProfiles();
    }

    @GetMapping("/{id}")
    public UserProfileResponse getProfile(@PathVariable Long id) {
        return userProfileService.getProfile(id);
    }

    @GetMapping("/{id}/detail")
    public ProfileDetailResponse getProfileDetail(@PathVariable Long id) {
        return profileInsightService.getProfileDetail(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserProfileResponse createProfile(@Valid @RequestBody UserProfileRequest request) {
        return userProfileService.createProfile(request);
    }

    @PutMapping("/{id}")
    public UserProfileResponse updateProfile(@PathVariable Long id, @Valid @RequestBody UserProfileRequest request) {
        return userProfileService.updateProfile(id, request);
    }

    @PostMapping("/{id}/ai-consultation")
    public AiConsultationResponse consultProfileWithAi(
            @PathVariable Long id, @Valid @RequestBody AiConsultationRequest request) {
        return deepSeekConsultationService.consult(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProfile(@PathVariable Long id) {
        userProfileService.deleteProfile(id);
    }
}
