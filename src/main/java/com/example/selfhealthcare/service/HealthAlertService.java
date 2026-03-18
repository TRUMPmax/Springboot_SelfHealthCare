package com.example.selfhealthcare.service;

import com.example.selfhealthcare.domain.AlertSeverity;
import com.example.selfhealthcare.domain.AlertStatus;
import com.example.selfhealthcare.domain.HealthAlert;
import com.example.selfhealthcare.dto.AlertStatusUpdateRequest;
import com.example.selfhealthcare.dto.HealthAlertResponse;
import com.example.selfhealthcare.exception.NotFoundException;
import com.example.selfhealthcare.repository.HealthAlertRepository;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HealthAlertService {

    private final HealthAlertRepository healthAlertRepository;

    public HealthAlertService(HealthAlertRepository healthAlertRepository) {
        this.healthAlertRepository = healthAlertRepository;
    }

    @Transactional(readOnly = true)
    public List<HealthAlertResponse> listAlerts(Long profileId, AlertStatus status, AlertSeverity severity) {
        return healthAlertRepository.findAll(Sort.by(Sort.Order.desc("createdAt"))).stream()
                .filter(alert -> profileId == null || alert.getProfile().getId().equals(profileId))
                .filter(alert -> status == null || alert.getStatus() == status)
                .filter(alert -> severity == null || alert.getSeverity() == severity)
                .map(HealthAlertResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public HealthAlertResponse getAlert(Long id) {
        return HealthAlertResponse.from(findEntity(id));
    }

    @Transactional
    public HealthAlertResponse updateStatus(Long id, AlertStatusUpdateRequest request) {
        HealthAlert alert = findEntity(id);
        alert.setStatus(request.status());
        alert.setHandledNote(normalize(request.handledNote()));
        return HealthAlertResponse.from(healthAlertRepository.save(alert));
    }

    @Transactional
    public void deleteAlert(Long id) {
        HealthAlert alert = findEntity(id);
        healthAlertRepository.delete(alert);
    }

    @Transactional(readOnly = true)
    public HealthAlert findEntity(Long id) {
        return healthAlertRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("未找到编号为 " + id + " 的预警信息"));
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
