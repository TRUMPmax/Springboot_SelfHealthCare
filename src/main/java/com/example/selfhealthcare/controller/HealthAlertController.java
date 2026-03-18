package com.example.selfhealthcare.controller;

import com.example.selfhealthcare.domain.AlertSeverity;
import com.example.selfhealthcare.domain.AlertStatus;
import com.example.selfhealthcare.dto.AlertStatusUpdateRequest;
import com.example.selfhealthcare.dto.HealthAlertResponse;
import com.example.selfhealthcare.service.HealthAlertService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/alerts")
public class HealthAlertController {

    private final HealthAlertService healthAlertService;

    public HealthAlertController(HealthAlertService healthAlertService) {
        this.healthAlertService = healthAlertService;
    }

    @GetMapping
    public List<HealthAlertResponse> listAlerts(
            @RequestParam(required = false) Long profileId,
            @RequestParam(required = false) AlertStatus status,
            @RequestParam(required = false) AlertSeverity severity) {
        return healthAlertService.listAlerts(profileId, status, severity);
    }

    @GetMapping("/{id}")
    public HealthAlertResponse getAlert(@PathVariable Long id) {
        return healthAlertService.getAlert(id);
    }

    @PutMapping("/{id}/status")
    public HealthAlertResponse updateStatus(
            @PathVariable Long id, @Valid @RequestBody AlertStatusUpdateRequest request) {
        return healthAlertService.updateStatus(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAlert(@PathVariable Long id) {
        healthAlertService.deleteAlert(id);
    }
}
