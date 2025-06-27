package org.example.energyuser.service;

import org.json.JSONObject;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class UserService {

    @Value("${user.queue.name}")
    private String queueName;

    private final RabbitTemplate rabbit;

    // Datumsformat erstellt für JSON message an die Queue
    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy'T'HH:mm:ss");

    public UserService(RabbitTemplate rabbit) {
        this.rabbit = rabbit;
    }

    // wird alle 5 Sekunden ausgeführt
    @Scheduled(fixedRate = 5000)
    public void runUserService() {
        String currentTime = LocalDateTime.now().format(dateFormat);

        JSONObject message = new JSONObject();

        message.put("type", "USER");
        message.put("association", "COMMUNITY");
        message.put("kWh", calculateUsage());
        message.put("datetime", currentTime);

        sendMessage(message.toString());

    }

    public static double calculateUsage() {
        double min = 0.01;
        double max = 0.7;
        int currentHour = LocalDateTime.now().getHour();

        if (currentHour >= 6 && currentHour <= 9 || currentHour >= 17 && currentHour <= 21) {
            min = 0.4;
        }

        double result = Math.random() * (max - min) + min;
        result = Math.round(result * 1000.0) / 1000.0;

        return result;
    }

    public void sendMessage(String message) {
        rabbit.convertAndSend(queueName, message);
        System.out.println("Outgoing User message: " + message);
    }
}
