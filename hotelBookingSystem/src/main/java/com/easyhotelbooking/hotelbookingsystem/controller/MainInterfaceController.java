package com.easyhotelbooking.hotelbookingsystem.controller;


import com.easyhotelbooking.hotelbookingsystem.util.Utility;
import hotelbookingcommon.domain.Hotel;
import hotelbookingserver.service.HotelService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.List;

public class MainInterfaceController {

        @FXML
        private ComboBox<?> clientCombo;

        @FXML
        private BorderPane bp;
        @FXML
        private StackPane contentPane;

        @FXML
        private DatePicker fromDate;

        @FXML
        private ComboBox<Hotel> hotelCombo;

        @FXML
        private Button searchButton;

        @FXML
        private TextArea textArea;

        @FXML
        private DatePicker toDate;

        private final HotelService hotelService = new HotelService();

        @FXML
        public void initialize() {
            List<Hotel> hotels = hotelService.getAllHotels();

            }

        @FXML
        void hotelOptionsOnAction() {
               Utility.loadPage("hoteloptions.fxml",bp);
        }

}



