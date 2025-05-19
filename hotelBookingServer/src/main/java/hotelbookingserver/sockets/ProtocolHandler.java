package hotelbookingserver.sockets;

import com.google.gson.Gson;
import hotelbookingcommon.domain.*;
import hotelbookingserver.service.HotelService;
import hotelbookingserver.service.RoomService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class ProtocolHandler {
    private static final Logger logger = LogManager.getLogger(ProtocolHandler.class);
    private final HotelService hotelService = new HotelService();
    private final RoomService roomService = new RoomService();
    private final Gson gson = new Gson();

    public Response handle(Request request) {
        logger.debug("Handling request: {}", request.getAction());
        try {
            switch (request.getAction()) {

                //  HOTELS
                case "getHotels": {
                    List<Hotel> hotels = hotelService.getAllHotels();
                    logger.debug("Retrieved {} hotels", hotels.size());
                    return new Response("200", "Hoteles cargados", hotels);
                }

                case "getHotel": {
                    try {
                        int hotelNumber = parseIntFromRequest(request.getData());
                        List<Hotel> hoteles = hotelService.getAllHotels();
                        for (Hotel h : hoteles) {
                            if (h.getNumHotel() == hotelNumber) {
                                return new Response("200", "Hotel encontrado", h);
                            }
                        }
                        return new Response("404", "Hotel no encontrado", null);
                    } catch (Exception e) {
                        logger.error("Error al consultar hotel", e);
                        return new Response("500", "Error interno al consultar hotel", null);
                    }
                }

                case "registerHotel": {
                    try {
                        Hotel newHotel = gson.fromJson(gson.toJson(request.getData()), Hotel.class);
                        List<Hotel> updatedHotels = hotelService.addHotel(newHotel);
                        logger.info("Hotel registrado: {}", newHotel);
                        return new Response("201", "Hotel registrado con éxito", updatedHotels);
                    } catch (Exception e) {
                        logger.error("Error al registrar hotel", e);
                        return new Response("500", "Error interno al registrar hotel", null);
                    }
                }

                //  ROOMS
                case "getRooms": {
                    List<Room> rooms = roomService.getAllRooms();
                    logger.debug("Habitaciones cargadas: {}", rooms.size());
                    return new Response("200", "Habitaciones cargadas", rooms);
                }

                case "getRoom": {
                    try {
                        int roomNumber = parseIntFromRequest(request.getData());
                        List<Room> rooms = roomService.getAllRooms();
                        for (Room r : rooms) {
                            if (r.getRoomNumber() == roomNumber) {
                                return new Response("200", "Habitación encontrada", r);
                            }
                        }
                        return new Response("404", "Habitación no encontrada", null);
                    } catch (Exception e) {
                        logger.error("Error al consultar habitación", e);
                        return new Response("500", "Error interno al consultar habitación", null);
                    }
                }

                case "registerRoom": {
                    try {
                        Room newRoom = gson.fromJson(gson.toJson(request.getData()), Room.class);
                        List<Room> updatedRooms = roomService.addRoom(newRoom);
                        logger.info("Habitación registrada: {}", newRoom);
                        return new Response("201", "Habitación registrada con éxito", updatedRooms);
                    } catch (Exception e) {
                        logger.error("Error al registrar habitación", e);
                        return new Response("500", "Error interno al registrar habitación", null);
                    }
                }

                default:
                    logger.warn("Acción no reconocida: {}", request.getAction());
                    return new Response("400", "Acción no reconocida: " + request.getAction(), null);
            }

        } catch (Exception e) {
            logger.error("Error handling request {}: {}", request.getAction(), e.getMessage());
            return new Response("500", "Internal Server Error: " + e.getMessage(), null);
        }
    }

    private int parseIntFromRequest(Object data) {
        if (data instanceof Double) {
            return ((Double) data).intValue();
        } else if (data instanceof Integer) {
            return (Integer) data;
        } else {
            throw new IllegalArgumentException("Tipo de dato inválido para ID numérico");
        }
    }
}
