package com.easyhotelbooking.hotelbookingsystem.controller.frontdeskclerkregister;

import com.easyhotelbooking.hotelbookingsystem.controller.maininterface.MainInterfaceController;
import com.easyhotelbooking.hotelbookingsystem.socket.ClientConnectionManager;
import com.easyhotelbooking.hotelbookingsystem.util.FXUtility;
import com.easyhotelbooking.hotelbookingsystem.util.Utility;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import hotelbookingcommon.domain.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class FrontDeskClerkRegisterController {

    @FXML private TextField employeeIdField;
    @FXML private TextField nameField;
    @FXML private TextField lastNameField;
    @FXML private TextField phoneNumberField;
    @FXML private TextField userField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<FrontDeskClerkRole> roleCombo;
    @FXML private ComboBox<Hotel>hotelCombo;
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

        loadHotelsIntoComboBox();
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
        Hotel selectedHotel = hotelCombo.getSelectionModel().getSelectedItem();

        if (id.isEmpty() || name.isEmpty() || lastName.isEmpty() || phone.isEmpty() || user.isEmpty() || password.isEmpty() || role == null || selectedHotel==null) {
            FXUtility.alert("Error", "Todos los campos son obligatorios.");
            return;
        }

        FrontDeskClerk clerk = new FrontDeskClerk(id, name, lastName, password, user, phone,role,selectedHotel.getNumHotel());
        mainController.registerFrontDeskClerk(clerk);
        clearFields();
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
    private void loadHotelsIntoComboBox() {
        Request request = new Request("getHotels", null);
        Response response = ClientConnectionManager.sendRequest(request);
        if ("200".equalsIgnoreCase(response.getStatus()) && response.getData() != null) {
            List<Hotel> hotels = new Gson().fromJson(new Gson().toJson(response.getData()), new TypeToken<List<Hotel>>() {}.getType());
            hotelCombo.setItems(FXCollections.observableArrayList(hotels));
            hotelCombo.setCellFactory(lv -> new ListCell<Hotel>() {
                @Override
                protected void updateItem(Hotel hotel, boolean empty) {
                    super.updateItem(hotel, empty);
                    setText(empty ? "" : hotel.getHotelName() + " (" + hotel.getNumHotel() + ")");
                }
            });
            hotelCombo.setButtonCell(new ListCell<Hotel>() {
                @Override
                protected void updateItem(Hotel hotel, boolean empty) {
                    super.updateItem(hotel, empty);
                    setText(empty ? "Seleccione un Hotel" : hotel.getHotelName() + " (" + hotel.getNumHotel() + ")");
                }
            });
        } else {
            FXUtility.alert("Error", "No se pudieron cargar los hoteles para el ComboBox.");
            logger.error("Error al cargar hoteles para ComboBox: {}", response != null ? response.getMessage() : "Desconocido");
        }
    }
    private void clearFields(){
        hotelCombo.getSelectionModel().clearSelection();
        nameField.clear();
        lastNameField.clear();
        passwordField.clear();
        userField.clear();
        roleCombo.getSelectionModel().clearSelection();
        hotelCombo.getSelectionModel().clearSelection();
        employeeIdField.clear();
        phoneNumberField.clear();
    }

}
