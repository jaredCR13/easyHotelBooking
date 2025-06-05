package hotelbookingcommon.domain;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class Booking implements Serializable {

    private int bookingNumber;
    private int hotelId;
    private int guestId;             // Solo el ID
    private Date startDate;
    private Date endDate;
    private String frontDeskClerkId; // Solo el ID
    private int daysOfStay;
    private int roomNumber;          // Solo el número de habitación (que actúa como ID)

    public Booking(int bookingNumber,int hotelId, int guestId, Date starDate, Date endDate, String frontDeskClerkId, int daysOfStay, int roomNumber) {
        this.bookingNumber = bookingNumber;
        this.hotelId= hotelId;
        this.guestId = guestId;
        this.startDate = starDate;
        this.endDate = endDate;
        this.frontDeskClerkId = frontDeskClerkId;
        this.daysOfStay = daysOfStay;
        this.roomNumber = roomNumber;

    }

    public Booking() {
    }

    public int getHotelId() {
        return hotelId;
    }

    public void setHotelId(int hotelId) {
        this.hotelId = hotelId;
    }

    public int getBookingNumber() {
        return bookingNumber;
    }

    public void setBookingNumber(int bookingNumber) {
        this.bookingNumber = bookingNumber;
    }

    public int getGuestId() { // Getter para el ID del huésped
        return guestId;
    }

    public void setGuestId(int guestId) { // Setter para el ID del huésped
        this.guestId = guestId;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getFrontDeskClerkId() { // Getter para el ID del recepcionista
        return frontDeskClerkId;
    }

    public void setFrontDeskClerkId(String frontDeskClerkId) { // Setter para el ID del recepcionista
        this.frontDeskClerkId = frontDeskClerkId;
    }

    public int getDaysOfStay() {
        return daysOfStay;
    }

    public void setDaysOfStay(int daysOfStay) {
        this.daysOfStay = daysOfStay;
    }

    public int getRoomNumber() { // Getter para el número de habitación
        return roomNumber;
    }

    public void setRoomNumber(int roomNumber) { // Setter para el número de habitación
        this.roomNumber = roomNumber;
    }

    public String getStartDateStr() {
        if (startDate == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(startDate);
    }

    public String getEndDateStr() {
        if (endDate == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(endDate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Booking booking = (Booking) o;

        return bookingNumber == booking.bookingNumber &&
                hotelId == booking.hotelId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookingNumber, hotelId);
    }
    @Override
    public String toString() {
        return "Booking{" +
                "bookingNumber=" + bookingNumber +
                ", hotelId=" + hotelId +
                ", guestId=" + guestId +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", frontDeskClerkId='" + frontDeskClerkId + '\'' +
                ", daysOfStay=" + daysOfStay +
                ", roomNumber=" + roomNumber +
                '}';
    }
}