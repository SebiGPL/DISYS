package org.example.energyproducer.service;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

@Service
public class WeatherService {

    @Value("${weather.api.url}")
    private String baseURL;

    @Value("${weather.api.key}")
    private String apiKey;

    @Value("${weather.api.city}")
    private String city;

    private double lastUvIndex;
    private int lastCloudCover;
    private boolean dataLoaded = false;

    private final RestTemplate restTemplate = new RestTemplate();

    @Scheduled(fixedRate = 15, timeUnit = TimeUnit.MINUTES)
    public void getCurrentWeather() {
        // siehe https://www.weatherapi.com/docs/ für api call Dokumentation
        String queryUrl = baseURL +
                "?key=" + apiKey +
                "&q=" + city +
                "&lang=de";

        String jsonText = restTemplate.getForObject(queryUrl, String.class);

        JSONObject currentWeather = new JSONObject(jsonText)
                .getJSONObject("current");

        this.lastUvIndex = currentWeather.getDouble("uv");
        this.lastCloudCover = currentWeather.getInt("cloud");
        dataLoaded = true;

        System.out.println(currentWeather);
        System.out.println(lastUvIndex);
        System.out.println(lastCloudCover);
    }

    public double getLastUvIndex() {
        ensureDataAvailable();
        return lastUvIndex;
    }

    public int getLastCloudCover() {
        ensureDataAvailable();
        return lastCloudCover;
    }

    public void ensureDataAvailable() {
        if (!dataLoaded) {
            getCurrentWeather();
        }
    }

}
