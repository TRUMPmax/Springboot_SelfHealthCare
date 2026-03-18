package com.example.selfhealthcare.dto;

import jakarta.validation.constraints.Size;

public record AiConsultationRequest(
        Long focusRecordId,
        @Size(max = 1000, message = "咨询问题长度不能超过1000个字符")
        String question) {
}
