package hotelbookingcommon.domain.LogIn;

import com.google.gson.annotations.Expose;
import hotelbookingcommon.domain.FrontDeskClerk;
import hotelbookingcommon.domain.FrontDeskClerkRole; // ¡Asegúrate de importar este enum!

import java.io.Serializable;

public class FrontDeskClerkDTO implements Serializable {
    @Expose
    private String employeeId;
    @Expose
    private String name;
    @Expose
    private String lastName;
    @Expose
    private String user;
    @Expose
    private String phoneNumber;
    @Expose
    private String role;
    @Expose
    private int hotelId;

    // Constructor que toma un objeto FrontDeskClerk para construir el DTO
    public FrontDeskClerkDTO(FrontDeskClerk clerk) {
        this.employeeId = clerk.getEmployeeId();
        this.name = clerk.getName();
        this.lastName = clerk.getLastName();
        this.user = clerk.getUser();
        this.phoneNumber = clerk.getPhoneNumber();
        this.role = clerk.getFrontDeskClerkRole().name(); // Convierte el enum a String
        this.hotelId = clerk.getHotelId();
    }

    // Constructor que toma los campos directamente
    public FrontDeskClerkDTO(String employeeId, String name, String lastName, String user, String phoneNumber, String role, int hotelId) {
        this.employeeId = employeeId;
        this.name = name;
        this.lastName = lastName;
        this.user = user;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.hotelId = hotelId;
    }

    // Getters y Setters
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getRole() { return role; } // Mantienes este getter para el String
    public void setRole(String role) { this.role = role; }
    public int getHotelId() { return hotelId; }
    public void setHotelId(int hotelId) { this.hotelId = hotelId; }


    public FrontDeskClerkRole getFrontDeskClerkRoleEnum() {
        try {
            return FrontDeskClerkRole.valueOf(this.role);
        } catch (IllegalArgumentException e) {
            // Manejar caso donde el string 'role' no coincide con un enum válido.
            // Esto podría ser un error de datos. Puedes retornar un valor por defecto
            // o lanzar una excepción. Aquí, retornamos null o un rol por defecto.
            System.err.println("Rol '" + this.role + "' no reconocido. Usando rol por defecto.");
            return null; // O FrontDeskClerkRole.FRONT_DESK_CLERK; o un rol de "invitado" si tienes uno
        }
    }


    @Override
    public String toString() {
        return "FrontDeskClerkDTO{" +
                "employeeId='" + employeeId + '\'' +
                ", name='" + name + '\'' +
                ", lastName='" + lastName + '\'' +
                ", user='" + user + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", role='" + role + '\'' +
                ", hotelId=" + hotelId +
                '}';
    }
}