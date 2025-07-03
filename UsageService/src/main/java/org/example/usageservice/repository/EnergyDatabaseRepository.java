package org.example.usageservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface EnergyDatabaseRepository extends JpaRepository<EnergyDataEntitiy, LocalDateTime> {
    EnergyDataEntitiy findByHour(LocalDateTime hour);
}
