package com.example.selfhealthcare.controller;

import com.example.selfhealthcare.dto.AiConsultationRequest;
import com.example.selfhealthcare.dto.AiConsultationResponse;
import com.example.selfhealthcare.service.DeepSeekConsultationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiAnalysisController {

    private final DeepSeekConsultationService deepSeekConsultationService;

    public AiAnalysisController(DeepSeekConsultationService deepSeekConsultationService) {
        this.deepSeekConsultationService = deepSeekConsultationService;
    }

    @PostMapping("/analysis")
    public AiConsultationResponse analyze(@Valid @RequestBody AiConsultationRequest request) {
        return deepSeekConsultationService.consult(request);
    }
}
