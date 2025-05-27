package hotelbookingserver.sockets;

import com.google.gson.Gson;
import hotelbookingcommon.domain.*;
import hotelbookingserver.service.FrontDeskClerkService;
import hotelbookingserver.service.HotelService;
import hotelbookingserver.service.RoomService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.stream.Collectors; // Necesario para filtrar listas

public class ProtocolHandler {
    private static final Logger logger = LogManager.getLogger(ProtocolHandler.class);
    private final HotelService hotelService = new HotelService();
    private final RoomService roomService = new RoomService();
    private final FrontDeskClerkService frontDeskClerkService = new FrontDeskClerkService();

    private final Gson gson = new Gson();

    public Response handle(Request request) {
        logger.debug("Handling request: {}", request.getAction());
        try {
            switch (request.getAction()) {

                // =================== HOTEL =========================
                case "getHotels": {
                    // hotelService.getAllHotels() ya carga las habitaciones asociadas
                    List<Hotel> hotels = hotelService.getAllHotels();
                    logger.debug("Retrieved {} hotels", hotels.size());
                    return new Response("200", "Hoteles cargados", hotels);
                }

                case "getHotel": {
                    try {
                        int hotelNumber = parseIntFromRequest(request.getData());
                        // hotelService.getAllHotels() carga las asociaciones
                        // Buscamos directamente en la lista completa
                        List<Hotel> hoteles = hotelService.getAllHotels();
                        Hotel foundHotel = hoteles.stream()
                                .filter(h -> h.getNumHotel() == hotelNumber)
                                .findFirst()
                                .orElse(null);

                        if (foundHotel != null) {
                            return new Response("200", "Hotel encontrado", foundHotel);
                        } else {
                            return new Response("404", "Hotel no encontrado", null);
                        }
                    } catch (Exception e) {
                        logger.error("Error al consultar hotel", e);
                        return new Response("500", "Error interno al consultar hotel", null);
                    }
                }

                case "registerHotel": {
                    try {
                        Hotel newHotel = gson.fromJson(gson.toJson(request.getData()), Hotel.class);
                        // Cuando se registra un hotel, su lista de habitaciones estará vacía
                        // Las habitaciones se annaden a través de registerRoom
                        List<Hotel> updatedHotels = hotelService.addHotel(newHotel);
                        logger.info("Hotel registrado: {}", newHotel);
                        return new Response("201", "Hotel registrado con éxito", updatedHotels);
                    } catch (Exception e) {
                        logger.error("Error al registrar hotel", e);
                        return new Response("500", "Error interno al registrar hotel", null);
                    }
                }

                case "updateHotel": {
                    try {
                        Hotel updated = gson.fromJson(gson.toJson(request.getData()), Hotel.class);
                        //hotelService.updateHotel() solo actualiza los campos básicos del Hotel
                        //'updated' contendrá la lista de habitaciones que venía del cliente,
                        Hotel result = hotelService.updateHotel(updated);
                        if (result != null) {
                            //Devuelve el objeto que se actualizo
                            //se obtiene la ista completa
                            List<Hotel> allHotels = hotelService.getAllHotels();
                            Hotel fullUpdatedHotel = allHotels.stream()
                                    .filter(h -> h.getNumHotel() == result.getNumHotel())
                                    .findFirst()
                                    .orElse(null);
                            return new Response("200", "Hotel actualizado", fullUpdatedHotel);
                        } else {
                            return new Response("404", "Hotel no encontrado", null);
                        }
                    } catch (Exception e) {
                        logger.error("Error al actualizar hotel", e);
                        return new Response("500", "Error interno al actualizar hotel", null);
                    }
                }

                case "deleteHotel": {
                    try {
                        int number = parseIntFromRequest(request.getData());
                        // hotelService.deleteHotel() elimina las habitaciones asociadas
                        boolean deleted = hotelService.deleteHotel(number);
                        if (deleted) {
                            return new Response("200", "Hotel eliminado", null);
                        } else {
                            return new Response("404", "Hotel no encontrado", null);
                        }
                    } catch (Exception e) {
                        logger.error("Error al eliminar hotel", e);
                        return new Response("500", "Error interno al eliminar hotel", null);
                    }
                }

                // =================== ROOMS =========================
                case "getRooms": {
                    // roomService.getAllRooms() carga el objeto Hotel asociado
                    List<Room> rooms = roomService.getAllRooms();
                    logger.debug("Habitaciones cargadas: {}", rooms.size());
                    return new Response("200", "Habitaciones cargadas", rooms);
                }

                case "getRoom": {
                    try {
                        int roomNumber = parseIntFromRequest(request.getData());
                        //Buscar la habitación directamente por ID,
                        // y roomService la cargue con su Hotel asociado
                        Room foundRoom = roomService.getRoomById(roomNumber); // Asumimos un nuevo método en RoomService

                        if (foundRoom != null) {
                            return new Response("200", "Habitación encontrada", foundRoom);
                        } else {
                            return new Response("404", "Habitación no encontrada", null);
                        }
                    } catch (Exception e) {
                        logger.error("Error al consultar habitación", e);
                        return new Response("500", "Error interno al consultar habitación", null);
                    }
                }

                case "registerRoom": {
                    try {
                        // CONVIERTE DE JSON a ROOM
                        Room newRoom = gson.fromJson(gson.toJson(request.getData()), Room.class);
                        boolean added = roomService.addRoom(newRoom);
                        if (added) {
                            Room addedRoomWithHotel = roomService.getRoomById(newRoom.getRoomNumber());
                            return new Response("201", "Habitación registrada con éxito", addedRoomWithHotel);
                        } else {
                            Room existingRoomCheck = roomService.getRoomById(newRoom.getRoomNumber());
                            if (existingRoomCheck != null) {
                                // Es un duplicado. Usamos el código de estado 409 Conflict.
                                logger.warn("Intento de registrar habitación duplicada: {}", newRoom.getRoomNumber());
                                return new Response("409", "El número de habitación " + newRoom.getRoomNumber() + " ya existe.", null);
                            } else {
                                logger.error("Error desconocido al registrar la habitación: {}", newRoom.getRoomNumber());
                                return new Response("500", "No se pudo registrar la habitación (ver logs para detalles)", null);
                            }
                        }

                    } catch (Exception e) {
                        logger.error("Error al registrar habitación", e);
                        return new Response("500", "Error interno al registrar habitación", null);
                    }
                }
                case "updateRoom": {
                    try {
                        Room updatedRoom = gson.fromJson(gson.toJson(request.getData()), Room.class);
                        boolean updated = roomService.updateRoom(updatedRoom);
                        if (updated) {
                            Room updatedRoomWithHotel = roomService.getRoomById(updatedRoom.getRoomNumber());
                            return new Response("200", "Habitación actualizada con éxito", updatedRoomWithHotel);
                        } else {
                            return new Response("404", "Habitación no encontrada", null);
                        }
                    } catch (Exception e) {
                        logger.error("Error al actualizar habitación", e);
                        return new Response("500", "Error interno al actualizar habitación", null);
                    }
                }

                case "deleteRoom": {
                    try {
                        int roomNumber = parseIntFromRequest(request.getData());
                        boolean deleted = roomService.deleteRoom(roomNumber);
                        if (deleted) {
                            return new Response("200", "Habitación eliminada con éxito", null);
                        } else {
                            return new Response("404", "Habitación no encontrada", null);
                        }
                    } catch (Exception e) {
                        logger.error("Error al eliminar habitación", e);
                        return new Response("500", "Error interno al eliminar habitación", null);
                    }
                }


                // =================== FRONT DESK CLERK =========================
                case "registerFrontDeskClerk": {
                    try {
                        FrontDeskClerk frontDeskClerk = gson.fromJson(gson.toJson(request.getData()), FrontDeskClerk.class);
                        List<FrontDeskClerk> updatedFrontDeskClerkList = frontDeskClerkService.addFrontDesk(frontDeskClerk);
                        logger.info("Recepcionista registrado: {}", frontDeskClerk);
                        return new Response("201", "Recepcionista registrado con éxito", updatedFrontDeskClerkList);
                    } catch (Exception e) {
                        logger.error("Error al registrar recepcionista", e);
                        return new Response("500", "Error interno al registrar recepcionista", null);
                    }
                }

                case "getFrontDeskClerk": {
                    try {
                        String employeeId = String.valueOf(parseIntFromRequest(request.getData()));
                        FrontDeskClerk found = frontDeskClerkService.findByEmployeeId(employeeId);
                        if (found != null) {
                            return new Response("200", "Recepcionista encontrado", found);
                        } else {
                            return new Response("404", "Recepcionista no encontrado", null);
                        }
                    } catch (Exception e) {
                        logger.error("Error al consultar recepcionista", e);
                        return new Response("500", "Error interno al consultar recepcionista", null);
                    }
                }

                default:
                    logger.warn("Acción no reconocida: {}", request.getAction());
                    return new Response("400", "Acción no reconocida: " + request.getAction(), null);
            }

        } catch (Exception e) {
            logger.error("Error handling request {}: {}", request.getAction(), e.getMessage());
            return new Response("500", "Internal Server Error: " + e.getMessage(), null);
        } finally {
            // hotelService.close();
            // roomService.close();
            // frontDeskClerkService.close();
        }
    }

    private int parseIntFromRequest(Object data) {
        if (data instanceof Double) {
            return ((Double) data).intValue();
        } else if (data instanceof Integer) {
            return (Integer) data;
        } else {
            try {
                return Integer.parseInt(data.toString());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Tipo de dato inválido para ID numérico: " + data, e);
            }
        }
    }
}