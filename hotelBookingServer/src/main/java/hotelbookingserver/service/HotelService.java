package hotelbookingserver.service;

import hotelbookingcommon.domain.Hotel;
import hotelbookingcommon.domain.Room; // Importar Room
import hotelbookingserver.datamanager.HotelData;
import hotelbookingserver.datamanager.RoomData; // Importar RoomData
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors; // Para facilitar la carga de habitaciones

public class HotelService {

    private static final String HOTEL_FILE = "C:\\Users\\XT\\Documents\\ProyectoProgra2\\hotels.dat";
    private static final String ROOM_FILE = "C:\\Users\\XT\\Documents\\ProyectoProgra2\\rooms.dat"; // Ruta del archivo de habitaciones

    private HotelData hotelData;
    private RoomData roomData; // Instancia de RoomData para cargar habitaciones

    public HotelService() {
        try {
            hotelData = new HotelData(new File(HOTEL_FILE));
            roomData = new RoomData(new File(ROOM_FILE)); // Inicializar RoomData
        } catch (IOException e) {
            throw new RuntimeException("Error al abrir archivos de hoteles o habitaciones", e);
        }
    }

    public List<Hotel> getAllHotels() {
        try {
            List<Hotel> hotels = hotelData.findAll();
            List<Room> allRooms = roomData.findAll(); // Cargar todas las habitaciones una vez

            // Iterar sobre cada hotel y asignar sus habitaciones
            for (Hotel hotel : hotels) {
                // Filtrar las habitaciones que pertenecen a este hotel
                List<Room> roomsForThisHotel = allRooms.stream()
                        .filter(room -> room.getHotelId() == hotel.getNumHotel())
                        .collect(Collectors.toList());
                hotel.setRooms(roomsForThisHotel); // Asignar la lista de habitaciones al hotel

                // Asegurar la relación bidireccional en memoria
                for (Room room : roomsForThisHotel) {
                    room.setHotel(hotel); // Establecer la referencia del hotel en la habitación
                }
            }
            return hotels;
        } catch (IOException e) {
            throw new RuntimeException("Error al obtener hoteles con habitaciones", e);
        }
    }

    // ... (Métodos addHotel, updateHotel, deleteHotel se mantienen igual) ...

    public List<Hotel> addHotel(Hotel hotel) {
        try {
            hotelData.insert(hotel);
            return getAllHotels(); // Se podría llamar a getAllHotels para reflejar los cambios con las habitaciones cargadas.
        } catch (IOException e) {
            throw new RuntimeException("Error al agregar hotel", e);
        }
    }

    public Hotel updateHotel(Hotel updatedHotel) {
        try {
            boolean actualizado = hotelData.update(updatedHotel);
            if (actualizado) {
                // Si la actualización es exitosa, se podría recargar el hotel con sus habitaciones
                // Esto es solo si necesitas que el objeto retornado ya tenga las habitaciones cargadas
                // Opcional: Podrías buscar el hotel actualizado y luego cargar sus habitaciones
                // return hotelData.findById(updatedHotel.getNumHotel()); // Necesitarías un findById público en HotelService
                return updatedHotel; // Retorna el objeto que ya tienes (sin habitaciones cargadas por defecto)
            }
            return null;
        } catch (IOException e) {
            throw new RuntimeException("Error al actualizar hotel", e);
        }
    }

    public boolean deleteHotel(int hotelNumber) {
        try {
            // Consideración: Al eliminar un hotel, ¿qué pasa con sus habitaciones?
            // Deberías eliminar también las habitaciones asociadas.
            List<Room> allRooms = roomData.findAll();
            for (Room room : allRooms) {
                if (room.getHotelId() == hotelNumber) {
                    roomData.delete(room.getRoomNumber()); // Eliminar la habitación
                }
            }
            return hotelData.delete(hotelNumber);
        } catch (IOException e) {
            throw new RuntimeException("Error al eliminar hotel y sus habitaciones", e);
        }
    }

    public void close() {
        try {
            hotelData.close();
            roomData.close(); // Cerrar también el RoomData
        } catch (IOException e) {
            // Log o manejar
        }
    }
}