/*package hotelbookingserver.service;

import hotelbookingcommon.domain.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class BookingServiceTest {

    @Test
    void addBooking() {

        // Arrange
        Guest guest = new Guest(2, 23, "Jared", "Morales Morales", "Cartago", "@jmorales", "83934394", "Costa Rica");

        //Convertir LocalDate a Date
        Date startDate = new GregorianCalendar(2025, Calendar.JANUARY, 1).getTime();
        Date endDate = new GregorianCalendar(2025, Calendar.JANUARY, 2).getTime();

        FrontDeskClerk frontDeskClerk = new FrontDeskClerk("1", "Kristel", "Picado", "123", "kp", "9394343", FrontDeskClerkRole.FRONTDESKCLERK,1);
        Room room = new Room(23, 900, "Bonita", RoomStatus.AVAILABLE, RoomStyle.DELUXE, Collections.emptyList(), 1);
        Booking booking = new Booking(1, guest.getId(), startDate, endDate, frontDeskClerk.getEmployeeId(), 5, room.getRoomNumber());

        GuestService guestService = new GuestService();
        guestService.addGuest(guest);
        FrontDeskClerkService frontDeskClerkService=  new FrontDeskClerkService();
        frontDeskClerkService.addClerk(frontDeskClerk);
        RoomService roomService = new RoomService();
        roomService.addRoom(room);

        BookingService bookingService = new BookingService();

        // Act
        bookingService.addBooking(booking);

        // Assert
        Booking resultadoEsperado = bookingService.getBookingById(1);
        assertNotNull(resultadoEsperado, "No deberia ser nulo");
        assertEquals(booking.getBookingNumber(), resultadoEsperado.getBookingNumber(), " ids de reservacion deberian coincidir");


    }
}*/