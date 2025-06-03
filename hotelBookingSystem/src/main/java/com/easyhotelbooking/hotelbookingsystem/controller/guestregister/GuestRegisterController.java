package com.easyhotelbooking.hotelbookingsystem.controller.guestregister;

import com.easyhotelbooking.hotelbookingsystem.controller.maininterface.MainInterfaceController;
import com.easyhotelbooking.hotelbookingsystem.socket.ClientConnectionManager;
import com.easyhotelbooking.hotelbookingsystem.util.FXUtility;
import com.easyhotelbooking.hotelbookingsystem.util.Utility;
import com.google.gson.Gson;
import hotelbookingcommon.domain.Guest;
import hotelbookingcommon.domain.Request;
import hotelbookingcommon.domain.Response;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GuestRegisterController {

    @FXML private TextField idField;
    @FXML private TextField credentialField;
    @FXML private TextField nameField;
    @FXML private TextField lastNameField;
    @FXML private TextField addressField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField countryField;
    @FXML private Button confirmButton;
    @FXML private Button cancelButton;

    private BorderPane parentBp;
    private MainInterfaceController mainController;
    private Stage primaryStage;
    private static final Logger logger = LogManager.getLogger(GuestRegisterController.class);

    public void setParentBp(BorderPane parentBp) {
        this.parentBp = parentBp;
    }

    public void setMainController(MainInterfaceController mainController) {
        this.mainController = mainController;
    }

    public void setStage(Stage stage) {
        this.primaryStage = stage;
    }

    @FXML
    void onSave() {
        try {
            int id = Integer.parseInt(idField.getText().trim());
            int credential = Integer.parseInt(credentialField.getText().trim());
            String name = nameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String address = addressField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String country = countryField.getText().trim();

            if (name.isEmpty() || lastName.isEmpty() || email.isEmpty()) {
                FXUtility.alert("Error de Validación", "Nombre, Apellido y Email son requeridos.");
                return;
            }

            Guest guest = new Guest(id, credential, name, lastName, address, email, phone, country);
            Request request = new Request("registerGuest", guest);
            Response response = ClientConnectionManager.sendRequest(request);

            if ("201".equalsIgnoreCase(response.getStatus())) {
                FXUtility.alertInfo("Éxito", "Huésped registrado correctamente.");

                // Vuelve a GuestOptions después del registro
                GuestOptionsController controller = Utility.loadPage2("guestinterface/guestoptions.fxml", parentBp);
                if (controller != null) {
                    controller.setMainController(mainController);
                    if (this.primaryStage != null) {
                        controller.setStage(this.primaryStage);
                    }
                    controller.loadGuestsIntoTable();
                }

                clearFields();
            } else {
                FXUtility.alert("Error", "No se pudo registrar el huésped: " + response.getMessage());
            }

        } catch (NumberFormatException e) {
            FXUtility.alert("Error", "ID y credencial deben ser valores numéricos.");
        } catch (Exception e) {
            logger.error("Error inesperado al registrar huésped", e);
            FXUtility.alert("Error", "Ocurrió un error inesperado.");
        }
    }

    @FXML
    void onCancel() {
        try {
            GuestOptionsController controller = Utility.loadPage2("guestinterface/guestoptions.fxml", parentBp);
            if (controller != null) {
                controller.setMainController(mainController);
                if (this.primaryStage != null) {
                    controller.setStage(this.primaryStage);
                }
                controller.loadGuestsIntoTable();
            }
            clearFields();
        } catch (Exception e) {
            logger.error("Error al cancelar y volver a opciones de huésped", e);
            FXUtility.alert("Error", "No se pudo regresar a la vista de opciones.");
        }
    }

    private void clearFields() {
        idField.clear();
        credentialField.clear();
        nameField.clear();
        lastNameField.clear();
        addressField.clear();
        emailField.clear();
        phoneField.clear();
        countryField.clear();
    }
}