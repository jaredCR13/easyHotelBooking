package hotelbookingserver.datamanager;

import hotelbookingcommon.domain.Room;
import hotelbookingcommon.domain.RoomStatus;
import hotelbookingcommon.domain.RoomStyle;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class RoomData {
    private RandomAccessFile raf;

    private static final int ROOM_NUMBER_SIZE = 4; // int
    private static final int ROOM_PRICE_SIZE = 8;  // double
    private static final int DESCRIPTION_SIZE = 100; // bytes
    private static final int STATUS_SIZE = 20; // enum name string
    private static final int STYLE_SIZE = 20;  // enum name string
    private static final int HOTEL_ID_SIZE = 4; // int - hotel ID

    private static final int MAX_IMAGES = 5;
    private static final int IMAGE_PATH_SIZE = 100; // bytes per image path
    private static final int IMAGES_SIZE = MAX_IMAGES * IMAGE_PATH_SIZE;

    // Nuevo tamaño total del registro
    private static final int RECORD_SIZE = ROOM_NUMBER_SIZE + ROOM_PRICE_SIZE + DESCRIPTION_SIZE + STATUS_SIZE + STYLE_SIZE + HOTEL_ID_SIZE + IMAGES_SIZE;

    public RoomData(File file) throws FileNotFoundException {
        raf = new RandomAccessFile(file, "rw");
    }

    // Convierte String a bytes fijos de longitud length rellenando con espacios
    private byte[] toFixedBytes(String data, int length) {
        byte[] bytes = new byte[length];
        byte[] temp = data != null ? data.getBytes() : new byte[0];
        for (int i = 0; i < length; i++) {
            bytes[i] = (byte) ((i < temp.length) ? temp[i] : ' ');
        }
        return bytes;
    }

    // Lee String de longitud fija, recorta espacios al final
    private String readFixedString(int length) throws IOException {
        byte[] data = new byte[length];
        raf.readFully(data);
        return new String(data).trim();
    }

    public void insert(Room room) throws IOException {
        raf.seek(raf.length());
        raf.writeInt(room.getRoomNumber());
        raf.writeDouble(room.getRoomPrice());
        raf.write(toFixedBytes(room.getDetailedDescription(), DESCRIPTION_SIZE));
        raf.write(toFixedBytes(room.getStatus().name(), STATUS_SIZE));
        raf.write(toFixedBytes(room.getStyle().name(), STYLE_SIZE));
        raf.writeInt(room.getHotelId());

        List<String> images = room.getImagesPaths();
        for (int i = 0; i < MAX_IMAGES; i++) {
            if (i < images.size()) {
                raf.write(toFixedBytes(images.get(i), IMAGE_PATH_SIZE));
            } else {
                raf.write(toFixedBytes("", IMAGE_PATH_SIZE));
            }
        }
    }

    public List<Room> findAll() throws IOException {
        List<Room> rooms = new ArrayList<>();
        raf.seek(0);

        while (raf.getFilePointer() + RECORD_SIZE <= raf.length()) {
            int number = raf.readInt();
            double price = raf.readDouble();
            String desc = readFixedString(DESCRIPTION_SIZE);
            String status = readFixedString(STATUS_SIZE);
            String style = readFixedString(STYLE_SIZE);
            int hotelId = raf.readInt();

            List<String> images = new ArrayList<>();
            for (int i = 0; i < MAX_IMAGES; i++) {
                String imagePath = readFixedString(IMAGE_PATH_SIZE);
                if (!imagePath.isEmpty()) {
                    images.add(imagePath);
                }
            }

            Room room = new Room(number, price, desc,
                    RoomStatus.valueOf(status),
                    RoomStyle.valueOf(style),
                    images,
                    hotelId);
            rooms.add(room);
        }

        return rooms;
    }

    public Room findById(int roomNumber) throws IOException {
        raf.seek(0);

        while (raf.getFilePointer() + RECORD_SIZE <= raf.length()) {
            int currentNumber = raf.readInt();
            double price = raf.readDouble();
            String desc = readFixedString(DESCRIPTION_SIZE);
            String status = readFixedString(STATUS_SIZE);
            String style = readFixedString(STYLE_SIZE);
            int hotelId = raf.readInt();

            List<String> images = new ArrayList<>();
            for (int i = 0; i < MAX_IMAGES; i++) {
                String imagePath = readFixedString(IMAGE_PATH_SIZE);
                if (!imagePath.isEmpty()) {
                    images.add(imagePath);
                }
            }

            if (currentNumber == roomNumber) {
                return new Room(currentNumber, price, desc,
                        RoomStatus.valueOf(status),
                        RoomStyle.valueOf(style),
                        images,
                        hotelId);
            }
        }

        return null;
    }

    public boolean update(Room updatedRoom) throws IOException {
        List<Room> rooms = findAll();
        boolean updated = false;

        for (int i = 0; i < rooms.size(); i++) {
            if (rooms.get(i).getRoomNumber() == updatedRoom.getRoomNumber()) {
                rooms.set(i, updatedRoom);
                updated = true;
                break;
            }
        }

        if (updated) {
            raf.setLength(0); // Borra todo el archivo
            for (Room room : rooms) {
                insert(room);
            }
        }

        return updated;
    }

    public boolean delete(int roomNumber) throws IOException {
        List<Room> rooms = findAll();
        boolean removed = rooms.removeIf(r -> r.getRoomNumber() == roomNumber);

        if (removed) {
            raf.setLength(0); // Borra todo el archivo
            for (Room room : rooms) {
                insert(room);
            }
        }

        return removed;
    }

    public void close() throws IOException {
        if (raf != null) raf.close();
    }
}
