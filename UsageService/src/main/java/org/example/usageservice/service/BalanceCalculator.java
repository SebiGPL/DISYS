package org.example.usageservice.service;

import org.example.usageservice.repository.EnergyDataEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class BalanceCalculator {

    public EnergyDataEntity applyConsumption(EnergyDataEntity existing, LocalDateTime hour, double usedKWh) {
        EnergyDataEntity entry = existing;

        if (entry == null) {
            // erster Eintrag in dieser Stunde
            entry = new EnergyDataEntity();
            entry.setHour(hour);
            entry.setCommunityUsed(0);
            entry.setCommunityProduced(0);
            entry.setGridUsed(usedKWh);
            return entry;
        }

        // bereits vorhanden: Verbrauch addieren, gegenüber Produktion abgleichen
        double usedBefore = entry.getCommunityUsed();
        double produced  = entry.getCommunityProduced();
        double newUsed   = usedBefore + usedKWh;

        if (newUsed <= produced) {
            entry.setCommunityUsed(newUsed);
            // gridUsed bleibt unverändert
        } else {
            entry.setCommunityUsed(produced);
            double extra = newUsed - produced;
            entry.setGridUsed(entry.getGridUsed() + extra);
        }

        return entry;
    }

    public EnergyDataEntity applyProduction(EnergyDataEntity existing,
                                            LocalDateTime hour,
                                            double productionKWh) {
        EnergyDataEntity entry = existing;
        if (entry == null) {
            entry = new EnergyDataEntity();
            entry.setHour(hour);
            entry.setCommunityUsed(0);
            entry.setCommunityProduced(productionKWh);
            entry.setGridUsed(0);
            return entry;
        }

        double producedBefore = entry.getCommunityProduced();
        double used          = entry.getCommunityUsed();
        double newProduced   = producedBefore + productionKWh;

        entry.setCommunityProduced(newProduced);

        // Wenn schon Verbrauch > Produktion, mindere gridUsed entsprechend
        if (used > newProduced) {
            double deficit = used - newProduced;
            // gridUsed wurde ursprünglich um diesen Überschuss erhöht,
            // wir ziehen es nun wieder ab
            entry.setGridUsed(entry.getGridUsed() - deficit);
        }

        return entry;
    }
}
