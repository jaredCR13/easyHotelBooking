// In hotelbookingcommon.domain.Room
package hotelbookingcommon.domain;

import com.google.gson.annotations.Expose; // Import this!
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Collections; // For Collections.emptyList()

public class Room implements Serializable {

    @Expose
    private int roomNumber;
    @Expose
    private double roomPrice;
    @Expose
    private String detailedDescription;
    @Expose
    private RoomStatus status;
    @Expose
    private RoomStyle style;
    @Expose
    private List<String> imagesPaths; // Assuming this might be serialized later

    // This is the field that causes the circular reference.
    // We *do not* want Gson to serialize the entire Hotel object when it's serializing a Room.
    // We only want the hotelId. So, we DO NOT use @Expose here.
    private transient Hotel hotel; // Mark as transient so it's ignored by default serialization

    // This field will be serialized instead of the full Hotel object
    @Expose
    private int hotelId; // Store the ID of the associated hotel

    public Room(int roomNumber, double roomPrice, String detailedDescription, RoomStatus status, RoomStyle style, List<String> imagesPaths) {
        this.roomNumber = roomNumber;
        this.roomPrice = roomPrice;
        this.detailedDescription = detailedDescription;
        this.status = status;
        this.style = style;
        this.imagesPaths = imagesPaths != null ? new ArrayList<>(imagesPaths) : new ArrayList<>();
        this.hotelId = -1; // Default to no hotel associated
    }

    // Constructor with hotelId (for loading from data layer)
    public Room(int roomNumber, double roomPrice, String detailedDescription, RoomStatus status, RoomStyle style, List<String> imagesPaths, int hotelId) {
        this(roomNumber, roomPrice, detailedDescription, status, style, imagesPaths);
        this.hotelId = hotelId;
    }

    // Add a default constructor for Gson if you don't have one
    public Room() {
        this.imagesPaths = new ArrayList<>();
        this.hotelId = -1;
    }

    // Getters and Setters

    public int getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(int roomNumber) {
        this.roomNumber = roomNumber;
    }

    public double getRoomPrice() {
        return roomPrice;
    }

    public void setRoomPrice(double roomPrice) {
        this.roomPrice = roomPrice;
    }

    public String getDetailedDescription() {
        return detailedDescription;
    }

    public void setDetailedDescription(String detailedDescription) {
        this.detailedDescription = detailedDescription;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public void setStatus(RoomStatus status) {
        this.status = status;
    }

    public RoomStyle getStyle() {
        return style;
    }

    public void setStyle(RoomStyle style) {
        this.style = style;
    }

    public List<String> getImagesPaths() {
        return imagesPaths;
    }

    public void setImagesPaths(List<String> imagesPaths) {
        this.imagesPaths = imagesPaths;
    }

    // *** IMPORTANT for breaking circular reference ***
    // This getter is for the actual Hotel object in memory.
    // It should *not* be serialized by Gson.
    public Hotel getHotel() {
        return hotel;
    }

    // This setter is used to link the Room to a Hotel object in memory.
    // It is called when associating a room with a hotel in the service layer.
    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
    }

    public int getHotelId() {
        return hotelId;
    }

    public void setHotelId(int hotelId) {
        this.hotelId = hotelId;
    }

    @Override
    public String toString() {
        return "Room{" +
                "roomNumber=" + roomNumber +
                ", roomPrice=" + roomPrice +
                ", detailedDescription='" + detailedDescription + '\'' +
                ", status=" + status +
                ", style=" + style +
                ", hotelId=" + hotelId + // Display hotelId here
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Room room = (Room) o;
        return roomNumber == room.roomNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(roomNumber);
    }
}