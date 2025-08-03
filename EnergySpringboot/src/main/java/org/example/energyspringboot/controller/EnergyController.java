package org.example.energyspringboot.controller;

import org.example.energyspringboot.repository.EnergyDataEntity;
import org.example.energyspringboot.repository.EnergyDataRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@RestController
@RequestMapping("/energy")
public class EnergyController {

    EnergyDataRepository energyDataRepository;

    public EnergyController(EnergyDataRepository energyDataRepository) {
        this.energyDataRepository = energyDataRepository;
    }

    @GetMapping("/current")
    public EnergyDataEntity getCurrentData() {
        LocalDateTime dateTime = LocalDateTime.now();
        LocalDateTime hour = dateTime.truncatedTo(ChronoUnit.HOURS);

        return new EnergyDataEntity(hour,9,9,9);
    }

    @GetMapping("/historical")
    public EnergyDataEntity getHistoricalData(
            @RequestParam("startDate")
            @DateTimeFormat(pattern = "yyyy-mm-dd'T'HH:mm:ss") LocalDateTime startDate,

            @RequestParam("endDate")
            @DateTimeFormat(pattern = "yyyy-mm-dd'T'HH:mm:ss") LocalDateTime endDate) {

        // Wenn Zeitraum genau 1 Stunde → einzelne Stunde abfragen
        if (startDate.equals(endDate)) {
            EnergyDataEntity singleHour = energyDataRepository.findByHour(startDate);
            if (singleHour != null) {
                return singleHour;
            } else {
                // Leeres Objekt zurückgeben, falls Stunde nicht existiert
                return new EnergyDataEntity(startDate, 0, 0, 0);
            }
        }

        // Ansonsten summierte Daten liefern
        return energyDataRepository.findTotalBetween(startDate, endDate);
    }
}
