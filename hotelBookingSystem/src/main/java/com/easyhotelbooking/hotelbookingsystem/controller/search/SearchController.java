package com.easyhotelbooking.hotelbookingsystem.controller.search;

import com.easyhotelbooking.hotelbookingsystem.controller.maininterface.MainInterfaceController;
import com.easyhotelbooking.hotelbookingsystem.controller.roomregister.RoomConsultController;
import com.easyhotelbooking.hotelbookingsystem.socket.ClientConnectionManager;
import com.easyhotelbooking.hotelbookingsystem.util.FXUtility;
import com.easyhotelbooking.hotelbookingsystem.util.Utility;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import hotelbookingcommon.domain.Hotel;
import hotelbookingcommon.domain.Request;
import hotelbookingcommon.domain.Response;
import hotelbookingcommon.domain.Room;
import hotelbookingcommon.domain.RoomStatus;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader; // Importar FXMLLoader
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.Modality; // Importar Modality
import javafx.scene.Scene; // Importar Scene

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.io.ByteArrayInputStream;
import java.io.IOException; // Importar IOException
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.scene.text.Font;
import javafx.scene.control.Button;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;

public class SearchController {

    @FXML private Label searchResultsLabel;
    @FXML private VBox roomsDisplayVBox;
    @FXML private ScrollPane scrollPane;
    @FXML private Text hotelNameText;
    private MainInterfaceController mainController;
    private Stage stage;
    private Hotel selectedHotel;
    private LocalDate fromDate;

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

    public void setSearchCriteria(Hotel hotel) {
        this.selectedHotel = hotel;
        performSearch();
    }

    @FXML
    public void initialize() {
        // Inicializa cualquier cosa que no dependa de los criterios de búsqueda.

    }

    private void performSearch() {
        if (selectedHotel == null) {
            searchResultsLabel.setText("No se seleccionó un hotel para la búsqueda.");
            return;
        }

        roomsDisplayVBox.getChildren().clear();
        searchResultsLabel.setText("Buscando habitaciones en " + selectedHotel.getHotelName() + "...");
        hotelNameText.setText("Hotel: "+selectedHotel.getHotelName());
        Request request = new Request("getRoomsByHotelId", selectedHotel.getNumHotel());
        logger.info("Enviando solicitud de búsqueda para hotel ID: " + selectedHotel.getNumHotel());

        new Thread(() -> {
            Response response = ClientConnectionManager.sendRequest(request);

            Platform.runLater(() -> {
                if ("200".equalsIgnoreCase(response.getStatus()) && response.getData() != null) {
                    try {
                        List<Room> rooms = gson.fromJson(gson.toJson(response.getData()), new TypeToken<List<Room>>() {}.getType());

                        List<Room> filteredRooms = rooms.stream()
                                .filter(room -> room.getStatus() == RoomStatus.AVAILABLE)
                                .collect(Collectors.toList());

                        if (filteredRooms.isEmpty()) {
                            searchResultsLabel.setText("No se encontraron habitaciones disponibles para los criterios seleccionados en " + selectedHotel.getHotelName() + ".");
                        } else {
                            searchResultsLabel.setText("Se encontraron " + filteredRooms.size() + " habitaciones disponibles en " + selectedHotel.getHotelName() + ":");
                            for (Room room : filteredRooms) {
                                roomsDisplayVBox.getChildren().add(createRoomCard(room));
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Error al procesar resultados de búsqueda de habitaciones: " + e.getMessage(), e);
                        searchResultsLabel.setText("Error al cargar habitaciones.");
                    }
                } else {
                    logger.warn("Error al buscar habitaciones: " + response.getMessage());
                    searchResultsLabel.setText("Error al buscar habitaciones o no se encontraron.");
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
                                List<Double> doubleList = gson.fromJson(gson.toJson(response.getData()), new TypeToken<List<Double>>() {}.getType());
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

        // CAMBIO: Label de descripción por Label de número de habitación
        Label roomNumberLabel = new Label("Número de Habitación: " + room.getRoomNumber());
        roomNumberLabel.setWrapText(true);

        // Region para empujar el contenido hacia abajo
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        VBox priceButtonVBox = new VBox(5.0);
        priceButtonVBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        Label priceTitleLabel = new Label("Precio por noche:");
        priceTitleLabel.setFont(new Font(14.0));

        Label roomPriceLabel = new Label(String.format("$%.2f", room.getRoomPrice()));
        roomPriceLabel.setFont(new Font("System Bold", 20.0));

        HBox buttonContainer = new HBox(10.0); // Contenedor para los botones con espaciado
        buttonContainer.setAlignment(javafx.geometry.Pos.CENTER_RIGHT); // Alinea los botones a la derecha

        Button viewDescriptionButton = new Button("Ver Descripción");
        viewDescriptionButton.setStyle("-fx-background-color: #007BFF; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 8 15;");
        viewDescriptionButton.setOnAction(event -> showDescriptionWindow(room.getDetailedDescription(),room.getRoomNumber())); // Llama al nuevo método

        Button selectRoomButton = new Button("Seleccionar Habitación");
        selectRoomButton.setStyle("-fx-background-color: #28A745; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 8 15;");
        selectRoomButton.setOnAction(event -> {
            System.out.println("Se seleccionó la habitación: " + room.getRoomNumber() + " (" + room.getStyle() + ")");
            // Lógica para reservar la habitación
        });

        buttonContainer.getChildren().addAll(viewDescriptionButton, selectRoomButton); // Añade ambos botones

        priceButtonVBox.getChildren().addAll(priceTitleLabel, roomPriceLabel, buttonContainer); // Añade el contenedor de botones

        // Añadir el nuevo Label y el spacer al roomDetailsVBox
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

    // Nuevo método para mostrar la ventana de descripción
    private void showDescriptionWindow(String description,int roomNumber) {
        try {
            // Cargar el FXML de la nueva ventana
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/searchinterface/roomdescription.fxml"));
            VBox descriptionRoot = loader.load();

            // Obtener el controlador y pasar la descripción
            RoomDescriptionController controller = loader.getController();
            controller.setDescriptionText(description);
            controller.setRoomNumber(roomNumber);
            // Crear una nueva Stage (ventana)
            Stage descriptionStage = new Stage();
            descriptionStage.setTitle("Descripción de la Habitación");
            descriptionStage.initModality(Modality.APPLICATION_MODAL); // Hace que sea modal
            descriptionStage.initOwner(stage); // Establece la ventana principal como propietaria
            descriptionStage.setScene(new Scene(descriptionRoot));
            descriptionStage.setResizable(false); // La ventana de descripción no debería ser redimensionable

            descriptionStage.showAndWait(); // Muestra la ventana y espera a que se cierre
        } catch (IOException e) {
            logger.error("Error al cargar la ventana de descripción: " + e.getMessage(), e);
            // Opcional: Mostrar un Alert al usuario si la ventana no se puede abrir
            FXUtility.alert("Error", "No se pudo cargar la descripción.Por favor, intente de nuevo más tarde.");
        }
    }
     @FXML
    public void goBackOnAction(ActionEvent event) {
        Utility.loadPage2("maininterface.fxml",bp);
    }
    @FXML
    public void refreshWithFilterOnAction(){

    }
}
