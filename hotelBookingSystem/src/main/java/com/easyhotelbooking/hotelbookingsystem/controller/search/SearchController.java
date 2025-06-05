package com.easyhotelbooking.hotelbookingsystem.controller.search;

import com.easyhotelbooking.hotelbookingsystem.Main;
import com.easyhotelbooking.hotelbookingsystem.controller.bookingregister.BookingRegisterController;
import com.easyhotelbooking.hotelbookingsystem.controller.bookingregister.BookingTableController;
import com.easyhotelbooking.hotelbookingsystem.controller.maininterface.MainInterfaceController;
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
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.scene.Scene;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.scene.text.Font;
import javafx.scene.control.Button;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;

public class SearchController {

    @FXML
    private Label searchResultsLabel;
    @FXML
    private VBox roomsDisplayVBox;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private Text hotelNameText;
    private MainInterfaceController mainController;
    private Stage stage;
    private Hotel selectedHotel;
    private Date startDate;
    private Date endDate;
    private final Gson gson = new Gson();
    private static final Logger logger = LogManager.getLogger(SearchController.class);
    @FXML
    private BorderPane bp;

    public void setMainController(MainInterfaceController mainController) {
        this.mainController = mainController;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
    private FrontDeskClerkDTO loggedInClerk; // Add a field to store the logged-in clerk
    private Main mainAppReference;

    public void setLoggedInClerk(FrontDeskClerkDTO loggedInClerk) {
        this.loggedInClerk = loggedInClerk;
        logger.info("HotelOptionsController: Logged-in clerk received: {}", loggedInClerk.getUser());
    }

    public void setMainApp(Main mainAppReference) {
        this.mainAppReference = mainAppReference;
        logger.info("HotelOptionsController: Main application reference set.");
    }

    public void setSearchCriteria(Hotel hotel, Date startDate, Date endDate) {
        this.selectedHotel = hotel;
        this.startDate = startDate;
        this.endDate = endDate;
        performSearch(); // Se llama aquí cuando se establece el hotel por primera vez
    }

    public Label getSearchResultsLabel() {
        return searchResultsLabel;
    }

    public void setSearchResultsLabel(Label searchResultsLabel) {
        this.searchResultsLabel = searchResultsLabel;
    }

    public Hotel getSelectedHotel() {
        return selectedHotel;
    }

    public void setSelectedHotel(Hotel selectedHotel) {
        this.selectedHotel = selectedHotel;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public void refreshRoomDisplay() {
        if (selectedHotel != null) {

            performSearch();
            logger.info("refreshRoomDisplay llamado. Recargando habitaciones para hotel: " + selectedHotel.getHotelName());
        } else {

            logger.warn("refreshRoomDisplay llamado pero no hay hotel seleccionado. No se recargan las habitaciones.");
            searchResultsLabel.setText("Por favor, seleccione un hotel para buscar habitaciones.");
            roomsDisplayVBox.getChildren().clear();
        }
    }


    @FXML
    public void initialize() {

    }

    private void performSearch() {
        if (selectedHotel == null || startDate == null || endDate == null) {
            searchResultsLabel.setText("Faltan datos para la búsqueda. Asegúrese de seleccionar un hotel y fechas.");
            roomsDisplayVBox.getChildren().clear();
            return;
        }

        roomsDisplayVBox.getChildren().clear();
        searchResultsLabel.setText("Buscando habitaciones disponibles en " + selectedHotel.getHotelName() + "...");
        hotelNameText.setText("Hotel: " + selectedHotel.getHotelName());

        // Convertir fechas Date -> ISO 8601 string
        DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        ZonedDateTime startZoned = ZonedDateTime.ofInstant(startDate.toInstant(), ZoneId.systemDefault());
        ZonedDateTime endZoned = ZonedDateTime.ofInstant(endDate.toInstant(), ZoneId.systemDefault());

        String isoStart = startZoned.format(formatter);
        String isoEnd = endZoned.format(formatter);

        Booking bookingCriteria = new Booking();
        bookingCriteria.setHotelId(selectedHotel.getNumHotel());
        bookingCriteria.setStartDate(Date.from(startZoned.toInstant()));
        bookingCriteria.setEndDate(Date.from(endZoned.toInstant()));

        Request request = new Request("getAvailableRoomsByDate", bookingCriteria);


        logger.info("Enviando solicitud de búsqueda para hotel ID: {}, desde {} hasta {}",
                selectedHotel.getNumHotel(), isoStart, isoEnd);

        new Thread(() -> {
            Response response = ClientConnectionManager.sendRequest(request);

            Platform.runLater(() -> {
                if ("200".equalsIgnoreCase(response.getStatus()) && response.getData() != null) {
                    try {
                        List<Room> rooms = gson.fromJson(
                                gson.toJson(response.getData()),
                                new TypeToken<List<Room>>() {
                                }.getType()
                        );

                        if (rooms.isEmpty()) {
                            searchResultsLabel.setText("No se encontraron habitaciones disponibles para los criterios seleccionados.");
                        } else {
                            searchResultsLabel.setText("Se encontraron " + rooms.size() + " habitaciones disponibles:");
                            for (Room room : rooms) {
                                roomsDisplayVBox.getChildren().add(createRoomCard(room));
                            }
                        }

                    } catch (Exception e) {
                        logger.error("Error al procesar los resultados: " + e.getMessage(), e);
                        searchResultsLabel.setText("Error al mostrar los resultados.");
                    }
                } else {
                    logger.warn("Respuesta del servidor no exitosa: {}", response.getMessage());
                    searchResultsLabel.setText("No se pudieron obtener habitaciones.");
                }
            });
        }).start();

    }


    private HBox createRoomCard(Room room) {
        ImageView roomImageView = new ImageView();
        roomImageView.setFitHeight(150.0);
        roomImageView.setFitWidth(180.0);
        roomImageView.setPreserveRatio(true);
        HBox.setMargin(roomImageView, new Insets(0, 15.0, 0, 0));

        if (room.getImagesPaths() != null && !room.getImagesPaths().isEmpty()) {
            String serverImagePath = room.getImagesPaths().get(0);

            new Thread(() -> {
                Request request = new Request("downloadRoomImage", serverImagePath);
                Response response = ClientConnectionManager.sendRequest(request);

                Platform.runLater(() -> {
                    if ("200".equalsIgnoreCase(response.getStatus()) && response.getData() != null) {
                        try {
                            byte[] imageData;
                            if (response.getData() instanceof List) {
                                List<Double> doubleList = gson.fromJson(gson.toJson(response.getData()), new TypeToken<List<Double>>() {
                                }.getType());
                                imageData = new byte[doubleList.size()];
                                for (int i = 0; i < doubleList.size(); i++) {
                                    imageData[i] = doubleList.get(i).byteValue();
                                }
                            } else if (response.getData() instanceof byte[]) {
                                imageData = (byte[]) response.getData();
                            } else {
                                logger.warn("Tipo de dato inesperado para imagen del servidor en createRoomCard: {}. Saltando imagen {}.", response.getData().getClass().getName(), serverImagePath);
                                return;
                            }
                            try (ByteArrayInputStream bis = new ByteArrayInputStream(imageData)) {
                                Image img = new Image(bis, 180, 150, true, true);
                                roomImageView.setImage(img);
                            } catch (Exception e) {
                                logger.error("Error al crear Image desde bytes en createRoomCard: " + e.getMessage(), e);
                            }
                        } catch (Exception e) {
                            logger.error("Error al procesar datos de imagen en createRoomCard: " + e.getMessage(), e);
                        }
                    } else {
                        logger.warn("No se pudo descargar la imagen para la tarjeta: " + serverImagePath + ". " + response.getMessage());
                    }
                });
            }).start();
        } else {
            logger.info("No hay rutas de imagen para la habitación {}. Dejando el ImageView vacío.", room.getRoomNumber());
        }

        VBox roomDetailsVBox = new VBox();
        double roomDetailsMinBaseHeight = roomImageView.getFitHeight(); // 150.0
        roomDetailsVBox.setMinHeight(roomDetailsMinBaseHeight);
        roomDetailsVBox.setPrefHeight(roomDetailsMinBaseHeight);
        roomDetailsVBox.setMaxHeight(Region.USE_COMPUTED_SIZE);


        Label roomTypeLabel = new Label("Tipo: " + room.getStyle().toString());
        roomTypeLabel.setFont(new Font("System Bold", 18.0));
        VBox.setMargin(roomTypeLabel, new Insets(0, 0, 5.0, 0));

        Label roomNumberLabel = new Label("Número de Habitación: " + room.getRoomNumber());
        roomNumberLabel.setWrapText(true);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        VBox priceButtonVBox = new VBox(5.0);
        priceButtonVBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        Label priceTitleLabel = new Label("Precio por noche:");
        priceTitleLabel.setFont(new Font(14.0));

        Label roomPriceLabel = new Label(String.format("$%.2f", room.getRoomPrice()));
        roomPriceLabel.setFont(new Font("System Bold", 20.0));

        HBox buttonContainer = new HBox(10.0);
        buttonContainer.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        Button viewDescriptionButton = new Button("Ver Descripción");
        viewDescriptionButton.setStyle("-fx-background-color: #007BFF; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 8 15;");
        viewDescriptionButton.setOnAction(event -> showDescriptionWindow(room.getDetailedDescription(), room.getRoomNumber()));

        Button selectRoomButton = new Button("Reservar Habitación");
        selectRoomButton.setStyle("-fx-background-color: #28A745; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 8 15;");
        selectRoomButton.setOnAction(event -> {
            BookingRegisterController bookingRegController = Utility.loadPage2("bookinginterface/bookinginterface.fxml", bp);
            if (bookingRegController != null) {
                // Pasa el hotel seleccionado a la instancia de BookingRegisterController
                bookingRegController.setSelectedHotelFromSearch(this.selectedHotel, startDate, endDate);
                bookingRegController.setSelectedRoomFromSearch(room);
                bookingRegController.setMainController(mainController);
                bookingRegController.setParentBp(bp); // para poder regresar después
                bookingRegController.setSearchController(this); // si necesitas volver aquí
                bookingRegController.setLoggedInClerk(this.loggedInClerk);
                bookingRegController.setMainApp(this.mainAppReference);
            }
        });

        buttonContainer.getChildren().addAll(viewDescriptionButton, selectRoomButton);

        priceButtonVBox.getChildren().addAll(priceTitleLabel, roomPriceLabel, buttonContainer);

        roomDetailsVBox.getChildren().addAll(roomTypeLabel, roomNumberLabel, spacer, priceButtonVBox);

        HBox roomCardHBox = new HBox();
        roomCardHBox.setAlignment(javafx.geometry.Pos.TOP_LEFT);
        roomCardHBox.setStyle("-fx-border-color: #D3D3D3; -fx-border-radius: 5; -fx-background-color: white; -fx-padding: 10;");
        roomCardHBox.getChildren().addAll(roomImageView, roomDetailsVBox);

        double cardVerticalPadding = 20.0;
        double minCardHeight = roomImageView.getFitHeight() + cardVerticalPadding;

        roomCardHBox.setMinHeight(minCardHeight);
        roomCardHBox.setPrefHeight(Region.USE_COMPUTED_SIZE);
        roomCardHBox.setMaxHeight(Region.USE_COMPUTED_SIZE);

        double desiredCardWidth = 570.0;
        roomCardHBox.setPrefWidth(desiredCardWidth);
        roomCardHBox.setMaxWidth(desiredCardWidth);
        roomCardHBox.setMinWidth(desiredCardWidth);

        double roomDetailsWidth = desiredCardWidth - roomImageView.getFitWidth() - HBox.getMargin(roomImageView).getRight() - 10;
        roomDetailsVBox.setPrefWidth(roomDetailsWidth);
        roomDetailsVBox.setMaxWidth(roomDetailsWidth);
        roomDetailsVBox.setMinWidth(roomDetailsWidth);

        return roomCardHBox;
    }

    private void showDescriptionWindow(String description, int roomNumber) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/searchinterface/roomdescription.fxml"));
            VBox descriptionRoot = loader.load();

            RoomDescriptionController controller = loader.getController();
            controller.setDescriptionText(description);
            controller.setRoomNumber(roomNumber);

            Stage descriptionStage = new Stage();
            descriptionStage.setTitle("Descripción de la Habitación");
            descriptionStage.initModality(Modality.APPLICATION_MODAL);
            descriptionStage.initOwner(stage);
            descriptionStage.setScene(new Scene(descriptionRoot));
            descriptionStage.setResizable(false);

            descriptionStage.showAndWait();
        } catch (IOException e) {
            logger.error("Error al cargar la ventana de descripción: " + e.getMessage(), e);
            FXUtility.alert("Error", "No se pudo cargar la descripción.Por favor, intente de nuevo más tarde.");
        }
    }

    @FXML
    public void goBackOnAction(ActionEvent event) {

        if (mainAppReference != null && loggedInClerk != null) {
            mainAppReference.loadMainInterface(loggedInClerk);
            logger.info("SearchController: Volviendo a la interfaz principal con el recepcionista loggeado.");
        } else {
            logger.error("SearchController: No se puede volver a la interfaz principal. mainAppReference o loggedInClerk es null.");
            FXUtility.alert("Error de Navegación", "No se pudo regresar a la interfaz principal. Por favor, reinicie la aplicación.");
        }
    }

    @FXML
    public void refreshWithFilterOnAction() {

        performSearch();
    }

    @FXML
    public void tablaReservacionesOnAction() {
        BookingTableController controller = Utility.loadPage2("bookinginterface/bookingtable.fxml", bp);
        if (controller != null) {
            controller.setSelectedHotelFromSearchTable(selectedHotel, startDate, endDate);
            controller.setStage(stage);
            controller.setMainApp(mainAppReference);
            controller.setLoggedInClerk(loggedInClerk);
        }
    }
}