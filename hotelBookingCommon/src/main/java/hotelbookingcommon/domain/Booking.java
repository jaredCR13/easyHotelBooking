package hotelbookingcommon.domain;

import java.util.Date;

public class Booking {

    private int bookingNumber;
    private Guest guest;
    private Date starDate;
    private Date endDate;
    private FrontDeskClerk frontDeskClerk;
    private int daysOfStay;
    private Room room;

    public Booking(int bookingNumber, Guest guest, Date starDate, Date endDate, FrontDeskClerk frontDeskClerk, int daysOfStay, Room room) {
        this.bookingNumber = bookingNumber;
        this.guest = guest;
        this.starDate = starDate;
        this.endDate = endDate;
        this.frontDeskClerk = frontDeskClerk;
        this.daysOfStay = daysOfStay;
        this.room = room;
    }

    public int getBookingNumber() {
        return bookingNumber;
    }

    public void setBookingNumber(int bookingNumber) {
        this.bookingNumber = bookingNumber;
    }

    public Guest getGuest() {
        return guest;
    }

    public void setGuest(Guest guest) {
        this.guest = guest;
    }

    public Date getStarDate() {
        return starDate;
    }

    public void setStarDate(Date starDate) {
        this.starDate = starDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public FrontDeskClerk getFrontDesk() {
        return frontDeskClerk;
    }

    public void setFrontDesk(FrontDeskClerk frontDeskClerk) {
        this.frontDeskClerk = frontDeskClerk;
    }

    public int getDaysOfStay() {
        return daysOfStay;
    }

    public void setDaysOfStay(int daysOfStay) {
        this.daysOfStay = daysOfStay;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }
}
