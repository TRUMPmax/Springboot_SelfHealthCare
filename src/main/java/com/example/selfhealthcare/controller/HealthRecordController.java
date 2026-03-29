package com.example.selfhealthcare.controller;

import com.example.selfhealthcare.domain.RiskLevel;
import com.example.selfhealthcare.dto.HealthRecordRequest;
import com.example.selfhealthcare.dto.HealthRecordResponse;
import com.example.selfhealthcare.dto.PagedResponse;
import com.example.selfhealthcare.service.HealthRecordService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/records")
public class HealthRecordController {

    private final HealthRecordService healthRecordService;

    public HealthRecordController(HealthRecordService healthRecordService) {
        this.healthRecordService = healthRecordService;
    }

    @GetMapping
    public PagedResponse<HealthRecordResponse> listRecords(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) RiskLevel riskLevel) {
        return healthRecordService.listRecords(page, size, riskLevel);
    }

    @GetMapping("/{id}")
    public HealthRecordResponse getRecord(@PathVariable Long id) {
        return healthRecordService.getRecord(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HealthRecordResponse createRecord(@Valid @RequestBody HealthRecordRequest request) {
        return healthRecordService.createRecord(request);
    }

    @PutMapping("/{id}")
    public HealthRecordResponse updateRecord(@PathVariable Long id, @Valid @RequestBody HealthRecordRequest request) {
        return healthRecordService.updateRecord(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRecord(@PathVariable Long id) {
        healthRecordService.deleteRecord(id);
    }
}
