package hotelbookingserver.sockets;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import hotelbookingcommon.domain.*; // Asegúrate de que ImageUploadDTO es accesible aquí
import hotelbookingserver.datamanager.GuestData; // Importa GuestData
import hotelbookingserver.service.FrontDeskClerkService;
import hotelbookingserver.service.HotelService;
import hotelbookingserver.service.RoomService;
import hotelbookingserver.service.GuestService; // Importa GuestService
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.UUID;
import java.util.List;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.stream.Collectors;


public class ProtocolHandler {
    private static final Logger logger = LogManager.getLogger(ProtocolHandler.class);
    private final HotelService hotelService = new HotelService();
    private final RoomService roomService = new RoomService();
    private final FrontDeskClerkService frontDeskClerkService = new FrontDeskClerkService();
    private final GuestService guestService = new GuestService(); // Declaramos el servicio de huéspedes

    private final Gson gson = new Gson();

    private static final String SERVER_FILE_STORAGE_ROOT = "C:\\Users\\PC\\Documents\\Proyecto1 progra 2";
    private static final String ROOM_IMAGES_RELATIVE_PATH_PREFIX = "data/images/rooms/";
    private static final String TEMP_IMAGES_RELATIVE_PATH_PREFIX = "data/images/temp_rooms/";



    public Response handle(Request request) {
        logger.debug("Handling request: {}", request.getAction());
        try {
            switch (request.getAction()) {

                // =================== HOTEL =========================
                case "getHotels": {
                    List<Hotel> hotels = hotelService.getAllHotels();
                    logger.debug("Retrieved {} hotels", hotels.size());
                    return new Response("200", "Hoteles cargados", hotels);
                }

                case "getHotel": {
                    try {
                        int hotelNumber = parseIntFromRequest(request.getData());
                        List<Hotel> hoteles = hotelService.getAllHotels(); // Podrías llamar directamente a hotelService.getHotelById(hotelNumber) si existe
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
                        List<Hotel> updatedHotels = hotelService.addHotel(newHotel); // Asumiendo que addHotel devuelve List<Hotel>
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
                        Hotel result = hotelService.updateHotel(updated); // Asumiendo que updateHotel devuelve el Hotel actualizado
                        if (result != null) {
                            // Si updateHotel devuelve el objeto actualizado, no es necesario getAllHotels y el stream
                            // solo para obtener el hotel completo de nuevo.
                            return new Response("200", "Hotel actualizado", result);
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
                    List<Room> rooms = roomService.getAllRooms();
                    logger.debug("Habitaciones cargadas: {}", rooms.size());
                    return new Response("200", "Habitaciones cargadas", rooms);
                }

                case "getRoom": {
                    try {
                        int roomNumber = parseIntFromRequest(request.getData());
                        Room foundRoom = roomService.getRoomById(roomNumber);

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
                        Room newRoom = gson.fromJson(gson.toJson(request.getData()), Room.class);

                        // **AJUSTE DE VALIDACIÓN:**
                        // Usamos el nuevo método `getRoomByHotelAndRoomNumber` para verificar la unicidad
                        // de la habitación (roomNumber) dentro de un hotel específico (hotelId).
                        Room existingRoomInHotel = roomService.getRoomByHotelAndRoomNumber(newRoom.getRoomNumber(), newRoom.getHotelId());

                        if (existingRoomInHotel != null) {
                            logger.warn("Intento de registrar habitación duplicada con número {} en el hotel {}. Ya existe.", newRoom.getRoomNumber(), newRoom.getHotelId());
                            return new Response("409", "El número de habitación " + newRoom.getRoomNumber() + " ya existe en el hotel " + newRoom.getHotelId() + ".", null);
                        }

                        List<String> finalImagePaths = new ArrayList<>();
                        if (newRoom.getImagesPaths() != null && !newRoom.getImagesPaths().isEmpty()) {
                            for (String tempPath : newRoom.getImagesPaths()) {
                                Path sourcePath = Paths.get(SERVER_FILE_STORAGE_ROOT, tempPath);

                                String fileName = sourcePath.getFileName().toString();
                                Path destinationDirPath = Paths.get(SERVER_FILE_STORAGE_ROOT, ROOM_IMAGES_RELATIVE_PATH_PREFIX);
                                Path destinationPath = destinationDirPath.resolve(fileName);

                                Files.createDirectories(destinationDirPath); // Asegura que el directorio permanente exista

                                Files.move(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
                                String relativePermanentPath = ROOM_IMAGES_RELATIVE_PATH_PREFIX + fileName;
                                finalImagePaths.add(relativePermanentPath);
                                logger.info("Imagen movida de temporal a permanente: {} -> {}", tempPath, relativePermanentPath);
                            }
                        }
                        newRoom.setImagesPaths(finalImagePaths); // Asigna las nuevas rutas permanentes a la habitación

                        boolean added = roomService.addRoom(newRoom);
                        if (added) {
                            // Al devolver la habitación, asegúrate de obtenerla con el ID del hotel para la unicidad
                            Room addedRoomWithHotel = roomService.getRoomByHotelAndRoomNumber(newRoom.getRoomNumber(), newRoom.getHotelId());
                            return new Response("201", "Habitación registrada con éxito", addedRoomWithHotel);
                        } else {
                            // Este 'else' ahora solo se ejecutará si roomService.addRoom falla por una razón diferente
                            // a la duplicidad (ya que eso se maneja antes).
                            logger.error("Error desconocido al registrar la habitación: {}", newRoom.getRoomNumber());
                            return new Response("500", "No se pudo registrar la habitación (ver logs para detalles)", null);
                        }

                    } catch (Exception e) {
                        logger.error("Error al registrar habitación", e);
                        return new Response("500", "Error interno al registrar habitación: " + e.getMessage(), null);
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


                        Room roomToDelete = roomService.getRoomById(roomNumber);
                        boolean deleted = roomService.deleteRoom(roomNumber);
                        if (deleted) {
                            if (roomToDelete != null && roomToDelete.getImagesPaths() != null) {
                                for (String imagePath : roomToDelete.getImagesPaths()) {
                                    Path fullPath = Paths.get(SERVER_FILE_STORAGE_ROOT).resolve(imagePath);
                                    try {
                                        Files.deleteIfExists(fullPath);
                                        logger.info("Imagen eliminada físicamente: {}", fullPath);
                                    } catch (IOException ioE) {
                                        logger.error("Error al eliminar imagen física {}: {}", fullPath, ioE.getMessage());
                                    }
                                }
                            }
                            return new Response("200", "Habitación eliminada con éxito", null);
                        } else {
                            return new Response("404", "Habitación no encontrada", null);
                        }
                    } catch (Exception e) {
                        logger.error("Error al eliminar habitación", e);
                        return new Response("500", "Error interno al eliminar habitación", null);
                    }
                }

                case "getRoomByHotelAndRoomNumber": {
                    try {
                        java.util.Map<String, Double> searchParamsMap = gson.fromJson(gson.toJson(request.getData()), new TypeToken<Map<String, Double>>() {}.getType());

                        // Obtenemos roomNumber y hotelId
                        // CONVERTIMOS ID's DE GSON A INT
                        int roomNumber = searchParamsMap.get("roomNumber").intValue();
                        int hotelId = searchParamsMap.get("hotelId").intValue();

                        Room foundRoom = roomService.getRoomByHotelAndRoomNumber(roomNumber, hotelId);

                        if (foundRoom != null) {
                            return new Response("200", "Habitación encontrada en el hotel especificado", foundRoom);
                        } else {
                            return new Response("404", "Habitación no encontrada con ese número en el hotel especificado", null);
                        }
                    } catch (Exception e) {
                        logger.error("Error al consultar habitación por número y hotel", e);
                        return new Response("500", "Error interno al consultar habitación por número y hotel", null);
                    }
                }


                // =================== FRONT DESK CLERK =========================
                case "registerFrontDeskClerk": {
                    try {
                        FrontDeskClerk frontDeskClerk = gson.fromJson(gson.toJson(request.getData()), FrontDeskClerk.class);
                        boolean success = frontDeskClerkService.addClerk(frontDeskClerk);

                        if (success) {
                            logger.info("Recepcionista registrado: {}", frontDeskClerk);
                            return new Response("201", "Recepcionista registrado con éxito", frontDeskClerk);
                        } else {
                            logger.warn("No se pudo registrar el recepcionista: {}", frontDeskClerk);
                            return new Response("400", "No se pudo registrar el recepcionista (posible duplicado o error de hotel)", null);
                        }
                    } catch (Exception e) {
                        logger.error("Error al registrar recepcionista", e);
                        return new Response("500", "Error interno al registrar recepcionista", null);
                    }
                }


                case "getFrontDeskClerk": {
                    try {
                        String employeeId = String.valueOf(parseIntFromRequest(request.getData()));
                        FrontDeskClerk found = frontDeskClerkService.getClerkById(employeeId);
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

                // =================== GUEST (HUESPEDES) =========================
                case "addGuest": {
                    try {
                        Guest newGuest = gson.fromJson(gson.toJson(request.getData()), Guest.class);
                        boolean added = guestService.addGuest(newGuest);
                        if (added) {
                            logger.info("Huésped agregado: {}", newGuest);
                            // Opcional: podrías devolver el huésped agregado si necesitas alguna propiedad generada
                            return new Response("201", "Huésped agregado con éxito", newGuest);
                        } else {
                            logger.warn("No se pudo agregar el huésped (posible duplicado): {}", newGuest.getId());
                            return new Response("409", "El huésped con ID " + newGuest.getId() + " ya existe o hubo un error.", null);
                        }
                    } catch (Exception e) {
                        logger.error("Error al agregar huésped: {}", e.getMessage(), e);
                        return new Response("500", "Error interno al agregar huésped.", null);
                    }
                }

                case "getAllGuests": {
                    try {
                        List<Guest> guests = guestService.getAllGuests();
                        logger.debug("Se cargaron {} huéspedes.", guests.size());
                        return new Response("200", "Huéspedes cargados", guests);
                    } catch (Exception e) {
                        logger.error("Error al obtener todos los huéspedes: {}", e.getMessage(), e);
                        return new Response("500", "Error interno al obtener huéspedes.", null);
                    }
                }

                case "getGuestById": {
                    try {
                        int guestId = parseIntFromRequest(request.getData());
                        Guest foundGuest = guestService.getGuestById(guestId);
                        if (foundGuest != null) {
                            return new Response("200", "Huésped encontrado", foundGuest);
                        } else {
                            return new Response("404", "Huésped no encontrado", null);
                        }
                    } catch (Exception e) {
                        logger.error("Error al obtener huésped por ID: {}", e.getMessage(), e);
                        return new Response("500", "Error interno al obtener huésped por ID.", null);
                    }
                }

                case "updateGuest": {
                    try {
                        Guest updatedGuest = gson.fromJson(gson.toJson(request.getData()), Guest.class);
                        boolean updated = guestService.updateGuest(updatedGuest);
                        if (updated) {
                            logger.info("Huésped actualizado: {}", updatedGuest);
                            // Opcional: podrías devolver el huésped actualizado
                            return new Response("200", "Huésped actualizado con éxito", updatedGuest);
                        } else {
                            logger.warn("No se encontró huésped para actualizar con ID: {}", updatedGuest.getId());
                            return new Response("404", "Huésped no encontrado para actualizar.", null);
                        }
                    } catch (Exception e) {
                        logger.error("Error al actualizar huésped: {}", e.getMessage(), e);
                        return new Response("500", "Error interno al actualizar huésped.", null);
                    }
                }

                case "deleteGuest": {
                    try {
                        int guestId = parseIntFromRequest(request.getData());
                        boolean deleted = guestService.deleteGuest(guestId);
                        if (deleted) {
                            logger.info("Huésped eliminado con ID: {}", guestId);
                            return new Response("200", "Huésped eliminado con éxito", null);
                        } else {
                            logger.warn("No se encontró huésped para eliminar con ID: {}", guestId);
                            return new Response("404", "Huésped no encontrado para eliminar.", null);
                        }
                    } catch (Exception e) {
                        logger.error("Error al eliminar huésped: {}", e.getMessage(), e);
                        return new Response("500", "Error interno al eliminar huésped.", null);
                    }
                }

                // =================== IMAGENES DE HABITACIONES =========================

                case "uploadTempRoomImage": {
                    logger.info("Recibida acción: uploadTempRoomImage");
                    try {
                        ImageUploadDTO uploadDto = gson.fromJson(gson.toJson(request.getData()), ImageUploadDTO.class);
                        byte[] imageData = uploadDto.getImageData();
                        String originalFileName = uploadDto.getFileName();

                        String fileExtension = "";
                        int dotIndex = originalFileName.lastIndexOf('.');
                        if (dotIndex > 0) {
                            fileExtension = originalFileName.substring(dotIndex);
                        }
                        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

                        Path tempDirPath = Paths.get(SERVER_FILE_STORAGE_ROOT, TEMP_IMAGES_RELATIVE_PATH_PREFIX);
                        Path tempFilePath = tempDirPath.resolve(uniqueFileName);

                        Files.createDirectories(tempDirPath);
                        Files.write(tempFilePath, imageData);

                        String relativePathToClient = TEMP_IMAGES_RELATIVE_PATH_PREFIX + uniqueFileName;
                        logger.info("Imagen temporal guardada en: {}", relativePathToClient);
                        return new Response("200", "Imagen temporal subida", relativePathToClient);

                    } catch (IOException e) {
                        logger.error("Error de E/S al subir imagen temporal al servidor: {}", e.getMessage(), e);
                        return new Response("500", "Error de E/S al procesar la imagen temporal.", null);
                    } catch (Exception e) {
                        logger.error("Error inesperado al manejar 'uploadTempRoomImage': {}", e.getMessage(), e);
                        return new Response("500", "Error interno del servidor al subir imagen temporal.", null);
                    }
                }


                case "uploadRoomImage": {
                    try {
                        ImageUploadDTO uploadData = gson.fromJson(gson.toJson(request.getData()), ImageUploadDTO.class);
                        int roomNumber = uploadData.getRoomNumber();
                        byte[] imageData = uploadData.getImageData();
                        String originalFileName = uploadData.getFileName();

                        String fileExtension = "";
                        int dotIndex = originalFileName.lastIndexOf('.');
                        if (dotIndex > 0 && dotIndex < originalFileName.length() - 1) {
                            fileExtension = originalFileName.substring(dotIndex + 1);
                        }

                        String uniqueFileName = UUID.randomUUID().toString() + "." + fileExtension;

                        Path serverImageDirPath = Paths.get(SERVER_FILE_STORAGE_ROOT, ROOM_IMAGES_RELATIVE_PATH_PREFIX);
                        Path serverFullImagePath = serverImageDirPath.resolve(uniqueFileName);

                        Files.createDirectories(serverImageDirPath);

                        Files.write(serverFullImagePath, imageData);
                        logger.info("Imagen guardada en el servidor (directo a permanente): {}", serverFullImagePath.toAbsolutePath());

                        Room room = roomService.getRoomById(roomNumber);
                        if (room != null) {
                            if (room.getImagesPaths() == null) {
                                room.setImagesPaths(new ArrayList<>());
                            }
                            String relativePermanentPath = ROOM_IMAGES_RELATIVE_PATH_PREFIX + uniqueFileName;
                            room.getImagesPaths().add(relativePermanentPath);
                            roomService.updateRoom(room);
                            logger.info("Ruta de imagen '{}' añadida a la habitación {}", relativePermanentPath, roomNumber);

                            return new Response("200", "Imagen subida y path guardado.", room);
                        } else {
                            logger.warn("Habitación {} no encontrada para asociar la imagen. Eliminando archivo: {}", roomNumber, serverFullImagePath.toAbsolutePath());
                            Files.deleteIfExists(serverFullImagePath);
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
                        String imageRelativePath = gson.fromJson(gson.toJson(request.getData()), String.class);
                        Path serverFullImagePath = Paths.get(SERVER_FILE_STORAGE_ROOT).resolve(imageRelativePath);

                        logger.info("Cliente solicita descarga de imagen: {}. Buscando en: {}", imageRelativePath, serverFullImagePath.toAbsolutePath());

                        if (Files.exists(serverFullImagePath)) {
                            byte[] imageData = Files.readAllBytes(serverFullImagePath);
                            logger.info("Imagen de {} bytes leída y lista para enviar al cliente.", imageData.length);
                            return new Response("200", "Imagen enviada.", imageData);
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
            // No es necesario cerrar los servicios aquí, ya que el ProtocolHandler es de larga duración.
            // Los servicios deben cerrar sus recursos al finalizar la aplicación (ej. en el main/server shutdown).
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

    // Método para cerrar los recursos de los servicios si es necesario al apagar el servidor
    public void closeServices() {
        // Asegúrate de que los otros servicios también tengan un método close()
        // para liberar sus recursos (ej. archivos, conexiones a DB).
        guestService.close();
        // hotelService.close(); // Si tu HotelService tiene un close()
        // roomService.close();   // Si tu RoomService tiene un close()
        // frontDeskClerkService.close(); // Si tu FrontDeskClerkService tiene un close()
        logger.info("Servicios del ProtocolHandler cerrados.");
    }
}