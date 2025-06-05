package hotelbookingserver.datamanager;

import com.google.gson.annotations.Expose;
import hotelbookingcommon.domain.Guest;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GuestData {

    private RandomAccessFile raf;

    // Tamaños fijos por campo
    private static final int ID_SIZE = 4; // int
    private static final int CREDENTIAL_SIZE = 4; // int
    private static final int NAME_SIZE = 50; // bytes
    private static final int LAST_NAME_SIZE = 50;
    private static final int ADDRESS_SIZE = 100;
    private static final int EMAIL_SIZE = 100;
    private static final int PHONE_NUMBER_SIZE = 20;
    private static final int COUNTRY_SIZE = 50;

    private static final int RECORD_SIZE = ID_SIZE + CREDENTIAL_SIZE +
            NAME_SIZE + LAST_NAME_SIZE + ADDRESS_SIZE +
            EMAIL_SIZE + PHONE_NUMBER_SIZE + COUNTRY_SIZE;

    public GuestData(File file) throws FileNotFoundException {
        raf = new RandomAccessFile(file, "rw");
    }

    private byte[] toFixedBytes(String data, int length) {
        byte[] bytes = new byte[length];
        byte[] temp = data != null ? data.getBytes() : new byte[0];
        for (int i = 0; i < length; i++) {
            bytes[i] = (byte) ((i < temp.length) ? temp[i] : ' ');
        }
        return bytes;
    }

    private String readFixedString(int length) throws IOException {
        byte[] data = new byte[length];
        raf.readFully(data);
        return new String(data).trim();
    }

    public void insert(Guest guest) throws IOException {
        raf.seek(raf.length());

        raf.writeInt(guest.getId());
        raf.writeInt(guest.getCredential());
        raf.write(toFixedBytes(guest.getName(), NAME_SIZE));
        raf.write(toFixedBytes(guest.getLastName(), LAST_NAME_SIZE));
        raf.write(toFixedBytes(guest.getAddress(), ADDRESS_SIZE));
        raf.write(toFixedBytes(guest.getEmail(), EMAIL_SIZE));
        raf.write(toFixedBytes(guest.getPhoneNumber(), PHONE_NUMBER_SIZE));
        raf.write(toFixedBytes(guest.getNativeCountry(), COUNTRY_SIZE));
    }

    public List<Guest> findAll() throws IOException {
        List<Guest> guests = new ArrayList<>();
        raf.seek(0);

        while (raf.getFilePointer() + RECORD_SIZE <= raf.length()) {
            int id = raf.readInt();
            int credential = raf.readInt();
            String name = readFixedString(NAME_SIZE);
            String lastName = readFixedString(LAST_NAME_SIZE);
            String address = readFixedString(ADDRESS_SIZE);
            String email = readFixedString(EMAIL_SIZE);
            String phone = readFixedString(PHONE_NUMBER_SIZE);
            String country = readFixedString(COUNTRY_SIZE);

            Guest guest = new Guest(id, credential, name, lastName, address, email, phone, country);
            guests.add(guest);
        }

        return guests;
    }

    public Guest findById(int guestId) throws IOException {
        raf.seek(0);

        while (raf.getFilePointer() + RECORD_SIZE <= raf.length()) {
            int id = raf.readInt();
            int credential = raf.readInt();
            String name = readFixedString(NAME_SIZE);
            String lastName = readFixedString(LAST_NAME_SIZE);
            String address = readFixedString(ADDRESS_SIZE);
            String email = readFixedString(EMAIL_SIZE);
            String phone = readFixedString(PHONE_NUMBER_SIZE);
            String country = readFixedString(COUNTRY_SIZE);

            if (id == guestId) {
                return new Guest(id, credential, name, lastName, address, email, phone, country);
            }
        }

        return null;
    }

    public boolean update(Guest updatedGuest) throws IOException {
        List<Guest> guests = findAll();
        boolean updated = false;

        for (int i = 0; i < guests.size(); i++) {
            if (guests.get(i).getId() == updatedGuest.getId()) {
                guests.set(i, updatedGuest);
                updated = true;
                break;
            }
        }

        if (updated) {
            raf.setLength(0);
            for (Guest guest : guests) {
                insert(guest);
            }
        }

        return updated;
    }

    public boolean delete(int guestId) throws IOException {
        List<Guest> guests = findAll();
        boolean removed = guests.removeIf(g -> g.getId() == guestId);

        if (removed) {
            raf.setLength(0);
            for (Guest guest : guests) {
                insert(guest);
            }
        }

        return removed;
    }

    public void close() throws IOException {
        if (raf != null) raf.close();
    }
}
