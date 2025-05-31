package com.easyhotelbooking.hotelbookingsystem.controller.frontdeskclerkregister;

import com.easyhotelbooking.hotelbookingsystem.util.Utility;
import hotelbookingcommon.domain.FrontDeskClerk;
import hotelbookingcommon.domain.Room;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class FrontDeskClerkConsultController {

    @FXML
    private Label employeeIdLabel;
    @FXML private Label nameLabel;
    @FXML private Label lastNameLabel;
    @FXML private Label phoneLabel;
    @FXML private Label usernameLabel;
    @FXML private Label roleLabel;
    @FXML private Label hotelIdLabel;


    public void setClerk(FrontDeskClerk clerk){

        employeeIdLabel.setText(String.valueOf(clerk.getEmployeeId()));
        nameLabel.setText(String.valueOf(clerk.getName()));
        lastNameLabel.setText(String.valueOf(clerk.getLastName()));
        phoneLabel.setText(String.valueOf(clerk.getPhoneNumber()));
        usernameLabel.setText(String.valueOf(clerk.getUser()));
        roleLabel.setText(String.valueOf(clerk.getFrontDeskClerkRole()));
        hotelIdLabel.setText(String.valueOf(clerk.getHotelId()));


    }
    @FXML
    public void onClose(){
        Stage stage = (Stage) employeeIdLabel.getScene().getWindow();
        stage.close();

    }
}
