package com.easyhotelbooking.hotelbookingsystem.controller.bookingregister;

import com.easyhotelbooking.hotelbookingsystem.util.Utility;
import hotelbookingcommon.domain.Booking;
import hotelbookingcommon.domain.Hotel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;

import java.util.Date;

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
    private BookingTableController bookingTableController;
    private Booking booking;
    private Hotel selectedHotel;
    private Date startDate;
    private Date endDate;

    public void setSelectedHotel(Hotel hotel, Date startDate, Date endDate) {
        this.selectedHotel = hotel;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void setBookingTableController(BookingTableController bookingTableController){
        this.bookingTableController= bookingTableController;
    }
    @FXML
    void onCancel(ActionEvent event) {

        BookingTableController controller = Utility.loadPage2("bookinginterface/bookingtable.fxml", bp);
        if (controller != null) {
            controller.setSelectedHotelFromSearchTable(selectedHotel,startDate,endDate);
        }
    }

    @FXML
    void onModify(ActionEvent event) {

    }
    public void setBooking(Booking booking) {
        this.booking = booking;

        // Cargar los datos en los campos (esto se puede mejorar)
        bookingNumberTf.setText(String.valueOf(booking.getBookingNumber()));
        daysOfStayTf.setText(String.valueOf(booking.getDaysOfStay()));
        startDatePicker.setValue(Utility.convertToLocalDate(booking.getStartDate()));
        endDatePicker.setValue(Utility.convertToLocalDate(booking.getEndDate()));
        textAreaRoomId.setText(String.valueOf(booking.getRoomNumber()));
        // También puedes setear los valores en los ComboBox si ya están cargados
    }
}

