package com.easyhotelbooking.hotelbookingsystem.controller;


import com.easyhotelbooking.hotelbookingsystem.socket.ClientConnectionManager;
import com.easyhotelbooking.hotelbookingsystem.util.Utility;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import hotelbookingcommon.domain.*;
import hotelbookingserver.service.HotelService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.stream.Collectors;

public class MainInterfaceController  {

        @FXML
        private ComboBox<?> clientCombo;

        @FXML
        private BorderPane bp;
        @FXML
        private StackPane contentPane;

        @FXML
        private DatePicker fromDate;

        @FXML
        private ComboBox<String> hotelCombo;

        @FXML
        private Button searchButton;

        @FXML
        private TextArea textArea;

        @FXML
        private DatePicker toDate;

        private final HotelService hotelService = new HotelService();

        private static final Logger logger = LogManager.getLogger(MainInterfaceController.class);
        private Gson gson = new Gson();

        @FXML
        public void initialize() {
                loadHotelNames();
        }

        // =================== HOTEL CRUD =========================

        @FXML
        void hotelOptionsOnAction() {
                HotelOptionsController controller = Utility.loadPage2("hoteloptions.fxml", bp);
                if (controller != null) {
                        controller.setMainController(this); // 👈 Aquí se pasa la referencia correctamente
                } else {
                        logger.error("No se pudo cargar hoteloptions.fxml o el controlador es null.");
                        mostrarAlerta("Error", "No se pudo cargar la página de opciones de hotel.");
                }
        }

        private void loadHotelNames() {
                Request request = new Request("getHotels", null);
                Response response = ClientConnectionManager.sendRequest(request);

                if ("200".equalsIgnoreCase(response.getStatus()) && response.getData() != null) {
                        List<Hotel> hotelList = gson.fromJson(gson.toJson(response.getData()), new TypeToken<List<Hotel>>() {
                        }.getType());
                        List<String> names = hotelList.stream().map(Hotel::getHotelName).collect(Collectors.toList());
                        hotelCombo.getItems().clear();
                        hotelCombo.getItems().addAll(names);
                        hotelCombo.setValue(names.isEmpty() ? null : names.get(0));
                } else {
                        String message = response != null ? response.getMessage() : "Error desconocido al obtener hoteles";
                        logger.error("Error al obtener hoteles: {}", message);
                        mostrarAlerta("Error", "Error al obtener hoteles: " + message);
                }
        }

        public void registerHotel(Hotel hotel) {
                Request request = new Request("registerHotel", hotel);
                Response response = ClientConnectionManager.sendRequest(request);

                if ("201".equalsIgnoreCase(response.getStatus()) && response.getData() != null) {
                        logger.info("Hotel registrado exitosamente: {}", hotel);
                        mostrarAlerta("Éxito", "Hotel registrado exitosamente.");

                        List<Hotel> updatedHotelList = gson.fromJson(gson.toJson(response.getData()), new TypeToken<List<Hotel>>() {}.getType());
                        List<String> names = updatedHotelList.stream().map(Hotel::getHotelName).collect(Collectors.toList());
                        hotelCombo.getItems().clear();
                        hotelCombo.getItems().addAll(names);
                        hotelCombo.setValue(names.isEmpty() ? null : names.get(0));
                } else {
                        String message = response != null ? response.getMessage() : "Error desconocido al registrar hotel";
                        logger.error("Error al registrar hotel: {}", message);
                        mostrarAlerta("Error", "Error al registrar hotel: " + message);
                }
        }

        public void consultHotel(int hotelNumber) {
                Request request = new Request("getHotel", hotelNumber);
                Response response = ClientConnectionManager.sendRequest(request);

                if ("200".equalsIgnoreCase(response.getStatus()) && response.getData() != null) {
                        Hotel hotel = gson.fromJson(gson.toJson(response.getData()), Hotel.class); // ✅ conversión segura
                        logger.info("Hotel consultado exitosamente: {}", hotel);
                        mostrarAlerta("Información del Hotel", "Número: " + hotel.getNumHotel() + "\nNombre: " + hotel.getHotelName() + "\nUbicación: " + hotel.getHotelLocation());
                } else {
                        String message = response != null ? response.getMessage() : "No se encontró el hotel";
                        logger.error("Error al consultar hotel: {}", message);
                        mostrarAlerta("Error", message);
                }
        }

        public void updateHotel(Hotel hotel) {
                Request request = new Request("updateHotel", hotel);
                Response response = ClientConnectionManager.sendRequest(request);

                if ("200".equalsIgnoreCase(response.getStatus())) {
                        mostrarAlerta("Éxito", "Hotel actualizado correctamente.");
                        loadHotelNames();
                } else {
                        mostrarAlerta("Error", "No se pudo actualizar el hotel: " + response.getMessage());
                }
        }

        public void deleteHotel(int hotelNumber) {
                Request request = new Request("deleteHotel", hotelNumber);
                Response response = ClientConnectionManager.sendRequest(request);

                if ("200".equalsIgnoreCase(response.getStatus())) {
                        mostrarAlerta("Éxito", "Hotel eliminado correctamente.");
                        loadHotelNames();
                } else {
                        mostrarAlerta("Error", "No se pudo eliminar el hotel: " + response.getMessage());
                }
        }

