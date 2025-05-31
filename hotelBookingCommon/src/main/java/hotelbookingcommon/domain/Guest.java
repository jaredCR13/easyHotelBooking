package hotelbookingcommon.domain;

import com.google.gson.annotations.Expose;
import java.util.List;
import java.util.ArrayList;

public class Guest {

    @Expose
    private int id;

    @Expose
    private int credential;

    @Expose
    private String name;

    @Expose
    private String lastName;

    @Expose
    private String address;

    @Expose
    private String email;

    @Expose
    private String phoneNumber;

    @Expose
    private String nativeCountry;

    @Expose
    private List<Booking> bookings; // 1 GUEST HAS 0...** BOOKINGS

    // Constructor sin lista
    public Guest(int id, int credential, String name, String lastName, String address, String email, String phoneNumber, String nativeCountry) {
        this.id = id;
        this.credential = credential;
        this.name = name;
        this.lastName = lastName;
        this.address = address;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.nativeCountry = nativeCountry;
        this.bookings = new ArrayList<>();
    }

    // Constructor con lista
    public Guest(int id, int credential, String name, String lastName, String address, String email, String phoneNumber, String nativeCountry, List<Booking> bookings) {
        this(id, credential, name, lastName, address, email, phoneNumber, nativeCountry);
        this.bookings = bookings != null ? bookings : new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCredential() {
        return credential;
    }

    public void setCredential(int credential) {
        this.credential = credential;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getNativeCountry() {
        return nativeCountry;
    }

    public void setNativeCountry(String nativeCountry) {
        this.nativeCountry = nativeCountry;
    }

    public List<Booking> getBookings() {
        return bookings;
    }

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings != null ? bookings : new ArrayList<>();
    }

    public void addBooking(Booking booking) {
        this.bookings.add(booking);
    }
}
