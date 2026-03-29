package com.example.selfhealthcare.dto;

public record AuthSessionResponse(
        Long id,
        String username,
        String displayName,
        boolean authenticated) {
}
