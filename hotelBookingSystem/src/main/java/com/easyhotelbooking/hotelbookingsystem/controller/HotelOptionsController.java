package com.easyhotelbooking.hotelbookingsystem.controller;


import com.easyhotelbooking.hotelbookingsystem.util.Utility;
import hotelbookingcommon.domain.Hotel;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

    public class HotelOptionsController {

        @FXML
        private BorderPane bp;

        @FXML
        private Button consultButton;

        @FXML
        private TextField hotelNumberField;

        @FXML
        private TextField locationField;

        @FXML
        private Button modifyButton;

        @FXML
        private TextField nameField;

        @FXML
        private Button registerButton;

        @FXML
        private Button removeButton;

        @FXML
        private Button goBack;

        @FXML
        public void initialize(){

        }

        // Referencia al controlador de la aplicación principal (o un objeto que gestione la comunicación con el servidor)
        private MainInterfaceController mainController;
        private static final Logger logger = LogManager.getLogger(HotelOptionsController.class);

        public void setMainController(MainInterfaceController mainController) {
            this.mainController = mainController;
        }

        @FXML
        void goBackOnAction() {
            Utility.loadFullView("maininterface.fxml", goBack);
        }

        @FXML
        void registerHotelOnAction() {
            try {
                // 1. Obtener datos de la interfaz
                String numberStr = hotelNumberField.getText();
                String name = nameField.getText();
                String location = locationField.getText();

                // 2. Validar datos
                if (numberStr.isEmpty() || name.isEmpty() || location.isEmpty()) {
                    mostrarAlerta("Error", "Por favor, complete todos los campos.");
                    return;
                }

                int number;
                try {
                    number = Integer.parseInt(numberStr);
                } catch (NumberFormatException e) {
                    mostrarAlerta("Error", "El número de hotel debe ser un valor numérico.");
                    return;
                }

                // 3. Crear objeto Hotel
                Hotel hotel = new Hotel(number, name, location);

                // 4. Enviar solicitud al servidor (a través del MainInterfaceController)
                mainController.registerHotel(hotel); // Llama al método en MainInterfaceController

            } catch (Exception e) {
                logger.error("Error al registrar hotel: {}", e.getMessage());
                mostrarAlerta("Error", "Ocurrió un error al registrar el hotel: " + e.getMessage());
            }
        }

        @FXML
        void consultHotelOnAction() {
            try {
                // 1. Obtener número de hotel de la interfaz
                String numberStr = hotelNumberField.getText();
                if (numberStr.isEmpty()) {
                    mostrarAlerta("Error", "Por favor, ingrese el número de hotel a consultar.");
                    return;
                }
                int number = Integer.parseInt(numberStr);

                // 2. Enviar solicitud al servidor (a través del MainInterfaceController)
                mainController.consultHotel(number); // Llama al método en MainInterfaceController

            } catch (NumberFormatException e) {
                mostrarAlerta("Error", "El número de hotel debe ser un valor numérico.");
            } catch (Exception e) {
                logger.error("Error al consultar hotel: {}", e.getMessage());
                mostrarAlerta("Error", "Ocurrió un error al consultar el hotel: " + e.getMessage());
            }
        }

        @FXML
        void modifyHotelOnAction() {
            try {
                String numberStr = hotelNumberField.getText();
                String name = nameField.getText();
                String location = locationField.getText();

                if (numberStr.isEmpty() || name.isEmpty() || location.isEmpty()) {
                    mostrarAlerta("Error", "Por favor, complete todos los campos.");
                    return;
                }

                int number = Integer.parseInt(numberStr);
                Hotel hotel = new Hotel(number, name, location);
                mainController.updateHotel(hotel);
            } catch (NumberFormatException e) {
                mostrarAlerta("Error", "Número inválido.");
            } catch (Exception e) {
                logger.error("Error al modificar hotel: {}", e.getMessage());
                mostrarAlerta("Error", "Ocurrió un error al modificar el hotel.");
            }
        }

        @FXML
        void removeHotelOnAction() {
            try {
                String numberStr = hotelNumberField.getText();
                if (numberStr.isEmpty()) {
                    mostrarAlerta("Error", "Ingrese el número de hotel a eliminar.");
                    return;
                }

                int number = Integer.parseInt(numberStr);
                mainController.deleteHotel(number);
            } catch (NumberFormatException e) {
                mostrarAlerta("Error", "Número inválido.");
            } catch (Exception e) {
                logger.error("Error al eliminar hotel: {}", e.getMessage());
                mostrarAlerta("Error", "Ocurrió un error al eliminar el hotel.");
            }
        }

        private void mostrarAlerta(String title, String content) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setContentText(content);
            alert.showAndWait();
        }


    }




