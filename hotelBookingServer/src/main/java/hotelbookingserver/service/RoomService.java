package hotelbookingserver.service;

import hotelbookingcommon.domain.Room;
import hotelbookingserver.datamanager.RoomData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class RoomService {
    private static final Logger logger = LogManager.getLogger(RoomService.class);
    private static final String ROOM_FILE = "C:\\Users\\PC\\Documents\\Proyecto1 progra 2\\rooms.dat";

    private RoomData roomData;

    public RoomService() {
        try {
            roomData = new RoomData(new File(ROOM_FILE));
        } catch (IOException e) {
            logger.error("Error al abrir archivo de habitaciones: {}", e.getMessage());
            throw new RuntimeException("No se pudo inicializar RoomData", e);
        }
    }

    /**
     * Agrega una nueva habitación al archivo.
     */
    public boolean addRoom(Room room) {
        try {
            roomData.insert(room);
            logger.info("Habitación agregada: {}", room);
            return true;
        } catch (IOException e) {
            logger.error("Error al agregar habitación: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Devuelve todas las habitaciones.
     */
    public List<Room> getAllRooms() {
        try {
            return roomData.findAll();
        } catch (IOException e) {
            logger.error("Error al obtener habitaciones: {}", e.getMessage());
            throw new RuntimeException("Error al obtener habitaciones", e);
        }
    }

    /**
     * Actualiza una habitación existente.
     */
    public boolean updateRoom(Room room) {
        try {
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

    /**
     * Elimina una habitación por su número.
     */
    public boolean deleteRoom(int roomNumber) {
        try {
            boolean deleted = roomData.delete(roomNumber);
            if (deleted) {
                logger.info("Habitación eliminada con número: {}", roomNumber);
            } else {
                logger.warn("No se encontró habitación para eliminar: {}", roomNumber);
            }
            return deleted;
        } catch (IOException e) {
            logger.error("Error al eliminar habitación: {}", e.getMessage());
            return false;
        }
    }
}
