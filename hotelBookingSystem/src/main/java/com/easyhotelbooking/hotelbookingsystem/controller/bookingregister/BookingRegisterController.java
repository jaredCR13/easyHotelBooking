package com.easyhotelbooking.hotelbookingsystem.controller.bookingregister;

import com.easyhotelbooking.hotelbookingsystem.Main;
import com.easyhotelbooking.hotelbookingsystem.controller.search.SearchController;
import com.easyhotelbooking.hotelbookingsystem.socket.ClientConnectionManager;
import com.easyhotelbooking.hotelbookingsystem.util.FXUtility;
import com.easyhotelbooking.hotelbookingsystem.util.Utility;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import hotelbookingcommon.domain.*;
import hotelbookingcommon.domain.LogIn.FrontDeskClerkDTO;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;


public class BookingRegisterController {

    @FXML
    private BorderPane bp;
    @FXML
    private ComboBox<Guest> guestCombo;
    @FXML
    private ComboBox<FrontDeskClerk> frontDeskClerkCombo;
    @FXML
    private TextArea textAreaRoomId;
    @FXML
    private TextField bookingNumberTf;
    @FXML
    private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField daysOfStayTf;
    @FXML
    private FlowPane flowPane;
    private Stage stage;
    private Hotel selectedHotelFromSearch;
    private Date startDate;
    private Date endDate;
    private final Gson gson = new Gson();
    private static final Logger logger = LogManager.getLogger(BookingRegisterController.class);
    private Room selectedRoomFromSearch;
    private Stage primaryStage;
    private FrontDeskClerkDTO loggedInClerk; // Add a field to store the logged-in clerk
    private Main mainAppReference;

    public void setLoggedInClerk(FrontDeskClerkDTO loggedInClerk) {
        this.loggedInClerk = loggedInClerk;
        logger.info("HotelOptionsController: Logged-in clerk received: {}", loggedInClerk.getUser());
    }

    public void setSearchApp(Main mainAppReference) {
        this.mainAppReference = mainAppReference;
        logger.info("HotelOptionsController: Main application reference set.");
    }
    public void setSelectedHotelFromSearch(Hotel hotel,Date startDate,Date endDate) {
        this.selectedHotelFromSearch = hotel;
        this.startDate= startDate;
        this.endDate= endDate;
    }
    public void setSelectedRoomFromSearch(Room room){
        this.selectedRoomFromSearch=room;
        updateRoomDetails();
        loadImagesIntoFlowPane();
    }

