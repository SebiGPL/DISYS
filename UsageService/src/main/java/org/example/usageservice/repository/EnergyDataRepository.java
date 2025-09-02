package org.example.usageservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface EnergyDataRepository extends JpaRepository<EnergyDataEntity, LocalDateTime> {
    EnergyDataEntity findByHour(LocalDateTime hour);
}
