package hotelbookingserver.sockets;




import hotelbookingcommon.domain.Hotel;
import hotelbookingcommon.domain.Request;
import hotelbookingcommon.domain.Response;
import hotelbookingserver.service.HotelService;

import java.util.List;

public class ProtocolHandler {
    private final HotelService hotelService = new HotelService();

    public Response handle(Request request) {
        switch (request.getAction()) {
            case "getHotels":
                List<Hotel> hotels = hotelService.getAllHotels();
                return new Response("OK", "Hoteles cargados", hotels);
            default:
                return new Response("ERROR", "Acción no reconocida", null);
        }
    }
}
