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
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class BookingTableController {

    @FXML private BorderPane bp;
    @FXML private TableView<Booking> bookingRegister;
    @FXML private TableColumn<Booking, Integer> bookingNumberColumn;
    @FXML private TableColumn<Booking, Integer> GuestIdColumn;
    @FXML private TableColumn<Booking, Integer> roomNumberColumn;
    @FXML private TableColumn<Booking, Integer> hotelIdColumn;
    @FXML private TableColumn<Booking, String> frontDeskIdColumn;
    @FXML private TableColumn<Booking, Integer> daysOfStayColumn;
    @FXML private TableColumn<Booking, String> startDateColumn;
    @FXML private TableColumn<Booking, String> endDateColumn;
    @FXML private TableColumn<Booking, Void> ActionsColumn;
    @FXML private TextField quickSearchField;
    private ScheduledExecutorService scheduler;
    @FXML private Button goBack;

    private static final Logger logger = LogManager.getLogger(BookingTableController.class);


    private Stage stage;
    private Room selectedRoomFromSearch;
    private Hotel selectedHotelFromSearch;
    private Date startDate, endDate;
    private FrontDeskClerkDTO loggedInClerk;
    private Main mainAppReference;

    public void setLoggedInClerk(FrontDeskClerkDTO loggedInClerk) {
        this.loggedInClerk = loggedInClerk;
        logger.info("HotelOptionsController: Logged-in clerk received: {}", loggedInClerk.getUser());
    }

    public void setMainApp(Main mainAppReference) {
        this.mainAppReference = mainAppReference;
        logger.info("HotelOptionsController: Main application reference set.");
    }

    public void initialize() {
        bookingNumberColumn.setCellValueFactory(new PropertyValueFactory<>("bookingNumber"));
        GuestIdColumn.setCellValueFactory(new PropertyValueFactory<>("guestId"));
        roomNumberColumn.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        hotelIdColumn.setCellValueFactory(new PropertyValueFactory<>("hotelId"));
        frontDeskIdColumn.setCellValueFactory(new PropertyValueFactory<>("frontDeskClerkId"));
        daysOfStayColumn.setCellValueFactory(new PropertyValueFactory<>("daysOfStay"));
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDateStr"));
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDateStr"));
        addButtonsToTable();
        startPolling();
    }

    public void setSelectedHotelFromSearchTable(Hotel hotel, Date start, Date end) {
        this.selectedHotelFromSearch = hotel;
        this.startDate = start;
        this.endDate = end;


        loadBookings();
    }
    public void setSelectedRoomFromSearchTable(Room room) {
        this.selectedRoomFromSearch = room;
    }
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void loadBookings() {
        if (selectedHotelFromSearch == null) {
            FXUtility.alert("Error", "Debe seleccionar un hotel para cargar las reservaciones.");
            logger.error("Hotel no seleccionado");
            return;
        }

        int hotelId = selectedHotelFromSearch.getNumHotel();
        Request request = new Request("getBookingsByHotelId", hotelId);
        Response response = ClientConnectionManager.sendRequest(request);

        if ("200".equalsIgnoreCase(response.getStatus())) {
            Type bookingListType = new TypeToken<List<Booking>>() {}.getType();
            List<Booking> bookings = new Gson().fromJson(new Gson().toJson(response.getData()), bookingListType);
            bookingRegister.setItems(FXCollections.observableArrayList(bookings));
        } else {
            FXUtility.alert("Error", "No se pudieron cargar las reservaciones.");
            logger.error("Error al obtener reservaciones: {}", response != null ? response.getMessage() : "null");
        }
    }


    private void addButtonsToTable() {
        Callback<TableColumn<Booking, Void>, TableCell<Booking, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Booking, Void> call(final TableColumn<Booking, Void> param) {
                return new TableCell<>() {

                    private final Button modify = new Button("Modify");
                    private final Button remove = new Button("Remove");
                    private final HBox buttonsBox = new HBox(10, modify, remove);

                    {
                        modify.setStyle("-fx-background-color: #3985d8; -fx-text-fill: white;");
                        remove.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");

                        modify.setOnAction(event -> {
                            Booking booking = getTableView().getItems().get(getIndex());
                            openModifyBooking(booking);
                        });

                        remove.setOnAction(event -> {
                            Booking booking = getTableView().getItems().get(getIndex());
                            removeBooking(booking);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : buttonsBox);
                    }
                };
            }
        };

        ActionsColumn.setCellFactory(cellFactory);
    }


    private void openModifyBooking(Booking booking) {
        ModifyBookingController controller = Utility.loadPage2("bookinginterface/modifybookinginterface.fxml", bp);
        if (controller != null) {
            controller.setBookingTableController(this);
            controller.setBooking(booking);
            controller.setSelectedHotel(selectedHotelFromSearch, startDate, endDate);
            controller.setLoggedInClerk(loggedInClerk);
            controller.setMainApp(mainAppReference);
        }
    }


    private void removeBooking(Booking booking) {

        Request request = new Request("deleteBooking", booking);
        Response response = ClientConnectionManager.sendRequest(request);

        if ("200".equalsIgnoreCase(response.getStatus())) {
            FXUtility.alertInfo("Eliminado", "Reservación eliminada con éxito.");
            loadBookings();
        } else {
            FXUtility.alert("Error", "No se pudo eliminar la reservación.");
            logger.error("Error al eliminar reservación: {}", response != null ? response.getMessage() : "null");
        }
    }

    public void startPolling() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> Platform.runLater(this::loadBookings), 0, 2, TimeUnit.SECONDS);
    }

    public void stopPolling() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }

    @FXML
    void goBackOnAction(ActionEvent event) {
        try {
            SearchController controller = Utility.loadPage2("searchinterface/searchinterface.fxml", bp);
            if (controller != null) {
                controller.setStage(stage);
                controller.setLoggedInClerk(loggedInClerk);
                controller.setMainApp(mainAppReference);
                if (selectedHotelFromSearch != null) {
                    controller.setSearchCriteria(selectedHotelFromSearch, startDate, endDate);
                }
            }
        } catch (Exception e) {
            FXUtility.alert("Error", "No se pudo regresar a la búsqueda.");
            logger.error("Error al volver: ", e);
        }
    }




    @FXML
    private void onQuickSearch() {
        String bookingText = quickSearchField.getText().trim();
        stopPolling();
        if (bookingText.isEmpty()) {
            FXUtility.alert("Error", "Por favor ingrese el número de Reservación.");
            return;
        }

        if (selectedHotelFromSearch == null) {
            FXUtility.alert("Error", "Debe seleccionar un hotel para realizar la búsqueda de la reservación.");
            logger.warn("Intento de búsqueda rápida sin hotel seleccionado.");
            return;
        }

        try {
            int bookingNumber = Integer.parseInt(bookingText);
            int hotelId = selectedHotelFromSearch.getNumHotel(); // Obtener el hotelId del hotel seleccionado

            // Crear un mapa para enviar la clave compuesta (bookingNumber y hotelId)
            Map<String, Integer> bookingCriteria = new HashMap<>();
            bookingCriteria.put("bookingNumber", bookingNumber);
            bookingCriteria.put("hotelId", hotelId);

            // Crear la solicitud con el comando y los criterios
            Request request = new Request("getBookingById", bookingCriteria);
            logger.info("Enviando solicitud de búsqueda rápida para bookingNumber: {} y hotelId: {}", bookingNumber, hotelId);

            new Thread(() -> { // Ejecutar en un nuevo hilo para no bloquear la UI
                Response response = ClientConnectionManager.sendRequest(request);

                Platform.runLater(() -> { // Actualizar UI en el hilo de JavaFX
                    if ("200".equalsIgnoreCase(response.getStatus()) && response.getData() != null) {
                        Booking booking = new Gson().fromJson(new Gson().toJson(response.getData()), Booking.class);
                        bookingRegister.setItems(FXCollections.observableArrayList(booking)); // Mostrar solo la reserva encontrada
                        logger.info("Reserva encontrada: {}", booking.getBookingNumber());
                    } else {
                        FXUtility.alert("Error", "No existe una reservación con ese número en el hotel seleccionado.");
                        logger.warn("Reserva no encontrada para bookingNumber {} y hotelId {}. Mensaje del servidor: {}", bookingNumber, hotelId, response != null ? response.getMessage() : "null");
                    }
                });
            }).start();

        } catch (NumberFormatException e) {
            FXUtility.alert("Error", "El número de reservación debe ser un número entero válido.");
            logger.error("Error de formato en el número de reservación: {}", bookingText, e);
        } catch (Exception e) {
            FXUtility.alert("Error", "Ocurrió un error inesperado al buscar la reservación.");
            logger.error("Error inesperado en onQuickSearch: {}", e.getMessage(), e);
        }
    }

    @FXML
    void onClearSearch() {
        quickSearchField.clear();
        startPolling();
        loadBookings();
    }



}


