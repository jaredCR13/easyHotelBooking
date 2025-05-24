package com.easyhotelbooking.hotelbookingsystem.controller;

import com.easyhotelbooking.hotelbookingsystem.socket.ClientConnectionManager;
import com.easyhotelbooking.hotelbookingsystem.util.Utility;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import hotelbookingcommon.domain.*;
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
    @FXML private ComboBox<Hotel> hotelComboBox; // Ya tienes el ComboBox de Hoteles

    @FXML private TableView<Room> roomRegister;
    @FXML private TableColumn<Room, Integer> roomNumberColumn;
    @FXML private TableColumn<Room, Double> priceColumn;
    @FXML private TableColumn<Room, String> descriptionColumn;
    @FXML private TableColumn<Room, RoomStatus> statusColumn;
    @FXML private TableColumn<Room, RoomStyle> styleColumn;
    @FXML private TableColumn<Room, Integer> hotelIdColumn; // Nueva columna para mostrar el ID del hotel

    private static final Logger logger = LogManager.getLogger(RoomOptionsController.class);

    private MainInterfaceController mainController;

    public void setMainController(MainInterfaceController mainController) {
        this.mainController = mainController;
        // Se llama loadRoomsIntoRegister() solo una vez al cargar el controlador
        // para que la tabla se llene con datos
        loadRoomsIntoRegister();
    }

    @FXML
    public void initialize() {
        statusCombo.getItems().addAll(RoomStatus.values());
        styleCombo.getItems().addAll(RoomStyle.values());

        // Cargar los hoteles disponibles en el ComboBox al inicializar
        loadHotelsIntoComboBox();

        // Configuración de las columnas de la tabla de habitaciones
        roomNumberColumn.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("roomPrice"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("detailedDescription"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        styleColumn.setCellValueFactory(new PropertyValueFactory<>("style"));
        // Nueva fábrica de valores para la columna del ID del hotel
        hotelIdColumn.setCellValueFactory(new PropertyValueFactory<>("hotelId"));


        // Listener para cuando se selecciona una fila en la tabla
        roomRegister.setOnMouseClicked(event -> {
            Room selected = roomRegister.getSelectionModel().getSelectedItem();
            if (selected != null) {
                roomNumberTf.setText(String.valueOf(selected.getRoomNumber()));
                priceTf.setText(String.valueOf(selected.getRoomPrice()));
                descriptionTf.setText(selected.getDetailedDescription());
                statusCombo.setValue(selected.getStatus());
                styleCombo.setValue(selected.getStyle());

                // Seleccionar el Hotel correcto en el ComboBox basado en el objeto Hotel asociado
                // Ojo: el Room del cliente debe tener el objeto Hotel cargado para que esto funcione directamente.
                // En MainInterfaceController.consultRoom se aseguro que esto suceda.
                if (selected.getHotel() != null) {
                    hotelComboBox.setValue(selected.getHotel());
                } else {
                    // Si el objeto Hotel no está en la Room (ej. al cargar todas las Rooms inicialmente
                    // o si no se recuperó el hotel completo), podemos intentar buscarlo en la lista del ComboBox
                    // basándonos en el hotelId.
                    hotelComboBox.getItems().stream()
                            .filter(hotel -> hotel.getNumHotel() == selected.getHotelId())
                            .findFirst()
                            .ifPresent(hotelComboBox::setValue);
                }
            }
        });
    }

    private void loadHotelsIntoComboBox() {
        Request request = new Request("getHotels", null);
        Response response = ClientConnectionManager.sendRequest(request);
        if ("200".equalsIgnoreCase(response.getStatus()) && response.getData() != null) {
            List<Hotel> hotels = new Gson().fromJson(new Gson().toJson(response.getData()), new TypeToken<List<Hotel>>() {}.getType());
            hotelComboBox.setItems(FXCollections.observableArrayList(hotels));
            // Importante: Necesitas un CellFactory si quieres que el ComboBox muestre el nombre del hotel
            // en lugar del toString() por defecto de Hotel.
            hotelComboBox.setCellFactory(lv -> new ListCell<Hotel>() {
                @Override
                protected void updateItem(Hotel hotel, boolean empty) {
                    super.updateItem(hotel, empty);
                    setText(empty ? "" : hotel.getHotelName() + " (" + hotel.getNumHotel() + ")");
                }
            });
            // Esto también se aplica al botón de selección (representation)
            hotelComboBox.setButtonCell(new ListCell<Hotel>() {
                @Override
                protected void updateItem(Hotel hotel, boolean empty) {
                    super.updateItem(hotel, empty);
                    setText(empty ? "Seleccione un Hotel" : hotel.getHotelName() + " (" + hotel.getNumHotel() + ")");
                }
            });
        } else {
            mostrarAlerta("Error", "No se pudieron cargar los hoteles para el ComboBox.");
            logger.error("Error al cargar hoteles para ComboBox: {}", response != null ? response.getMessage() : "Desconocido");
        }
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

            // Obtener el Hotel seleccionado del ComboBox
            Hotel selectedHotel = hotelComboBox.getSelectionModel().getSelectedItem();

            if (numberStr.isEmpty() || priceStr.isEmpty() || description.isEmpty() || selectedHotel == null) {
                mostrarAlerta("Error", "Por favor, complete todos los campos y seleccione un hotel.");
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

            // Crear el objeto Room
            Room room = new Room(number, price, description, status, style, Collections.emptyList());
            // ¡CRUCIAL! Asignar el hotelId de la Room con el ID del hotel seleccionado
            room.setHotelId(selectedHotel.getNumHotel());
            // Opcionalmente, puedes asignar el objeto Hotel completo a la Room en el cliente
            // para mantener la coherencia en memoria antes de enviar.
            room.setHotel(selectedHotel);


            mainController.registerRoom(room); // Envía la Room al servidor

            loadRoomsIntoRegister(); // Recarga la tabla de habitaciones
            clearFields();

        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Número de habitación o precio inválido.");
            logger.error("Error de formato al registrar habitación: {}", e.getMessage());
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
            mainController.consultRoom(number); // Llama al método del MainController

        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Número de habitación inválido.");
            logger.error("Error de formato al consultar habitación: {}", e.getMessage());
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

            // Obtener el Hotel seleccionado del ComboBox
            Hotel selectedHotel = hotelComboBox.getSelectionModel().getSelectedItem();

            if (numberStr.isEmpty() || priceStr.isEmpty() || description.isEmpty() || selectedHotel == null) {
                mostrarAlerta("Error", "Complete todos los campos y seleccione un hotel.");
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

            // Crear el objeto Room con los datos actualizados
            Room room = new Room(number, price, description, status, style, Collections.emptyList());
            // ¡CRUCIAL! Asignar el hotelId de la Room con el ID del hotel seleccionado
            room.setHotelId(selectedHotel.getNumHotel());
            // Opcionalmente, asignar el objeto Hotel completo a la Room en el cliente
            room.setHotel(selectedHotel);

            mainController.updateRoom(room); // Envía la Room al servidor
            loadRoomsIntoRegister(); // Recarga la tabla
            clearFields();

        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Número o precio inválido.");
            logger.error("Error de formato al modificar habitación: {}", e.getMessage());
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
            mainController.deleteRoom(number); // Llama al método del MainController
            loadRoomsIntoRegister(); // Recarga la tabla
            clearFields();

        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Número inválido.");
            logger.error("Error de formato al eliminar habitación: {}", e.getMessage());
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
        hotelComboBox.setValue(null); // Limpiar también la selección del hotel
    }

    private void loadRoomsIntoRegister() {
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