package com.example.selfhealthcare.repository;

import com.example.selfhealthcare.domain.AlertSeverity;
import com.example.selfhealthcare.domain.AlertStatus;
import com.example.selfhealthcare.domain.HealthAlert;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HealthAlertRepository extends JpaRepository<HealthAlert, Long> {

    Page<HealthAlert> findByUserId(Long userId, Pageable pageable);

    Page<HealthAlert> findByUserIdAndStatus(Long userId, AlertStatus status, Pageable pageable);

    Page<HealthAlert> findByUserIdAndSeverity(Long userId, AlertSeverity severity, Pageable pageable);

    Page<HealthAlert> findByUserIdAndStatusAndSeverity(
            Long userId, AlertStatus status, AlertSeverity severity, Pageable pageable);

    Optional<HealthAlert> findByIdAndUserId(Long id, Long userId);

    long countByUserIdAndStatus(Long userId, AlertStatus status);

    List<HealthAlert> findTop6ByUserIdOrderByCreatedAtDesc(Long userId);

    List<HealthAlert> findTop10ByUserIdOrderByObservedDateDescCreatedAtDesc(Long userId);

    void deleteByHealthRecordId(Long healthRecordId);
}
