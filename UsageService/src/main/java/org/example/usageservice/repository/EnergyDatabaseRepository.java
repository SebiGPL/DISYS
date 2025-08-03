package org.example.usageservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface EnergyDatabaseRepository extends JpaRepository<EnergyDataEntity, LocalDateTime> {
    EnergyDataEntity findByHour(LocalDateTime hour);
}
