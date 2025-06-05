package hotelbookingserver.service;

import hotelbookingcommon.domain.Booking;
import hotelbookingcommon.domain.Room;
import hotelbookingcommon.domain.RoomStatus;
import hotelbookingserver.datamanager.BookingData;
import hotelbookingserver.datamanager.FrontDeskClerkData;
import hotelbookingserver.datamanager.GuestData;
import hotelbookingserver.datamanager.RoomData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class BookingService {
    private static final Logger logger = LogManager.getLogger(BookingService.class);
    private static final String BOOKING_FILE = "C:\\Users\\Lexis\\Desktop\\Proyecto\\Data\\bookings.dat";
    private static final String GUEST_FILE = "C:\\Users\\Lexis\\Desktop\\Proyecto\\Data\\guests.dat";
    private static final String ROOM_FILE = "C:\\Users\\Lexis\\Desktop\\Proyecto\\Data\\rooms.dat";
    private static final String FRONT_DESK_CLERK_FILE = "C:\\Users\\Lexis\\Desktop\\Proyecto\\Data\\frontdeskclerks.dat";


    private BookingData bookingData;
    private GuestData guestData;
    private RoomData roomData;
    private FrontDeskClerkData frontDeskClerkData;
    private final RoomService roomService= new RoomService();
    public BookingService() {
        try {
            bookingData = new BookingData(new File(BOOKING_FILE));
            guestData = new GuestData(new File(GUEST_FILE));
            roomData = new RoomData(new File(ROOM_FILE));
            frontDeskClerkData = new FrontDeskClerkData(new File(FRONT_DESK_CLERK_FILE));


        } catch (IOException e) {
            logger.error("Error al abrir archivo de reservas, huéspedes, habitaciones o recepcionistas: {}", e.getMessage());
            throw new RuntimeException("No se pudo inicializar BookingData, GuestData, RoomData o FrontDeskClerkData", e);
        }
    }



    public boolean addBooking(Booking booking) {
        try {


            if (hasConflictingBooking(booking.getRoomNumber(), booking.getHotelId(), booking.getStartDate(), booking.getEndDate())) {
                logger.error("Conflicto de fechas al agregar reserva: La habitación {} en el hotel {} ya está ocupada en el rango [{} - {}]",
                        booking.getRoomNumber(), booking.getHotelId(), booking.getStartDate(), booking.getEndDate());
                return false;
            }
            if (guestData.findById(booking.getGuestId()) == null ||
                    roomData.findById(booking.getRoomNumber()) == null ||
                    frontDeskClerkData.findById(booking.getFrontDeskClerkId()) == null) {
                logger.error("Algún ID relacionado con la reserva no existe.");
                return false;
            }


            bookingData.insert(booking);
            logger.info("Reserva agregada: {}", booking);
            return true;
        } catch (IOException e) {
            logger.error("Error al agregar reserva: {}", e.getMessage());
            return false;
        }

    }


    public List<Booking> getAllBookings() {
        try {
            List<Booking> bookings = bookingData.findAll();
            logger.info("Obtenidas {} reservas del sistema.", bookings.size());
            return bookings;
        } catch (IOException e) {
            logger.error("Error al obtener reservas: {}", e.getMessage());
            throw new RuntimeException("Error al obtener reservas", e);
        }
    }

    public boolean updateBooking(Booking booking) {
        try {

            boolean updated = bookingData.update(booking);
            if (updated) {
                logger.info("Reserva actualizada: {}", booking);
            } else {
                logger.warn("No se encontró reserva con número {} en el hotel {} para actualizar.", booking.getBookingNumber(), booking.getHotelId());
            }
            return updated;
        } catch (IOException e) {
            logger.error("Error al actualizar reserva: {}", e.getMessage());
            return false;
        }
    }


    public boolean deleteBooking(int bookingNumber, int hotelId) {
        try {
            boolean deleted = bookingData.delete(bookingNumber, hotelId);
            if (deleted) {
                logger.info("Reserva eliminada con número: {} del hotel {}.", bookingNumber, hotelId);
            } else {
                logger.warn("No se encontró reserva para eliminar con número: {} en el hotel {}.", bookingNumber, hotelId);
            }
            return deleted;
        } catch (IOException e) {
            logger.error("Error al eliminar reserva: {}", e.getMessage());
            return false;
        }
    }


    public Booking getBookingById(int bookingNumber, int hotelId) {
        try {
            Booking foundBooking = bookingData.findById(bookingNumber, hotelId);
            if (foundBooking != null) {
                logger.info("Reserva encontrada por ID: {} en hotel {}.", bookingNumber, hotelId);
            } else {
                logger.warn("No se encontró reserva con número: {} en hotel {}.", bookingNumber, hotelId);
            }
            return foundBooking;
        } catch (IOException e) {
            logger.error("Error al obtener reserva por ID: {}", e.getMessage());
            throw new RuntimeException("Error al obtener reserva por ID", e);
        }
    }


    public List<Booking> getBookingsByGuestId(int guestId) {
        try {
            return bookingData.findAll().stream()
                    .filter(booking -> booking.getGuestId() == guestId)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            logger.error("Error al obtener reservas por ID de huésped {}: {}", guestId, e.getMessage());
            throw new RuntimeException("Error al obtener reservas por ID de huésped", e);
        }
    }


    public List<Booking> getBookingsByRoomNumber(int roomNumber) {
        try {
            return bookingData.findAll().stream()
                    .filter(booking -> booking.getRoomNumber() == roomNumber)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            logger.error("Error al obtener reservas por número de habitación {}: {}", roomNumber, e.getMessage());
            throw new RuntimeException("Error al obtener reservas por número de habitación", e);
        }
    }


    public List<Booking> getBookingsByHotelId(int hotelId) {
        try {
            return bookingData.findAll().stream()
                    .filter(booking -> booking.getHotelId() == hotelId)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            logger.error("Error al obtener reservas por ID de hotel {}: {}", hotelId, e.getMessage());
            throw new RuntimeException("Error al obtener reservas por ID de hotel", e);
        }
    }

    public boolean hasConflictingBooking(int roomNumber, int hotelId, Date newStartDate, Date newEndDate) throws IOException {
        if (newStartDate == null || newEndDate == null || newStartDate.after(newEndDate)) {
            logger.warn("Fechas de nueva reserva inválidas: StartDate={}, EndDate={}", newStartDate, newEndDate);
            return true;
        }

        List<Booking> existingBookings = bookingData.findAll();

        for (Booking existingBooking : existingBookings) {

            if (existingBooking.getRoomNumber() == roomNumber && existingBooking.getHotelId() == hotelId) {
                Date existingStartDate = existingBooking.getStartDate();
                Date existingEndDate = existingBooking.getEndDate();

                if (existingStartDate == null || existingEndDate == null) {
                    logger.warn("Reserva existente con fechas nulas encontrada para habitación {} en hotel {}: {}", roomNumber, hotelId, existingBooking.getBookingNumber());
                    continue;
                }

                boolean overlaps = newStartDate.before(existingEndDate) && existingStartDate.before(newEndDate);

                if (overlaps) {
                    logger.info("Conflicto de reserva detectado para habitación {} en hotel {}. Nueva reserva [{} - {}] se superpone con reserva existente [{} - {}].",
                            roomNumber, hotelId, newStartDate, newEndDate, existingStartDate, existingEndDate);
                    return true;
                }
            }
        }

        logger.info("No se encontraron conflictos para la habitación {} en el hotel {} en el rango [{} - {}].", roomNumber, hotelId, newStartDate, newEndDate);
        return false;
    }


    public void close() {
        try {
            bookingData.close();
            guestData.close();
            roomData.close();
            frontDeskClerkData.close();
        } catch (IOException e) {
            logger.error("Error al cerrar archivos de datos de reservas, huéspedes, habitaciones o recepcionistas: {}", e.getMessage());
        }
    }

    public List<Room> getAvailableRooms(int hotelId, Date startDate, Date endDate) throws IOException {
        logger.info("Buscando habitaciones en hotel {} entre {} y {}", hotelId, startDate, endDate);

        List<Room> allHotelRooms = roomService.getRoomsByHotelId(hotelId);
        if (allHotelRooms == null) {
            logger.warn("No se encontraron habitaciones para hotel {}", hotelId);
            return new ArrayList<>();
        }

        logger.info("Se encontraron {} habitaciones para hotel {}", allHotelRooms.size(), hotelId);

        List<Room> available = new ArrayList<>();

        for (Room room : allHotelRooms) {
            logger.debug("Revisando habitación {} con estado: {}", room.getRoomNumber(), room.getStatus());


            boolean hasConflict = hasConflictingBooking(room.getRoomNumber(), hotelId, startDate, endDate);
            if (!hasConflict) {
                available.add(room);
            }
        }

        logger.info("Habitaciones disponibles encontradas: {}", available.size());
        return available;
    }

}