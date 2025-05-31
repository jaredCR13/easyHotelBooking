package com.easyhotelbooking.hotelbookingsystem.controller.frontdeskclerkregister;

import com.easyhotelbooking.hotelbookingsystem.controller.maininterface.MainInterfaceController;
import com.easyhotelbooking.hotelbookingsystem.util.Utility;
import hotelbookingcommon.domain.FrontDeskClerk;
import hotelbookingcommon.domain.FrontDeskClerkRole;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FrontDeskClerkRegisterController {

    @FXML private TextField employeeIdField;
    @FXML private TextField nameField;
    @FXML private TextField lastNameField;
    @FXML private TextField phoneNumberField;
    @FXML private TextField userField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<FrontDeskClerkRole> roleCombo;
    private BorderPane parentBp;
    private MainInterfaceController mainController;
    private FrontDeskClerkOptionsController optionsController; // opcional
    private static final Logger logger = LogManager.getLogger(FrontDeskClerkRegisterController.class);

        public void setParentBp(BorderPane parentBp) {
            this.parentBp = parentBp;
        }

        public void setMainController(MainInterfaceController controller) {
            this.mainController = controller;
        }

        @FXML
        void initialize() {
            roleCombo.getItems().setAll(FrontDeskClerkRole.values());
            //roleComboBox.getItems().addAll("Administrador", "Recepcionista");
            //roleComboBox.setValue("Recepcionista");
        }


    public void setOptionsController(FrontDeskClerkOptionsController optionsController) {
            this.optionsController = optionsController;
        }

            


        @FXML
        void onConfirm() {
            String id = employeeIdField.getText();
            String name = nameField.getText();
            String lastName = lastNameField.getText();
            String phone = phoneNumberField.getText();
            String user = userField.getText();
            String password = passwordField.getText();
            FrontDeskClerkRole role = roleCombo.getValue();

            if (id.isEmpty() || name.isEmpty() || lastName.isEmpty() || phone.isEmpty() || user.isEmpty() || password.isEmpty() || role == null) {
                util.FXUtility.alert("Error", "Todos los campos son obligatorios.");
                return;
            }

            FrontDeskClerk clerk = new FrontDeskClerk(id, name, lastName, password, user, phone, -1);
            //clerk.setRole(role);
            mainController.registerFrontDeskClerk(clerk);

            ((Stage) employeeIdField.getScene().getWindow()).close();
        }

        @FXML
        void onCancel() {
            // Volver a cargar la vista anterior (FrontDeskClerkOptions)
            FrontDeskClerkOptionsController controller = Utility.loadPage2("frontdeskclerkinterface/frontdeskclerkoptions.fxml", parentBp);
            if (controller != null) {
                controller.setMainController(mainController);
                controller.setStage(((Stage) parentBp.getScene().getWindow())); // opcional
            }
        }



}
