package com.example.selfhealthcare.repository;

import com.example.selfhealthcare.domain.HealthRecord;
import com.example.selfhealthcare.domain.RiskLevel;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HealthRecordRepository extends JpaRepository<HealthRecord, Long> {

    Page<HealthRecord> findByUserId(Long userId, Pageable pageable);

    Page<HealthRecord> findByUserIdAndRiskLevel(Long userId, RiskLevel riskLevel, Pageable pageable);

    Optional<HealthRecord> findByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);

    long countByUserIdAndRiskLevelIn(Long userId, Collection<RiskLevel> riskLevels);

    List<HealthRecord> findTop5ByUserIdOrderByRecordDateDescCreatedAtDesc(Long userId);

    List<HealthRecord> findTop10ByUserIdOrderByRecordDateDescCreatedAtDesc(Long userId);

    List<HealthRecord> findTop12ByUserIdOrderByRecordDateDescCreatedAtDesc(Long userId);

    List<HealthRecord> findByUserIdOrderByRecordDateAscCreatedAtAsc(Long userId);
}