    @FXML
    public void initialize() {
        startDatePicker.valueProperty().addListener((obs, oldDate, newDate) -> updateDaysOfStay());
        endDatePicker.valueProperty().addListener((obs, oldDate, newDate) -> updateDaysOfStay());
        daysOfStayTf.setEditable(false);
        // Configura cómo se muestran los objetos en los ComboBox
        guestCombo.setCellFactory(lv -> new ListCell<Guest>() {
            @Override
            protected void updateItem(Guest item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getName() + " " + item.getLastName()+" ("+item.getId()+")");
            }
        });
        guestCombo.setButtonCell(new javafx.scene.control.ListCell<Guest>() {
            @Override
            protected void updateItem(Guest item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getName() + " " + item.getLastName()+" ("+item.getId()+")");
            }
        });

        frontDeskClerkCombo.setCellFactory(lv -> new javafx.scene.control.ListCell<FrontDeskClerk>() {
            @Override
            protected void updateItem(FrontDeskClerk item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getName() + " " + item.getLastName()+" ("+item.getEmployeeId()+")");
            }
        });
        frontDeskClerkCombo.setButtonCell(new javafx.scene.control.ListCell<FrontDeskClerk>() {
            @Override
            protected void updateItem(FrontDeskClerk item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getName() + " " + item.getLastName()+" ("+item.getEmployeeId()+")");
            }
        });

        loadGuestsIntoComboBox();
        loadFrontDeskClerksIntoComboBox();

    }

    private void updateDaysOfStay() {

            LocalDate start = startDatePicker.getValue();
            LocalDate end = endDatePicker.getValue();

            if (start != null && end != null && !end.isBefore(start)) {
                long days = java.time.temporal.ChronoUnit.DAYS.between(start, end);
                daysOfStayTf.setText(String.valueOf(days));
            } else {
                daysOfStayTf.clear();
            }

    }

    private void loadGuestsIntoComboBox() {
        Request request = new Request("getGuests", null); // O el método correcto para obtener todos los huéspedes
        logger.info("Enviando solicitud para obtener todos los huéspedes.");

        new Thread(() -> {
            Response response = ClientConnectionManager.sendRequest(request);

            Platform.runLater(() -> {
                if ("200".equalsIgnoreCase(response.getStatus()) && response.getData() != null) {
                    try {
                        List<Guest> guests = gson.fromJson(gson.toJson(response.getData()), new TypeToken<List<Guest>>() {}.getType());
                        guestCombo.getItems().setAll(guests);
                        logger.info("Huéspedes cargados en ComboBox: " + guests.size());
                    } catch (Exception e) {
                        logger.error("Error al procesar la lista de huéspedes: " + e.getMessage(), e);
                        FXUtility.alert("Error", "No se pudo cargar la lista de huéspedes.");
                    }
                } else {
                    logger.warn("Error al obtener huéspedes: " + response.getMessage());
                    FXUtility.alert("Error", "No se pudo obtener la lista de huéspedes del servidor.");
                }
            });
        }).start();
    }

    private void loadFrontDeskClerksIntoComboBox() {
        Request request = new Request("getClerks", null);
        logger.info("Enviando solicitud para obtener todos los recepcionistas.");

        new Thread(() -> {
            Response response = ClientConnectionManager.sendRequest(request);

            Platform.runLater(() -> {
                if ("200".equalsIgnoreCase(response.getStatus()) && response.getData() != null) {
                    try {
                        List<FrontDeskClerk> clerks = gson.fromJson(gson.toJson(response.getData()), new TypeToken<List<FrontDeskClerk>>() {}.getType());
                        frontDeskClerkCombo.getItems().setAll(clerks);
                        logger.info("Recepcionistas cargados en ComboBox: " + clerks.size());
                    } catch (Exception e) {
                        logger.error("Error al procesar la lista de recepcionistas: " + e.getMessage(), e);
                        FXUtility.alert("Error", "No se pudo cargar la lista de recepcionistas.");
                    }
                } else {
                    logger.warn("Error al obtener recepcionistas: " + response.getMessage());
                    FXUtility.alert("Error", "No se pudo obtener la lista de recepcionistas del servidor.");
                }
            });
        }).start();
    }
    @FXML
    public void onSave(ActionEvent event) {

        Guest selectedGuest = guestCombo.getSelectionModel().getSelectedItem();
        int guestId;
        if (selectedGuest != null) {
            guestId = selectedGuest.getId();
        } else {
            FXUtility.alert("Error de validación", "Por favor, seleccione un huésped.");
            return;
        }

        FrontDeskClerk selectedClerk = frontDeskClerkCombo.getSelectionModel().getSelectedItem();
        String frontDeskClerkId;
        if (selectedClerk != null) {
            frontDeskClerkId = selectedClerk.getEmployeeId();
        } else {
            FXUtility.alert("Error de validación", "Por favor, seleccione un recepcionista.");
            return;
        }

        int roomNumber;
        if (selectedRoomFromSearch != null) {
            roomNumber = selectedRoomFromSearch.getRoomNumber();
        } else {
            FXUtility.alert("Error de sistema", "No se encontró la información de la habitación. Por favor, intente de nuevo.");
            return;
        }

        LocalDate localStartDate = startDatePicker.getValue();
        LocalDate localEndDate = endDatePicker.getValue();

        if (localStartDate == null || localEndDate == null) {
            FXUtility.alert("Error de validación", "Por favor, seleccione las fechas de inicio y fin.");
            return;
        }
        if (localEndDate.isBefore(localStartDate)) {
            FXUtility.alert("Error de validación", "La fecha de fin no puede ser anterior a la fecha de inicio.");
            return;
        }

        Date startDate = Date.from(localStartDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(localEndDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        int daysOfStay = (int) java.time.temporal.ChronoUnit.DAYS.between(localStartDate, localEndDate);
        if (daysOfStay < 1) {
            FXUtility.alert("Error de validación", "La estancia debe ser de al menos un día.");
            return;
        }


        String bookingNumberText = bookingNumberTf.getText();
        int bookingNumber;
        if (bookingNumberText == null || bookingNumberText.trim().isEmpty()) {
            FXUtility.alert("Error de validación", "Por favor, ingrese un número de reserva.");
            return;
        }
        try {
            bookingNumber = Integer.parseInt(bookingNumberText.trim());
            if (bookingNumber <= 0) { // O cualquier otra validación de rango
                FXUtility.alert("Error de validación", "El número de reserva debe ser un número entero positivo.");
                return;
            }
        } catch (NumberFormatException e) {
            FXUtility.alert("Error de validación", "El número de reserva ingresado no es válido. Debe ser un número.");
            logger.warn("Número de reserva no válido: {}", bookingNumberText);
            return;
        }

        //Crear el objeto Booking con el ID ingresado por el usuario
        Booking newBooking = new Booking(bookingNumber,selectedHotelFromSearch.getNumHotel(), guestId, startDate, endDate, frontDeskClerkId, daysOfStay, roomNumber);

        logger.info("Intentando crear reserva con Booking Number ingresado: " + newBooking.getBookingNumber());

        //Enviar la reserva al servidor
        Request request = new Request("addBooking", newBooking);
        new Thread(() -> {
            Response response = ClientConnectionManager.sendRequest(request);
            Platform.runLater(() -> {
                if ("201".equalsIgnoreCase(response.getStatus())) {
                    FXUtility.alertInfo("Éxito", "Reserva creada exitosamente!");
                    logger.info("Reserva exitosa: " + newBooking.getBookingNumber());
                    onCancel(null); // O redirigir
                } else {
                    FXUtility.alert("Error de Reserva", "No se pudo crear la reserva: " + response.getMessage());
                    logger.error("Fallo al crear reserva: " + response.getMessage());
                }
            });
        }).start();
    }



    public void onCancel(ActionEvent event) {
        try {
            SearchController searchController = Utility.loadPage2("searchinterface/searchinterface.fxml", bp);

            if (searchController != null) {
                searchController.setStage(this.stage);

                if (this.selectedHotelFromSearch != null) {
                    searchController.setSearchCriteria(this.selectedHotelFromSearch,startDate,endDate);
                    logger.info("Regresando a búsqueda y restableciendo hotel: " + this.selectedHotelFromSearch.getHotelName());
                } else {
                    logger.warn("No se pudo restablecer el hotel en SearchController al cancelar. La vista de habitaciones puede no cargarse correctamente.");

                }
            } else {
                logger.error("No se pudo obtener el controlador para searchinterface.fxml.");
                FXUtility.alert("Error", "No se pudo cargar la página de búsqueda.");
            }
        } catch (Exception e) {
            logger.error("Error al cargar la vista de búsqueda: " + e.getMessage(), e);
            FXUtility.alert("Error", "No se pudo cargar la página de búsqueda. " + e.getMessage());
        }
    }
    private void updateRoomDetails() {
        textAreaRoomId.setEditable(false);
        if (textAreaRoomId != null && selectedRoomFromSearch != null) {
            Platform.runLater(() -> {
                textAreaRoomId.setText(String.valueOf(selectedRoomFromSearch.getRoomNumber()));
                logger.info("ID de habitación establecido: " + selectedRoomFromSearch.getRoomNumber());

            });
        } else {
            logger.warn("textAreaRoomId o selectedRoomFromSearch es nulo. No se pudo actualizar la UI de la habitación.");
        }
    }
    private void loadImagesIntoFlowPane() {
        if (selectedRoomFromSearch.getImagesPaths() != null && !selectedRoomFromSearch.getImagesPaths().isEmpty()) {
            String serverImagePath = selectedRoomFromSearch.getImagesPaths().get(0); // Solo una imagen

            new Thread(() -> {
                Request request = new Request("downloadRoomImage", serverImagePath);
                Response response = ClientConnectionManager.sendRequest(request);

                Platform.runLater(() -> {
                    if ("200".equalsIgnoreCase(response.getStatus()) && response.getData() != null) {
                        try {
                            byte[] imageData;

                            if (response.getData() instanceof List) {
                                List<Double> doubleList = new Gson().fromJson(new Gson().toJson(response.getData()),
                                        new TypeToken<List<Double>>() {}.getType());
                                imageData = new byte[doubleList.size()];
                                for (int i = 0; i < doubleList.size(); i++) {
                                    imageData[i] = doubleList.get(i).byteValue();
                                }
                            } else if (response.getData() instanceof byte[]) {
                                imageData = (byte[]) response.getData();
                            } else {
                                logger.warn("Tipo inesperado: {}", response.getData().getClass().getName());
                                return;
                            }

                            try (ByteArrayInputStream bis = new ByteArrayInputStream(imageData)) {
                                Image img = new Image(bis, 180, 150, true, true);
                                ImageView imageView = new ImageView(img);
                                imageView.setFitWidth(250);
                                imageView.setFitHeight(200);
                                imageView.setPreserveRatio(true);
                                imageView.setSmooth(true);

                                flowPane.getChildren().addAll(imageView); // reemplaza contenido del flowPane
                            } catch (Exception e) {
                                logger.error("Error creando imagen: ", e);
                            }
                        } catch (Exception e) {
                            logger.error("Error procesando imagen: ", e);
                        }
                    } else {
                        logger.warn("Error al descargar la imagen desde el servidor: {}", response.getMessage());
                    }
                });
            }).start();
        } else {
            logger.info("No hay rutas de imagen para la habitación.");
        }
    }

}
