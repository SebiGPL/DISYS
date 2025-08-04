package org.example.currentpercentageservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.currentpercentageservice.repository.CurrentPercentageEntity;
import org.example.currentpercentageservice.repository.CurrentPercentageRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class CurrentPercentageService {

    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy'T'HH:mm:ss");
    private final ObjectMapper mapper;

    private final CurrentPercentageRepository currentPercentageRepository;

    public CurrentPercentageService(ObjectMapper mapper, CurrentPercentageRepository currentPercentageRepository) {
        this.mapper = mapper;
        this.currentPercentageRepository = currentPercentageRepository;
    }

    @RabbitListener(queues = "current_percentage_mq")
    public void saveCurrentPercentageToDatabase(String jsonMessage) {
        System.out.println("empfangene Nachricht" + jsonMessage);

        try {
            JsonNode node = mapper.readTree(jsonMessage);

            String hourStr = node.get("hour").asText();
            LocalDateTime hour = LocalDateTime.parse(hourStr, dateFormat);

            double communityUsed = node.get("communityUsed").asDouble();
            double gridUsed = node.get("gridUsed").asDouble();
            double totalConsumption = communityUsed + gridUsed;

            double gridPortion;
            double communityPool;

            if (totalConsumption > 0) {
                communityPool = (communityUsed / totalConsumption) * 100;
                gridPortion = (gridUsed / totalConsumption) * 100;
            } else {
                communityPool = 0.0;
                gridPortion = 0.0;
            }

            System.out.printf("Lokal genutzt: %.2f%%, Netzanteil: %.2f%%%n", communityPool, gridPortion);

            CurrentPercentageEntity percentageEntity = new CurrentPercentageEntity();
            percentageEntity.setHour(hour);
            percentageEntity.setCommunityPool(communityPool);
            percentageEntity.setGridPortion(gridPortion);

            currentPercentageRepository.save(percentageEntity);
            System.out.println(percentageEntity + " Gespeichert");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            System.err.println("Fehler beim Parsen der JSON Nachricht");
        }


    }
}
