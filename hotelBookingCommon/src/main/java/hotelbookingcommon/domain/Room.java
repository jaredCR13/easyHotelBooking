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
    private List<String> imagesPaths;

    private transient Hotel hotel;

    @Expose
    private int hotelId;

    public Room(int roomNumber, double roomPrice, String detailedDescription, RoomStatus status, RoomStyle style, List<String> imagesPaths) {
        this.roomNumber = roomNumber;
        this.roomPrice = roomPrice;
        this.detailedDescription = detailedDescription;
        this.status = status;
        this.style = style;
        this.imagesPaths = imagesPaths != null ? new ArrayList<>(imagesPaths) : new ArrayList<>();
        this.hotelId = -1;
    }

    public Room(int roomNumber, double roomPrice, String detailedDescription, RoomStatus status, RoomStyle style, List<String> imagesPaths, int hotelId) {
        this(roomNumber, roomPrice, detailedDescription, status, style, imagesPaths);
        this.hotelId = hotelId;
    }

    public Room() {
        this.imagesPaths = new ArrayList<>();
        this.hotelId = -1;
    }



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

    @Override
    public String toString() {
        return "Room{" +
                "roomNumber=" + roomNumber +
                ", roomPrice=" + roomPrice +
                ", detailedDescription='" + detailedDescription + '\'' +
                ", status=" + status +
                ", style=" + style +
                ", hotelId=" + hotelId +
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