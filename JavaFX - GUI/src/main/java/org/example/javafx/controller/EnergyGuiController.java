package org.example.javafx.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.DatePicker;

import javafx.scene.control.TextField;
import org.example.javafx.dto.EnergyDataEntity;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;


public class EnergyGuiController {

    @FXML private Label labelCommunityPool;
    @FXML private Label labelGridPortion;
    @FXML private Label labelCommunityProduced;
    @FXML private Label labelCommunityUsed;
    @FXML private Label labelGridUsed;
    @FXML private DatePicker dpStart;
    @FXML private DatePicker dpEnd;
    @FXML private TextField tfStart;
    @FXML private TextField tfEnd;
    @FXML private Label labelErrorMessage;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm[:ss]");

    // checken, welcher Port tatsächlich verwendet wird (8080 oder 8081)
    private final String BASE_URL = "http://localhost:8080/energy";

    @FXML
    public void onRefresh() {

    }

    @FXML
    public void onShowData() {
        LocalDateTime start;
        LocalDateTime end;

        try {
            start = parseDateTime(dpStart, tfStart, "Start");
            end = parseDateTime(dpEnd, tfEnd, "Ende");

        } catch (IllegalArgumentException exception) {
            System.err.println("Fehler bei der Datenabfrage: " + exception.getMessage());
            labelErrorMessage.setText("Fehler bei der Datenabfrage. " + exception.getMessage());
            labelCommunityProduced.setText("");
            labelCommunityUsed.setText("");
            labelGridUsed.setText("");
            return;
        }

        String dateStart = URLEncoder.encode(start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")), StandardCharsets.UTF_8);
        String dateEnd = URLEncoder.encode(end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")), StandardCharsets.UTF_8);

        String url = BASE_URL + "/historical?startDate=" + dateStart + "&endDate=" + dateEnd;

        /* falls die Verbindung zur DB aus irgendwelchen Gründen auch immer nicht funktionieren sollte,
        wird mit new Thread sichergestellt, dass die UI nicht einfriert */
        new Thread(() ->{
            try {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    throw new RuntimeException("HTTP-Fehler: " + response.statusCode());
                }

                String jsonResponse = response.body();

                // JSON antwort in eine EnergyDataEntity umwandeln um leichter mit den Rückgabewerten arbeiten zu können
                ObjectMapper mapper = new ObjectMapper();
                EnergyDataEntity responseData = mapper.readValue(jsonResponse, EnergyDataEntity.class);

                Platform.runLater(() -> {
                    labelCommunityProduced.setText(String.format("%.2f kWh", responseData.getCommunityProduced()));
                    labelCommunityUsed.setText(String.format("%.2f kWh", responseData.getCommunityUsed()));
                    labelGridUsed.setText(String.format("%.2f kWh", responseData.getGridUsed()));
                    labelErrorMessage.setText("");
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    labelErrorMessage.setText("Fehler beim Datenabruf.");
                    labelCommunityProduced.setText("");
                    labelCommunityUsed.setText("");
                    labelGridUsed.setText("");
                });
            }

        }).start();
    }

    private LocalDateTime parseDateTime(DatePicker dp, TextField tf, String label) {
        LocalDate date = dp.getValue();
        String timeText = tf.getText();

        if (date == null) {
            throw new IllegalArgumentException(label + "-Datum fehlt");
        }
        if (timeText == null || timeText.isBlank()) {
            throw new IllegalArgumentException(label + "-Datum fehlt");
        }

        LocalTime time;
        try {
            time = LocalTime.parse(timeText, TIME_FORMATTER);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException(label + "-Uhrzeit ungültig (HH:mm oder HH:mm:ss)");
        }

        return LocalDateTime.of(date, time);
    }
}