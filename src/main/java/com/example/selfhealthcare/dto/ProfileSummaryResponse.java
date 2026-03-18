package com.example.selfhealthcare.dto;

import com.example.selfhealthcare.domain.Gender;
import com.example.selfhealthcare.domain.UserProfile;

public record ProfileSummaryResponse(
        Long id,
        String fullName,
        String relationToUser,
        Gender gender,
        Integer age) {

    public static ProfileSummaryResponse from(UserProfile profile) {
        return new ProfileSummaryResponse(
                profile.getId(),
                profile.getFullName(),
                profile.getRelationToUser(),
                profile.getGender(),
                profile.getAge());
    }
}
