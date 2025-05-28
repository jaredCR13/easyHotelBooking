package com.easyhotelbooking.hotelbookingsystem.controller;

import com.easyhotelbooking.hotelbookingsystem.util.Utility;
import hotelbookingcommon.domain.FrontDeskClerk;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FrontDeskClerkOptionsController {

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
    void goBackOnAction() {
        Utility.loadFullView("maininterface.fxml", goBack);
    }

    @FXML
    void registerFrontDeskClerkOnAction() {
        try {
            String employeeId = employeeIdField.getText();
            String name = nameField.getText();
            String lastName = lastNameField.getText();
            String phoneNumber = phoneNumberField.getText();
            String user = userField.getText();
            String password = passwordField.getText();

            if (employeeId.isEmpty() || name.isEmpty() || lastName.isEmpty() || phoneNumber.isEmpty() || user.isEmpty() || password.isEmpty()) {
                util.FXUtility.alert("Error", "Todos los campos son obligatorios.");
                return;
            }

            int employeeNumber = Integer.parseInt(employeeId);

            FrontDeskClerk frontDeskClerk = new FrontDeskClerk(employeeId, name, lastName, password, user, phoneNumber );
            mainController.registerFrontDeskClerk(frontDeskClerk);

        } catch (NumberFormatException e) {
            util.FXUtility.alert("Error", "Número de empleado debe ser numérico.");
        } catch (Exception e) {
            logger.error("Error al registrar recepcionista: {}", e.getMessage());
            util.FXUtility.alert("Error", "Error al registrar recepcionista: " + e.getMessage());
        }
    }

    @FXML
    void consultFrontDeskClerkOnAction() {
        try {
            String frontDeskClerkEmployee = employeeIdField.getText();
            if (frontDeskClerkEmployee.isEmpty()) {
                util.FXUtility.alert("Error", "Ingrese el número de empleado a consultar.");
                return;
            }
            String employeeId = String.valueOf(Integer.parseInt(frontDeskClerkEmployee));
            mainController.consultFrontDeskClerk(employeeId);
        } catch (NumberFormatException e) {
            util.FXUtility.alert("Error", "Número de empleado inválido.");
        } catch (Exception e) {
            logger.error("Error al consultar recepcionista: {}", e.getMessage());
            util.FXUtility.alert("Error", "Error al consultar recepcionista: " + e.getMessage());
        }
    }

}


