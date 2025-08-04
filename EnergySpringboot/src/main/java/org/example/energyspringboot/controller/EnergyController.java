package org.example.energyspringboot.controller;

import org.example.energyspringboot.repository.CurrentPercentageEntity;
import org.example.energyspringboot.repository.CurrentPercentageRepository;
import org.example.energyspringboot.repository.EnergyDataEntity;
import org.example.energyspringboot.repository.EnergyDataRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@RestController
@RequestMapping("/energy")
public class EnergyController {

    private final CurrentPercentageRepository currentPercentageRepository;
    EnergyDataRepository energyDataRepository;

    public EnergyController(EnergyDataRepository energyDataRepository, CurrentPercentageRepository currentPercentageRepository) {
        this.energyDataRepository = energyDataRepository;
        this.currentPercentageRepository = currentPercentageRepository;
    }

    @GetMapping("/current")
    public CurrentPercentageEntity getCurrentData() {
        LocalDateTime hour = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS);

        CurrentPercentageEntity result = currentPercentageRepository.findByHour(hour);
        if (result == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Kein Eintrag für " + hour
            );
        }
        return result;

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