        private void mostrarAlerta(String title, String content) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle(title);
                alert.setContentText(content);
                alert.showAndWait();
        }

        // ================= ROOM CRUD =====================


        @FXML
        void roomOptionsOnAction(){
                RoomOptionsController controller = Utility.loadPage2("roomoptions.fxml", bp);
                if (controller != null) {
                        controller.setMainController(this); // 👈 Aquí se pasa la referencia correctamente
                } else {
                        logger.error("No se pudo cargar roomoptions.fxml o el controlador es null.");
                        mostrarAlerta("Error", "No se pudo cargar la página de opciones de habitaciones.");
                }
        }

        public void registerRoom(Room room) {
                Request request = new Request("registerRoom", room);
                Response response = ClientConnectionManager.sendRequest(request);

                if ("201".equalsIgnoreCase(response.getStatus())) {
                        mostrarAlerta("Éxito", "Habitación registrada correctamente.");
                } else {
                        mostrarAlerta("Error", "No se pudo registrar la habitación: " + response.getMessage());
                }
        }

        public void consultRoom(int roomNumber) {
                Request request = new Request("getRoom", roomNumber);
                Response response = ClientConnectionManager.sendRequest(request);

                if ("200".equalsIgnoreCase(response.getStatus()) && response.getData() != null) {
                        Room room = gson.fromJson(gson.toJson(response.getData()), Room.class);
                        mostrarAlerta("Habitación encontrada",
                                "Número: " + room.getRoomNumber() +
                                        "\nPrecio: " + room.getRoomPrice() +
                                        "\nDescripción detallada:"+ room.getDetailedDescription()+
                                        "\nEstado: " + room.getStatus() +
                                        "\nEstilo: " + room.getStyle());
                } else {
                        mostrarAlerta("Error", "Habitación no encontrada.");
                }
        }

        public void updateRoom(Room room) {
                Request request = new Request("updateRoom", room);
                Response response = ClientConnectionManager.sendRequest(request);

                if ("200".equalsIgnoreCase(response.getStatus())) {
                        mostrarAlerta("Éxito", "Habitación actualizada correctamente.");
                        // Si quieres, puedes refrescar alguna lista o comboBox relacionado a las habitaciones
                } else {
                        mostrarAlerta("Error", "No se pudo actualizar la habitación: " + response.getMessage());
                }
        }

        public void deleteRoom(int roomNumber) {
                Request request = new Request("deleteRoom", roomNumber);
                Response response = ClientConnectionManager.sendRequest(request);

                if ("200".equalsIgnoreCase(response.getStatus())) {
                        mostrarAlerta("Éxito", "Habitación eliminada correctamente.");
                        // También aquí podrías actualizar listas o combos de habitaciones
                } else {
                        mostrarAlerta("Error", "No se pudo eliminar la habitación: " + response.getMessage());
                }
        }


        //==================== RECEPCIONIST CRUD =================================


        @FXML
        void frontDeskClerkOptionsOnAction() {
                FrontDeskClerkOptionsController controller = Utility.loadPage2("frontdeskclerkoptions.fxml", bp);
                if (controller != null) {
                        controller.setMainController(this); // Referencia al controlador principal
                } else {
                        logger.error("No se pudo cargar frontdeskoptionsclerk.fxml o el controlador es null.");
                        mostrarAlerta("Error", "No se pudo cargar la página de opciones de recepcionista.");
                }
        }

        public void registerFrontDeskClerk(FrontDeskClerk frontDeskClerk) {
                Request request = new Request("registerFrontDeskClerk", frontDeskClerk);
                Response response = ClientConnectionManager.sendRequest(request);

                if ("201".equalsIgnoreCase(response.getStatus())) {
                        mostrarAlerta("Éxito", "Recepcionista registrado correctamente.");
                } else {
                        mostrarAlerta("Error", "No se pudo registrar el recepcionista: " + response.getMessage());
                }
        }

        public void consultFrontDeskClerk(String employeeId) {
                Request request = new Request("getFrontDeskClerk", employeeId);
                Response response = ClientConnectionManager.sendRequest(request);

                if ("200".equalsIgnoreCase(response.getStatus()) && response.getData() != null) {
                        FrontDeskClerk frontDeskClerk = gson.fromJson(gson.toJson(response.getData()), FrontDeskClerk.class);
                        mostrarAlerta("Recepcionista encontrado",
                                "N° Empleado: " + frontDeskClerk.getEmployeeId() +
                                        "\nNombre: " + frontDeskClerk.getName() + " " + frontDeskClerk.getLastName() +
                                        "\nTeléfono: " + frontDeskClerk.getPhoneNumber() +
                                        "\nUsuario: " + frontDeskClerk.getUser());
                } else {
                        mostrarAlerta("Error", "Recepcionista no encontrado.");
                }
        }


}




