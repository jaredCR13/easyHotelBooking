package com.easyhotelbooking.hotelbookingsystem.controller.roomregister;

import com.easyhotelbooking.hotelbookingsystem.controller.maininterface.MainInterfaceController;
import com.easyhotelbooking.hotelbookingsystem.socket.ClientConnectionManager;
import com.easyhotelbooking.hotelbookingsystem.util.FXUtility;
import com.easyhotelbooking.hotelbookingsystem.util.Utility;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import hotelbookingcommon.domain.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
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
import javafx.stage.Window;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RoomOptionsController {

    @FXML private BorderPane bp;
    @FXML private Button goBack;
    @FXML private ComboBox<RoomStatus> statusCombo;
    @FXML private ComboBox<RoomStyle> styleCombo;
    @FXML private ComboBox<Hotel> hotelComboBox;
    @FXML private TableView<Room> roomRegister;
    @FXML private TableColumn<Room, Integer> roomNumberColumn;
    @FXML private TableColumn<Room, Double> priceColumn;
    @FXML private TableColumn<Room, String> descriptionColumn;
    @FXML private TableColumn<Room, RoomStatus> statusColumn;
    @FXML private TableColumn<Room, RoomStyle> styleColumn;
    @FXML private TableColumn<Room, Integer> hotelIdColumn; // Nueva columna para mostrar el ID del hotel
    @FXML private TableColumn<Room, Void> actionColumn;
    @FXML private TextField quickSearchField;
    @FXML private ComboBox<Hotel> quickHotelCombo;
    private ScheduledExecutorService scheduler;

    private static final Logger logger = LogManager.getLogger(RoomOptionsController.class);

    private MainInterfaceController mainController;
    private Stage currentStage;
    private Window stage;

    public void setStage(Stage stage) {
        this.currentStage = stage;
    }

    public BorderPane getBp() {
        return bp;
    }

    public void setMainController(MainInterfaceController mainController) {
        this.mainController = mainController;

        loadRoomsIntoRegister();
    }


    @FXML
    public void initialize() {

        roomNumberColumn.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("roomPrice"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("detailedDescription"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        styleColumn.setCellValueFactory(new PropertyValueFactory<>("style"));
        hotelIdColumn.setCellValueFactory(new PropertyValueFactory<>("hotelId"));

        addButtonsToRoomTable();
        loadHotelsIntoQuickCombo();

        quickHotelCombo.setConverter(new StringConverter<Hotel>() {
        public String toString(Hotel hotel) {
            return hotel != null ? hotel.getHotelName() : "";
        }

        @Override
        public Hotel fromString(String string) {
            return quickHotelCombo.getItems().stream()
                    .filter(h -> h.getHotelName().equals(string))
                    .findFirst()
                    .orElse(null);
        }
        });
    }



    private void addButtonsToRoomTable() {
        Callback<TableColumn<Room, Void>, TableCell<Room, Void>> cellFactory = new Callback<TableColumn<Room, Void>, TableCell<Room, Void>>() {
            @Override
            public TableCell<Room, Void> call(final TableColumn<Room, Void> param) {
                return new TableCell<Room, Void>() {

                    private final Button consult = new Button("Consult");
                    private final Button modify = new Button("Modify");
                    private final Button remove = new Button("Remove");

                    private final HBox buttonsBox = new HBox(10, consult, modify, remove);

                    {
                        consult.setStyle("-fx-background-color: #5fabff; -fx-text-fill: white;");
                        modify.setStyle("-fx-background-color: #3985d8; -fx-text-fill: white;");
                        remove.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");

                        consult.setOnAction(event -> {
                            Room room = getTableView().getItems().get(getIndex());
                            openConsultRoom(room);
                        });

                        modify.setOnAction(event -> {
                            Room room = getTableView().getItems().get(getIndex());
                            openModifyOnAction(room);
                        });

                        remove.setOnAction(event -> {
                            Room room = getTableView().getItems().get(getIndex());
                            removeRoomOnAction(room);

                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(buttonsBox);
                        }
                    }
                };
            }
        };

        actionColumn.setCellFactory(cellFactory);
    }



    @FXML
    void goBackOnAction() {
        Utility.loadFullView("maininterface.fxml", goBack);
    }

    private void openModifyOnAction(Room room) {
        Request request = new Request("getRoom", room.getRoomNumber());
        Response response = ClientConnectionManager.sendRequest(request);

        if (response != null && "200".equalsIgnoreCase(response.getStatus())) {
            Room completeRoom = new Gson().fromJson(new Gson().toJson(response.getData()), Room.class);

            ModifyRoomController controller = Utility.loadPage2("roominterface/modifyroom.fxml", bp);

            if (controller != null) {
                controller.setParentBp(bp);
                controller.setMainController(mainController);
                controller.setRoomOptionsController(this);
                controller.setRoom(completeRoom);
            }
        } else {
            FXUtility.alert("Error", "No se pudo cargar la información completa de la habitación.");
        }
    }

    @FXML
    void registerRoomOnAction() {
        RoomRegisterController controller = Utility.loadPage2("roominterface/registerroom.fxml", bp);
        if (controller != null) {
            controller.setParentBp(bp);
            controller.setMainController(mainController);
            controller.setRoomOptionsController(this);
        }
    }

    private void openConsultRoom(Room room) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/roominterface/roomconsult.fxml"));
            Parent root = loader.load();

            RoomConsultController consultController = loader.getController();
            consultController.setRoom(room);

            Stage stage = new Stage();
            stage.setTitle("Consult Room");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void removeRoomOnAction(Room room){
        try {
            mainController.deleteRoom(room.getRoomNumber());
            loadRoomsIntoRegister();
        } catch (Exception e) {
            mostrarAlertaError("Error", "Ocurrió un error al eliminar el hotel.");
        }
    }

    private void loadHotelsIntoQuickCombo() {
        Request request = new Request("getHotels", null);
        Response response = ClientConnectionManager.sendRequest(request);

        if ("200".equalsIgnoreCase(response.getStatus()) && response.getData() != null) {
            List<Hotel> hotelList = new Gson().fromJson(
                    new Gson().toJson(response.getData()),
                    new TypeToken<List<Hotel>>() {}.getType()
            );
            quickHotelCombo.getItems().clear();
            quickHotelCombo.getItems().addAll(hotelList);
        } else {
            mostrarAlertaError("Error", "No se pudieron cargar los hoteles para búsqueda rápida.");
        }
    }

    @FXML
    private void onQuickSearch() {
        String roomText = quickSearchField.getText().trim();
        Hotel selectedHotel = quickHotelCombo.getValue();

        if (roomText.isEmpty() || selectedHotel == null) {
            mostrarAlertaError("Error", "Por favor ingrese el número de habitación y seleccione un hotel.");
            return;
        }

        try {
            int roomNumber = Integer.parseInt(roomText);
            Request request = new Request("getRoom", roomNumber);
            Response response = ClientConnectionManager.sendRequest(request);

            if ("200".equalsIgnoreCase(response.getStatus()) && response.getData() != null) {
                Room room = new Gson().fromJson(new Gson().toJson(response.getData()), Room.class);

                if (room.getHotelId() == selectedHotel.getNumHotel()) {
                    roomRegister.setItems(FXCollections.observableArrayList(room));
                } else {
                    mostrarAlertaError("Error", "La habitación no pertenece al hotel seleccionado.");
                }
            } else {
                mostrarAlertaError("Error", "No existe una habitación con ese número.");
            }

        } catch (NumberFormatException e) {
            mostrarAlertaError("Error", "El número de habitación debe ser un número entero.");
        }
    }

    @FXML
    void onClearSearch() {
        quickSearchField.clear();
        quickHotelCombo.setValue(null);
        loadRoomsIntoRegister();
    }

    public void loadRoomsIntoRegister() {
        Request request = new Request("getRooms", null);
        Response response = ClientConnectionManager.sendRequest(request);
        if ("200".equalsIgnoreCase(response.getStatus()) && response.getData() != null) {
            List<Room> rooms = new Gson().fromJson(new Gson().toJson(response.getData()), new TypeToken<List<Room>>() {}.getType());
            roomRegister.setItems(FXCollections.observableArrayList(rooms));
        } else {
            mostrarAlertaError("Error", "No se pudieron cargar las habitaciones.");
            logger.error("Error al cargar habitaciones en la tabla: {}", response != null ? response.getMessage() : "Desconocido");
        }
    }

    //Actaulización en tiempo real con intervalo de tiempo para el uso de servidor en diferentes computadoras
    public void startPolling() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            Platform.runLater(() -> loadRoomsIntoRegister());
        }, 0, 2, TimeUnit.SECONDS); // cada 2 segundos se actualiza
    }

    public void stopPolling() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }

    private void mostrarAlertaError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);

        if (this.stage != null) {
            alert.initOwner(this.stage);
        }
        alert.showAndWait();
    }

}