package org.example.energyspringboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface EnergyDataRepository extends JpaRepository<EnergyDataEntity, Long> {
        // Einzelne Stunde abfragen
    EnergyDataEntity findByHour(LocalDateTime hour);

    // Summen über Zeitraum berechnen
    @Query("""
    SELECT new org.example.energyspringboot.repository.EnergyDataEntity(
        NULL,
        SUM(e.communityProduced),
        SUM(e.communityUsed),
        SUM(e.gridUsed)
    )
    FROM EnergyDataEntity e
    WHERE e.hour >= :start AND e.hour <= :end
    """)
    EnergyDataEntity findTotalBetween(LocalDateTime start, LocalDateTime end);
}
