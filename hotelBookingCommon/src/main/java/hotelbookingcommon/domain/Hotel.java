
package hotelbookingcommon.domain;

import com.google.gson.annotations.Expose; // Import this!
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Hotel implements Serializable {

    @Expose
    private int numHotel;
    @Expose
    private String hotelName;
    @Expose
    private String hotelLocation;

    @Expose
    private List<Room> rooms; //1 HOTEL HAS 0..** ROOMS
    private List<FrontDeskClerk>frontDeskClerks;
    //TODO
    // 1 HOTEL HAS 0...** BOOKINGS

    public Hotel(int numHotel, String hotelName, String hotelLocation) {
        this.numHotel = numHotel;
        this.hotelName = hotelName;
        this.hotelLocation = hotelLocation;
        this.rooms = new ArrayList<>();
        this.frontDeskClerks=new ArrayList<>();
    }

    public Hotel(int numHotel, String hotelName, String hotelLocation, List<Room> rooms) {
        this(numHotel, hotelName, hotelLocation);
        if (rooms != null) {
            this.rooms = new ArrayList<>(rooms);
        }
    }

    public Hotel() {
        this.rooms = new ArrayList<>();
        this.frontDeskClerks=new ArrayList<>();
    }

    public List<FrontDeskClerk> getFrontDeskClerks() {
        return frontDeskClerks;
    }

    public void setFrontDeskClerks(List<FrontDeskClerk> frontDeskClerks) {
        this.frontDeskClerks = frontDeskClerks;
    }

    //Getters and Setters
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
        room.setHotel(this);
        room.setHotelId(this.numHotel);
    }

    public void removeRoom(Room room) {
        if (this.rooms != null) {
            this.rooms.remove(room);
            //ANULA LA ASOCIACION
            room.setHotel(null);
            room.setHotelId(-1);
        }
    }

    public void addFrontDeskClerk(FrontDeskClerk clerk) {
        if (this.frontDeskClerks == null) {
            this.frontDeskClerks = new ArrayList<>();
        }
        this.frontDeskClerks.add(clerk);
        clerk.setHotel(this);           //Asigna el hotel al clerk
        clerk.setHotelId(this.numHotel);//Asigna el id del hotel al clerk
    }

    public void removeFrontDeskClerk(FrontDeskClerk clerk) {
        if (this.frontDeskClerks != null) {
            this.frontDeskClerks.remove(clerk);
            clerk.setHotel(null); //Rompe la relación
            clerk.setHotelId(-1);
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