package com.easyhotelbooking.hotelbookingsystem.controller;


import com.easyhotelbooking.hotelbookingsystem.socket.ClientConnectionManager;
import com.easyhotelbooking.hotelbookingsystem.util.Utility;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import hotelbookingcommon.domain.Hotel;
import hotelbookingcommon.domain.Request;
import hotelbookingcommon.domain.Response;
import hotelbookingserver.service.HotelService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class MainInterfaceController  {

        @FXML
        private ComboBox<?> clientCombo;

        @FXML
        private BorderPane bp;
        @FXML
        private StackPane contentPane;

        @FXML
        private DatePicker fromDate;

        @FXML
        private ComboBox<String> hotelCombo;

        @FXML
        private Button searchButton;

        @FXML
        private TextArea textArea;

        @FXML
        private DatePicker toDate;

        private final HotelService hotelService = new HotelService();

        @FXML
        public void initialize() {
           loadHotelNames();
            }

        @FXML
        void hotelOptionsOnAction() {
               Utility.loadPage("hoteloptions.fxml",bp);
        }

        private void loadHotelNames() {
                Request request = new Request("getHotels", null);
                Response response = ClientConnectionManager.sendRequest(request);

                if ("OK".equalsIgnoreCase(response.getStatus()) && response.getData() != null) {
                        // Convertir la lista de hoteles desde JSON a List<Hotel>
                        Gson gson = new Gson();
                        List<Hotel> hotelList = gson.fromJson(gson.toJson(response.getData()),
                                new TypeToken<List<Hotel>>() {}.getType());

                        // Obtener los nombres y llenar el combo
                        List<String> names = hotelList.stream()
                                .map(Hotel::getHotelName)
                                .collect(Collectors.toList());

                        hotelCombo.getItems().addAll(names);
                } else {
                        System.out.println("❌ Error al obtener hoteles: " + response.getMessage());
                }
        }
}




