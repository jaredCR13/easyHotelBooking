package hotelbookingcommon.domain;

import java.io.Serializable;
import java.util.List;

public class Room implements Serializable {

    private int roomNumber;
    private double roomPrice;
    private String detailedDescription;
    private RoomStatus status;
    private RoomStyle style;
    List<String> imagesPaths;

    public Room(int roomNumber, double roomPrice, String detailedDescription, RoomStatus status, RoomStyle style, List<String> imagesPaths) {
        this.roomNumber = roomNumber;
        this.roomPrice = roomPrice;
        this.detailedDescription = detailedDescription;
        this.status = status;
        this.style = style;
        this.imagesPaths = imagesPaths;
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

    @Override
    public String toString() {
        return "Room{" +
                "roomNumber=" + roomNumber +
                ", roomPrice=" + roomPrice +
                ", detailedDescription='" + detailedDescription + '\'' +
                ", status=" + status +
                ", style=" + style +
                ", imagesPaths=" + imagesPaths +
                '}';
    }
}
