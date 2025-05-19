package hotelbookingserver.sockets;

import com.google.gson.Gson;
import hotelbookingcommon.domain.Hotel;
import hotelbookingcommon.domain.Request;
import hotelbookingcommon.domain.Response;
import hotelbookingserver.service.HotelService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class ProtocolHandler {
    private static final Logger logger = LogManager.getLogger(ProtocolHandler.class);
    private final HotelService hotelService = new HotelService();
    private final Gson gson = new Gson();

    public Response handle(Request request) {
        logger.debug("Handling request: {}", request.getAction());
        try {
            switch (request.getAction()) {

                case "getHotels": {
                    List<Hotel> hotels = hotelService.getAllHotels();
                    logger.debug("Retrieved {} hotels", hotels.size());
                    return new Response("200", "Hoteles cargados", hotels);
                }

                case "getHotel": {
                    try {
                        Object obj = request.getData();
                        int hotelNumber;

                        if (obj instanceof Double) {
                            hotelNumber = ((Double) obj).intValue(); // ✅ cast seguro
                        } else if (obj instanceof Integer) {
                            hotelNumber = (Integer) obj;
                        } else {
                            return new Response("400", "Tipo de dato inválido para número de hotel", null);
                        }

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

                case "registerHotel": { // ✅ cambio de nombre de acción
                    try {
                        Hotel newHotel = gson.fromJson(gson.toJson(request.getData()), Hotel.class);
                        List<Hotel> updatedHotels = hotelService.addHotel(newHotel); // ✅ usamos método nuevo
                        logger.info("Hotel registrado: {}", newHotel);
                        return new Response("201", "Hotel registrado con éxito", updatedHotels);
                    } catch (Exception e) {
                        logger.error("Error al registrar hotel", e);
                        return new Response("500", "Error interno al registrar hotel", null);
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
}
