package com.easyhotelbooking.hotelbookingsystem.controller.maininterface;

import com.easyhotelbooking.hotelbookingsystem.Main;
import com.easyhotelbooking.hotelbookingsystem.controller.frontdeskclerkregister.FrontDeskClerkOptionsController;
import com.easyhotelbooking.hotelbookingsystem.controller.guestregister.GuestOptionsController;
import com.easyhotelbooking.hotelbookingsystem.controller.hotelregister.HotelOptionsController;
import com.easyhotelbooking.hotelbookingsystem.controller.roomregister.RoomOptionsController;
import com.easyhotelbooking.hotelbookingsystem.controller.search.SearchController;
import com.easyhotelbooking.hotelbookingsystem.socket.ClientConnectionManager;
import com.easyhotelbooking.hotelbookingsystem.util.FXUtility;
import com.easyhotelbooking.hotelbookingsystem.util.Utility;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import hotelbookingcommon.domain.*;
import hotelbookingcommon.domain.LogIn.FrontDeskClerkDTO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.HBox; // Import HBox
import javafx.scene.layout.VBox; // Import VBox
import javafx.stage.Popup;     // Import Popup
import javafx.stage.Stage;
import javafx.stage.Window;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MainInterfaceController {

        // Removed @FXML private ComboBox<?> clientCombo;

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
        private Button clientSelectorButton;


        private Stage stage;

        private static final Logger logger = LogManager.getLogger(MainInterfaceController.class);
        private Gson gson = new Gson();
        private Main mainAppReference;
        private FrontDeskClerkDTO loggedInClerk;

        private Spinner<Integer> adultsSpinner;
        private Spinner<Integer> childrenSpinner;
        private Popup spinnerPopup;
        private List<Hotel> allHotels = new ArrayList<>();

        public void setStage(Stage stage) {
                this.stage = stage;
                logger.info("Stage principal establecido en MainInterfaceController.");
        }


        public void setMainApp(Main mainAppReference) {
                this.mainAppReference = mainAppReference;
                logger.info("FrontDeskClerkRegisterController: Main application reference set.");
        }

        public void setLoggedInClerk(FrontDeskClerkDTO loggedInClerk) {
                this.loggedInClerk = loggedInClerk;
                if (loggedInClerk != null) {
                        logger.info("MainInterfaceController: Logged-in clerk received: {}", loggedInClerk.getUser());
                } else {
                        logger.warn("MainInterfaceController: setLoggedInClerk called with null loggedInClerk.");
                }
        }

        @FXML
        public void initialize() {
                loadHotelNames();
                initializeSpinnersAndPopup();

                clientSelectorButton.setOnAction(event -> {
                        if (spinnerPopup != null && spinnerPopup.isShowing()) {
                                spinnerPopup.hide();
                        } else {
                                showSpinnerPopup();
                        }
                });
                updateClientSelectorButtonText();

        }

        private void initializeSpinnersAndPopup() {
                // CREAMOS LOS SPINNERS
                adultsSpinner = new Spinner<>();
                SpinnerValueFactory<Integer> adultsValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1);
                adultsSpinner.setValueFactory(adultsValueFactory);

                childrenSpinner = new Spinner<>();
                SpinnerValueFactory<Integer> childrenValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 0);
                childrenSpinner.setValueFactory(childrenValueFactory);

                // VBOX PARA SPINNERS Y LABELS
                VBox content = new VBox(10); // 10 is spacing
                content.setStyle("-fx-background-color: white; -fx-padding: 15px; -fx-border-color: lightgray; -fx-border-width: 1px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 2);");

                HBox adultsRow = new HBox(5, new Label("Adults:"), adultsSpinner);
                HBox childrenRow = new HBox(5, new Label("Children:"), childrenSpinner);

                Button doneButton = new Button("Done");
                doneButton.setOnAction(event -> {
                        if (spinnerPopup != null) {
                                spinnerPopup.hide();
                                updateClientSelectorButtonText();
                        }
                });

                content.getChildren().addAll(adultsRow, childrenRow, doneButton);

                //CREA EL POP UP Y SETTEA EL CONTENIDO
                spinnerPopup = new Popup();
                spinnerPopup.getContent().add(content);
                spinnerPopup.setAutoHide(true);
        }

        private void showSpinnerPopup() {
                if (spinnerPopup != null) {
                        //COORDENADAS PARA EL POP UP
                        Window ownerWindow = clientSelectorButton.getScene().getWindow();
                        double x = ownerWindow.getX() + clientSelectorButton.localToScene(0, 0).getX() + clientSelectorButton.getScene().getX();
                        double y = ownerWindow.getY() + clientSelectorButton.localToScene(0, 0).getY() + clientSelectorButton.getScene().getY() + clientSelectorButton.getHeight();

                        spinnerPopup.show(ownerWindow, x, y);
                }
        }

        private void updateClientSelectorButtonText() {
                int adults = adultsSpinner.getValue();
                int children = childrenSpinner.getValue();
                clientSelectorButton.setText(adults + " Adult" + (adults > 1 ? "s" : "") + ", " + children + " Child" + (children > 1 ? "ren" : ""));
        }

        // =================== HOTEL CRUD =========================

        @FXML
        void hotelOptionsOnAction() {
                HotelOptionsController controller = Utility.loadPage2("hotelinterface/hoteloptions.fxml", bp);
                if (controller != null) {
                        controller.setMainController(this);
                        controller.setLoggedInClerk(this.loggedInClerk);
                        controller.setMainApp(this.mainAppReference);
                        if (this.stage != null) {
                                controller.setStage(this.stage);
                        } else {
                                logger.error("Error: stage es null en MainInterfaceController al cargar HotelOptionsController.");
                        }
                } else {
                        logger.error("No se pudo cargar hoteloptions.fxml o el controlador es null.");
                        FXUtility.alert("Error", "No se pudo cargar la página de opciones de hotel.");
                }
        }

        private void loadHotelNames() {
                Request request = new Request("getHotels", null);
                Response response = ClientConnectionManager.sendRequest(request); // <-- La llamada al servidor

                if ("200".equalsIgnoreCase(response.getStatus()) && response.getData() != null) {
                        try {
                                allHotels = gson.fromJson(gson.toJson(response.getData()), new TypeToken<List<Hotel>>() {}.getType());
                                if (allHotels == null) {
                                        allHotels = new ArrayList<>();
                                }

                                List<String> names = allHotels.stream().map(Hotel::getHotelName).collect(Collectors.toList());

                                hotelCombo.getItems().clear();
                                hotelCombo.getItems().addAll(names);

                                logger.info("Hoteles cargados con éxito. Total: {}", allHotels.size());
                                for (Hotel h : allHotels) {
                                        logger.info("  - Hotel en allHotels: '{}' (ID: {})", h.getHotelName(), h.getNumHotel());
                                }

                        } catch (Exception e) {
                                logger.error("Error al deserializar la lista de hoteles: {}", e.getMessage(), e);
                                allHotels = new ArrayList<>();
                                FXUtility.alert("Error", "Error al procesar la lista de hoteles del servidor.");
                        }
                } else {
                        String message = response != null ? response.getMessage() : "Error desconocido al obtener hoteles";
                        logger.error("Error al obtener hoteles: {}", message); // <-- ¡Busca este log!
                        FXUtility.alert("Error", "Error al obtener hoteles: " + message);
                        allHotels = new ArrayList<>(); // Aseguramos que siga siendo una lista vacía
                }
        }

        // ...


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
                        controller.setLoggedInClerk(this.loggedInClerk);
                        controller.setMainApp(this.mainAppReference);
                        if (this.stage != null) {
                                controller.setStage(this.stage);
                        } else {
                                logger.error("Error: stage es null en MainInterfaceController al cargar RoomOptionsController.");
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
                // Solo ADMINISTRATOR puede acceder a esta sección
                if (loggedInClerk == null || loggedInClerk.getFrontDeskClerkRoleEnum() != FrontDeskClerkRole.ADMINISTRATOR) {
                        FXUtility.alert("Permiso Denegado", "No tienes los permisos para gestionar recepcionistas.");
                        return;
                }

                FrontDeskClerkOptionsController controller = Utility.loadPage2("frontdeskclerkinterface/frontdeskclerkoptions.fxml", bp);
                if (controller != null) {
                        controller.setMainController(this);
                        controller.setLoggedInClerk(this.loggedInClerk);
                        controller.setMainApp(this.mainAppReference);
                        if (this.stage != null) {
                                controller.setStage(this.stage);
                        }
                } else {
                        logger.error("No se pudo cargar frontdeskoptionsclerk.fxml o el controlador es null.");
                        FXUtility.alert("Error", "No se pudo cargar la página de opciones de recepcionista.");
                }
        }

        public void registerFrontDeskClerk(FrontDeskClerk frontDeskClerk) {
                // Solo ADMINISTRATOR puede registrar recepcionistas
                if (loggedInClerk == null || loggedInClerk.getFrontDeskClerkRoleEnum() != FrontDeskClerkRole.ADMINISTRATOR) {
                        FXUtility.alert("Permiso Denegado", "No tienes los permisos para registrar recepcionistas.");
                        return;
                }

                Request request = new Request("registerFrontDeskClerk", frontDeskClerk);
                Response response = ClientConnectionManager.sendRequest(request);

                if ("201".equalsIgnoreCase(response.getStatus())) {
                        FXUtility.alertInfo("Éxito", "Recepcionista registrado correctamente.");

                } else if ("409".equalsIgnoreCase(response.getStatus())) {
                        FXUtility.alert("Error", response.getMessage());
                        logger.warn("Intento de registrar habitación duplicada: {}", frontDeskClerk.getEmployeeId());
                } else {
                        FXUtility.alert("Error", "No se pudo registrar el recepcionista: " + response.getMessage());
                }
        }

        public void consultFrontDeskClerk(String employeeId) {
                // La consulta de recepcionistas puede ser accesible para todos

                Request request = new Request("getFrontDeskClerk", employeeId);
                Response response = ClientConnectionManager.sendRequest(request);

                if ("200".equalsIgnoreCase(response.getStatus()) && response.getData() != null) {

                        /**
                         * FrontDeskClerkDTO frontDeskClerkDTO = gson.fromJson(gson.toJson(response.getData()), FrontDeskClerkDTO.class);
                         *                         FXUtility.alertInfo("Recepcionista encontrado",
                         *                                 "N° Empleado: " + frontDeskClerkDTO.getEmployeeId() +
                         *                                         "\nNombre: " + frontDeskClerkDTO.getName() + " " + frontDeskClerkDTO.getLastName() +
                         *                                         "\nTeléfono: " + frontDeskClerkDTO.getPhoneNumber() +
                         *                                         "\nUsuario: " + frontDeskClerkDTO.getUser() +
                         *                                         "\nRol: " + frontDeskClerkDTO.getFrontDeskClerkRoleEnum() +
                         *                                         "\nID Hotel: " + frontDeskClerkDTO.getHotelId());
                         */

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

        public void deleteFrontDeskClerk(FrontDeskClerk frontDeskClerk) {
                // Solo ADMINISTRATOR puede eliminar recepcionistas
                if (loggedInClerk == null || loggedInClerk.getFrontDeskClerkRoleEnum() != FrontDeskClerkRole.ADMINISTRATOR) {
                        FXUtility.alert("Permiso Denegado", "No tienes los permisos para eliminar recepcionistas.");
                        return;
                }

                Request request = new Request("deleteFrontDeskClerk", frontDeskClerk.getEmployeeId());
                Response response = ClientConnectionManager.sendRequest(request);

                if ("200".equalsIgnoreCase(response.getStatus())) {
                        FXUtility.alertInfo("Éxito", "FrontDeskClerk " + frontDeskClerk.getEmployeeId() + " eliminado correctamente.");
                } else {
                        FXUtility.alert("Error", "No se pudo eliminar el frontDeskClerk: " + response.getMessage());
                }
        }

        public void updateClerk(FrontDeskClerk frontDeskClerk) {
                // Solo ADMINISTRATOR puede actualizar recepcionistas
                if (loggedInClerk == null || loggedInClerk.getFrontDeskClerkRoleEnum() != FrontDeskClerkRole.ADMINISTRATOR) {
                        FXUtility.alert("Permiso Denegado", "No tienes los permisos para actualizar recepcionistas.");
                        return;
                }

                Request request = new Request("updateClerk", frontDeskClerk);
                Response response = ClientConnectionManager.sendRequest(request);

                if ("200".equalsIgnoreCase(response.getStatus())) {
                        FXUtility.alertInfo("Éxito", "FrontDeskClerk actualizado correctamente.");
                } else {
                        FXUtility.alert("Error", "No se pudo actualizar el frontDeskClerk: " + response.getMessage());
                }
        }

        // ================= GUEST CRUD =====================

        public void guestOptionsOnAction() {
                GuestOptionsController controller = Utility.loadPage2("guestinterface/guestoptions.fxml", bp);
                if (controller != null) {
                        controller.setMainController(this);
                        controller.setLoggedInClerk(this.loggedInClerk);
                        controller.setMainApp(this.mainAppReference);

                        if (this.stage != null) {
                                controller.setStage(this.stage);
                        }
                } else {
                        logger.error("No se pudo cargar guest.fxml o el controlador es null.");
                        FXUtility.alert("Error", "No se pudo cargar la página de opciones de guest.");
                }
        }
        public void registerGuest(Guest guest) {
                Request request = new Request("registerGuest", guest);
                Response response = ClientConnectionManager.sendRequest(request);

                if ("201".equalsIgnoreCase(response.getStatus())) {
                        FXUtility.alertInfo("Éxito", "Huésped registrado correctamente.");
                } else {
                        FXUtility.alert("Error", "No se pudo registrar el huésped: " + response.getMessage());
                }
        }

        public void consultGuest(int guestId) {
                Request request = new Request("getGuest", guestId);
                Response response = ClientConnectionManager.sendRequest(request);

                if ("200".equalsIgnoreCase(response.getStatus()) && response.getData() != null) {
                        Guest guest = new Gson().fromJson(new Gson().toJson(response.getData()), Guest.class);
                        FXUtility.alertInfo("Huésped encontrado",
                                "ID: " + guest.getId() +
                                        "\nNombre: " + guest.getName() + " " + guest.getLastName() +
                                        "\nEmail: " + guest.getEmail() +
                                        "\nTeléfono: " + guest.getPhoneNumber());
                } else {
                        FXUtility.alert("Error", "Huésped no encontrado: " + response.getMessage());
                }
        }

        public void updateGuest(Guest guest) {
                Request request = new Request("updateGuest", guest);
                Response response = ClientConnectionManager.sendRequest(request);

                if (!"200".equals(response.getStatus())) {
                        FXUtility.alert("Error", "No se pudo actualizar el huésped.");
                }
        }

        public void deleteGuest(int guestId) {
                Request request = new Request("deleteGuest", guestId);
                Response response = ClientConnectionManager.sendRequest(request);

                if ("200".equalsIgnoreCase(response.getStatus())) {
                        FXUtility.alertInfo("Éxito", "Huésped eliminado correctamente.");
                } else {
                        FXUtility.alert("Error", "No se pudo eliminar el huésped: " + response.getMessage());
                }
        }


        @FXML
        private void searchOnAction() {
                String selectedHotelName = hotelCombo.getSelectionModel().getSelectedItem();


                logger.info("searchOnAction disparado.");
                logger.info("Nombre de hotel seleccionado en ComboBox: '{}'", selectedHotelName);
                logger.info("Tamaño de allHotels en searchOnAction: {}", allHotels.size());


                if (selectedHotelName == null || selectedHotelName.isEmpty()) {
                        FXUtility.alert("Advertencia", "Por favor, seleccione un hotel para buscar habitaciones.");
                        return;
                }

                if (allHotels == null || allHotels.isEmpty()) {
                        logger.error("La lista allHotels está vacía o nula. No se pueden buscar habitaciones.");
                        FXUtility.alert("Error", "No se pudo cargar la lista de hoteles. Intente recargar la aplicación.");
                        return;
                }


                Optional<Hotel> selectedHotelOpt = allHotels.stream()
                        .filter(h -> h.getHotelName() != null &&
                                h.getHotelName().trim().equalsIgnoreCase(selectedHotelName.trim()))
                        .findFirst();

                if (selectedHotelOpt.isPresent()) {
                        Hotel selectedHotel = selectedHotelOpt.get();

                        logger.info("¡Hotel '{}' (ID: {}) encontrado en allHotels!", selectedHotel.getHotelName(), selectedHotel.getNumHotel());


                        int adults = adultsSpinner.getValue();
                        int children = childrenSpinner.getValue();
                        LocalDate from = fromDate.getValue();

                        try {
                                SearchController searchController = Utility.loadPage2("searchinterface/searchinterface.fxml", bp);

                                if (searchController != null) {
                                        searchController.setMainController(this);
                                        searchController.setStage(this.stage);
                                        searchController.setSearchCriteria(selectedHotel);
                                } else {
                                        logger.error("No se pudo obtener el controlador para searchinterface.fxml.");
                                        FXUtility.alert("Error", "No se pudo cargar la página de búsqueda.");
                                }

                                logger.info("Cargada vista de búsqueda con criterios para hotel: " + selectedHotel.getHotelName());

                        } catch (Exception e) {
                                logger.error("Error al cargar la vista de búsqueda: " + e.getMessage(), e);
                                FXUtility.alert("Error", "No se pudo cargar la página de búsqueda. " + e.getMessage());
                        }

                } else {

                        logger.warn("El nombre de hotel seleccionado '{}' NO SE ENCONTRÓ en la lista de hoteles cargados.", selectedHotelName);

                        FXUtility.alert("Error", "No se pudo encontrar la información del hotel seleccionado.");
                        logger.error("Hotel seleccionado en ComboBox no encontrado en la lista allHotels: {}", selectedHotelName);
                }
        }

}


