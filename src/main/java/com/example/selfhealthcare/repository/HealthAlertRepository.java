package com.example.selfhealthcare.repository;

import com.example.selfhealthcare.domain.AlertStatus;
import com.example.selfhealthcare.domain.HealthAlert;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HealthAlertRepository extends JpaRepository<HealthAlert, Long> {

    long countByStatus(AlertStatus status);

    List<HealthAlert> findTop6ByOrderByCreatedAtDesc();

    List<HealthAlert> findTop8ByProfileIdOrderByObservedDateDescCreatedAtDesc(Long profileId);

    void deleteByHealthRecordId(Long healthRecordId);

    void deleteByProfileId(Long profileId);
}
