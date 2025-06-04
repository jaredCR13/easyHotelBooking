package com.easyhotelbooking.hotelbookingsystem.controller.guestregister;

import com.easyhotelbooking.hotelbookingsystem.Main;
import com.easyhotelbooking.hotelbookingsystem.controller.maininterface.MainInterfaceController;
import com.easyhotelbooking.hotelbookingsystem.socket.ClientConnectionManager;
import com.easyhotelbooking.hotelbookingsystem.util.FXUtility;
import com.easyhotelbooking.hotelbookingsystem.util.Utility;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import hotelbookingcommon.domain.Guest;
import hotelbookingcommon.domain.LogIn.FrontDeskClerkDTO;
import hotelbookingcommon.domain.Request;
import hotelbookingcommon.domain.Response;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
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

import javax.swing.border.Border;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GuestOptionsController {
    @FXML private BorderPane bp;
    @FXML private Button registerButton;
    @FXML private Button modifyButton;
    @FXML private Button removeButton;
    @FXML private Button goBack;
    @FXML private TextField quickSearchField;
    @FXML private TableView<Guest> guestTable;
    @FXML private TableColumn<Guest, Integer> idColumn;
    @FXML private TableColumn<Guest, Integer> credentialColumn;
    @FXML private TableColumn<Guest, String> nameColumn;
    @FXML private TableColumn<Guest, String> lastNameColumn;
    @FXML private TableColumn<Guest, String> addressColumn;
    @FXML private TableColumn<Guest, String> emailColumn;
    @FXML private TableColumn<Guest, String> phoneColumn;
    @FXML private TableColumn<Guest, String> countryColumn;
    @FXML private TableColumn<Guest, Void> actionColumn;



    private Stage stage;
    private MainInterfaceController mainController;
    private ScheduledExecutorService scheduler;
    private static final Logger logger = LogManager.getLogger(GuestOptionsController.class);

    public void setMainController(MainInterfaceController mainController) {
        this.mainController = mainController;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private FrontDeskClerkDTO loggedInClerk; // Add a field to store the logged-in clerk
    private Main mainAppReference;

    public void setMainApp(Main mainAppReference) {
        this.mainAppReference = mainAppReference;
        logger.info("GuestOptionsController: Main application reference set.");
    }

    public void setLoggedInClerk(FrontDeskClerkDTO loggedInClerk) {
        this.loggedInClerk = loggedInClerk;
        if (loggedInClerk != null) {
            logger.info("GuestOptionsController: Logged-in clerk received: {}", loggedInClerk.getUser());
        } else {
            logger.warn("GuestOptionsController: setLoggedInClerk called with a null loggedInClerk. This indicates an issue in the login or navigation flow.");
        }
    }

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId()).asObject());
        credentialColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getCredential()).asObject());
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        lastNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLastName()));
        addressColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAddress()));
        emailColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        phoneColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPhoneNumber()));
        countryColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNativeCountry()));

        loadGuestsIntoTable();
        addButtonsToTableView();
        startPolling();
    }

    private void addButtonsToTableView() {
        Callback<TableColumn<Guest, Void>, TableCell<Guest, Void>> cellFactory = param -> new TableCell<>() {
            final Button consult = new Button("Consult");
            final Button modify = new Button("Modify");
            final Button remove = new Button("Remove");
            final HBox hbox = new HBox(5, consult, modify, remove);

            {
                consult.setStyle("-fx-background-color: #5fabff; -fx-text-fill: white;");
                modify.setStyle("-fx-background-color: #3985d8; -fx-text-fill: white;");
                remove.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");

                consult.setOnAction(event -> {
                    Guest guest = getTableView().getItems().get(getIndex());
                    openConsultGuest(guest);
                });
                modify.setOnAction(event -> {
                    Guest guest = getTableView().getItems().get(getIndex());
                    openModifyGuest(guest);
                });
                remove.setOnAction(e -> removeGuest(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : hbox);
            }
        };

        actionColumn.setCellFactory(cellFactory);
    }

    @FXML
    private void onQuickSearch() {
        stopPolling();

        String input = quickSearchField.getText().trim();
        if (input.isEmpty()) {
            mostrarAlertaError("Búsqueda Vacía", "Por favor, ingrese un ID para buscar.");
            return;
        }

        try {
            int guestId = Integer.parseInt(input);
            Request request = new Request("getGuest", guestId);
            Response response = ClientConnectionManager.sendRequest(request);

            if ("200".equalsIgnoreCase(response.getStatus()) && response.getData() != null) {
                Guest foundGuest = new Gson().fromJson(new Gson().toJson(response.getData()), Guest.class);
                guestTable.setItems(FXCollections.observableArrayList(foundGuest));
            } else {
                mostrarAlertaError("No encontrado", "No se encontró el huésped con ID: " + guestId);
            }
        } catch (NumberFormatException e) {
            mostrarAlertaError("Formato Inválido", "El ID debe ser un número entero.");
        }
    }

    @FXML
    private void onClearSearch() {
        quickSearchField.clear();
        loadGuestsIntoTable();
        startPolling(); // <--- Reanuda actualización automática
    }

    public void loadGuestsIntoTable() {
        Request request = new Request("getGuests", null);
        Response response = ClientConnectionManager.sendRequest(request);

        if ("200".equalsIgnoreCase(response.getStatus()) && response.getData() != null) {
            List<Guest> guests = new Gson().fromJson(new Gson().toJson(response.getData()), new TypeToken<List<Guest>>() {}.getType());
            guestTable.setItems(FXCollections.observableArrayList(guests));
            guestTable.refresh();

        } else {
            mostrarAlertaError("Error", "No se pudieron cargar los huespedess.");
            logger.error("Error al cargar habitaciones en la tabla: {}", response != null ? response.getMessage() : "Desconocido");
        }

    }


    @FXML
    void goBackOnAction() {
        if (mainAppReference != null && loggedInClerk != null) {
            mainAppReference.loadMainInterface(loggedInClerk);
            logger.info("GuestOptionsController: Volviendo a la interfaz principal con el recepcionista loggeado.");
        } else {
            logger.error("GuestOptionsController: No se puede volver a la interfaz principal. mainAppReference o loggedInClerk es null.");
            FXUtility.alert("Error de Navegación", "No se pudo regresar a la interfaz principal. Por favor, reinicie la aplicación.");
        }
    }
    @FXML
    void registerGuestOnAction() throws IOException {
        GuestRegisterController controller = Utility.loadPage2("guestinterface/guestregister.fxml", bp);
        if (controller != null) {
            controller.setMainController(mainController);
            controller.setParentBp(bp);
            controller.setStage(stage); // si usas primaryStage también
            controller.setLoggedInClerk(this.loggedInClerk);
            controller.setMainApp(this.mainAppReference);

        }
    }

    private void openConsultGuest(Guest guest) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/guestinterface/guestconsult.fxml"));
            Parent root = loader.load();

            GuestConsultController controller = loader.getController();
            controller.setGuest(guest);

            Stage stage = new Stage();
            stage.setTitle("Consult Guest");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException e) {
            logger.error("Error al abrir ventana de consulta de huésped: {}", e.getMessage(), e);
            mostrarAlertaError("Error", "No se pudo abrir la ventana de consulta.");
        }
    }

    private void openModifyGuest(Guest guest) {
        Request request = new Request("getGuest", guest.getId());
        Response response = ClientConnectionManager.sendRequest(request);

        if ("200".equalsIgnoreCase(response.getStatus()) && response.getData() != null) {
            Guest completeGuest = new Gson().fromJson(new Gson().toJson(response.getData()), Guest.class);

            GuestModifyController controller = Utility.loadPage2("guestinterface/guestmodify.fxml", bp);
            if (controller != null) {
                controller.setParentBp(bp);
                controller.setMainController(mainController);
                controller.setGuestOptionsController(this);
                controller.setGuest(completeGuest);
                controller.setLoggedInClerk(this.loggedInClerk);
                controller.setMainApp(this.mainAppReference);

            }
        } else {
            mostrarAlertaError("Error", "No se pudo cargar la información completa del huésped.");
        }
    }


    private void removeGuest(Guest guest) {
        Request request = new Request("deleteGuest", guest.getId());
        Response response = ClientConnectionManager.sendRequest(request);

        if ("200".equalsIgnoreCase(response.getStatus())) {
            FXUtility.alertInfo("Eliminado", "Huesped eliminado exitosamente.");
            loadGuestsIntoTable();
        } else {
            mostrarAlertaError("Error", "No se pudo eliminar el huésped.");
        }
    }

    //Actaulización en tiempo real con intervalo de tiempo para el uso de servidor en diferentes computadoras
    public void startPolling() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            Platform.runLater(() -> loadGuestsIntoTable());
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

}//Fin
