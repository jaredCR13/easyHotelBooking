package hotelbookingcommon.domain;

import com.google.gson.annotations.Expose;
import org.mindrot.jbcrypt.BCrypt;

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
        //SE HASHEA LA PASSWORD
        this.password = BCrypt.hashpw(password, BCrypt.gensalt());
        this.user = user;
        this.phoneNumber = phoneNumber;
        this.frontDeskClerkRole=frontDeskClerkRole;
    }


    public FrontDeskClerk(String employeeId, String name, String lastName, String password, String user, String phoneNumber, FrontDeskClerkRole frontDeskClerkRole,int hotelId) {
        this.employeeId = employeeId;
        this.name = name;
        this.lastName = lastName;
        //SE HASHEA LA PASSWORD
        this.password = BCrypt.hashpw(password, BCrypt.gensalt());
        this.user = user;
        this.phoneNumber = phoneNumber;
        this.hotelId = hotelId;
        this.frontDeskClerkRole=frontDeskClerkRole;
    }

    //CONSTRUCTOR PARA CARGAR PERSISTENCIA (SIN RE-HASHEAR)**
    public FrontDeskClerk(String employeeId, String name, String lastName, String hashedPassword, String user, String phoneNumber, FrontDeskClerkRole frontDeskClerkRole, int hotelId, boolean isHashed) {
        this.employeeId = employeeId;
        this.name = name;
        this.lastName = lastName;
        this.password = hashedPassword; // Asignar la contraseña hasheada directamente
        this.user = user;
        this.phoneNumber = phoneNumber;
        this.hotelId = hotelId;
        this.frontDeskClerkRole = frontDeskClerkRole;
    }

    public FrontDeskClerk() {

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
        this.password = BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public void setHashedPassword(String hashedPassword) {
        this.password = hashedPassword; // Asigna el hash directamente, sin re-hashear
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

    //mét odo para verificar la contraseña
    public boolean checkPassword(String candidatePassword) {
        return BCrypt.checkpw(candidatePassword, this.password);
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
