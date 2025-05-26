package com.easyhotelbooking.hotelbookingsystem.controller.hotelregister;

import hotelbookingcommon.domain.Hotel;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class HotelConsult {

    @FXML
    private Label numberHotel;

    @FXML
    private Label nameHotel;

    @FXML
    private Label locationHotel;


    public void setHotel(Hotel hotel) {
        numberHotel.setText(String.valueOf(hotel.getNumHotel()));
        nameHotel.setText(hotel.getHotelName());
        locationHotel.setText(hotel.getHotelLocation());
    }

    @FXML
    void onClose() {
        Stage stage = (Stage) numberHotel.getScene().getWindow();
        stage.close();
    }
}
