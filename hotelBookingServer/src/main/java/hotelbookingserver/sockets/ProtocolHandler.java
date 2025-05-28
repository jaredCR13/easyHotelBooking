package hotelbookingserver.sockets;

import com.google.gson.Gson;
import hotelbookingcommon.domain.*;
import hotelbookingserver.service.FrontDeskClerkService;
import hotelbookingserver.service.HotelService;
import hotelbookingserver.service.RoomService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.UUID;
import java.util.List;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;


public class ProtocolHandler {
    private static final Logger logger = LogManager.getLogger(ProtocolHandler.class);
    private final HotelService hotelService = new HotelService();
    private final RoomService roomService = new RoomService();
    private final FrontDeskClerkService frontDeskClerkService = new FrontDeskClerkService();

    private final Gson gson = new Gson();

    // Esta es la ruta base donde el servidor guardará físicamente las imágenes.
    // Ajusta esta ruta según la estructura de tu proyecto en el servidor.
    private static final String SERVER_FILE_STORAGE_ROOT = "C:\\Users\\XT\\Documents\\ProyectoProgra2";
    // El prefijo de la ruta relativa que se guardará en la DB y que el cliente usará para solicitar
    private static final String ROOM_IMAGES_RELATIVE_PATH_PREFIX = "data\\images\\rooms";
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

                // =================== IMAGENES DE HABITACIONES =========================
                case "uploadRoomImage": {
                    try {
                        // 1. Deserializar el DTO que contiene los bytes de la imagen del cliente
                        ImageUploadDTO uploadData = gson.fromJson(gson.toJson(request.getData()), ImageUploadDTO.class);
                        int roomNumber = uploadData.getRoomNumber();
                        byte[] imageData = uploadData.getImageData();
                        String originalFileName = uploadData.getFileName();

                        // 2. Extraer la extensión del archivo original
                        String fileExtension = "";
                        int dotIndex = originalFileName.lastIndexOf('.');
                        if (dotIndex > 0 && dotIndex < originalFileName.length() - 1) {
                            fileExtension = originalFileName.substring(dotIndex + 1);
                        }

                        // 3. Generar un nombre de archivo único para evitar colisiones
                        String uniqueFileName = UUID.randomUUID().toString() + "." + fileExtension;
                        // Construye la ruta completa donde se guardará la imagen en el disco del servidor
                        Path serverImageDirPath = Paths.get(SERVER_FILE_STORAGE_ROOT, ROOM_IMAGES_RELATIVE_PATH_PREFIX);
                        Path serverFullImagePath = serverImageDirPath.resolve(uniqueFileName);

                        // 4. Crear los directorios si no existen
                        Files.createDirectories(serverImageDirPath);

                        // 5. Guardar los bytes de la imagen en el archivo físico del servidor
                        Files.write(serverFullImagePath, imageData);
                        logger.info("Imagen guardada en el servidor: {}", serverFullImagePath.toAbsolutePath());

                        // 6. Actualizar la lista de rutas de la habitación en la base de datos
                        Room room = roomService.getRoomById(roomNumber); // Obtener la habitación de la DB
                        if (room != null) {
                            if (room.getImagesPaths() == null) {
                                room.setImagesPaths(new ArrayList<>());
                            }
                            // Añade la ruta RELATIVA que el cliente usará para pedir la imagen
                            room.getImagesPaths().add(ROOM_IMAGES_RELATIVE_PATH_PREFIX + uniqueFileName);
                            roomService.updateRoom(room); // Actualiza la habitación en la DB (vía RoomData)
                            logger.info("Ruta de imagen '{}' añadida a la habitación {}", room.getImagesPaths().get(room.getImagesPaths().size() -1), roomNumber);

                            // Devuelve la habitación actualizada para que el cliente tenga los nuevos paths
                            return new Response("200", "Imagen subida y path guardado.", room);
                        } else {
                            logger.warn("Habitación {} no encontrada para asociar la imagen. Eliminando archivo: {}", roomNumber, serverFullImagePath.toAbsolutePath());
                            Files.deleteIfExists(serverFullImagePath); // Elimina el archivo si la habitación no existe
                            return new Response("404", "Habitación no encontrada para asociar la imagen.", null);
                        }
                    } catch (IOException e) {
                        logger.error("Error de E/S al subir imagen al servidor: {}", e.getMessage(), e);
                        return new Response("500", "Error de E/S al procesar la imagen en el servidor.", null);
                    } catch (Exception e) {
                        logger.error("Error inesperado al manejar 'uploadRoomImage': {}", e.getMessage(), e);
                        return new Response("500", "Error interno del servidor al subir imagen.", null);
                    }
                }

                case "downloadRoomImage": {
                    try {
                        // 1. Obtener la ruta relativa de la imagen solicitada por el cliente
                        String imageRelativePath = gson.fromJson(gson.toJson(request.getData()), String.class);

                        // 2. Construir la ruta física completa en el disco del servidor
                        Path serverFullImagePath = Paths.get(SERVER_FILE_STORAGE_ROOT).resolve(imageRelativePath);

                        logger.info("Cliente solicita descarga de imagen: {}. Buscando en: {}", imageRelativePath, serverFullImagePath.toAbsolutePath());

                        // 3. Leer el archivo de imagen a bytes y enviarlos al cliente
                        if (Files.exists(serverFullImagePath)) {
                            byte[] imageData = Files.readAllBytes(serverFullImagePath); // <- ¡Lee el archivo a bytes!
                            logger.info("Imagen de {} bytes leída y lista para enviar al cliente.", imageData.length);
                            return new Response("200", "Imagen enviada.", imageData); // <- ¡Envía los bytes!
                        } else {
                            logger.warn("Imagen no encontrada en el servidor: {}", serverFullImagePath.toAbsolutePath());
                            return new Response("404", "Imagen no encontrada en el servidor.", null);
                        }
                    } catch (IOException e) {
                        logger.error("Error de E/S al leer imagen del servidor para enviar: {}", e.getMessage(), e);
                        return new Response("500", "Error de E/S al enviar imagen desde el servidor.", null);
                    } catch (Exception e) {
                        logger.error("Error inesperado al manejar 'downloadRoomImage': {}", e.getMessage(), e);
                        return new Response("500", "Error interno del servidor al descargar imagen.", null);
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