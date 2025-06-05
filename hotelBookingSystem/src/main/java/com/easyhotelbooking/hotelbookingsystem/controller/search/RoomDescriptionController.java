package com.easyhotelbooking.hotelbookingsystem.controller.search;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label; // <<-- IMPORTACIÓN CORRECTA
import javafx.stage.Stage;
import javafx.event.ActionEvent;

public class RoomDescriptionController { // Mantenemos el nombre original para evitar confusiones

    @FXML
    private Label descriptionLabel; // Esta es la referencia al Label del FXML
    @FXML
    private Label roomNumberLabel;
    // Método para establecer el texto de la descripción
    public void setDescriptionText(String text) {

        if (descriptionLabel != null) {
            descriptionLabel.setText(text);
        } else {

            System.err.println("Error: descriptionLabel no está inicializado (FXML no cargado o fx:id incorrecto).");
        }
    }

    // Método para cerrar la ventana (se mantiene igual)
    @FXML
    private void onClose(ActionEvent event) {
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.close();
    }

    public void setRoomNumber(int roomNumber) {
        if (roomNumberLabel!=null){
            roomNumberLabel.setText(String.valueOf(roomNumber));
        }else {
            System.out.println("Error: roomNumberLabel no está inicializado (FXML no cargado o fx:id incorrecto).");
        }
    }
}
