package com.easyhotelbooking.hotelbookingsystem.controller.bookingregister;

import com.easyhotelbooking.hotelbookingsystem.Main;
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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public class ModifyBookingController {

    @FXML
    private TextField bookingNumberTf;

    @FXML
    private BorderPane bp;

    @FXML
    private Button cancelButton;

    @FXML
    private TextField daysOfStayTf;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private FlowPane flowPane;

    @FXML
    private ComboBox<FrontDeskClerk> frontDeskClerkCombo;

    @FXML
    private ComboBox<Guest> guestCombo;

    @FXML
    private Button modifyButtom;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private TextArea textAreaRoomId;
    private BookingTableController bookingTableController;
    private Booking booking;
    private Hotel selectedHotel;
    private Date startDate;
    private Date endDate;
    private Stage primaryStage;
    private FrontDeskClerkDTO loggedInClerk; // Add a field to store the logged-in clerk
    private Main mainAppReference;
    private final Gson gson = new Gson();
    private static final Logger logger = LogManager.getLogger(BookingTableController.class);



    public void setLoggedInClerk(FrontDeskClerkDTO loggedInClerk) {
        this.loggedInClerk = loggedInClerk;
        logger.info("HotelOptionsController: Logged-in clerk received: {}", loggedInClerk.getUser());
    }

    public void setMainApp(Main mainAppReference) {
        this.mainAppReference = mainAppReference;
        logger.info("HotelOptionsController: Main application reference set.");
    }
    public void setStage(Stage stage) {
        this.primaryStage = stage;
    }
    public void setSelectedHotel(Hotel hotel, Date startDate, Date endDate) {
        this.selectedHotel = hotel;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void setBookingTableController(BookingTableController bookingTableController){
        this.bookingTableController= bookingTableController;
    }
    @FXML
    public void initialize(){
        startDatePicker.valueProperty().addListener((obs, oldDate, newDate) -> updateDaysOfStay());
        endDatePicker.valueProperty().addListener((obs, oldDate, newDate) -> updateDaysOfStay());
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
        textAreaRoomId.setEditable(false);
        bookingNumberTf.setEditable(false);
        daysOfStayTf.setEditable(false);
        loadGuestsIntoComboBox();
        loadFrontDeskClerksIntoComboBox();
    }
    @FXML
    void onCancel(ActionEvent event) {

        BookingTableController controller = Utility.loadPage2("bookinginterface/bookingtable.fxml", bp);
        if (controller != null) {
            controller.setMainApp(mainAppReference);
            controller.setLoggedInClerk(loggedInClerk);
            controller.setSelectedHotelFromSearchTable(selectedHotel,startDate,endDate);
        }
    }


    public void setBooking(Booking booking) {
        this.booking = booking;

        // Cargar los datos en los campos (esto se puede mejorar)
        bookingNumberTf.setText(String.valueOf(booking.getBookingNumber()));
        daysOfStayTf.setText(String.valueOf(booking.getDaysOfStay()));
        startDatePicker.setValue(Utility.convertToLocalDate(booking.getStartDate()));
        endDatePicker.setValue(Utility.convertToLocalDate(booking.getEndDate()));
        textAreaRoomId.setText(String.valueOf(booking.getRoomNumber()));


        Platform.runLater(() -> {
            // Buscar y setear el huésped
            for (Guest g : guestCombo.getItems()) {
                if (g.getId() == booking.getGuestId()) {
                    guestCombo.setValue(g);
                    break;
                }
            }

            // Buscar y setear el recepcionista
            for (FrontDeskClerk fdc : frontDeskClerkCombo.getItems()) {
                if (fdc.getEmployeeId().equals(booking.getFrontDeskClerkId())) {
                    frontDeskClerkCombo.setValue(fdc);
                    break;
                }
            }
        });
    }
    @FXML
    void onModify(ActionEvent event) {
        try {
            int bookingId = booking.getBookingNumber();
            int hotelId = selectedHotel.getNumHotel();
            int daysOfStay = Integer.parseInt(daysOfStayTf.getText());

            Date startDate = Utility.convertToDate(startDatePicker.getValue());
            Date endDate = Utility.convertToDate(endDatePicker.getValue());

            FrontDeskClerk selectedClerk = frontDeskClerkCombo.getValue();
            Guest selectedGuest = guestCombo.getValue();

            if (selectedClerk == null || selectedGuest == null) {
                FXUtility.alert("Error", "Debes seleccionar un huésped y un recepcionista.");
                return;
            }

            String clerkId = selectedClerk.getEmployeeId();
            int guestId = selectedGuest.getId();
            int roomNumber = booking.getRoomNumber(); // Asumiendo que no se puede cambiar la habitación

            Booking updatedBooking = new Booking(
                    bookingId, hotelId, guestId, startDate, endDate, clerkId, daysOfStay, roomNumber
            );

            Request request = new Request("updateBooking", updatedBooking);
            Response response = ClientConnectionManager.sendRequest(request);

            if ("200".equalsIgnoreCase(response.getStatus())) {
                FXUtility.alertInfo("Éxito", "Reservación modificada correctamente.");
                bookingTableController.loadBookings(); // 🔁 Recarga el TableView
            } else {
                FXUtility.alert("Error", "No se pudo modificar la reservación: " + response.getMessage());
            }

        } catch (NumberFormatException e) {
            FXUtility.alert("Error", "Los campos numéricos no son válidos.");
            logger.error("Error de formato: {}", e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Error general: {}", e.getMessage(), e);
            FXUtility.alert("Error", "Error al modificar la reservación: " + e.getMessage());
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
}

