package com.example.selfhealthcare.dto;

import java.time.LocalDateTime;

public record AiConsultationResponse(
        String model,
        String answer,
        String contextDigest,
        LocalDateTime generatedAt) {
}
