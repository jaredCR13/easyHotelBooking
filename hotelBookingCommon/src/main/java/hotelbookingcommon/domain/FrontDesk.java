package hotelbookingcommon.domain;

import java.io.Serializable;

public class FrontDesk implements Serializable {
    private String employeeId;
    private String name;
    private String lastName;
    private String password;
    private String user;
    private String phoneNumber;

    public FrontDesk(String employeeId, String name, String lastName, String password, String user, String phoneNumber) {
        this.employeeId = employeeId;
        this.name = name;
        this.lastName = lastName;
        this.password = password;
        this.user = user;
        this.phoneNumber = phoneNumber;
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
        return "FrontDeskStaff{" +
                "employeeNumber=" + employeeId +
                ", firstName='" + name + '\'' +
                ", lastName='" + lastName + '\'' +
                ", phone='" + phoneNumber + '\'' +
                ", username='" + user + '\'' +
                '}';
    }
}
