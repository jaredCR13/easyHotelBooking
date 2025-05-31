package com.easyhotelbooking.hotelbookingsystem.controller.hotelregister;


import com.easyhotelbooking.hotelbookingsystem.controller.maininterface.MainInterfaceController;
import com.easyhotelbooking.hotelbookingsystem.socket.ClientConnectionManager;
//import com.easyhotelbooking.hotelbookingsystem.util.FxUtility;
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
                        });

                        remove.setOnAction(event -> {
                            Hotel hotel = getTableView().getItems().get(getIndex());
                            removeHotelOnAction(hotel);
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
            e.printStackTrace();
        }
    }


    //Consultar Hoteles en ventana emergente

        void consultHotelOnAction(Hotel hotel) {
            openConsultHotel(hotel);
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
                e.printStackTrace();
            }
        }

        // Modificar en ventana emergente

        void modifyHotelOnAction(Hotel hotel) {
            openModifyHotel(hotel);
            loadHotelsIntoRegister();

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

            loadHotelsIntoRegister();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

        @FXML
        void removeHotelOnAction(Hotel hotel) {
            try {
                mainController.deleteHotel(hotel.getNumHotel());
                loadHotelsIntoRegister();
            } catch (Exception e) {
                mostrarAlertaError("Error", "Ocurrió un error al eliminar el hotel.");
            }
        }

    @FXML
    private void onQuickHotelSearch() {
        String input = quickSearchField.getText().trim();
        if (input.isEmpty()) {
            mostrarAlertaError("Error", "Por favor ingrese un número de hotel.");
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
            } else {
                mostrarAlertaError("Error", "Hotel no ha sido encontrado.");
            }
        } catch (NumberFormatException e) {
            mostrarAlertaError("Error", "El número de hotel debe ser un número entero.");
        }
    }

    @FXML
    private void onClearSearch() {
        quickSearchField.clear();
        loadHotelsIntoRegister();
    }

        private void loadHotelsIntoRegister() {
            Request request = new Request("getHotels", null);
            Response response = ClientConnectionManager.sendRequest(request);
            if ("200".equalsIgnoreCase(response.getStatus())) {
                List<Hotel> hotels = new Gson().fromJson(new Gson().toJson(response.getData()), new com.google.gson.reflect.TypeToken<List<Hotel>>(){}.getType());
                hotelRegister.setItems(javafx.collections.FXCollections.observableArrayList(hotels));
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




