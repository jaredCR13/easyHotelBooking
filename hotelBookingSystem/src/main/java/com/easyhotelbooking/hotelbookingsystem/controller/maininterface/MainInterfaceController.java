package com.easyhotelbooking.hotelbookingsystem.controller.maininterface;

import com.easyhotelbooking.hotelbookingsystem.controller.frontdeskclerkregister.FrontDeskClerkOptionsController;
import com.easyhotelbooking.hotelbookingsystem.controller.hotelregister.HotelOptionsController;
import com.easyhotelbooking.hotelbookingsystem.controller.roomregister.RoomOptionsController;
import com.easyhotelbooking.hotelbookingsystem.socket.ClientConnectionManager;
import com.easyhotelbooking.hotelbookingsystem.util.FXUtility;
import com.easyhotelbooking.hotelbookingsystem.util.Utility;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import hotelbookingcommon.domain.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.stream.Collectors;

public class MainInterfaceController {

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

        // Ya tienes esta variable, ¡excelente!
        private Stage stage;

        private static final Logger logger = LogManager.getLogger(MainInterfaceController.class);
        private Gson gson = new Gson();


        public void setStage(Stage stage) {
                this.stage = stage;
                logger.info("Stage principal establecido en MainInterfaceController.");
        }


        @FXML
        public void initialize() {
                loadHotelNames();
        }

        // =================== HOTEL CRUD =========================

        @FXML
        void hotelOptionsOnAction() {
                HotelOptionsController controller = Utility.loadPage2("hotelinterface/hoteloptions.fxml", bp);
                if (controller != null) {
                        controller.setMainController(this);

                        if (this.stage != null) {
                                controller.setStage(this.stage);
                        } else {

                        }
                } else {
                        logger.error("No se pudo cargar hoteloptions.fxml o el controlador es null.");
                        FXUtility.alert("Error", "No se pudo cargar la página de opciones de hotel.");
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
                        FXUtility.alert("Error", "Error al obtener hoteles: " + message);
                }
        }

        public void registerHotel(Hotel hotel) {
                Request request = new Request("registerHotel", hotel);
                Response response = ClientConnectionManager.sendRequest(request);

                if ("201".equalsIgnoreCase(response.getStatus()) && response.getData() != null) {
                        logger.info("Hotel registrado exitosamente: {}", hotel);
                        FXUtility.alertInfo("Éxito", "Hotel registrado exitosamente.");

                        List<Hotel> updatedHotelList = gson.fromJson(gson.toJson(response.getData()), new TypeToken<List<Hotel>>() {
                        }.getType());
                        List<String> names = updatedHotelList.stream().map(Hotel::getHotelName).collect(Collectors.toList());
                        hotelCombo.getItems().clear();
                        hotelCombo.getItems().addAll(names);
                        hotelCombo.setValue(names.isEmpty() ? null : names.get(0));
                } else {
                        String message = response != null ? response.getMessage() : "Error desconocido al registrar hotel";
                        logger.error("Error al registrar hotel: {}", message);
                        FXUtility.alert("Error", "Error al registrar hotel: " + message);
                }
        }

