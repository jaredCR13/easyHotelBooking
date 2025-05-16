package hotelbookingcommon.domain;

import java.io.Serializable;

public class Hotel implements Serializable {
    private int numHotel;
    private String hotelName;
    private String hotelLocation;

    public Hotel(int numHotel, String hotelName, String hotelLocation) {
        this.numHotel = numHotel;
        this.hotelName = hotelName;
        this.hotelLocation = hotelLocation;
    }

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
}
