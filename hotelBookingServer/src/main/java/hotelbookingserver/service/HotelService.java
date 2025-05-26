package hotelbookingserver.service;




import hotelbookingcommon.domain.Hotel;
import hotelbookingserver.datamanager.HotelData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.ArrayList;
import java.util.List;
import java.io.*;

public class HotelService {

        private static final String HOTEL_FILE = "C:\\Users\\PC\\Documents\\Proyecto1 progra 2\\hotels.dat";

        private HotelData hotelData;


        public HotelService() {
            try {
                hotelData = new HotelData(new File(HOTEL_FILE));
            } catch (IOException e) {
                throw new RuntimeException("Error al abrir archivo de hoteles", e);
            }
        }

        public List<Hotel> getAllHotels() {
            try {
                return hotelData.findAll();
            } catch (IOException e) {
                throw new RuntimeException("Error al obtener hoteles", e);
            }
        }

        public List<Hotel> addHotel(Hotel hotel) {
            try {
                hotelData.insert(hotel);
                return hotelData.findAll();
            } catch (IOException e) {
                throw new RuntimeException("Error al agregar hotel", e);
            }
        }

        public Hotel updateHotel(Hotel updatedHotel) {
            try {
                boolean actualizado = hotelData.update(updatedHotel);
                if (actualizado) {
                    return updatedHotel;
                }
                return null;
            } catch (IOException e) {
                throw new RuntimeException("Error al actualizar hotel", e);
            }
        }

        public boolean deleteHotel(int hotelNumber) {
            try {
                return hotelData.delete(hotelNumber);
            } catch (IOException e) {
                throw new RuntimeException("Error al eliminar hotel", e);
            }
        }

        public void close() {
            try {
                hotelData.close();
            } catch (IOException e) {
                // Log o manejar
            }
        }


}
