package org.example.usageservice.service;

import org.example.usageservice.repository.EnergyDataEntity;
import org.example.usageservice.repository.EnergyDataRepository;
import org.json.JSONObject;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Service
public class UsageServiceUser {

    private final EnergyDataRepository energyDataRepository;

    @Value("${user.queue.name}")
    private String queueName;

    private final BalanceCalculator calculator;

    // weiß noch nicht, ob ich das wirklich brauche
    private final RabbitTemplate rabbit;

    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy'T'HH:mm:ss");

    public UsageServiceUser(EnergyDataRepository energyDataRepository, BalanceCalculator calculator, RabbitTemplate rabbit) {
        this.energyDataRepository = energyDataRepository;
        this.calculator = calculator;
        this.rabbit = rabbit;
    }

    @RabbitListener(queues = "user_mq")
    public void saveEntryToDatabase(String jsonMessage) {
        // Nachricht empfangen und kWh und Zeit extrahieren:
        System.out.println("Nachricht empfangen: " + jsonMessage);

        JSONObject receivedMessage = new JSONObject(jsonMessage);

        double kWh = receivedMessage.getDouble("kWh");
        System.out.println("extrahierte kWh: " + kWh);

        LocalDateTime receivedDate = LocalDateTime.parse(receivedMessage.getString("datetime"), dateFormat);
        LocalDateTime hour = receivedDate.truncatedTo(ChronoUnit.HOURS);
        System.out.println("gesendete Stunde: " + hour);

        // die kWh in die Datenbank speichern

        EnergyDataEntity existingEntry = energyDataRepository.findByHour(hour);
        System.out.println("Bestehender Datenbankeintrag: " + existingEntry);

        EnergyDataEntity updatedEntry = calculator.applyConsumption(existingEntry, hour, kWh);
        System.out.println("aktualisierter Datenbankeintrag: " + updatedEntry);

        energyDataRepository.save(updatedEntry);

        JSONObject updateMessage = new JSONObject();

        updateMessage.put("hour", hour.format(dateFormat));
        updateMessage.put("communityProduced",updatedEntry.getCommunityProduced());
        updateMessage.put("communityUsed", updatedEntry.getCommunityUsed());
        updateMessage.put("gridUsed", updatedEntry.getGridUsed());

        rabbit.convertAndSend(queueName, updateMessage.toString());
        System.out.println("Nachricht an percentage_mq gesendet:" + updateMessage);
    }

}
