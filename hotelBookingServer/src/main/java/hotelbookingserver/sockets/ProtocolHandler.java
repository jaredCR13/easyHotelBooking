package hotelbookingserver.sockets;

import com.google.gson.Gson;
import hotelbookingcommon.domain.*; // Asegúrate de que ImageUploadDTO es accesible aquí
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
import java.nio.file.StandardCopyOption; // Importa StandardCopyOption
import java.util.ArrayList;


public class ProtocolHandler {
    private static final Logger logger = LogManager.getLogger(ProtocolHandler.class);
    private final HotelService hotelService = new HotelService();
    private final RoomService roomService = new RoomService();
    private final FrontDeskClerkService frontDeskClerkService = new FrontDeskClerkService();

    private final Gson gson = new Gson();


    private static final String SERVER_FILE_STORAGE_ROOT = "C:\\Users\\Lexis\\Desktop\\Proyecto\\Data";
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
                        Hotel result = hotelService.updateHotel(updated);
                        if (result != null) {
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
                case "getRoomsByHotelId": {
                    try {

                        int hotelId = parseIntFromRequest(request.getData());
                        List<Room> rooms = roomService.getRoomsByHotelId(hotelId);
                        logger.debug("Retrieved {} rooms for hotel ID {}", rooms.size(), hotelId);
                        return new Response("200", "Habitaciones del hotel cargadas", rooms);
                    } catch (NumberFormatException e) {
                        logger.error("Formato de ID de hotel inválido para getRoomsByHotelId", e);
                        return new Response("400", "ID de hotel inválido.", null);
                    } catch (Exception e) {
                        logger.error("Error al obtener habitaciones por ID de hotel", e);
                        return new Response("500", "Error interno al obtener habitaciones por hotel.", null);
                    }
                }
                case "registerRoom": {
                    try {
                        Room newRoom = gson.fromJson(gson.toJson(request.getData()), Room.class);

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
                            Room addedRoomWithHotel = roomService.getRoomByHotelAndRoomNumber(newRoom.getRoomNumber(), newRoom.getHotelId());
                            return new Response("201", "Habitación registrada con éxito", addedRoomWithHotel);
                        } else {
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


                // =================== FRONT DESK CLERK =========================
                case "registerFrontDeskClerk": {
                    try {
                        FrontDeskClerk frontDeskClerk = gson.fromJson(gson.toJson(request.getData()), FrontDeskClerk.class);


                        FrontDeskClerk existingFrontDeskClerkInHotel = frontDeskClerkService.getFrontDeskClerkByEmployeeIdAndHotelId(frontDeskClerk.getEmployeeId(), frontDeskClerk.getHotelId());

                        if (existingFrontDeskClerkInHotel != null) {
                            logger.warn("Intento de registrar frontDeskClerk duplicada con número {} en el hotel {}. Ya existe.", frontDeskClerk.getEmployeeId(), frontDeskClerk.getHotelId());
                            return new Response("409", "El id de frontDeskClerk " + frontDeskClerk.getEmployeeId() + " ya existe en el hotel " + frontDeskClerk.getHotelId() + ".", null);
                        }
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
                        String employeeId = request.getData().toString();
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


                case "getClerks": {
                    try {
                        List<FrontDeskClerk> clerks = frontDeskClerkService.getAllClerks();
                        return new Response("200", "Recepcionistas cargados correctamente", clerks);
                    } catch (Exception e) {
                        logger.error("Error al obtener recepcionistas", e);
                        return new Response("500", "Error interno al obtener recepcionistas", null);
                    }
                }
                case "deleteFrontDeskClerk": {
                    try {
                        String employeeId = (String)request.getData();

                        //FrontDeskClerk clerkToDelete = frontDeskClerkService.getClerkById(employeeId);
                        boolean deleted = frontDeskClerkService.deleteClerk(employeeId);

                        if (deleted) {
                            return new Response("200", "Recepcionista eliminado con éxito", null);
                        } else {
                            return new Response("404", "Recepcionista no encontrado", null);
                        }
                    } catch (Exception e) {
                        logger.error("Error al eliminar recepcionista", e);
                        return new Response("500", "Error interno al eliminar recepcionista", null);
                    }
                }
                case "updateClerk": {
                    try {
                        FrontDeskClerk clerkToUpdate = gson.fromJson(gson.toJson(request.getData()), FrontDeskClerk.class);
                        boolean updated = frontDeskClerkService.updateClerk(clerkToUpdate);
                        if (updated) {
                            FrontDeskClerk updatedClerk = frontDeskClerkService.getClerkById(clerkToUpdate.getEmployeeId());
                            return new Response("200", "Recepcionista actualizado con éxito", updatedClerk);
                        } else {
                            return new Response("404", "Recepcionista no encontrado", null);
                        }
                    } catch (Exception e) {
                        logger.error("Error procesando updateClerk", e);
                        return new Response("500", "Error interno al actualizar recepcionista", null);
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
                        // Deserializa el DTO que contiene los bytes de la imagen del cliente
                        ImageUploadDTO uploadData = gson.fromJson(gson.toJson(request.getData()), ImageUploadDTO.class);
                        int roomNumber = uploadData.getRoomNumber();
                        byte[] imageData = uploadData.getImageData();
                        String originalFileName = uploadData.getFileName();

                        //Extrae la extensión del archivo original
                        String fileExtension = "";
                        int dotIndex = originalFileName.lastIndexOf('.');
                        if (dotIndex > 0 && dotIndex < originalFileName.length() - 1) {
                            fileExtension = originalFileName.substring(dotIndex + 1);
                        }

                        //Genera un nombre de archivo único para evitar colisiones
                        String uniqueFileName = UUID.randomUUID().toString() + "." + fileExtension;
                        // Construye la ruta completa donde se guardará la imagen en el disco del servidor

                        Path serverImageDirPath = Paths.get(SERVER_FILE_STORAGE_ROOT, ROOM_IMAGES_RELATIVE_PATH_PREFIX);
                        Path serverFullImagePath = serverImageDirPath.resolve(uniqueFileName);

                        // Crear los directorios si no existen
                        Files.createDirectories(serverImageDirPath);

                        // Guardar los bytes de la imagen en el archivo físico del servidor
                        Files.write(serverFullImagePath, imageData);
                        logger.info("Imagen guardada en el servidor (directo a permanente): {}", serverFullImagePath.toAbsolutePath());

                        //Actualizar la lista de rutas de la habitación en la base de datos
                        Room room = roomService.getRoomById(roomNumber); // Obtener la habitación de la DB
                        if (room != null) {
                            if (room.getImagesPaths() == null) {
                                room.setImagesPaths(new ArrayList<>());
                            }
                            // Añade la ruta relativa que el cliente usará para pedir la imagen
                            String relativePermanentPath = ROOM_IMAGES_RELATIVE_PATH_PREFIX + uniqueFileName;
                            room.getImagesPaths().add(relativePermanentPath);
                            roomService.updateRoom(room); // Actualiza la habitación en la DB (vía RoomData)
                            logger.info("Ruta de imagen '{}' añadida a la habitación {}", relativePermanentPath, roomNumber);

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
                        //Obtiene la ruta relativa de la imagen solicitada por el cliente
                        String imageRelativePath = gson.fromJson(gson.toJson(request.getData()), String.class);

                        //Construir la ruta física completa en el disco del servidor

                        Path serverFullImagePath = Paths.get(SERVER_FILE_STORAGE_ROOT).resolve(imageRelativePath);

                        logger.info("Cliente solicita descarga de imagen: {}. Buscando en: {}", imageRelativePath, serverFullImagePath.toAbsolutePath());

                        //Lee el archivo de imagen a bytes y enviarlos al cliente
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