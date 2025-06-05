package hotelbookingserver.service;

import hotelbookingcommon.domain.Guest;
import hotelbookingserver.datamanager.GuestData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class GuestService {
    private static final Logger logger = LogManager.getLogger(GuestService.class);
    private static final String GUEST_FILE = "C:\\Users\\PC\\Documents\\UCR\\Progra_II\\PROYECTO\\BinaryFilesLocal\\GuestFiles\\guests.dat";

    private GuestData guestData;

    public GuestService() {
        try {
            guestData = new GuestData(new File(GUEST_FILE));
        } catch (IOException e) {
            logger.error("Error al abrir archivo de huéspedes: {}", e.getMessage());
            throw new RuntimeException("No se pudo inicializar GuestData", e);
        }
    }

    public String addGuest(Guest guest) throws IOException {
        List<Guest> existingGuests = guestData.findAll();

        for (Guest existing : existingGuests) {
            if (existing.getId() == guest.getId()) {
                logger.warn("ID duplicado: {}", guest.getId());
                return "ID " + guest.getId();
            }
            if (Objects.equals(existing.getCredential(), guest.getCredential())) {
                logger.warn("Credential duplicado: {}", guest.getCredential());
                return "credencial " + guest.getCredential();
            }
            if (existing.getEmail() != null && existing.getEmail().equalsIgnoreCase(guest.getEmail())) {
                logger.warn("Email duplicado: {}", guest.getEmail());
                return "correo electrónico " + guest.getEmail();
            }
            if (existing.getPhoneNumber() != null && existing.getPhoneNumber().equals(guest.getPhoneNumber())) {
                logger.warn("Número de teléfono duplicado: {}", guest.getPhoneNumber());
                return "número de teléfono " + guest.getPhoneNumber();
            }
            if (existing.getName() != null && existing.getLastName() != null &&
                    guest.getName() != null && guest.getLastName() != null &&
                    existing.getName().equalsIgnoreCase(guest.getName()) &&
                    existing.getLastName().equalsIgnoreCase(guest.getLastName())) {
                logger.warn("Nombre y apellido duplicados: {} {}", guest.getName(), guest.getLastName());
                return "nombre y apellido (" + guest.getName() + " " + guest.getLastName() + ")"; // Return specific duplicate field
            }
        }

        guestData.insert(guest);
        logger.info("Huésped agregado: {}", guest);
        return null; // Success, no duplicate
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

    public String updateGuest(Guest guest) throws IOException { // Changed return type to String
        List<Guest> existingGuests = guestData.findAll();
        boolean guestExists = false;

        for (Guest existing : existingGuests) {
            if (existing.getId() == guest.getId()) {
                guestExists = true; //ENCUENTRA EL GUEST A ACTUALIZAR
            } else {
                //REVISAMOE DUPLICADOS
                if (Objects.equals(existing.getCredential(), guest.getCredential())) {
                    logger.warn("Credential duplicado: {}", guest.getCredential());
                    return "credencial " + guest.getCredential();
                }
                if (existing.getEmail() != null && existing.getEmail().equalsIgnoreCase(guest.getEmail())) {
                    logger.warn("Email duplicado: {}", guest.getEmail());
                    return "correo electrónico " + guest.getEmail();
                }
                if (existing.getPhoneNumber() != null && existing.getPhoneNumber().equals(guest.getPhoneNumber())) {
                    logger.warn("Teléfono duplicado: {}", guest.getPhoneNumber());
                    return "número de teléfono " + guest.getPhoneNumber();
                }
                if (existing.getName() != null && existing.getLastName() != null &&
                        guest.getName() != null && guest.getLastName() != null &&
                        existing.getName().equalsIgnoreCase(guest.getName()) &&
                        existing.getLastName().equalsIgnoreCase(guest.getLastName())) {
                    logger.warn("Nombre y apellido duplicados: {} {}", guest.getName(), guest.getLastName());
                    return "nombre y apellido (" + guest.getName() + " " + guest.getLastName() + ")";
                }
            }
        }

        if (!guestExists) {
            logger.warn("No se encontró huésped con ID: {}", guest.getId());
            return "not found"; // Indicate that the guest to update was not found
        }

        guestData.update(guest);
        logger.info("Huésped actualizado: {}", guest);
        return null; // Success, no duplicate
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