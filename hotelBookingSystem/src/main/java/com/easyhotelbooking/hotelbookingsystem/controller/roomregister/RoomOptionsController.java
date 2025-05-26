package com.easyhotelbooking.hotelbookingsystem.controller.roomregister;

import com.easyhotelbooking.hotelbookingsystem.controller.MainInterfaceController;
import com.easyhotelbooking.hotelbookingsystem.socket.ClientConnectionManager;
import com.easyhotelbooking.hotelbookingsystem.util.Utility;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import hotelbookingcommon.domain.*;
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
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class RoomOptionsController {

    @FXML private BorderPane bp;

    @FXML private Button goBack;


    @FXML private ComboBox<RoomStatus> statusCombo;
    @FXML private ComboBox<RoomStyle> styleCombo;
    @FXML private ComboBox<Hotel> hotelComboBox; // Ya tienes el ComboBox de Hoteles

    @FXML private TableView<Room> roomRegister;
    @FXML private TableColumn<Room, Integer> roomNumberColumn;
    @FXML private TableColumn<Room, Double> priceColumn;
    @FXML private TableColumn<Room, String> descriptionColumn;
    @FXML private TableColumn<Room, RoomStatus> statusColumn;
    @FXML private TableColumn<Room, RoomStyle> styleColumn;
    @FXML private TableColumn<Room, Integer> hotelIdColumn; // Nueva columna para mostrar el ID del hotel
    @FXML private TableColumn<Room, Void> actionColumn;
    private static final Logger logger = LogManager.getLogger(RoomOptionsController.class);

    private MainInterfaceController mainController;


    public BorderPane getBp() {
        return bp;
    }

    public void setMainController(MainInterfaceController mainController) {
        this.mainController = mainController;
        // Se llama loadRoomsIntoRegister() solo una vez al cargar el controlador
        // para que la tabla se llene con datos
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

        addButtonsToRoomTable(); // <-- aquí

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
                            // Rellenar campos para modificación
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

    private void openModifyOnAction(Room room) {
        Request request = new Request("getRoom", room.getRoomNumber()); // Usa el protocolo existente
        Response response = ClientConnectionManager.sendRequest(request);

        if (response != null && "200".equalsIgnoreCase(response.getStatus())) {
            Room completeRoom = new Gson().fromJson(new Gson().toJson(response.getData()), Room.class);

            ModifyRoomController controller = Utility.loadPage2("modifyroom.fxml", bp);

            if (controller != null) {
                controller.setParentBp(bp);
                controller.setMainController(mainController);
                controller.setRoomOptionsController(this);
                controller.setRoom(completeRoom); // ✅ Este ahora sí debería traer las imágenes
            }
        } else {
            mostrarAlerta("Error", "No se pudo cargar la información completa de la habitación.");
        }
    }




    @FXML
    void goBackOnAction() {
        Utility.loadFullView("maininterface.fxml", goBack);
    }

    @FXML
    void registerRoomOnAction() {
        RoomRegisterController controller = Utility.loadPage2("registerroom.fxml", bp);
        if (controller != null) {
            controller.setParentBp(bp); // Pasa el BorderPane para poder volver atrás desde "Cancel"
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
            mostrarAlerta("Error", "Ocurrió un error al eliminar el hotel.");
        }
    }

    public void loadRoomsIntoRegister() {
        Request request = new Request("getRooms", null); // Asegúrate de que el endpoint sea "getRooms"
        Response response = ClientConnectionManager.sendRequest(request);
        if ("200".equalsIgnoreCase(response.getStatus()) && response.getData() != null) {
            List<Room> rooms = new Gson().fromJson(new Gson().toJson(response.getData()), new TypeToken<List<Room>>() {}.getType());
            roomRegister.setItems(FXCollections.observableArrayList(rooms));
        } else {
            mostrarAlerta("Error", "No se pudieron cargar las habitaciones.");
            logger.error("Error al cargar habitaciones en la tabla: {}", response != null ? response.getMessage() : "Desconocido");
        }
    }

    private void mostrarAlerta(String titulo, String contenido) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}