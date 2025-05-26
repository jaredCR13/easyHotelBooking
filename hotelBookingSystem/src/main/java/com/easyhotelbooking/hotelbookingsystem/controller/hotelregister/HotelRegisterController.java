package com.easyhotelbooking.hotelbookingsystem.controller.hotelregister;

import com.easyhotelbooking.hotelbookingsystem.controller.MainInterfaceController;
import hotelbookingcommon.domain.Hotel;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class HotelRegisterController {

    @FXML
    private TextField hotelNumberField;
    @FXML
    private TextField nameField;
    @FXML
    private TextField locationField;

    private MainInterfaceController mainController;

    public HotelRegisterController() {

    }

    public void setMainController(MainInterfaceController mainController) {
        this.mainController = mainController;
    }

    @FXML
    void onConfirm() {
        try {
            int number = Integer.parseInt(hotelNumberField.getText());
            String name = nameField.getText();
            String location = locationField.getText();

            if (name.isEmpty() || location.isEmpty()) return;

            Hotel hotel = new Hotel(number, name, location);
            mainController.registerHotel(hotel);

            // Cerrar ventana
            Stage stage = (Stage) hotelNumberField.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            // Mostrar alerta si es necesario
        }
    }

    @FXML
    void onCancel() {
        Stage stage = (Stage) hotelNumberField.getScene().getWindow();
        stage.close();
    }
}

