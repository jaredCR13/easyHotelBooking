package com.easyhotelbooking.hotelbookingsystem.controller;


import com.easyhotelbooking.hotelbookingsystem.util.Utility;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

    public class HotelOptionsController {

        @FXML
        private BorderPane bp;

        @FXML
        private Button consultButton;

        @FXML
        private TextField hotelNumberField;

        @FXML
        private TextField locationField;

        @FXML
        private Button modifyButton;

        @FXML
        private TextField nameField;

        @FXML
        private Button registerButton;

        @FXML
        private Button removeButton;

        @FXML
        private Button goBack;
        @FXML
        public void initialize(){


        }

        @FXML
        void goBackOnAction() {

            Utility.loadFullView("maininterface.fxml",goBack);

        }



    }


