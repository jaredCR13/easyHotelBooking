package com.easyhotelbooking.hotelbookingsystem.controller.hotelregister;

import hotelbookingcommon.domain.Hotel;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.util.Map;
import java.util.stream.Collectors;

public class HotelConsultController {

    @FXML
    private Label numberHotel;

    @FXML
    private Label nameHotel;

    @FXML
    private Label locationHotel;

    @FXML
    private Label roomTypesSummary;


    public void setHotel(Hotel hotel) {
        numberHotel.setText(String.valueOf(hotel.getNumHotel()));
        nameHotel.setText(hotel.getHotelName());
        locationHotel.setText(hotel.getHotelLocation());

        // Contar habitaciones por tipo
        Map<String, Long> countByStyle = hotel.getRooms().stream()
                .collect(Collectors.groupingBy(room -> room.getStyle().name(), Collectors.counting()));

        // Crear resumen
        StringBuilder summary = new StringBuilder("Total rooms by type: \n");
        for (String style : new String[]{"STANDARD", "DELUXE", "SUITE", "FAMILY"}) {
            long count = countByStyle.getOrDefault(style, 0L);
            summary.append(style).append(": ").append(count).append("\n");
        }

        roomTypesSummary.setText(summary.toString());
    }

    @FXML
    void onClose() {
        Stage stage = (Stage) numberHotel.getScene().getWindow();
        stage.close();
    }
}
