package hotelbookingserver.service;




import hotelbookingcommon.domain.Hotel;

import java.util.ArrayList;
import java.util.List;

public class HotelService {
    // Simulación de base de datos
    private final List<Hotel> hoteles = new ArrayList<>();

    public HotelService() {
        // Datos de prueba
        hoteles.add(new Hotel(1, "Hotel Sol", "San José"));
        hoteles.add(new Hotel(2, "Hotel Luna", "Cartago"));
        hoteles.add(new Hotel(3,"Hotel Estrella","Heredia"));
    }

    public List<Hotel> getAllHotels() {
        return hoteles;
    }
}
