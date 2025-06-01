package hotelbookingserver.service;

import hotelbookingcommon.domain.Hotel;
import hotelbookingcommon.domain.Room;
import hotelbookingserver.datamanager.RoomData;
import hotelbookingserver.datamanager.HotelData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class RoomService {
    private static final Logger logger = LogManager.getLogger(RoomService.class);
    private static final String ROOM_FILE = "C:\\Users\\PC\\Documents\\UCR\\Progra_II\\PROYECTO\\BinaryFilesLocal\\HotelRoomFiles\\rooms.dat";
    private static final String HOTEL_FILE = "C:\\Users\\PC\\Documents\\UCR\\Progra_II\\PROYECTO\\BinaryFilesLocal\\HotelFiles\\hotels.dat";

    private RoomData roomData;
    private HotelData hotelData;

    public RoomService() {
        try {
            roomData = new RoomData(new File(ROOM_FILE));
            hotelData = new HotelData(new File(HOTEL_FILE));
        } catch (IOException e) {
            logger.error("Error al abrir archivo de habitaciones o hoteles: {}", e.getMessage());
            throw new RuntimeException("No se pudo inicializar RoomData o HotelData", e);
        }
    }

    public boolean addRoom(Room room) {
        try {
            if (room.getHotel() != null) {
                room.setHotelId(room.getHotel().getNumHotel());
            } else if (room.getHotelId() == -1) {
                logger.error("Error al agregar habitación: La habitación no tiene un hotel asociado (ni objeto ni ID).");
                return false;
            }

            Hotel associatedHotel = hotelData.findById(room.getHotelId());
            if (associatedHotel == null) {
                logger.error("Error al agregar habitación: El hotel con ID {} no existe.", room.getHotelId());
                return false;
            }

            // --- VERIFICACIÓN DUPLICADOS ---
            // Obtener todas las habitaciones para el hotel específico y verificar si  existe roomNumber
            List<Room> roomsInThisHotel = roomData.findAll().stream()
                    .filter(r -> r.getHotelId() == room.getHotelId())
                    .collect(Collectors.toList());

            boolean roomExistsInThisHotel = roomsInThisHotel.stream()
                    .anyMatch(r -> r.getRoomNumber() == room.getRoomNumber());

            if (roomExistsInThisHotel) {
                logger.warn("Intento de agregar habitación duplicada. El número de habitación {} ya existe en el hotel {}.", room.getRoomNumber(), room.getHotelId());
                return false; // Retorna false si ya existe una habitación con ese número en este hotel
            }

            roomData.insert(room);

            if (associatedHotel != null) {
                associatedHotel.addRoom(room);
            }

            logger.info("Habitación agregada: {}", room);
            return true;
        } catch (IOException e) {
            logger.error("Error al agregar habitación: {}", e.getMessage());
            return false;
        }
    }

    public List<Room> getAllRooms() {
        try {
            List<Room> rooms = roomData.findAll();
            List<Hotel> allHotels = hotelData.findAll();

            for (Room room : rooms) {
                allHotels.stream()
                        .filter(hotel -> hotel.getNumHotel() == room.getHotelId())
                        .findFirst()
                        .ifPresent(hotel -> {
                            room.setHotel(hotel);
                            // Solo añadir la habitación al hotel si aún no está en su lista
                            if (!hotel.getRooms().contains(room)) {
                                hotel.addRoom(room);
                            }
                        });
            }
            return rooms;
        } catch (IOException e) {
            logger.error("Error al obtener habitaciones: {}", e.getMessage());
            throw new RuntimeException("Error al obtener habitaciones", e);
        }
    }

    public boolean updateRoom(Room room) {
        try {
            if (room.getHotel() != null) {
                room.setHotelId(room.getHotel().getNumHotel());
            }

            if (room.getHotelId() != -1) {
                Hotel associatedHotel = hotelData.findById(room.getHotelId());
                if (associatedHotel == null) {
                    logger.error("Error al actualizar habitación: El hotel con ID {} no existe.", room.getHotelId());
                    return false;
                }
            }

            boolean updated = roomData.update(room);
            if (updated) {
                logger.info("Habitación actualizada: {}", room);
            } else {
                logger.warn("No se encontró habitación con número: {}", room.getRoomNumber());
            }
            return updated;
        } catch (IOException e) {
            logger.error("Error al actualizar habitación: {}", e.getMessage());
            return false;
        }
    }

    public boolean deleteRoom(int roomNumber) {
        try {
            Room roomToDelete = roomData.findById(roomNumber); // Find by ID, not just roomNumber
            boolean deleted = roomData.delete(roomNumber);

            if (deleted) {
                logger.info("Habitación eliminada con número: {}", roomNumber);
                if (roomToDelete != null && roomToDelete.getHotelId() != -1) {
                    Hotel associatedHotel = hotelData.findById(roomToDelete.getHotelId());
                    if (associatedHotel != null) {
                        associatedHotel.removeRoom(roomToDelete);
                    }
                }
            } else {
                logger.warn("No se encontró habitación para eliminar: {}", roomNumber);
            }
            return deleted;
        } catch (IOException e) {
            logger.error("Error al eliminar habitación: {}", e.getMessage());
            return false;
        }
    }

    public Room getRoomById(int roomNumber) { // This method still gets a room by its *unique* room number
        try {
            Room foundRoom = roomData.findById(roomNumber); // Assuming findById still finds a unique room.

            if (foundRoom != null) {
                if (foundRoom.getHotelId() != -1) {
                    Hotel associatedHotel = hotelData.findById(foundRoom.getHotelId());
                    if (associatedHotel != null) {
                        foundRoom.setHotel(associatedHotel);
                    }
                }

                logger.info("Habitación {} cargada con {} imágenes desde RoomData.", foundRoom.getRoomNumber(), foundRoom.getImagesPaths().size());
                foundRoom.getImagesPaths().forEach(path -> logger.info("  - Ruta de imagen: {}", path));

            } else {
                logger.warn("No se encontró la habitación con número: {}", roomNumber);
            }

            return foundRoom;

        } catch (IOException e) {
            logger.error("Error al obtener habitación por ID: {}", e.getMessage());
            throw new RuntimeException("Error al obtener habitación por ID", e);
        }
    }

    /**
     * Retrieves a room by its room number and the hotel it belongs to.
     * This method is crucial for ensuring uniqueness within a hotel.
     * @param roomNumber The number of the room.
     * @param hotelId The ID of the hotel the room belongs to.
     * @return The Room object if found, otherwise null.
     */
    public Room getRoomByHotelAndRoomNumber(int roomNumber, int hotelId) {
        try {
            return roomData.findAll().stream()
                    .filter(room -> room.getRoomNumber() == roomNumber && room.getHotelId() == hotelId)
                    .findFirst()
                    .orElse(null);
        } catch (IOException e) {
            logger.error("Error al obtener habitación por número y hotel: {}", e.getMessage());
            throw new RuntimeException("Error al obtener habitación por número y hotel", e);
        }
    }


    public void close() {
        try {
            roomData.close();
            hotelData.close();
        } catch (IOException e) {
            logger.error("Error al cerrar archivos de habitaciones o hoteles: {}", e.getMessage());
        }
    }
}