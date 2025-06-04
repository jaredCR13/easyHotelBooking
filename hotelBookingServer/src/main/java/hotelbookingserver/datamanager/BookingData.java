package hotelbookingserver.datamanager;

import hotelbookingcommon.domain.Booking;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BookingData {
    private RandomAccessFile raf;


    private static final int BOOKING_NUMBER_SIZE = 4;
    private static final int HOTEL_ID_SIZE = 4;
    private static final int GUEST_ID_SIZE = 4;
    private static final int START_DATE_SIZE = 8;
    private static final int END_DATE_SIZE = 8;
    private static final int FRONT_DESK_CLERK_ID_MAX_LENGTH = 20;
    private static final int FRONT_DESK_CLERK_ID_SIZE = FRONT_DESK_CLERK_ID_MAX_LENGTH * 2;
    private static final int DAYS_OF_STAY_SIZE = 4;
    private static final int ROOM_NUMBER_SIZE = 4;

    // Tamaño total del registro de Booking (Ahora incluye HOTEL_ID_SIZE)
    private static final int RECORD_SIZE = BOOKING_NUMBER_SIZE + HOTEL_ID_SIZE + GUEST_ID_SIZE + START_DATE_SIZE + END_DATE_SIZE +
            FRONT_DESK_CLERK_ID_SIZE + DAYS_OF_STAY_SIZE + ROOM_NUMBER_SIZE;


    public BookingData(File file) throws FileNotFoundException {
        raf = new RandomAccessFile(file, "rw");
    }


    private byte[] toFixedBytes(String data, int length) {
        byte[] bytes;
        try {
            byte[] temp = data != null ? data.getBytes("UTF-8") : new byte[0];
            bytes = new byte[length];
            System.arraycopy(temp, 0, bytes, 0, Math.min(temp.length, length));
            // Rellenar el resto con espacios si es necesario
            for (int i = temp.length; i < length; i++) {
                bytes[i] = ' ';
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 encoding not supported", e);
        }
        return bytes;
    }


    private String readFixedString(int length) throws IOException {
        byte[] data = new byte[length];
        raf.readFully(data);
        return new String(data, "UTF-8").trim();
    }


    private void writeBookingRecord(Booking booking) throws IOException {
        raf.writeInt(booking.getBookingNumber());
        raf.writeInt(booking.getHotelId()); // Escribe el hotelId
        raf.writeInt(booking.getGuestId());
        raf.writeLong(booking.getStartDate().getTime());
        raf.writeLong(booking.getEndDate().getTime());
        raf.write(toFixedBytes(booking.getFrontDeskClerkId(), FRONT_DESK_CLERK_ID_SIZE));
        raf.writeInt(booking.getDaysOfStay());
        raf.writeInt(booking.getRoomNumber());
    }


    private Booking readBookingRecord() throws IOException {
        int bookingNumber = raf.readInt();
        int hotelId = raf.readInt(); // Lee el hotelId
        int guestId = raf.readInt();
        Date startDate = new Date(raf.readLong());
        Date endDate = new Date(raf.readLong());
        String frontDeskClerkId = readFixedString(FRONT_DESK_CLERK_ID_SIZE);
        int daysOfStay = raf.readInt();
        int roomNumber = raf.readInt();

        return new Booking(bookingNumber, hotelId, guestId, startDate, endDate, frontDeskClerkId, daysOfStay, roomNumber);
    }


    public void insert(Booking booking) throws IOException {
        List<Booking> existingBookings = findAll();

        // Verificar unicidad por la clave compuesta (bookingNumber, hotelId)
        for (Booking b : existingBookings) {
            if (b.getBookingNumber() == booking.getBookingNumber() && b.getHotelId() == booking.getHotelId()) {
                System.out.println("Booking with number " + booking.getBookingNumber() + " for hotel " + booking.getHotelId() + " already exists. Not inserting.");
                return;
            }
        }

        existingBookings.add(booking);

        existingBookings.sort((b1, b2) -> {
            int numCompare = Integer.compare(b1.getBookingNumber(), b2.getBookingNumber());
            if (numCompare != 0) {
                return numCompare;
            }
            return Integer.compare(b1.getHotelId(), b2.getHotelId());
        });

        // Reconstruir el archivo (costoso para archivos grandes, pero simple para esta implementación)
        raf.setLength(0); // Vaciar el archivo
        for (Booking b : existingBookings) {
            writeBookingRecord(b);
        }
    }


    public List<Booking> findAll() throws IOException {
        List<Booking> bookings = new ArrayList<>();
        raf.seek(0); // Ir al inicio del archivo

        while (raf.getFilePointer() + RECORD_SIZE <= raf.length()) {
            bookings.add(readBookingRecord());
        }
        return bookings;
    }


    public Booking findById(int bookingNumber, int hotelId) throws IOException {
        raf.seek(0);

        while (raf.getFilePointer() + RECORD_SIZE <= raf.length()) {
            long currentRecordStart = raf.getFilePointer();
            int currentBookingNumber = raf.readInt();


            if (currentBookingNumber == bookingNumber) {
                int currentHotelId = raf.readInt();

                if (currentHotelId == hotelId) {

                    raf.seek(currentRecordStart);
                    return readBookingRecord();
                }
            }

            raf.seek(currentRecordStart + RECORD_SIZE);
        }
        return null; // No se encontró la reserva
    }


    public boolean update(Booking updatedBooking) throws IOException {
        List<Booking> bookings = findAll();
        boolean updated = false;

        for (int i = 0; i < bookings.size(); i++) {
            // Comparar por la clave compuesta (bookingNumber, hotelId)
            if (bookings.get(i).getBookingNumber() == updatedBooking.getBookingNumber() &&
                    bookings.get(i).getHotelId() == updatedBooking.getHotelId()) {
                bookings.set(i, updatedBooking);
                updated = true;
                break;
            }
        }

        if (updated) {
            // Reconstruir el archivo
            raf.setLength(0);
            for (Booking booking : bookings) {
                writeBookingRecord(booking);
            }
        }
        return updated;
    }


    public boolean delete(int bookingNumber, int hotelId) throws IOException {
        List<Booking> bookings = findAll();
        // Eliminar por la clave compuesta
        boolean removed = bookings.removeIf(b -> b.getBookingNumber() == bookingNumber && b.getHotelId() == hotelId);

        if (removed) {
            // Reconstruir el archivo
            raf.setLength(0);
            for (Booking booking : bookings) {
                writeBookingRecord(booking);
            }
        }
        return removed;
    }

    public void close() throws IOException {
        if (raf != null) raf.close();
    }
}