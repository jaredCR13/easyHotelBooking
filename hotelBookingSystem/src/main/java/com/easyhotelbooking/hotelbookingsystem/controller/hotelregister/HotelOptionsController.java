package com.easyhotelbooking.hotelbookingsystem.controller.hotelregister;


import com.easyhotelbooking.hotelbookingsystem.controller.maininterface.MainInterfaceController;
import com.easyhotelbooking.hotelbookingsystem.socket.ClientConnectionManager;
//import com.easyhotelbooking.hotelbookingsystem.util.FxUtility;
import com.easyhotelbooking.hotelbookingsystem.util.FXUtility;
import com.easyhotelbooking.hotelbookingsystem.util.Utility;
import com.google.gson.Gson;
import hotelbookingcommon.domain.Hotel;
import hotelbookingcommon.domain.Request;
import hotelbookingcommon.domain.Response;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HotelOptionsController {

    @FXML private BorderPane bp;
    @FXML private Button consultButton;
    @FXML private TextField hotelNumberField;
    @FXML private TextField locationField;
    @FXML private Button modifyButton;
    @FXML private TextField nameField;
    @FXML private Button removeButton;
    @FXML private Button goBack;
    @FXML private TableView<Hotel> hotelRegister;
    @FXML private TableColumn<Hotel, Integer> numberHotelRegister;
    @FXML private TableColumn<Hotel, String>  nameHotelRegister;
    @FXML private TableColumn<Hotel, String> locationHotelRegister;
    @FXML private TableColumn<Hotel, Void> hotelActionColumn;
    @FXML private TextField quickSearchField;
    private ScheduledExecutorService scheduler;


    private Stage stage;
    private MainInterfaceController mainController;
    private static final Logger logger = LogManager.getLogger(HotelOptionsController.class);

    public void setMainController(MainInterfaceController mainController) {
        this.mainController = mainController;
    }
    public void setStage(Stage stage){
        this.stage=stage;
    }

    @FXML
    public void initialize(){
        numberHotelRegister.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().getNumHotel()).asObject());

        nameHotelRegister.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getHotelName()));
        locationHotelRegister.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getHotelLocation()));

        loadHotelsIntoRegister();
        addButtonsToTableView();

        //Elimina una columna vacía que se crea sola en el TableView
        hotelRegister.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // 3

    }

    //Botones dentro de la TableView

    private void addButtonsToTableView() {
        Callback<TableColumn<Hotel, Void>, TableCell<Hotel, Void>> cellFactory = new Callback<TableColumn<Hotel, Void>, TableCell<Hotel, Void>>() {
            @Override
            public TableCell<Hotel, Void> call(final TableColumn<Hotel, Void> param) {
                final TableCell<Hotel, Void> cell = new TableCell<Hotel, Void>() {

                    private final Button consult = new Button("Consult");
                    private final Button modify = new Button("Modify");
                    private final Button remove = new Button("Remove");

                    private final HBox buttonsBox = new HBox(10, consult, modify, remove);

                    {
                        consult.setStyle("-fx-background-color: #5fabff; -fx-text-fill: white;");
                        modify.setStyle("-fx-background-color: #3985d8; -fx-text-fill: white;");
                        remove.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");

                        consult.setOnAction(event -> {
                            Hotel hotel = getTableView().getItems().get(getIndex());
                            openConsultHotel(hotel);
                        });

                        modify.setOnAction(event -> {
                            Hotel hotel = getTableView().getItems().get(getIndex());
                            openModifyHotel(hotel);
                            // Refresh the table after potential modification
                            loadHotelsIntoRegister(); // Added this line
                        });

                        remove.setOnAction(event -> {
                            Hotel hotel = getTableView().getItems().get(getIndex());
                            removeHotelOnAction(hotel);
                            // The alert for removal is handled in MainInterfaceController
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
                return cell;
            }
        };

        hotelActionColumn.setCellFactory(cellFactory);
    }


    @FXML
    void goBackOnAction() {
        Utility.loadFullView("maininterface.fxml", goBack);
    }


    // Registrar en ventana emergente
    @FXML
    void registerHotelOnAction() {
        openRegisterHotel();
        // Alert for registration is handled in MainInterfaceController
    }

    public void openRegisterHotel() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/hotelinterface/hotelregister.fxml"));
            Parent root = loader.load();

            HotelRegisterController registrerController = loader.getController();
            registrerController.setMainController(mainController);

            Stage stage = new Stage();
            stage.setTitle("Register Hotel");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL); // Modal = bloquea ventana anterior
            stage.showAndWait(); // Espera hasta que se cierre

            loadHotelsIntoRegister(); // Refresca la tabla al cerrar
        } catch (Exception e) {
            logger.error("Error al abrir ventana de registro de hotel: {}", e.getMessage(), e);
            mostrarAlertaError("Error", "Ocurrió un error al intentar abrir el formulario de registro de hotel.");
        }
    }


    //Consultar Hoteles en ventana emergente

    void consultHotelOnAction(Hotel hotel) {
        openConsultHotel(hotel);
        // Alert for consultation is handled in MainInterfaceController
    }

    private void openConsultHotel(Hotel hotel) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/hotelinterface/hotelconsult.fxml"));
            Parent root = loader.load();

            HotelConsultController consultController = loader.getController();
            consultController.setHotel(hotel);

            Stage stage = new Stage();
            stage.setTitle("Consult Hotel");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (Exception e) {
            logger.error("Error al abrir ventana de consulta de hotel: {}", e.getMessage(), e);
            mostrarAlertaError("Error", "Ocurrió un error al intentar abrir el formulario de consulta de hotel.");
        }
    }

    // Modificar en ventana emergente

    void modifyHotelOnAction(Hotel hotel) {
        openModifyHotel(hotel);
        loadHotelsIntoRegister(); // Ensure table is refreshed after modification
        // Alert for modification is handled in MainInterfaceController
    }

    private void openModifyHotel(Hotel hotel) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/hotelinterface/hotelmodify.fxml"));
            Parent root = loader.load();

            HotelModifyController controller = loader.getController();
            controller.setMainController(mainController);
            controller.setHotel(hotel);

            Stage stage = new Stage();
            stage.setTitle("Modify Hotel");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadHotelsIntoRegister(); // Also refresh here in case the modal closes without a successful update

        } catch (Exception e) {
            logger.error("Error al abrir ventana de modificación de hotel: {}", e.getMessage(), e);
            mostrarAlertaError("Error", "Ocurrió un error al intentar abrir el formulario de modificación de hotel.");
        }
    }

    @FXML
    void removeHotelOnAction(Hotel hotel) {
        try {
            // The actual alert for success/failure is handled in MainInterfaceController.deleteHotel
            mainController.deleteHotel(hotel.getNumHotel());
            loadHotelsIntoRegister(); // Refresh table after deletion attempt
        } catch (Exception e) {
            logger.error("Error al intentar eliminar hotel: {}", e.getMessage(), e);
            mostrarAlertaError("Error", "Ocurrió un error al eliminar el hotel.");
        }
    }

    @FXML
    private void onQuickHotelSearch() {
        String input = quickSearchField.getText().trim();
        if (input.isEmpty()) {
            mostrarAlertaError("Error de búsqueda", "Por favor ingrese un número de hotel para buscar.");
            return;
        }

        try {
            int hotelNumber = Integer.parseInt(input);
            Request request = new Request("getHotel", hotelNumber);
            Response response = ClientConnectionManager.sendRequest(request);

            if ("200".equalsIgnoreCase(response.getStatus()) && response.getData() != null) {
                Hotel foundHotel = new Gson().fromJson(new Gson().toJson(response.getData()), Hotel.class);
                hotelRegister.getItems().clear();
                hotelRegister.getItems().add(foundHotel);
                // Informational alert for successful search
                FXUtility.alertInfo("Búsqueda Exitosa", "Hotel encontrado: " + foundHotel.getHotelName());
            } else {
                mostrarAlertaError("Hotel No Encontrado", "No existe un hotel con el número: " + hotelNumber + ".");
            }
        } catch (NumberFormatException e) {
            mostrarAlertaError("Error de Entrada", "El número de hotel debe ser un número entero válido.");
        } catch (Exception e) {
            logger.error("Error inesperado en la búsqueda rápida de hotel: {}", e.getMessage(), e);
            mostrarAlertaError("Error Interno", "Ocurrió un error inesperado durante la búsqueda del hotel.");
        }
    }

    @FXML
    private void onClearSearch() {
        quickSearchField.clear();
        loadHotelsIntoRegister();
        // Informational alert for clearing search
        FXUtility.alertInfo("Búsqueda Limpiada", "Se han mostrado todos los hoteles nuevamente.");
    }

    private void loadHotelsIntoRegister() {
        Request request = new Request("getHotels", null);
        Response response = ClientConnectionManager.sendRequest(request);
        if ("200".equalsIgnoreCase(response.getStatus())) {
            List<Hotel> hotels = new Gson().fromJson(new Gson().toJson(response.getData()), new com.google.gson.reflect.TypeToken<List<Hotel>>(){}.getType());
            hotelRegister.setItems(javafx.collections.FXCollections.observableArrayList(hotels));
        } else {
            logger.error("Error al cargar hoteles en el registro: {}", response != null ? response.getMessage() : "Respuesta nula");
            mostrarAlertaError("Error de Carga", "No se pudieron cargar los hoteles en la tabla. " + (response != null ? response.getMessage() : ""));
        }
        hotelRegister.refresh();
    }

    //Actaulización en tiempo real con intervalo de tiempo para el uso de servidor en diferentes computadoras
    public void startPolling() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            Platform.runLater(() -> loadHotelsIntoRegister());
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