        public void consultHotel(int hotelNumber) {
                Request request = new Request("getHotel", hotelNumber);
                Response response = ClientConnectionManager.sendRequest(request);

                if ("200".equalsIgnoreCase(response.getStatus()) && response.getData() != null) {
                        Hotel hotel = gson.fromJson(gson.toJson(response.getData()), Hotel.class);
                        logger.info("Hotel consultado exitosamente: {}", hotel);

                        StringBuilder hotelInfo = new StringBuilder();
                        hotelInfo.append("Número: ").append(hotel.getNumHotel()).append("\n");
                        hotelInfo.append("Nombre: ").append(hotel.getHotelName()).append("\n");
                        hotelInfo.append("Ubicación: ").append(hotel.getHotelLocation()).append("\n");
                        hotelInfo.append("Cantidad de Habitaciones: ").append(hotel.getRooms().size()).append("\n");

                        if (!hotel.getRooms().isEmpty()) {
                                hotelInfo.append("\nDetalles de Habitaciones:\n");
                                for (Room room : hotel.getRooms()) {
                                        hotelInfo.append("  - Habitación ").append(room.getRoomNumber())
                                                .append(" (Precio: ").append(room.getRoomPrice())
                                                .append(", Estado: ").append(room.getStatus())
                                                .append(", Estilo: ").append(room.getStyle()).append(")\n");
                                }
                        }

                       FXUtility.alertInfo("Información del Hotel", hotelInfo.toString());
                } else {
                        String message = response != null ? response.getMessage() : "No se encontró el hotel";
                        logger.error("Error al consultar hotel: {}", message);
                       FXUtility.alert("Error", message);
                }
        }

        public void updateHotel(Hotel hotel) {
                Request request = new Request("updateHotel", hotel);
                Response response = ClientConnectionManager.sendRequest(request);

                if ("200".equalsIgnoreCase(response.getStatus())) {
                        mostrarAlerta("Éxito", "Hotel actualizado correctamente.");
                        loadHotelNames();
                } else {
                        FXUtility.alert("Error", "No se pudo actualizar el hotel: " + response.getMessage());
                }
        }

        public void deleteHotel(int hotelNumber) {
                Request request = new Request("deleteHotel", hotelNumber);
                Response response = ClientConnectionManager.sendRequest(request);

                if ("200".equalsIgnoreCase(response.getStatus())) {
                        FXUtility.alertInfo("Éxito", "Hotel eliminado correctamente (y sus habitaciones asociadas).");
                        loadHotelNames();
                } else {
                       FXUtility.alert("Error", "No se pudo eliminar el hotel: " + response.getMessage());
                }
        }

