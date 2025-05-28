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
    private static final String ROOM_FILE = "C:\\Users\\XT\\Documents\\ProyectoProgra2\\rooms.dat";
    private static final String HOTEL_FILE = "C:\\Users\\XT\\Documents\\ProyectoProgra2\\hotels.dat"; // Ruta del archivo de hoteles

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

    /**
     * Agrega una nueva habitación al archivo.
     * Antes de agregar, verifica si el hotel al que se asocia existe.
     * Se asegura que la habitación tenga el ID del hotel establecido.
     */
    public boolean addRoom(Room room) {
        try {
            if (room.getHotel() != null) {
                room.setHotelId(room.getHotel().getNumHotel());
            } else if (room.getHotelId() == -1) { // Si no tiene objeto Hotel ni ID, es un error o necesita ser establecido
                logger.error("Error al agregar habitación: La habitación no tiene un hotel asociado (ni objeto ni ID).");
                return false;
            }

            // Opcional: Verificar que el hotelId realmente exista en el sistema
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
            // Si la habitación se agrega exitosamente, también deberíamos añadirla al objeto Hotel en memoria
            // Esto no afecta el archivo, solo la coherencia de los objetos si se están usando en esta transacción
            if (associatedHotel != null) {
                associatedHotel.addRoom(room); // Asegura que la lista de rooms del hotel se actualice en memoria
            }

            logger.info("Habitación agregada: {}", room);
            return true;
        } catch (IOException e) {
            logger.error("Error al agregar habitación: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Devuelve todas las habitaciones, cargando también sus objetos Hotel asociados.
     */
    public List<Room> getAllRooms() {
        try {
            List<Room> rooms = roomData.findAll();
            // Cargar todos los hoteles una vez para optimizar las búsquedas
            List<Hotel> allHotels = hotelData.findAll();

            for (Room room : rooms) {
                // Buscar el hotel asociado por ID
                // Usamos un stream para buscar en la lista allHotels cargada
                allHotels.stream()
                        .filter(hotel -> hotel.getNumHotel() == room.getHotelId())
                        .findFirst()
                        .ifPresent(hotel -> {
                            room.setHotel(hotel); // Establecer la referencia al objeto Hotel
                            hotel.addRoom(room); // Asegurar bidireccionalidad (si el hotel aún no la tiene)
                        });
            }
            return rooms;
        } catch (IOException e) {
            logger.error("Error al obtener habitaciones: {}", e.getMessage());
            throw new RuntimeException("Error al obtener habitaciones", e);
        }
    }

    /**
     * Actualiza una habitación existente, asegurando que su hotelId sea consistente.
     */
    public boolean updateRoom(Room room) {
        try {
            // Asegúrate de que el hotelId esté establecido antes de la actualización
            if (room.getHotel() != null) {
                room.setHotelId(room.getHotel().getNumHotel());
            }

            // Opcional: Verificar que el hotelId realmente exista si se ha cambiado
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

    /**
     * Elimina una habitación por su número.
     * Consideración: Si se elimina una habitación, también se debería eliminar de la lista de habitaciones de su hotel asociado en memoria si el hotel está cargado.
     */
    public boolean deleteRoom(int roomNumber) {
        try {
            // Opcional: Obtener la habitación antes de eliminarla para saber a qué hotel pertenecía
            Room roomToDelete = roomData.findById(roomNumber);
            boolean deleted = roomData.delete(roomNumber);

            if (deleted) {
                logger.info("Habitación eliminada con número: {}", roomNumber);
                // Si la habitación se eliminó y conocíamos su hotel, eliminarla también del objeto Hotel en memoria
                if (roomToDelete != null && roomToDelete.getHotelId() != -1) {
                    Hotel associatedHotel = hotelData.findById(roomToDelete.getHotelId());
                    if (associatedHotel != null) {
                        associatedHotel.removeRoom(roomToDelete); // Actualiza el objeto Hotel en memoria
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
            Room foundRoom = roomData.findById(roomNumber); // <-- Aquí se carga la habitación con sus imágenes desde rooms.dat

            if (foundRoom != null) {
                // Cargar el Hotel asociado (esto sí es correcto)
                if (foundRoom.getHotelId() != -1) {
                    Hotel associatedHotel = hotelData.findById(foundRoom.getHotelId());
                    if (associatedHotel != null) {
                        foundRoom.setHotel(associatedHotel);
                        // Opcional: Asegurar bidireccionalidad si es necesario para el Hotel en memoria
                        // associatedHotel.addRoom(foundRoom);
                    }
                }
                //foundRoom.getImagesPaths() contiene las rutas

                logger.info("Habitación {} cargada con {} imágenes desde RoomData.", foundRoom.getRoomNumber(), foundRoom.getImagesPaths().size());
                // Opcional: Loguea las rutas para depurar
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
            hotelData.close(); // Cerrar también el HotelData
        } catch (IOException e) {
            // Log o manejar
        }
    }
}