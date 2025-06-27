package org.example.energyproducer.service;

import org.json.JSONObject;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class ProducerService {

    @Value("${producer.queue.name}")
    private String queueName;

    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy'T'HH:mm:ss");

    private final RabbitTemplate rabbit;

    private final WeatherService weatherService;

    private static final double max_uv = 11.0;

    public ProducerService(RabbitTemplate rabbit, WeatherService weatherService) {
        this.rabbit = rabbit;
        this.weatherService = weatherService;
    }

    @Scheduled(fixedRate = 10000)
    public void runProducerService() {
        // genaue Berechnung mit UV-Index und Bewölkung mithilfe der API weatherapi einfügen
        // uv und cloud (cloud cover in Prozent) maybe?
        // API URL ist in application.properties
        // example response: {"current":{"feelslike_c":20.2,"feelslike_f":68.4,"wind_degree":309,"windchill_f":63,"windchill_c":17.2,"last_updated_epoch":1751060700,"temp_c":20.2,"temp_f":68.4,"cloud":75,"wind_kph":15.1,"wind_mph":9.4,"humidity":94,"dewpoint_f":58.6,"uv":0,"last_updated":"2025-06-27 23:45","heatindex_f":63,"dewpoint_c":14.8,"is_day":0,"precip_in":0,"heatindex_c":17.2,"wind_dir":"NW","gust_mph":15.8,"pressure_in":30.18,"gust_kph":25.5,"precip_mm":0,"condition":{"code":1189,"icon":"//cdn.weatherapi.com/weather/64x64/night/302.png","text":"mäßiger Regenfall"},"vis_km":10,"pressure_mb":1022,"vis_miles":6},"location":{"localtime":"2025-06-27 23:56","country":"Austria","localtime_epoch":1751061413,"name":"Vienna","lon":16.3667,"region":"Wien","lat":48.2,"tz_id":"Europe/Vienna"}}

        String currentTime = LocalDateTime.now().format(dateFormat);

        JSONObject message = new JSONObject();

        message.put("type", "PRODUCER");
        message.put("association", "COMMUNITY");
        message.put("kWh", calculateProduction());
        message.put("datetime", currentTime);

        sendMessage(message.toString());
    }

    public double calculateProduction() {
        int cloudCover = weatherService.getLastCloudCover();
        double uvIndex = weatherService.getLastUvIndex();

        double irradFactor = (uvIndex / max_uv) * (1 - cloudCover / 100.0);

        double baseKwh = calculateRandomKwh();
        return baseKwh * irradFactor;
    }

    public double calculateRandomKwh() {
        double min = 0.5;
        double max = 1.0;

        double result = Math.random() * (max - min) + min;
        result = Math.round(result * 1000.0) / 1000.0;

        return result;
    }

    public void sendMessage(String message) {
        rabbit.convertAndSend(queueName, message);
        System.out.println("Outgoing Producer message: " + message);
    }

}
