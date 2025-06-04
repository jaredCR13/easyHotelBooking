package com.easyhotelbooking.hotelbookingsystem.controller.frontdeskclerkregister;

import com.easyhotelbooking.hotelbookingsystem.Main;
import com.easyhotelbooking.hotelbookingsystem.controller.maininterface.MainInterfaceController;
import com.easyhotelbooking.hotelbookingsystem.controller.roomregister.ModifyRoomController;
import com.easyhotelbooking.hotelbookingsystem.controller.roomregister.RoomConsultController;
import com.easyhotelbooking.hotelbookingsystem.socket.ClientConnectionManager;
import com.easyhotelbooking.hotelbookingsystem.util.FXUtility;
import com.easyhotelbooking.hotelbookingsystem.util.Utility;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import hotelbookingcommon.domain.FrontDeskClerk;
import hotelbookingcommon.domain.Hotel;
import hotelbookingcommon.domain.LogIn.FrontDeskClerkDTO;
import hotelbookingcommon.domain.Request;
import hotelbookingcommon.domain.Response;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.util.StringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.easyhotelbooking.hotelbookingsystem.socket.ClientConnectionManager.sendRequest;

public class FrontDeskClerkOptionsController {

    @FXML private BorderPane bp;
    @FXML private Button registerButton;
    @FXML private Button goBack;
    @FXML private Button consultButton;
    @FXML private Button removeButton;
    @FXML private Button modifyButton;
    @FXML private TextField employeeIdField;
    @FXML private TextField nameField;
    @FXML private TextField lastNameField;
    @FXML private TextField phoneNumberField;
    @FXML private TextField userField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private TableView<FrontDeskClerk> frontDeskTable;
    @FXML private TableColumn<FrontDeskClerk, String> employeeIdColumn;
    @FXML private TableColumn<FrontDeskClerk, String> nameColumn;
    @FXML private TableColumn<FrontDeskClerk, String> lastNameColumn;
    @FXML private TableColumn<FrontDeskClerk, String> userColumn;
    @FXML private TableColumn<FrontDeskClerk, String> phoneColumn;
    @FXML private TableColumn<FrontDeskClerk, String> hotelIdColumn;
    @FXML private TableColumn<FrontDeskClerk, String> roleColumn;
    @FXML private TableColumn<FrontDeskClerk, Void> actionColumn;
    @FXML private TextField quickSearchField;
    @FXML private Button quickSearchButton;
    @FXML private Button clearSearchButton;
    @FXML private ComboBox<Hotel> hotelComboBox;
    private ScheduledExecutorService scheduler;

    private FrontDeskClerkDTO loggedInClerk; // Add a field to store the logged-in clerk
    private Main mainAppReference;

    private static final Logger logger = LogManager.getLogger(FrontDeskClerkOptionsController.class);

    private MainInterfaceController mainController;
    private Stage stage;

    public void setStage(Stage stage){
        this.stage=stage;
    }
    public void setMainController(MainInterfaceController mainController) {
        this.mainController = mainController;
    }

    public void setLoggedInClerk(FrontDeskClerkDTO loggedInClerk) {
        this.loggedInClerk = loggedInClerk;
        logger.info("FrontDeskClerkOptionsController: Logged-in clerk received: {}", loggedInClerk.getUser());
    }

    public void setMainApp(Main mainAppReference) {
        this.mainAppReference = mainAppReference;
        logger.info("FrontDeskClerkOptionsController: Main application reference set.");
    }

