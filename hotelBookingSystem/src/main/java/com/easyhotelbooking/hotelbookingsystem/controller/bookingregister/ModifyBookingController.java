package com.easyhotelbooking.hotelbookingsystem.controller.bookingregister;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;

public class ModifyBookingController {

    @FXML
    private TextField bookingNumberTf;

    @FXML
    private BorderPane bp;

    @FXML
    private Button cancelButton;

    @FXML
    private TextField daysOfStayTf;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private FlowPane flowPane;

    @FXML
    private ComboBox<?> frontDeskClerkCombo;

    @FXML
    private ComboBox<?> guestCombo;

    @FXML
    private Button modifyButtom;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private TextArea textAreaRoomId;

    @FXML
    void onCancel(ActionEvent event) {

    }

    @FXML
    void onModify(ActionEvent event) {

    }

}

