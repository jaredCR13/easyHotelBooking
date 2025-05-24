// In hotelbookingcommon.domain.Hotel
package hotelbookingcommon.domain;

import com.google.gson.annotations.Expose; // Import this!
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Hotel implements Serializable {

    @Expose // This field will be serialized
    private int numHotel;
    @Expose // This field will be serialized
    private String hotelName;
    @Expose // This field will be serialized
    private String hotelLocation;

    // Use @Expose for the rooms list if you want it serialized when serializing a Hotel.
    // If you don't want the rooms list to be serialized WITH the hotel, remove @Expose here.
    // For displaying rooms in MainInterfaceController, you DO want this.
    @Expose
    private List<Room> rooms;

    public Hotel(int numHotel, String hotelName, String hotelLocation) {
        this.numHotel = numHotel;
        this.hotelName = hotelName;
        this.hotelLocation = hotelLocation;
        this.rooms = new ArrayList<>(); // Initialize the list
    }

    // Constructor without rooms (for initial creation)
    public Hotel(int numHotel, String hotelName, String hotelLocation, List<Room> rooms) {
        this(numHotel, hotelName, hotelLocation); // Call existing constructor
        if (rooms != null) {
            this.rooms = new ArrayList<>(rooms);
        }
    }

    // Add a default constructor for Gson if you don't have one
    public Hotel() {
        this.rooms = new ArrayList<>();
    }

    // Getters and Setters

    public int getNumHotel() {
        return numHotel;
    }

    public void setNumHotel(int numHotel) {
        this.numHotel = numHotel;
    }

    public String getHotelName() {
        return hotelName;
    }

    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }

    public String getHotelLocation() {
        return hotelLocation;
    }

    public void setHotelLocation(String hotelLocation) {
        this.hotelLocation = hotelLocation;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public void setRooms(List<Room> rooms) {
        this.rooms = rooms;
    }

    public void addRoom(Room room) {
        if (this.rooms == null) {
            this.rooms = new ArrayList<>();
        }
        this.rooms.add(room);
        room.setHotel(this); // Ensure bidirectionality
        room.setHotelId(this.numHotel); // Ensure hotelId is set
    }

    public void removeRoom(Room room) {
        if (this.rooms != null) {
            this.rooms.remove(room);
            room.setHotel(null); // Clear bidirectionality
            room.setHotelId(-1); // Or a default invalid ID
        }
    }

    @Override
    public String toString() {
        return "Hotel{" +
                "numHotel=" + numHotel +
                ", hotelName='" + hotelName + '\'' +
                ", hotelLocation='" + hotelLocation + '\'' +
                ", roomsCount=" + (rooms != null ? rooms.size() : 0) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Hotel hotel = (Hotel) o;
        return numHotel == hotel.numHotel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(numHotel);
    }
}