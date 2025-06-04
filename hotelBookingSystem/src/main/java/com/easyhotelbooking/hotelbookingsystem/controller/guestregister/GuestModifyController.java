package com.easyhotelbooking.hotelbookingsystem.controller.guestregister;

import com.easyhotelbooking.hotelbookingsystem.Main;
import com.easyhotelbooking.hotelbookingsystem.controller.maininterface.MainInterfaceController;
import com.easyhotelbooking.hotelbookingsystem.socket.ClientConnectionManager;
import com.easyhotelbooking.hotelbookingsystem.util.FXUtility;
import com.easyhotelbooking.hotelbookingsystem.util.Utility;
import com.google.gson.Gson;
import hotelbookingcommon.domain.Guest;
import hotelbookingcommon.domain.LogIn.FrontDeskClerkDTO;
import hotelbookingcommon.domain.Request;
import hotelbookingcommon.domain.Response;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GuestModifyController {

    @FXML private TextField idField;
    @FXML private TextField credentialField;
    @FXML private TextField nameField;
    @FXML private TextField lastNameField;
    @FXML private TextField addressField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField countryField;
    @FXML private Button cancelButton;
    @FXML private Button saveButton;

    private MainInterfaceController mainController;
    private GuestOptionsController guestOptionsController;
    private Guest currentGuest;
    private BorderPane parentBp;
    private static final Logger logger = LogManager.getLogger(GuestModifyController.class);
    private FrontDeskClerkDTO loggedInClerk; // Add a field to store the logged-in clerk
    private Main mainAppReference;

    public void setMainApp(Main mainAppReference) {
        this.mainAppReference = mainAppReference;
        logger.info("RoomOptionsController: Main application reference set.");
    }

    public void setLoggedInClerk(FrontDeskClerkDTO loggedInClerk) {
        this.loggedInClerk = loggedInClerk;
        if (loggedInClerk != null) {
            logger.info("RoomOptionsController: Logged-in clerk received: {}", loggedInClerk.getUser());
        } else {
            logger.warn("RoomOptionsController: setLoggedInClerk called with a null loggedInClerk. This indicates an issue in the login or navigation flow.");
        }
    }
    public void setMainController(MainInterfaceController controller) {
        this.mainController = controller;
    }

    public void setGuestOptionsController(GuestOptionsController controller) {
        this.guestOptionsController = controller;
    }

    public void setParentBp(BorderPane parentBp) {
        this.parentBp = parentBp;
    }

    public void setGuest(Guest guest) {
        this.currentGuest = guest;
        if (guest != null) populateFields();
    }

    private void populateFields() {
        idField.setText(String.valueOf(currentGuest.getId()));
        idField.setDisable(true);
        credentialField.setText(String.valueOf(currentGuest.getCredential()));
        nameField.setText(currentGuest.getName());
        lastNameField.setText(currentGuest.getLastName());
        addressField.setText(currentGuest.getAddress());
        emailField.setText(currentGuest.getEmail());
        phoneField.setText(currentGuest.getPhoneNumber());
        countryField.setText(currentGuest.getNativeCountry());
    }

    @FXML
    void onCancel() {
        GuestOptionsController controller = Utility.loadPage2("guestinterface/guestoptions.fxml", parentBp);
        if (controller != null) {
            controller.setMainController(mainController);
            controller.loadGuestsIntoTable();
            controller.setLoggedInClerk(this.loggedInClerk);
            controller.setMainApp(this.mainAppReference);

        }
    }

    @FXML
    void onSave() {
        try {
            currentGuest.setCredential(Integer.parseInt(credentialField.getText()));
            currentGuest.setName(nameField.getText());
            currentGuest.setLastName(lastNameField.getText());
            currentGuest.setAddress(addressField.getText());
            currentGuest.setEmail(emailField.getText());
            currentGuest.setPhoneNumber(phoneField.getText());
            currentGuest.setNativeCountry(countryField.getText());

            Request request = new Request("updateGuest", currentGuest);
            Response response = ClientConnectionManager.sendRequest(request);

            if ("200".equalsIgnoreCase(response.getStatus())) {
                FXUtility.alertInfo("Éxito", "Huesped modificado correctamente.");
                onCancel();
            } else {
                FXUtility.alert("Error", "No se pudo modificar el huésped: " + response.getMessage());
            }

        } catch (NumberFormatException e) {
            FXUtility.alert("Error", "La credencial debe ser un número válido.");
            logger.error("Error de formato en GuestModify: {}", e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Error al modificar huésped: {}", e.getMessage(), e);
            FXUtility.alert("Error", "Error al modificar huésped: " + e.getMessage());
        }
    }
}
