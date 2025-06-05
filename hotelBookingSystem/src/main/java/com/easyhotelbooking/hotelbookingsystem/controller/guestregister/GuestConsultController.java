package com.easyhotelbooking.hotelbookingsystem.controller.guestregister;
import hotelbookingcommon.domain.Guest;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class GuestConsultController {

    @FXML private Label idLabel;
    @FXML private Label credentialLabel;
    @FXML private Label nameLabel;
    @FXML private Label lastNameLabel;
    @FXML private Label addressLabel;
    @FXML private Label emailLabel;
    @FXML private Label phoneLabel;
    @FXML private Label countryLabel;
    @FXML private Button goBackButton;
    private BorderPane parentBp;

    public void setGuest(Guest guest) {
        idLabel.setText(String.valueOf(guest.getId()));
        credentialLabel.setText(String.valueOf(guest.getCredential()));
        nameLabel.setText(guest.getName());
        lastNameLabel.setText(guest.getLastName());
        addressLabel.setText(guest.getAddress());
        emailLabel.setText(guest.getEmail());
        phoneLabel.setText(guest.getPhoneNumber());
        countryLabel.setText(guest.getNativeCountry());
    }

    @FXML
    void goBackOnAction() {
        Stage stage = (Stage) goBackButton.getScene().getWindow();
        stage.close();
    }
}
