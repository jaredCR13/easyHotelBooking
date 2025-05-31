package com.easyhotelbooking.hotelbookingsystem.controller.frontdeskclerkregister;

import com.easyhotelbooking.hotelbookingsystem.controller.maininterface.MainInterfaceController;
import com.easyhotelbooking.hotelbookingsystem.controller.roomregister.RoomConsultController;
import com.easyhotelbooking.hotelbookingsystem.socket.ClientConnectionManager;
import com.easyhotelbooking.hotelbookingsystem.util.Utility;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import hotelbookingcommon.domain.FrontDeskClerk;
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
    private ScheduledExecutorService scheduler;


    private static final Logger logger = LogManager.getLogger(FrontDeskClerkOptionsController.class);

    private MainInterfaceController mainController;
    private Stage stage;

    public void setStage(Stage stage){
        this.stage=stage;
    }
    public void setMainController(MainInterfaceController mainController) {
        this.mainController = mainController;
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
        //roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));

        addButtonsToTable();
        loadFrontDeskClerkIntoRegister();
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

                });
                remove.setOnAction(e -> {
                    FrontDeskClerk clerk = getTableView().getItems().get(getIndex());
                    //removeClerk(clerk);
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
        Utility.loadFullView("maininterface.fxml", goBack);
    }

    //Registrar Recepcionistas
    @FXML
    void openRegisterFrontDeskClerkOnAction() {
        try {
            FrontDeskClerkRegisterController controller = Utility.loadPage2("frontdeskclerkinterface/frontdeskclerkregisteroptions.fxml", bp);
            if (controller != null) {
                controller.setMainController(mainController);
                controller.setParentBp(bp); // para poder regresar después
                controller.setOptionsController(this); // si necesitas volver aquí
            }
        } catch (Exception e) {
            util.FXUtility.alert("Error", "No se pudo abrir la pantalla de registro.");
            e.printStackTrace();
        }

    }

    //Consultar Recepcionistas
    private void openConsultFrontDeskClerk(FrontDeskClerk clerk) {

    }

    //Modificar Recepcionistas
    private void openModifytFrontDeskClerk(FrontDeskClerk clerk) {

    }

    //Eliminar Recepcionistas
    //@Override
    void removetFrontDeskClerkOnAction(FrontDeskClerk clerk) {

    }

    private void loadFrontDeskClerkIntoRegister() {
        Request request = new Request("getClerks", null);
        Response response = ClientConnectionManager.sendRequest(request);
        if ("200".equalsIgnoreCase(response.getStatus()) && response.getData() != null) {
            List<FrontDeskClerk> clerks = new Gson().fromJson(new Gson().toJson(response.getData()), new TypeToken<List<FrontDeskClerk>>(){}.getType());
            frontDeskTable.setItems(FXCollections.observableArrayList(clerks));
        } else {
            mostrarAlertaError("Error", "No se pudieron cargar los recepcionistas.");
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



