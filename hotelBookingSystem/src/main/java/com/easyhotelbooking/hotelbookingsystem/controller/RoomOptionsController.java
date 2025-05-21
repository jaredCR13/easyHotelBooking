package com.easyhotelbooking.hotelbookingsystem.controller;

import com.easyhotelbooking.hotelbookingsystem.socket.ClientConnectionManager;
import com.easyhotelbooking.hotelbookingsystem.util.Utility;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import hotelbookingcommon.domain.Request;
import hotelbookingcommon.domain.Response;
import hotelbookingcommon.domain.Room;
import hotelbookingcommon.domain.RoomStatus;
import hotelbookingcommon.domain.RoomStyle;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;

public class RoomOptionsController {

    @FXML private BorderPane bp;

    @FXML private Button consultButton;
    @FXML private Button registerButton;
    @FXML private Button modifyButton;
    @FXML private Button removeButton;
    @FXML private Button goBack;

    @FXML private TextField roomNumberTf;
    @FXML private TextField priceTf;
    @FXML private TextField descriptionTf;

    @FXML private ComboBox<RoomStatus> statusCombo;
    @FXML private ComboBox<RoomStyle> styleCombo;

    @FXML private TableView<Room> roomRegister;
    @FXML private TableColumn<Room, Integer> roomNumberColumn;
    @FXML private TableColumn<Room, Double> priceColumn;
    @FXML private TableColumn<Room, String> descriptionColumn;
    @FXML private TableColumn<Room, RoomStatus> statusColumn;
    @FXML private TableColumn<Room, RoomStyle> styleColumn;

    private static final Logger logger = LogManager.getLogger(RoomOptionsController.class);

    private MainInterfaceController mainController;

    public void setMainController(MainInterfaceController mainController) {
        this.mainController = mainController;
        loadRoomsIntoRegister();
    }

    @FXML
    public void initialize() {
        statusCombo.getItems().addAll(RoomStatus.values());
        styleCombo.getItems().addAll(RoomStyle.values());

        roomNumberColumn.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("roomPrice"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("detailedDescription"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        styleColumn.setCellValueFactory(new PropertyValueFactory<>("style"));

        roomRegister.setOnMouseClicked(event -> {
            Room selected = roomRegister.getSelectionModel().getSelectedItem();
            if (selected != null) {
                roomNumberTf.setText(String.valueOf(selected.getRoomNumber()));
                priceTf.setText(String.valueOf(selected.getRoomPrice()));
                descriptionTf.setText(selected.getDetailedDescription());
                statusCombo.setValue(selected.getStatus());
                styleCombo.setValue(selected.getStyle());
            }
        });
    }

    @FXML
    void goBackOnAction() {
        Utility.loadFullView("maininterface.fxml", goBack);
    }

    @FXML
    void registerRoomOnAction() {
        try {
            String numberStr = roomNumberTf.getText();
            String priceStr = priceTf.getText();
            String description = descriptionTf.getText();

            if (numberStr.isEmpty() || priceStr.isEmpty()) {
                mostrarAlerta("Error", "Por favor, complete todos los campos obligatorios.");
                return;
            }

            int number = Integer.parseInt(numberStr);
            double price = Double.parseDouble(priceStr);
            RoomStatus status = statusCombo.getValue();
            RoomStyle style = styleCombo.getValue();

            if (status == null || style == null) {
                mostrarAlerta("Error", "Seleccione estado y estilo de la habitación.");
                return;
            }

            Room room = new Room(number, price, description, status, style, Collections.emptyList());

            mainController.registerRoom(room);
            loadRoomsIntoRegister();
            clearFields();

        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Número de habitación o precio inválido.");
        } catch (Exception e) {
            logger.error("Error al registrar habitación: {}", e.getMessage());
            mostrarAlerta("Error", "Error al registrar habitación: " + e.getMessage());
        }
    }

    @FXML
    void consultRoomOnAction() {
        try {
            String numberStr = roomNumberTf.getText();
            if (numberStr.isEmpty()) {
                mostrarAlerta("Error", "Ingrese el número de habitación a consultar.");
                return;
            }

            int number = Integer.parseInt(numberStr);
            mainController.consultRoom(number);

        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Número de habitación inválido.");
        } catch (Exception e) {
            logger.error("Error al consultar habitación: {}", e.getMessage());
            mostrarAlerta("Error", "Error al consultar habitación: " + e.getMessage());
        }
    }

    @FXML
    void modifyRoomOnAction() {
        try {
            String numberStr = roomNumberTf.getText();
            String priceStr = priceTf.getText();
            String description = descriptionTf.getText();

            if (numberStr.isEmpty() || priceStr.isEmpty()) {
                mostrarAlerta("Error", "Complete todos los campos obligatorios.");
                return;
            }

            int number = Integer.parseInt(numberStr);
            double price = Double.parseDouble(priceStr);
            RoomStatus status = statusCombo.getValue();
            RoomStyle style = styleCombo.getValue();

            if (status == null || style == null) {
                mostrarAlerta("Error", "Seleccione estado y estilo de la habitación.");
                return;
            }

            Room room = new Room(number, price, description, status, style, Collections.emptyList());

            mainController.updateRoom(room);
            loadRoomsIntoRegister();
            clearFields();

        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Número o precio inválido.");
        } catch (Exception e) {
            logger.error("Error al modificar habitación: {}", e.getMessage());
            mostrarAlerta("Error", "Error al modificar habitación: " + e.getMessage());
        }
    }

    @FXML
    void removeRoomOnAction() {
        try {
            String numberStr = roomNumberTf.getText();
            if (numberStr.isEmpty()) {
                mostrarAlerta("Error", "Ingrese el número de habitación a eliminar.");
                return;
            }

            int number = Integer.parseInt(numberStr);
            mainController.deleteRoom(number);
            loadRoomsIntoRegister();
            clearFields();

        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Número inválido.");
        } catch (Exception e) {
            logger.error("Error al eliminar habitación: {}", e.getMessage());
            mostrarAlerta("Error", "Error al eliminar habitación: " + e.getMessage());
        }
    }

    void clearFields() {
        roomNumberTf.clear();
        priceTf.clear();
        descriptionTf.clear();
        statusCombo.setValue(null);
        styleCombo.setValue(null);
    }

    private void loadRoomsIntoRegister() {
        Request request = new Request("getRooms", null);
        Response response = ClientConnectionManager.sendRequest(request);
        if ("200".equalsIgnoreCase(response.getStatus())) {
            List<Room> rooms = new Gson().fromJson(new Gson().toJson(response.getData()), new TypeToken<List<Room>>() {}.getType());
            roomRegister.setItems(FXCollections.observableArrayList(rooms));
        } else {
            mostrarAlerta("Error", "No se pudieron cargar las habitaciones.");
        }
    }

    private void mostrarAlerta(String titulo, String contenido) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setContentText(contenido);
        alert.showAndWait();
    }

}