        private void mostrarAlerta(String title, String content) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle(title);
                alert.setContentText(content);
                if (this.stage != null) {
                        alert.initOwner(this.stage);
                }
                alert.showAndWait();
        }

        // ================= ROOM CRUD =====================

        @FXML
        void roomOptionsOnAction() {
                RoomOptionsController controller = Utility.loadPage2("roominterface/roomoptions.fxml", bp);
                if (controller != null) {
                        controller.setMainController(this);

                        if (this.stage != null) {
                                controller.setStage(this.stage);
                        } else {

                        }
                } else {
                        logger.error("No se pudo cargar roomoptions.fxml o el controlador es null.");
                       FXUtility.alert("Error", "No se pudo cargar la página de opciones de habitaciones.");
                }
        }

        public Response registerRoom(Room room) {
                Request request = new Request("registerRoom", room);
                Response response = ClientConnectionManager.sendRequest(request);
                return response;
        }

        public void consultRoom(int roomNumber) {
                Request request = new Request("getRoom", roomNumber);
                Response response = ClientConnectionManager.sendRequest(request);

                if ("200".equalsIgnoreCase(response.getStatus()) && response.getData() != null) {
                        Room room = gson.fromJson(gson.toJson(response.getData()), Room.class);

                        StringBuilder roomInfo = new StringBuilder();
                        roomInfo.append("Número: ").append(room.getRoomNumber()).append("\n");
                        roomInfo.append("Precio: ").append(room.getRoomPrice()).append("\n");
                        roomInfo.append("Descripción detallada: ").append(room.getDetailedDescription()).append("\n");
                        roomInfo.append("Estado: ").append(room.getStatus()).append("\n");
                        roomInfo.append("Estilo: ").append(room.getStyle()).append("\n");

                        if (room.getHotel() != null) {
                                roomInfo.append("\nAsociada al Hotel:\n");
                                roomInfo.append("  - Número de Hotel: ").append(room.getHotel().getNumHotel()).append("\n");
                                roomInfo.append("  - Nombre de Hotel: ").append(room.getHotel().getHotelName()).append("\n");
                        } else if (room.getHotelId() != -1) {
                                roomInfo.append("\nAsociada al Hotel (ID): ").append(room.getHotelId()).append(" (Detalles del hotel no cargados)\n");
                        } else {
                                roomInfo.append("\nNo asociada a ningún hotel.\n");
                        }

                        FXUtility.alertInfo("Habitación encontrada", roomInfo.toString());
                } else {
                       FXUtility.alert("Error", "Habitación no encontrada: " + (response != null ? response.getMessage() : ""));
                }
        }

        public void updateRoom(Room room) {
                Request request = new Request("updateRoom", room);
                Response response = ClientConnectionManager.sendRequest(request);

                if ("200".equalsIgnoreCase(response.getStatus())) {
                       FXUtility.alertInfo("Éxito", "Habitación actualizada correctamente.");
                } else {
                        FXUtility.alert("Error", "No se pudo actualizar la habitación: " + response.getMessage());
                }
        }

        public void deleteRoom(int roomNumber) {
                Request request = new Request("deleteRoom", roomNumber);
                Response response = ClientConnectionManager.sendRequest(request);

                if ("200".equalsIgnoreCase(response.getStatus())) {
                        FXUtility.alertInfo("Éxito", "Habitación eliminada correctamente.");
                } else {
                        FXUtility.alert("Error", "No se pudo eliminar la habitación: " + response.getMessage());
                }
        }


        // ==================== RECEPCIONIST CRUD =================================

        @FXML
        void frontDeskClerkOptionsOnAction() {
                FrontDeskClerkOptionsController controller = Utility.loadPage2("frontdeskclerkinterface/frontdeskclerkoptions.fxml", bp);
                if (controller != null) {
                        controller.setMainController(this);

                        if (this.stage != null) {
                                controller.setStage(this.stage);
                        } else {
                                logger.error("Error: stage es null en MainInterfaceController al cargar FrontDeskClerkOptionsController.");
                        }
                } else {
                        logger.error("No se pudo cargar frontdeskoptionsclerk.fxml o el controlador es null.");
                        FXUtility.alert("Error", "No se pudo cargar la página de opciones de recepcionista.");
                }
        }

        public void registerFrontDeskClerk(FrontDeskClerk frontDeskClerk) {
                Request request = new Request("registerFrontDeskClerk", frontDeskClerk);
                Response response = ClientConnectionManager.sendRequest(request);

                if ("201".equalsIgnoreCase(response.getStatus())) {
                      FXUtility.alertInfo("Éxito", "Recepcionista registrado correctamente.");

                } else {
                        FXUtility.alert("Error", "No se pudo registrar el recepcionista: " + response.getMessage());
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
                       FXUtility.alert("Error", "Recepcionista no encontrado: " + (response != null ? response.getMessage() : ""));
                }
        }
        public void deleteFrontDeskClerk(FrontDeskClerk frontDeskClerk){
                Request request= new Request("deleteFrontDeskClerk",frontDeskClerk.getEmployeeId());
                Response response= ClientConnectionManager.sendRequest(request);

                if ("200".equalsIgnoreCase(response.getStatus())) {
                        FXUtility.alertInfo("Éxito", "FrontDeskClerk "+frontDeskClerk.getEmployeeId()+" eliminado correctamente.");
                } else {
                       FXUtility.alert("Error", "No se pudo eliminar el frontDeskClerk: " + response.getMessage());
                }
        }
        public void updateClerk(FrontDeskClerk frontDeskClerk) {
                Request request = new Request("updateClerk", frontDeskClerk);
                Response response = ClientConnectionManager.sendRequest(request);

                if ("200".equalsIgnoreCase(response.getStatus())) {
                       FXUtility.alertInfo("Éxito", "FrontDeskClerk actualizado correctamente.");
                } else {
                        FXUtility.alert("Error", "No se pudo actualizar el frontDeskClerk: " + response.getMessage());
                }
        }
}
