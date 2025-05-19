package hotelbookingserver.service;




import hotelbookingcommon.domain.Hotel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.ArrayList;
import java.util.List;
import java.io.*;

public class HotelService {
    // Simulación de base de datos
    private final List<Hotel> hoteles = new ArrayList<>();
    private static final Logger logger = LogManager.getLogger(HotelService.class);

    private static final String HOTEL_FILE = "C:\\Users\\XT\\Documents\\ProyectoProgra2\\hotels.dat"; // Nombre del archivo


    public HotelService() {
        // No inicializar la lista con datos de prueba.  Se cargan del archivo.
    }

    public List<Hotel> addHotel(Hotel hotel) {
        List<Hotel> hoteles = loadHotels();
        hoteles.add(hotel);
        saveHotels(hoteles);
        return hoteles;
    }

    public List<Hotel> getAllHotels() {
        logger.info("Obteniendo todos los hoteles");
        List<Hotel> hoteles = loadHotels(); // Cargar del archivo
        if (hoteles == null) {
            hoteles = new ArrayList<>();
        }
        return hoteles;
    }

    public void saveHotels(List<Hotel> hoteles) {
        logger.info("Guardando hoteles en archivo binario: {}", HOTEL_FILE);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(HOTEL_FILE))) {
            oos.writeObject(hoteles);
        } catch (IOException e) {
            logger.error("Error al guardar hoteles en {}: {}", HOTEL_FILE, e.getMessage());
            throw new RuntimeException("Error al guardar hoteles", e); // Propaga la excepción para que se maneje en una capa superior
        }
    }

    @SuppressWarnings("unchecked")  // Suprime el warning de unchecked cast
    private List<Hotel> loadHotels() {
        File file = new File(HOTEL_FILE);
        if (!file.exists()) {
            logger.warn("El archivo de hoteles {} no existe. Se devuelve una lista vacía.", HOTEL_FILE);
            return new ArrayList<>(); // Retorna lista vacía si no existe el archivo
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            List<Hotel> hoteles = (List<Hotel>) ois.readObject();
            logger.info("Hoteles cargados desde {}: {}", HOTEL_FILE, hoteles.size());
            return hoteles;
        } catch (IOException | ClassNotFoundException e) {
            logger.error("Error al cargar hoteles desde {}: {}", HOTEL_FILE, e.getMessage());
            return new ArrayList<>(); // Retorna una lista vacía en caso de error al leer el archivo
            // Considerar si se debe propagar la excepción en lugar de retornar null/[]
        }
    }

    public Hotel updateHotel(Hotel updatedHotel) {
        List<Hotel> hoteles = loadHotels();
        for (int i = 0; i < hoteles.size(); i++) {
            if (hoteles.get(i).getNumHotel() == updatedHotel.getNumHotel()) {
                hoteles.set(i, updatedHotel);
                saveHotels(hoteles);
                return updatedHotel;
            }
        }
        return null;
    }

    public boolean deleteHotel(int hotelNumber) {
        List<Hotel> hoteles = loadHotels();
        boolean removed = hoteles.removeIf(h -> h.getNumHotel() == hotelNumber);
        if (removed) {
            saveHotels(hoteles);
        }
        return removed;
    }

    // Ejemplo de uso del método saveHotels (podría estar en otra parte, como en el ProtocolHandler)
    public static void main(String[] args) {
        HotelService hotelService = new HotelService();
        List<Hotel> hoteles = new ArrayList<>();
        hoteles.add(new Hotel(1, "Hotel Sol", "San José"));
        hoteles.add(new Hotel(2, "Hotel Luna", "Cartago"));
        hotelService.saveHotels(hoteles); // Guarda los hoteles en el archivo
        List<Hotel> loadedHotels = hotelService.getAllHotels(); //Los carga del archivo
        System.out.println("Hoteles cargados: " + loadedHotels);
    }


}
