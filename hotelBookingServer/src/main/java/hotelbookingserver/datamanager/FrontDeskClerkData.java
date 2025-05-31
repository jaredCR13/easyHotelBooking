package hotelbookingserver.datamanager;

import hotelbookingcommon.domain.FrontDeskClerk;
import hotelbookingcommon.domain.FrontDeskClerkRole;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FrontDeskClerkData {
    private RandomAccessFile raf;

    private static final int EMPLOYEE_ID_SIZE = 20;
    private static final int NAME_SIZE = 30;
    private static final int LAST_NAME_SIZE = 30;
    private static final int PASSWORD_SIZE = 30;
    private static final int USER_SIZE = 30;
    private static final int PHONE_NUMBER_SIZE = 15;
    private static final int ROLE_SIZE = 20;         // Nuevo campo
    private static final int HOTEL_ID_SIZE = 4;      // int

    private static final int RECORD_SIZE = EMPLOYEE_ID_SIZE + NAME_SIZE + LAST_NAME_SIZE +
            PASSWORD_SIZE + USER_SIZE + PHONE_NUMBER_SIZE + ROLE_SIZE + HOTEL_ID_SIZE;

    public FrontDeskClerkData(File file) throws FileNotFoundException {
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

    public void insert(FrontDeskClerk clerk) throws IOException {
        raf.seek(raf.length());
        raf.write(toFixedBytes(clerk.getEmployeeId(), EMPLOYEE_ID_SIZE));
        raf.write(toFixedBytes(clerk.getName(), NAME_SIZE));
        raf.write(toFixedBytes(clerk.getLastName(), LAST_NAME_SIZE));
        raf.write(toFixedBytes(clerk.getPassword(), PASSWORD_SIZE));
        raf.write(toFixedBytes(clerk.getUser(), USER_SIZE));
        raf.write(toFixedBytes(clerk.getPhoneNumber(), PHONE_NUMBER_SIZE));
        raf.write(toFixedBytes(clerk.getFrontDeskClerkRole().name(), ROLE_SIZE)); // Nuevo
        raf.writeInt(clerk.getHotelId());
    }

    public List<FrontDeskClerk> findAll() throws IOException {
        List<FrontDeskClerk> clerks = new ArrayList<>();
        raf.seek(0);

        while (raf.getFilePointer() + RECORD_SIZE <= raf.length()) {
            String employeeId = readFixedString(EMPLOYEE_ID_SIZE);
            String name = readFixedString(NAME_SIZE);
            String lastName = readFixedString(LAST_NAME_SIZE);
            String password = readFixedString(PASSWORD_SIZE);
            String user = readFixedString(USER_SIZE);
            String phoneNumber = readFixedString(PHONE_NUMBER_SIZE);
            String roleStr = readFixedString(ROLE_SIZE); // Nuevo
            int hotelId = raf.readInt();

            FrontDeskClerkRole role = FrontDeskClerkRole.valueOf(roleStr);
            FrontDeskClerk clerk = new FrontDeskClerk(employeeId, name, lastName, password, user, phoneNumber, role, hotelId);
            clerks.add(clerk);
        }

        return clerks;
    }

    public FrontDeskClerk findById(String employeeIdToFind) throws IOException {
        raf.seek(0);

        while (raf.getFilePointer() + RECORD_SIZE <= raf.length()) {
            long recordStart = raf.getFilePointer();

            String employeeId = readFixedString(EMPLOYEE_ID_SIZE);
            String name = readFixedString(NAME_SIZE);
            String lastName = readFixedString(LAST_NAME_SIZE);
            String password = readFixedString(PASSWORD_SIZE);
            String user = readFixedString(USER_SIZE);
            String phoneNumber = readFixedString(PHONE_NUMBER_SIZE);
            String roleStr = readFixedString(ROLE_SIZE); // Nuevo
            int hotelId = raf.readInt();

            if (employeeId.equals(employeeIdToFind)) {
                FrontDeskClerkRole role = FrontDeskClerkRole.valueOf(roleStr);
                return new FrontDeskClerk(employeeId, name, lastName, password, user, phoneNumber, role, hotelId);
            }
        }

        return null;
    }

    public boolean update(FrontDeskClerk updatedClerk) throws IOException {
        List<FrontDeskClerk> clerks = findAll();
        boolean updated = false;

        for (int i = 0; i < clerks.size(); i++) {
            if (clerks.get(i).getEmployeeId().equals(updatedClerk.getEmployeeId())) {
                clerks.set(i, updatedClerk);
                updated = true;
                break;
            }
        }

        if (updated) {
            raf.setLength(0);
            for (FrontDeskClerk clerk : clerks) {
                insert(clerk);
            }
        }

        return updated;
    }

    public boolean delete(String employeeId) throws IOException {
        List<FrontDeskClerk> clerks = findAll();
        boolean removed = clerks.removeIf(c -> c.getEmployeeId().equals(employeeId));

        if (removed) {
            raf.setLength(0);
            for (FrontDeskClerk clerk : clerks) {
                insert(clerk);
            }
        }

        return removed;
    }

    public void close() throws IOException {
        if (raf != null) raf.close();
    }
}
