package com.example.selfhealthcare.service;

import com.example.selfhealthcare.domain.AlertSeverity;
import com.example.selfhealthcare.domain.AlertStatus;
import com.example.selfhealthcare.domain.AppUser;
import com.example.selfhealthcare.domain.HealthAlert;
import com.example.selfhealthcare.dto.AlertStatusUpdateRequest;
import com.example.selfhealthcare.dto.HealthAlertResponse;
import com.example.selfhealthcare.dto.PagedResponse;
import com.example.selfhealthcare.exception.NotFoundException;
import com.example.selfhealthcare.repository.HealthAlertRepository;
import com.example.selfhealthcare.util.PagingSupport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HealthAlertService {

    private final HealthAlertRepository healthAlertRepository;
    private final AuthService authService;

    public HealthAlertService(HealthAlertRepository healthAlertRepository, AuthService authService) {
        this.healthAlertRepository = healthAlertRepository;
        this.authService = authService;
    }

    @Transactional(readOnly = true)
    public PagedResponse<HealthAlertResponse> listAlerts(int page, int size, AlertStatus status, AlertSeverity severity) {
        AppUser currentUser = authService.requireAuthenticatedUser();
        int normalizedPage = PagingSupport.normalizePage(page);
        Pageable pageable = PageRequest.of(
                normalizedPage - 1,
                PagingSupport.normalizeSize(size),
                Sort.by(Sort.Order.desc("observedDate"), Sort.Order.desc("createdAt")));

        Page<HealthAlert> alerts;
        if (status != null && severity != null) {
            alerts = healthAlertRepository.findByUserIdAndStatusAndSeverity(
                    currentUser.getId(), status, severity, pageable);
        } else if (status != null) {
            alerts = healthAlertRepository.findByUserIdAndStatus(currentUser.getId(), status, pageable);
        } else if (severity != null) {
            alerts = healthAlertRepository.findByUserIdAndSeverity(currentUser.getId(), severity, pageable);
        } else {
            alerts = healthAlertRepository.findByUserId(currentUser.getId(), pageable);
        }

        return PagingSupport.toResponse(alerts, normalizedPage, HealthAlertResponse::from);
    }

    @Transactional(readOnly = true)
    public HealthAlertResponse getAlert(Long id) {
        return HealthAlertResponse.from(findOwnedEntity(id));
    }

    @Transactional
    public HealthAlertResponse updateStatus(Long id, AlertStatusUpdateRequest request) {
        HealthAlert alert = findOwnedEntity(id);
        alert.setStatus(request.status());
        alert.setHandledNote(normalize(request.handledNote()));
        return HealthAlertResponse.from(healthAlertRepository.save(alert));
    }

    @Transactional
    public void deleteAlert(Long id) {
        HealthAlert alert = findOwnedEntity(id);
        healthAlertRepository.delete(alert);
    }

    @Transactional(readOnly = true)
    public HealthAlert findOwnedEntity(Long id) {
        AppUser currentUser = authService.requireAuthenticatedUser();
        return healthAlertRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new NotFoundException("未找到对应的预警信息"));
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
