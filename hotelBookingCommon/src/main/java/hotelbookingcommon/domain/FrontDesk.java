package hotelbookingcommon.domain;

public class FrontDesk {
    private int employeeId;
    private String name;
    private String lastName;
    private String password;
    private String user;
    private int phoneNumber;

    public FrontDesk(int employeeId, String name, String lastName, String password, String user, int phoneNumber) {
        this.employeeId = employeeId;
        this.name = name;
        this.lastName = lastName;
        this.password = password;
        this.user = user;
        this.phoneNumber = phoneNumber;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
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

    public int getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(int phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