    @FXML
    public void initialize() {
        //roleComboBox.setItems(FXCollections.observableArrayList("Administrator", "Front Desk Clerk"));
        //roleComboBox.setValue("Front Desk Clerk");

        employeeIdColumn.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        userColumn.setCellValueFactory(new PropertyValueFactory<>("user"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("frontDeskClerkRole"));
        hotelIdColumn.setCellValueFactory(new PropertyValueFactory<>("hotelId"));
        addButtonsToTable();
        loadFrontDeskClerkIntoRegister();
        loadHotelsIntoQuickCombo();

        hotelComboBox.setConverter(new StringConverter<Hotel>() {
        public String toString(Hotel hotel) {
            return hotel != null ? hotel.getHotelName() : "";
        }

        @Override
        public Hotel fromString(String string) {
            return hotelComboBox.getItems().stream()
                    .filter(h -> h.getHotelName().equals(string))
                    .findFirst()
                    .orElse(null);
        }
    });
    }

    private void addButtonsToTable() {
        Callback<TableColumn<FrontDeskClerk, Void>, TableCell<FrontDeskClerk, Void>> cellFactory = param -> new TableCell<>() {
            private final Button consult = new Button("Consult");
            private final Button modify = new Button("Modify");
            private final Button remove = new Button("Remove");
            private final HBox buttons = new HBox(5, consult, modify, remove);

            {
                consult.setStyle("-fx-background-color: #5fabff; -fx-text-fill: white;");
                modify.setStyle("-fx-background-color: #3985d8; -fx-text-fill: white;");
                remove.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");

                consult.setOnAction(e -> {
                    FrontDeskClerk clerk = getTableView().getItems().get(getIndex());
                    openConsultFrontDeskClerk(clerk);
                });
                modify.setOnAction(e -> {
                    FrontDeskClerk clerk = getTableView().getItems().get(getIndex());
                    openModifytFrontDeskClerk(clerk);
                });
                remove.setOnAction(e -> {
                    FrontDeskClerk clerk = getTableView().getItems().get(getIndex());
                    removetFrontDeskClerkOnAction(clerk);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        };
        actionColumn.setCellFactory(cellFactory);
    }

    @FXML
    void goBackOnAction() {
        if (mainAppReference != null && loggedInClerk != null) {
            mainAppReference.loadMainInterface(loggedInClerk);
            logger.info("FrontDeskClerkOptionsController: Volviendo a la interfaz principal con el recepcionista loggeado.");
        } else {
            logger.error("FrontDeskClerkOptionsController: No se puede volver a la interfaz principal. mainAppReference o loggedInClerk es null.");
            FXUtility.alert("Error de Navegación", "No se pudo regresar a la interfaz principal. Por favor, reinicie la aplicación.");
        }
    }

    //Registrar Recepcionistas
    @FXML
    void openRegisterFrontDeskClerkOnAction() {
        try {
            FrontDeskClerkRegisterController controller = Utility.loadPage2("frontdeskclerkinterface/frontdeskclerkregister.fxml", bp);
            if (controller != null) {
                controller.setMainController(mainController);
                controller.setParentBp(bp); // para poder regresar después
                controller.setOptionsController(this); // si necesitas volver aquí
                controller.setLoggedInClerk(this.loggedInClerk);
                controller.setMainApp(this.mainAppReference);
            }
        } catch (Exception e) {
            FXUtility.alert("Error", "No se pudo abrir la pantalla de registro.");
            e.printStackTrace();
        }

    }

    //Consultar Recepcionistas
    private void openConsultFrontDeskClerk(FrontDeskClerk clerk) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/frontdeskclerkinterface/frontdeskclerkconsult.fxml"));
            Parent root = loader.load();

            FrontDeskClerkConsultController consultController = loader.getController();
            consultController.setClerk(clerk);

            Stage stage = new Stage();
            stage.setTitle("Consult Room");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Modificar Recepcionistas
    private void openModifytFrontDeskClerk(FrontDeskClerk clerk) {
        FrontDeskClerkModifyController controller = Utility.loadPage2("frontdeskclerkinterface/frontdeskclerkmodify.fxml", bp);

        if (controller != null) {
            controller.setParentBp(bp);
            controller.setMainController(mainController);
            controller.setFrontDeskClerk(clerk);
            controller.setOptionsController(this); // <- IMPORTANTE si quieres recargar luego
            controller.setLoggedInClerk(this.loggedInClerk);
            controller.setMainApp(this.mainAppReference);
        }
    }

    //Eliminar Recepcionistas
    //@Override
    void removetFrontDeskClerkOnAction(FrontDeskClerk clerk) {

        try {
            mainController.deleteFrontDeskClerk(clerk);
            loadFrontDeskClerkIntoRegister();
        } catch (Exception e) {
            mostrarAlertaError("Error", "Ocurrió un error al eliminar el hotel.");
        }

    }

    @FXML
    private void onQuickSearch() {
        String id = quickSearchField.getText().trim();
        Hotel selectedHotel = hotelComboBox.getValue();

        if (id.isEmpty() || selectedHotel == null) {
            mostrarAlertaError("Error", "Debe ingresar el ID y seleccionar un hotel.");
            return;
        }

        try{
        Request request = new Request("getFrontDeskClerk", id); // Usa el ID directamente
        Response response = ClientConnectionManager.sendRequest(request);

        if ("200".equalsIgnoreCase(response.getStatus()) && response.getData() != null) {
            FrontDeskClerk found = new Gson().fromJson(
                    new Gson().toJson(response.getData()),
                    FrontDeskClerk.class
            );

            if (found.getHotelId() == selectedHotel.getNumHotel()) {
                frontDeskTable.setItems(FXCollections.observableArrayList(found));
            } else {
                mostrarAlertaError("No coincide", "El recepcionista no pertenece al hotel seleccionado.");
            }
            } else {
                mostrarAlertaError("No encontrado", "No se encontró el recepcionista.");
            }
        } catch (NumberFormatException e) {
            mostrarAlertaError("Error", "El ID del recepcionista debe ser un número entero.");
        }
    }

    @FXML
    private void onClearSearch() {
        quickSearchField.clear();
        hotelComboBox.getSelectionModel().clearSelection();
        loadFrontDeskClerkIntoRegister();
    }

    public void loadFrontDeskClerkIntoRegister() {
        Request request = new Request("getClerks", null);
        Response response = ClientConnectionManager.sendRequest(request);
        if ("200".equalsIgnoreCase(response.getStatus()) && response.getData() != null) {
            List<FrontDeskClerk> clerks = new Gson().fromJson(new Gson().toJson(response.getData()), new TypeToken<List<FrontDeskClerk>>(){}.getType());
            frontDeskTable.setItems(FXCollections.observableArrayList(clerks));
        } else {
            mostrarAlertaError("Error", "No se pudieron cargar los recepcionistas.");
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
            hotelComboBox.getItems().clear();
            hotelComboBox.getItems().addAll(hotelList);
        } else {
            mostrarAlertaError("Error", "No se pudieron cargar los hoteles para búsqueda rápida.");
        }
    }


    //Actaulización en tiempo real con intervalo de tiempo para el uso de servidor en diferentes computadoras
    public void startPolling() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            Platform.runLater(() -> loadFrontDeskClerkIntoRegister());
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


