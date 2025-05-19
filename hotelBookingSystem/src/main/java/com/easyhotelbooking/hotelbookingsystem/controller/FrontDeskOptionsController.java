package com.easyhotelbooking.hotelbookingsystem.controller;

import com.easyhotelbooking.hotelbookingsystem.util.Utility;
import hotelbookingcommon.domain.FrontDesk;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FrontDeskOptionsController {

    @FXML
    private BorderPane bp;

    @FXML
    private Button registerButton;

    @FXML
    private Button goBack;

    @FXML
    private Button consultButton;

    @FXML
    private Button removeButton;

    @FXML
    private Button modifyButton;

    @FXML
    private TextField employeeIdField;

    @FXML
    private TextField nameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField phoneNumberField;

    @FXML
    private TextField userField;

    @FXML
    private PasswordField passwordField;

    private static final Logger logger = LogManager.getLogger(FrontDeskOptionsController.class);

    private MainInterfaceController mainController;

    public void setMainController(MainInterfaceController mainController) {
        this.mainController = mainController;
    }

    @FXML
    void goBackOnAction() {
        Utility.loadFullView("maininterface.fxml", goBack);
    }

    @FXML
    void registerFrontDeskOnAction() {
        try {
            String employeeId = employeeIdField.getText();
            String name = nameField.getText();
            String lastName = lastNameField.getText();
            String phoneNumber = phoneNumberField.getText();
            String user = userField.getText();
            String password = passwordField.getText();

            if (employeeId.isEmpty() || name.isEmpty() || lastName.isEmpty() || phoneNumber.isEmpty() || user.isEmpty() || password.isEmpty()) {
                mostrarAlerta("Error", "Todos los campos son obligatorios.");
                return;
            }

            int employeeNumber = Integer.parseInt(employeeId);

            FrontDesk frontDesk = new FrontDesk(employeeId, name, lastName, password, user, phoneNumber );
            mainController.registerFrontDesk(frontDesk);

        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Número de empleado debe ser numérico.");
        } catch (Exception e) {
            logger.error("Error al registrar recepcionista: {}", e.getMessage());
            mostrarAlerta("Error", "Error al registrar recepcionista: " + e.getMessage());
        }
    }

    @FXML
    void consultFrontDesOnAction() {
        try {
            String empStr = employeeIdField.getText();
            if (empStr.isEmpty()) {
                mostrarAlerta("Error", "Ingrese el número de empleado a consultar.");
                return;
            }
            String employeeId = String.valueOf(Integer.parseInt(empStr));
            mainController.consultFrontDesk(employeeId);
        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Número de empleado inválido.");
        } catch (Exception e) {
            logger.error("Error al consultar recepcionista: {}", e.getMessage());
            mostrarAlerta("Error", "Error al consultar recepcionista: " + e.getMessage());
        }
    }

    private void mostrarAlerta(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}


