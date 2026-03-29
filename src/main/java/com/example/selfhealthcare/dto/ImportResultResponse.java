package com.example.selfhealthcare.dto;

import java.util.List;

public record ImportResultResponse(
        String fileName,
        String fileType,
        String extractionMethod,
        String extractedTextPreview,
        UserProfileResponse profile,
        HealthRecordResponse archivedRecord,
        List<String> matchedFields,
        List<String> warnings,
        String disclaimer) {
}
