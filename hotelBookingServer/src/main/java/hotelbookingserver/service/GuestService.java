package hotelbookingserver.service;

import hotelbookingcommon.domain.Guest;
import hotelbookingserver.datamanager.GuestData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class GuestService {
    private static final Logger logger = LogManager.getLogger(GuestService.class);
    private static final String GUEST_FILE = "C:\\Users\\XT\\Documents\\ProyectoProgra2\\guests.dat";

    private GuestData guestData;

    public GuestService() {
        try {
            guestData = new GuestData(new File(GUEST_FILE));
        } catch (IOException e) {
            logger.error("Error al abrir archivo de huéspedes: {}", e.getMessage());
            throw new RuntimeException("No se pudo inicializar GuestData", e);
        }
    }

    public boolean addGuest(Guest guest) {
        try {
            // Verificar si ya existe un huésped con el mismo ID
            Guest existingGuest = guestData.findById(guest.getId());
            if (existingGuest != null) {
                logger.warn("Intento de agregar huésped duplicado con ID: {}", guest.getId());
                return false;
            }

            guestData.insert(guest);
            logger.info("Huésped agregado: {}", guest);
            return true;
        } catch (IOException e) {
            logger.error("Error al agregar huésped: {}", e.getMessage());
            return false;
        }
    }

    public List<Guest> getAllGuests() {
        try {
            List<Guest> guests = guestData.findAll();
            logger.info("Se cargaron {} huéspedes.", guests.size());
            return guests;
        } catch (IOException e) {
            logger.error("Error al obtener huéspedes: {}", e.getMessage());
            throw new RuntimeException("Error al obtener huéspedes", e);
        }
    }

    public Guest getGuestById(int id) {
        try {
            Guest guest = guestData.findById(id);
            if (guest != null) {
                logger.info("Huésped encontrado: {}", guest);
            } else {
                logger.warn("No se encontró huésped con ID: {}", id);
            }
            return guest;
        } catch (IOException e) {
            logger.error("Error al obtener huésped por ID: {}", e.getMessage());
            throw new RuntimeException("Error al obtener huésped por ID", e);
        }
    }

    public boolean updateGuest(Guest guest) {
        try {
            boolean updated = guestData.update(guest);
            if (updated) {
                logger.info("Huésped actualizado: {}", guest);
            } else {
                logger.warn("No se encontró huésped con ID: {}", guest.getId());
            }
            return updated;
        } catch (IOException e) {
            logger.error("Error al actualizar huésped: {}", e.getMessage());
            return false;
        }
    }

    public boolean deleteGuest(int id) {
        try {
            boolean deleted = guestData.delete(id);
            if (deleted) {
                logger.info("Huésped eliminado con ID: {}", id);
            } else {
                logger.warn("No se encontró huésped para eliminar con ID: {}", id);
            }
            return deleted;
        } catch (IOException e) {
            logger.error("Error al eliminar huésped: {}", e.getMessage());
            return false;
        }
    }

    public void close() {
        try {
            guestData.close();
        } catch (IOException e) {
            logger.error("Error al cerrar archivo de huéspedes: {}", e.getMessage());
        }
    }
}