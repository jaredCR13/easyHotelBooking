package hotelbookingserver.service;

import hotelbookingcommon.domain.FrontDeskClerk;
import hotelbookingcommon.domain.Hotel;
import hotelbookingserver.datamanager.FrontDeskClerkData;
import hotelbookingserver.datamanager.HotelData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class FrontDeskClerkService {
    private static final Logger logger = LogManager.getLogger(FrontDeskClerkService.class);
    private static final String FRONT_DESK_FILE = "C:\\Users\\XT\\Documents\\ProyectoProgra2\\frontdeskclerks.dat";
    private static final String HOTEL_FILE = "C:\\Users\\XT\\Documents\\ProyectoProgra2\\hotels.dat";


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
            if (clerk.getHotel() != null) {
                clerk.setHotelId(clerk.getHotel().getNumHotel());
            } else if (clerk.getHotelId() == -1) {
                logger.error("El recepcionista no tiene un hotel asociado.");
                return false;
            }

            Hotel associatedHotel = hotelData.findById(clerk.getHotelId());
            if (associatedHotel == null) {
                logger.error("El hotel con ID {} no existe.", clerk.getHotelId());
                return false;
            }

            FrontDeskClerk existingClerk = clerkData.findById(clerk.getEmployeeId());
            if (existingClerk != null) {
                logger.warn("Ya existe un recepcionista con ID: {}", clerk.getEmployeeId());
                return false;
            }

            clerkData.insert(clerk);
            logger.info("Recepcionista agregado: {}", clerk);
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

            boolean updated = clerkData.update(clerk);
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
            FrontDeskClerk clerkToDelete = clerkData.findById(employeeId);
            boolean deleted = clerkData.delete(employeeId);

            if (deleted) {
                logger.info("Recepcionista eliminado con ID: {}", employeeId);
            } else {
                logger.warn("No se encontró recepcionista para eliminar: {}", employeeId);
            }

            return deleted;
        } catch (IOException e) {
            logger.error("Error al eliminar recepcionista: {}", e.getMessage());
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

    public void close() {
        try {
            clerkData.close();
            hotelData.close();
        } catch (IOException e) {
            logger.error("Error al cerrar los archivos: {}", e.getMessage());
        }
    }
}

