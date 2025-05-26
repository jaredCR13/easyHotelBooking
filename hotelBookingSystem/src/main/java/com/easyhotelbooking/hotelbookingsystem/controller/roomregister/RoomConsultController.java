package com.easyhotelbooking.hotelbookingsystem.controller.roomregister;

import hotelbookingcommon.domain.Room;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class RoomConsultController {

    @FXML
    private Label descriptionLabel;

    @FXML
    private Label hotelIdLabel;

    @FXML
    private Label priceLabel;

    @FXML
    private Label roomNumberLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Label styleLabel;

    public void setRoom(Room room){

        descriptionLabel.setText(String.valueOf(room.getDetailedDescription()));
        hotelIdLabel.setText(String.valueOf(room.getHotelId()));
        priceLabel.setText(String.valueOf(room.getRoomPrice()));
        roomNumberLabel.setText(String.valueOf(room.getRoomNumber()));
        statusLabel.setText(String.valueOf(room.getStatus()));
        styleLabel.setText(String.valueOf(room.getStyle()));

    }

    @FXML
    void onClose(ActionEvent event) {
        Stage stage = (Stage) roomNumberLabel.getScene().getWindow();
        stage.close();
    }

}
