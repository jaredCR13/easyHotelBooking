package hotelbookingserver.service;


import hotelbookingcommon.domain.Room;
import hotelbookingcommon.domain.RoomStatus;
import hotelbookingcommon.domain.RoomStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RoomService {
    private final List<Room> rooms = new ArrayList<>();
    private static final Logger logger = LogManager.getLogger(RoomService.class);

    private static final String ROOM_FILE = "C:\\Users\\XT\\Documents\\ProyectoProgra2\\rooms.dat";

    public RoomService() {
        // Los datos se cargan desde archivo, no se inicializan aquí.
    }

    public List<Room> addRoom(Room room) {
        List<Room> currentRooms = loadRooms();
        currentRooms.add(room);
        saveRooms(currentRooms);
        return currentRooms;
    }

    public List<Room> getAllRooms() {
        logger.info("Obteniendo todas las habitaciones");
        List<Room> currentRooms = loadRooms();
        return currentRooms != null ? currentRooms : new ArrayList<>();
    }

    public void saveRooms(List<Room> roomList) {
        logger.info("Guardando habitaciones en archivo binario: {}", ROOM_FILE);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ROOM_FILE))) {
            oos.writeObject(roomList);
        } catch (IOException e) {
            logger.error("Error al guardar habitaciones en {}: {}", ROOM_FILE, e.getMessage());
            throw new RuntimeException("Error al guardar habitaciones", e);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Room> loadRooms() {
        File file = new File(ROOM_FILE);
        if (!file.exists()) {
            logger.warn("El archivo de habitaciones {} no existe. Se devuelve una lista vacía.", ROOM_FILE);
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            List<Room> loaded = (List<Room>) ois.readObject();
            logger.info("Habitaciones cargadas desde {}: {}", ROOM_FILE, loaded.size());
            return loaded;
        } catch (IOException | ClassNotFoundException e) {
            logger.error("Error al cargar habitaciones desde {}: {}", ROOM_FILE, e.getMessage());
            return new ArrayList<>();
        }
    }


    /*// Prueba independiente
    public static void main(String[] args) {
        RoomService roomService = new RoomService();
        List<Room> rooms = new ArrayList<>();
        rooms.add(new Room(101, 900, "Bonita", "Disponible","estandar",));
        rooms.add(new Room(202, 300, "Bien bonita", "Ocupado"));
        roomService.saveRooms(rooms);

        List<Room> loaded = roomService.getAllRooms();
        System.out.println("Habitaciones cargadas: " + loaded);
    }*/

    public static void main(String[] args) {
        RoomService srv = new RoomService();

        srv.addRoom(new Room(101, 900, "Bonita", RoomStatus.AVAILABLE, RoomStyle.DELUXE, Collections.emptyList()));
        System.out.println(srv.getAllRooms());
    }
}
