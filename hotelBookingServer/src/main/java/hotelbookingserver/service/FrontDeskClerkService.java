package hotelbookingserver.service;

import hotelbookingcommon.domain.FrontDeskClerk;
import hotelbookingcommon.domain.Hotel;
import hotelbookingcommon.domain.Room;
import hotelbookingserver.datamanager.FrontDeskClerkData;
import hotelbookingserver.datamanager.HotelData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class FrontDeskClerkService {
    private static final Logger logger = LogManager.getLogger(FrontDeskClerkService.class);
    private static final String FRONT_DESK_FILE = "C:\\Users\\Lexis\\Desktop\\Proyecto\\Data\\frontdeskclerks.dat";
    private static final String HOTEL_FILE = "C:\\Users\\Lexis\\Desktop\\Proyecto\\Data\\hotels.dat";

    private FrontDeskClerkData clerkData;
    private HotelData hotelData;

    public FrontDeskClerkService() {
        try {
            clerkData = new FrontDeskClerkData(new File(FRONT_DESK_FILE));
            hotelData = new HotelData(new File(HOTEL_FILE));
        } catch (IOException e) {
            logger.error("Error al abrir archivos de recepcionistas u hoteles: {}", e.getMessage());
            throw new RuntimeException("No se pudo inicializar FrontDeskClerkData o HotelData", e);
        }
    }

    public boolean addClerk(FrontDeskClerk clerk) {
        try {
            //Validar y setear hotelId si viene con objeto Hotel
            if (clerk.getHotel() != null) {
                clerk.setHotelId(clerk.getHotel().getNumHotel());
            }

            if (clerk.getHotelId() == -1) {
                logger.error("El recepcionista no tiene un hotel asociado.");
                return false;
            }

            //Verificar que el hotel realmente exista
            Hotel associatedHotel = hotelData.findById(clerk.getHotelId());
            if (associatedHotel == null) {
                logger.error("El hotel con ID {} no existe.", clerk.getHotelId());
                return false;
            }

            //Buscar si ya existe un recepcionista con ese mismo ID en el mismo hotel
            boolean exists = clerkData.findAll().stream()
                    .anyMatch(c -> c.getHotelId() == clerk.getHotelId() && c.getEmployeeId().equals(clerk.getEmployeeId()));

            if (exists) {
                logger.warn("Intento de registrar recepcionista duplicado. El employeeId {} ya existe en el hotel {}.", clerk.getEmployeeId(), clerk.getHotelId());
                return false;
            }

            //clerk.setHotel(associatedHotel);
            // Guardar recepcionista
            clerkData.insert(clerk);
            // if (associatedHotel != null) {
            //  associatedHotel.addFrontDeskClerk(clerk);
            //   }
            //  hotelData.update(associatedHotel);
            logger.info("Recepcionista registrado: {}", clerk);
            return true;

        } catch (IOException e) {
            logger.error("Error al agregar recepcionista: {}", e.getMessage());
            return false;
        }
    }




    public List<FrontDeskClerk> getAllClerks() {
        try {
            List<FrontDeskClerk> clerks = clerkData.findAll();
            List<Hotel> allHotels = hotelData.findAll();

            for (FrontDeskClerk clerk : clerks) {
                allHotels.stream()
                        .filter(h -> h.getNumHotel() == clerk.getHotelId())
                        .findFirst()
                        .ifPresent(clerk::setHotel);
            }

            return clerks;
        } catch (IOException e) {
            logger.error("Error al obtener recepcionistas: {}", e.getMessage());
            throw new RuntimeException("Error al obtener recepcionistas", e);
        }
    }

    public boolean updateClerk(FrontDeskClerk clerk) {
        try {
            if (clerk.getHotel() != null) {
                clerk.setHotelId(clerk.getHotel().getNumHotel());
            }

            if (clerk.getHotelId() != -1) {
                Hotel associatedHotel = hotelData.findById(clerk.getHotelId());
                if (associatedHotel == null) {
                    logger.error("No existe un hotel con ID: {}", clerk.getHotelId());
                    return false;
                }
            }

            boolean updated = clerkData.updateClerk(clerk);
            if (updated) {
                logger.info("Recepcionista actualizado: {}", clerk);
            } else {
                logger.warn("No se encontró recepcionista con ID: {}", clerk.getEmployeeId());
            }
            return updated;
        } catch (IOException e) {
            logger.error("Error al actualizar recepcionista: {}", e.getMessage());
            return false;
        }
    }

    public boolean deleteClerk(String employeeId) {
        try {
            boolean deleted = clerkData.delete(employeeId);

            if (deleted) {
                logger.info("Recepcionista eliminado con ID: {}", employeeId);
            } else {
                logger.warn("No se encontró recepcionista para eliminar con ID: {}", employeeId);
            }

            return deleted;
        } catch (IOException e) {
            logger.error("Error al eliminar recepcionista con ID {}: {}", employeeId, e.getMessage());
            return false;
        }
    }


    public FrontDeskClerk getClerkById(String employeeId) {
        try {
            FrontDeskClerk clerk = clerkData.findById(employeeId);
            if (clerk != null && clerk.getHotelId() != -1) {
                Hotel associatedHotel = hotelData.findById(clerk.getHotelId());
                if (associatedHotel != null) {
                    clerk.setHotel(associatedHotel);
                }
            }

            return clerk;
        } catch (IOException e) {
            logger.error("Error al obtener recepcionista por ID: {}", e.getMessage());
            throw new RuntimeException("Error al obtener recepcionista por ID", e);
        }
    }
    public FrontDeskClerk getFrontDeskClerkByEmployeeIdAndHotelId(String employeeId, int hotelId) {
        try {
            return clerkData.findAll().stream()
                    .filter(frontDeskClerk -> frontDeskClerk.getEmployeeId().equals( employeeId )&& frontDeskClerk.getHotelId() == hotelId)
                    .findFirst()
                    .orElse(null);
        } catch (IOException e) {
            logger.error("Error al obtener el frontDeskClerk por id y hotel: {}", e.getMessage());
            throw new RuntimeException("Error al obtener frontDeskClerk por id y hotel", e);
        }
    }

    public FrontDeskClerk getClerkByUsername(String username) {
        try {
            List<FrontDeskClerk> allClerks = clerkData.findAll(); // Lee todos los clerks para buscar por username
            for (FrontDeskClerk clerk : allClerks) {
                if (clerk.getUser().equalsIgnoreCase(username)) { // Compara el nombre de usuario ignorando mayúsculas/minúsculas
                    if (clerk.getHotelId() != -1) {
                        Hotel associatedHotel = hotelData.findById(clerk.getHotelId());
                        if (associatedHotel != null) {
                            clerk.setHotel(associatedHotel); // Asocia el objeto Hotel completo
                        }
                    }
                    return clerk; // Retorna el recepcionista encontrado
                }
            }
            return null; // Usuario no encontrado
        } catch (IOException e) {
            logger.error("Error al buscar recepcionista por nombre de usuario: {}", e.getMessage());
            throw new RuntimeException("Error al buscar recepcionista por nombre de usuario", e);
        }
    }

    public void close() {
        try {
            clerkData.close();
            hotelData.close();
        } catch (IOException e) {
            logger.error("Error al cerrar los archivos: {}", e.getMessage());
        }
    }
}

