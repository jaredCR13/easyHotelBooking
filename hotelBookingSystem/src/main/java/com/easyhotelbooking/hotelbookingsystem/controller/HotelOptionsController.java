package com.easyhotelbooking.hotelbookingsystem.controller;


import com.easyhotelbooking.hotelbookingsystem.socket.ClientConnectionManager;
import com.easyhotelbooking.hotelbookingsystem.util.Utility;
import com.google.gson.Gson;
import hotelbookingcommon.domain.Hotel;
import hotelbookingcommon.domain.Request;
import hotelbookingcommon.domain.Response;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

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
        private TableView<Hotel> hotelRegister;

        @FXML
        private TableColumn<Hotel, Integer> numberHotelRegister;

        @FXML
        private TableColumn<Hotel, String>  nameHotelRegister;

        @FXML
        private TableColumn<Hotel, String> locationHotelRegister;

        @FXML
        public void initialize(){
            numberHotelRegister.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getNumHotel()).asObject());
            nameHotelRegister.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getHotelName()));
            locationHotelRegister.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getHotelLocation()));
            loadHotelsIntoRegister();

            // Permitir seleccionar un hotel para modificar
            hotelRegister.setOnMouseClicked(event -> {
                Hotel selected = hotelRegister.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    hotelNumberField.setText(String.valueOf(selected.getNumHotel()));
                    nameField.setText(selected.getHotelName());
                    locationField.setText(selected.getHotelLocation());
                }
            });
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

                // Actualiza la tabla y limpia los campos
                loadHotelsIntoRegister();
                clearFieldsHotelOnAction();

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

                // Actualiza la tabla y limpia los campos
                loadHotelsIntoRegister();
                clearFieldsHotelOnAction();

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

                // Actualiza la tabla y limpia los campos
                loadHotelsIntoRegister();
                clearFieldsHotelOnAction();

            } catch (NumberFormatException e) {
                mostrarAlerta("Error", "Número inválido.");
            } catch (Exception e) {
                logger.error("Error al eliminar hotel: {}", e.getMessage());
                mostrarAlerta("Error", "Ocurrió un error al eliminar el hotel.");
            }
        }

        @FXML
        void clearFieldsHotelOnAction() {
            hotelNumberField.clear();
            nameField.clear();
            locationField.clear();
        }

        private void loadHotelsIntoRegister() {
            Request request = new Request("getHotels", null);
            Response response = ClientConnectionManager.sendRequest(request);
            if ("200".equalsIgnoreCase(response.getStatus())) {
                List<Hotel> hotels = new Gson().fromJson(new Gson().toJson(response.getData()), new com.google.gson.reflect.TypeToken<List<Hotel>>(){}.getType());
                hotelRegister.setItems(javafx.collections.FXCollections.observableArrayList(hotels));
            }
        }

        private void mostrarAlerta(String title, String content) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setContentText(content);
            alert.showAndWait();
        }


    }




