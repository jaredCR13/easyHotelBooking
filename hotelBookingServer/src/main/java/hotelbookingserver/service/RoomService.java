package hotelbookingserver.service;

import hotelbookingcommon.domain.Hotel; // Necesitarás Hotel para buscar el hotel asociado
import hotelbookingcommon.domain.Room;
import hotelbookingserver.datamanager.RoomData;
import hotelbookingserver.datamanager.HotelData; // Importar HotelData para buscar hoteles
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import java.util.concurrent.ConcurrentHashMap;

public class RoomService {
    private static final Logger logger = LogManager.getLogger(RoomService.class);
    private static final String ROOM_FILE = "C:\\Users\\Lexis\\Documents\\Progra_II\\PROYECTO\\BinaryFilesLocal\\HotelRoomFiles\\rooms.dat";
    private static final String HOTEL_FILE = "C:\\Users\\Lexis\\Documents\\Progra_II\\PROYECTO\\BinaryFilesLocal\\HotelFiles\\hotels.dat"; // Ruta del archivo de hoteles

    private RoomData roomData;
    private HotelData hotelData; // Instancia de HotelData

    
    public RoomService() {
        try {
            roomData = new RoomData(new File(ROOM_FILE));
            hotelData = new HotelData(new File(HOTEL_FILE)); // Inicializar HotelData
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

            // --- INICIO DE LA VERIFICACIÓN DE DUPLICADOS ---
            // Busca una habitación con el mismo roomNumber
            Room existingRoom = roomData.findById(room.getRoomNumber());
            if (existingRoom != null) {
                logger.warn("Intento de agregar habitación duplicada. El número de habitación {} ya existe.", room.getRoomNumber());
                return false; // Retorna false si ya existe una habitación con ese número
            }

            //HACE LA INSERCION
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
            // Cargar todos los hoteles una vez para optimizar las búsquedas
            List<Hotel> allHotels = hotelData.findAll();

            for (Room room : rooms) {
                // Buscar el hotel asociado por ID
                allHotels.stream()
                        .filter(hotel -> hotel.getNumHotel() == room.getHotelId())
                        .findFirst()
                        .ifPresent(hotel -> {
                            room.setHotel(hotel);
                            hotel.addRoom(room);
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
            Room roomToDelete = roomData.findById(roomNumber);
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

    public Room getRoomById(int roomNumber) {
        try {
            Room foundRoom = roomData.findById(roomNumber);

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

    public void close() {
        try {
            roomData.close();
            hotelData.close();
        } catch (IOException e) {

        }
    }
}