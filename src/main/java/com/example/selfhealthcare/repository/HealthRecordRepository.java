package com.example.selfhealthcare.repository;

import com.example.selfhealthcare.domain.HealthRecord;
import com.example.selfhealthcare.domain.RiskLevel;
import java.util.Collection;
import java.util.List;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HealthRecordRepository extends JpaRepository<HealthRecord, Long> {

    long countByRiskLevelIn(Collection<RiskLevel> riskLevels);

    List<HealthRecord> findTop5ByOrderByRecordDateDescCreatedAtDesc();

    List<HealthRecord> findTop12ByProfileIdOrderByRecordDateDescCreatedAtDesc(Long profileId);

    List<HealthRecord> findTop8ByProfileIdOrderByRecordDateDescCreatedAtDesc(Long profileId);

    boolean existsByProfileIdAndRecordDate(Long profileId, LocalDate recordDate);

    void deleteByProfileId(Long profileId);
}
