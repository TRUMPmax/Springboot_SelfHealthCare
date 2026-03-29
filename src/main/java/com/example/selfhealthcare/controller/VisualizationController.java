package com.example.selfhealthcare.controller;

import com.example.selfhealthcare.dto.VisualizationResponse;
import com.example.selfhealthcare.service.VisualizationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/visualization")
public class VisualizationController {

    private final VisualizationService visualizationService;

    public VisualizationController(VisualizationService visualizationService) {
        this.visualizationService = visualizationService;
    }

    @GetMapping
    public VisualizationResponse getVisualization() {
        return visualizationService.getVisualization();
    }
}
