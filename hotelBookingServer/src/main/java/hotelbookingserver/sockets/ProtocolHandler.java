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
                    // hotelService.getAllHotels() ya carga las habitaciones asociadas.
                    List<Hotel> hotels = hotelService.getAllHotels();
                    logger.debug("Retrieved {} hotels", hotels.size());
                    return new Response("200", "Hoteles cargados", hotels);
                }

                case "getHotel": {
                    try {
                        int hotelNumber = parseIntFromRequest(request.getData());
                        // hotelService.getAllHotels() ya carga las asociaciones.
                        // Buscamos directamente en la lista completa.
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
                        // Cuando se registra un hotel, su lista de habitaciones estará vacía inicialmente.
                        // Las habitaciones se añadirán a través de registerRoom.
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
                        // Asegúrate de que, si el cliente envía un Hotel para actualizar,
                        // la lista de habitaciones de ese Hotel en el objeto 'updated' no interfiera.
                        // hotelService.updateHotel() solo actualiza los campos básicos del Hotel,
                        // la gestión de las habitaciones es responsabilidad de roomService.
                        // El objeto 'updated' aquí contendrá la lista de habitaciones que venía del cliente,
                        // pero `hotelService.updateHotel` no la persiste.
                        Hotel result = hotelService.updateHotel(updated);
                        if (result != null) {
                            // Si necesitas devolver el Hotel actualizado con sus habitaciones,
                            // podrías buscarlo de nuevo en el servicio.
                            // Sin embargo, `hotelService.updateHotel` devuelve el objeto que se actualizó.
                            // Para consistencia con la carga de habitaciones, obtén la lista completa.
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
                        // hotelService.deleteHotel() ya se encarga de eliminar las habitaciones asociadas.
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
                    // roomService.getAllRooms() ya carga el objeto Hotel asociado a cada habitación.
                    List<Room> rooms = roomService.getAllRooms();
                    logger.debug("Habitaciones cargadas: {}", rooms.size());
                    return new Response("200", "Habitaciones cargadas", rooms);
                }

                case "getRoom": {
                    try {
                        int roomNumber = parseIntFromRequest(request.getData());
                        // La manera más eficiente es buscar la habitación directamente por ID,
                        // y que el servicio de habitaciones la cargue con su Hotel asociado.
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
                        // El cliente debería enviar un objeto Room que ya tiene el hotelId establecido
                        // Opcionalmente, podría enviar el objeto Hotel completo si se deserializa correctamente.
                        Room newRoom = gson.fromJson(gson.toJson(request.getData()), Room.class);

                        // Es crucial que el `newRoom` tenga un `hotelId` válido.
                        // Si el cliente envía solo el `roomNumber` y `hotelId`, o un `Hotel` parcial,
                        // Gson lo manejará. La validación real se hace en `roomService.addRoom`.
                        boolean added = roomService.addRoom(newRoom);
                        if (added) {
                            // Para devolver la habitación completa con su Hotel asociado,
                            // podrías obtenerla de nuevo del servicio o asegurarse que addRoom la retorne completa
                            // Por ahora, devolvemos `newRoom` que no tendrá el objeto Hotel en su interior
                            // a menos que el cliente lo haya enviado y Gson lo haya deserializado.
                            // Lo más seguro es recuperarla de nuevo.
                            Room addedRoomWithHotel = roomService.getRoomById(newRoom.getRoomNumber());
                            return new Response("201", "Habitación registrada con éxito", addedRoomWithHotel);
                        } else {
                            return new Response("500", "No se pudo registrar la habitación (ver logs para detalles)", null);
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
                            // Para devolver la habitación actualizada con su Hotel asociado.
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
                        // roomService.deleteRoom() ya maneja la lógica de eliminar la habitación
                        // y actualizar la lista del hotel en memoria.
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
                        // Asumo que findByEmployeeId ya maneja la conversión a String
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
            // Considera si quieres cerrar los servicios aquí o en otro lugar más apropiado
            // (ej. al apagar el servidor). Cerrarlos en cada request podría ser ineficiente.
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
            // Asegúrate de que el cliente siempre envíe un número para IDs.
            // O podrías intentar parsear un String si el 'data' llega como String.
            try {
                return Integer.parseInt(data.toString());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Tipo de dato inválido para ID numérico: " + data, e);
            }
        }
    }
}