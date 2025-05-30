package com.easyhotelbooking.hotelbookingsystem.controller.hotelregister;

import com.easyhotelbooking.hotelbookingsystem.controller.MainInterfaceController;
import hotelbookingcommon.domain.Hotel;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class HotelModifyController {
    @FXML
    private TextField numberHotel;

    @FXML
    private TextField nameHotel;

    @FXML
    private TextField locationHotel;


    private MainInterfaceController mainController;
    private Hotel hotelOriginal;

    public void setMainController(MainInterfaceController controller) {
        this.mainController = controller;
    }

    public void setHotel(Hotel hotel) {
        this.hotelOriginal = hotel;
        numberHotel.setText(String.valueOf(hotel.getNumHotel()));
        nameHotel.setText(hotel.getHotelName());
        locationHotel.setText(hotel.getHotelLocation());
        numberHotel.setEditable(false);
    }

    @FXML
    void onSave() {
        int number = Integer.parseInt(numberHotel.getText());
        String name = nameHotel.getText();
        String location = locationHotel.getText();

        Hotel updated = new Hotel(number, name, location);
        mainController.updateHotel(updated);
        closeWindow();
    }

    @FXML
    void onCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) numberHotel.getScene().getWindow();
        stage.close();
    }
}
