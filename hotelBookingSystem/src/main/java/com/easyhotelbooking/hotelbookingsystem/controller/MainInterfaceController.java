package com.easyhotelbooking.hotelbookingsystem.controller;


import hotelbookingcommon.domain.Hotel;
import hotelbookingserver.service.HotelService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

import java.util.List;

public class MainInterfaceController {

        @FXML
        private ComboBox<?> clientCombo;

        @FXML
        private StackPane contentPane;

        @FXML
        private DatePicker fromDate;

        @FXML
        private ComboBox<?> hotelCombo;

        @FXML
        private Button searchButton;

        @FXML
        private TableView hotelTable;

        @FXML
        private DatePicker toDate;

        private final HotelService hotelService = new HotelService();

        @FXML
        public void initialize() {
            List<Hotel> hotels = hotelService.getAllHotels();
            hotelTable.setItems(FXCollections.observableArrayList(hotels));
        }

    }

