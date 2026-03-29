package com.example.selfhealthcare.controller;

import com.example.selfhealthcare.dto.ProfileDetailResponse;
import com.example.selfhealthcare.dto.UserProfileRequest;
import com.example.selfhealthcare.dto.UserProfileResponse;
import com.example.selfhealthcare.service.ProfileInsightService;
import com.example.selfhealthcare.service.UserProfileService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final ProfileInsightService profileInsightService;

    public UserProfileController(
            UserProfileService userProfileService,
            ProfileInsightService profileInsightService) {
        this.userProfileService = userProfileService;
        this.profileInsightService = profileInsightService;
    }

    @GetMapping
    public UserProfileResponse getProfile() {
        return userProfileService.getCurrentProfile();
    }

    @GetMapping("/detail")
    public ProfileDetailResponse getProfileDetail() {
        return profileInsightService.getCurrentProfileDetail();
    }

    @PutMapping
    public UserProfileResponse saveProfile(@Valid @RequestBody UserProfileRequest request) {
        return userProfileService.saveCurrentProfile(request);
    }
}
