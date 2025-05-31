package hotelbookingcommon.domain;

import com.google.gson.annotations.Expose;

import java.io.Serializable;

public class FrontDeskClerk implements Serializable {
    @Expose
    private String employeeId;
    @Expose
    private String name;
    @Expose
    private String lastName;
    @Expose
    private String password;
    @Expose
    private String user;
    @Expose
    private String phoneNumber;
    private transient Hotel hotel;
    @Expose
    private FrontDeskClerkRole frontDeskClerkRole;
    @Expose
    private int hotelId;

    public FrontDeskClerk(String employeeId, String name, String lastName, String password, String user, String phoneNumber, FrontDeskClerkRole frontDeskClerkRole) {
        this.employeeId = employeeId;
        this.name = name;
        this.lastName = lastName;
        this.password = password;
        this.user = user;
        this.phoneNumber = phoneNumber;
        this.frontDeskClerkRole=frontDeskClerkRole;
    }


    public FrontDeskClerk(String employeeId, String name, String lastName, String password, String user, String phoneNumber, FrontDeskClerkRole frontDeskClerkRole,int hotelId) {
        this.employeeId = employeeId;
        this.name = name;
        this.lastName = lastName;
        this.password = password;
        this.user = user;
        this.phoneNumber = phoneNumber;
        this.hotelId = hotelId;
        this.frontDeskClerkRole=frontDeskClerkRole;
    }

    public FrontDeskClerkRole getFrontDeskClerkRole() {
        return frontDeskClerkRole;
    }

    public void setFrontDeskClerkRole(FrontDeskClerkRole frontDeskClerkRole) {
        this.frontDeskClerkRole = frontDeskClerkRole;
    }

    public Hotel getHotel() {
        return hotel;
    }

    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
    }

    public int getHotelId() {
        return hotelId;
    }

    public void setHotelId(int hotelId) {
        this.hotelId = hotelId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public String toString() {
        return "FrontDeskClerk{" +
                "employeeId=" + employeeId +
                ", name='" + name + '\'' +
                ", lastName='" + lastName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", usern='" + user + '\'' +
                '}';
    }
}
