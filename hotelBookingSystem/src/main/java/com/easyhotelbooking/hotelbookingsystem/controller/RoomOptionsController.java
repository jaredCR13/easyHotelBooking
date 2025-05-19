package com.easyhotelbooking.hotelbookingsystem.controller;

import com.easyhotelbooking.hotelbookingsystem.socket.ClientConnectionManager;
import com.easyhotelbooking.hotelbookingsystem.util.Utility;
import hotelbookingcommon.domain.Response;
import hotelbookingcommon.domain.Room;
import hotelbookingcommon.domain.RoomStatus;
import hotelbookingcommon.domain.RoomStyle;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;

public class RoomOptionsController {

    @FXML
    private BorderPane bp;

    @FXML
    private Button consultButton;

    @FXML
    private Button registerButton;

    @FXML
    private Button modifyButton;

    @FXML
    private Button removeButton;

    @FXML
    private Button goBack;

    @FXML
    private TextField roomNumberTf;

    @FXML
    private TextField priceTf;

    @FXML
    private TextField descriptionTf;
    @FXML private ComboBox<RoomStatus> statusCombo;
    @FXML private ComboBox<RoomStyle> styleCombo;



    private static final Logger logger = LogManager.getLogger(RoomOptionsController.class);

    // Referencia al controlador principal
    private MainInterfaceController mainController;

    public void setMainController(MainInterfaceController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        // Puedes cargar cosas aquí si lo deseas

        statusCombo.getItems().addAll(RoomStatus.values());
        styleCombo.getItems().addAll(RoomStyle.values());
    }

    @FXML
    void goBackOnAction() {
        Utility.loadFullView("maininterface.fxml", goBack);
    }

    @FXML
    void registerRoomOnAction() {
        try {
            String numberStr = roomNumberTf.getText();
            String priceStr = priceTf.getText();
            String description=descriptionTf.getText();

            if (numberStr.isEmpty() || priceStr.isEmpty() ) {
                mostrarAlerta("Error", "Por favor, complete todos los campos.");
                return;
            }

            int number = Integer.parseInt(numberStr);
            double price = Double.parseDouble(priceStr);

            RoomStatus selectedStatus = statusCombo.getValue();
            RoomStyle selectedStyle = styleCombo.getValue();

            Room room = new Room(
                    number,
                    price,
                    description,
                    selectedStatus,
                    selectedStyle,
                    Collections.emptyList()
            );

            mainController.registerRoom(room);


        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Número de habitación o precio inválido.");
        } catch (Exception e) {
            logger.error("Error al registrar habitación: {}", e.getMessage());
            mostrarAlerta("Error", "Error al registrar habitación: " + e.getMessage());
        }
    }

    @FXML
    void consultRoomOnAction() {
        try {
            String numberStr = roomNumberTf.getText();
            if (numberStr.isEmpty()) {
                mostrarAlerta("Error", "Ingrese el número de habitación a consultar.");
                return;
            }

            int number = Integer.parseInt(numberStr);
            mainController.consultRoom(number);

        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Número de habitación inválido.");
        } catch (Exception e) {
            logger.error("Error al consultar habitación: {}", e.getMessage());
            mostrarAlerta("Error", "Error al consultar habitación: " + e.getMessage());
        }
    }

    private void mostrarAlerta(String titulo, String contenido) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}


