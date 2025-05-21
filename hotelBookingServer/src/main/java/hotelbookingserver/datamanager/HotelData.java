package hotelbookingserver.datamanager;

import hotelbookingcommon.domain.Hotel;

import java.io.*;
import java.util.ArrayList;

public class HotelData {
    private static final int TAMANO_REGISTRO = 84;
    private static final int TAMANO_NOMBRE = 30;
    private static final int TAMANO_UBICACION = 50;

    private RandomAccessFile raf;

    public HotelData(File file) throws FileNotFoundException {
        raf = new RandomAccessFile(file, "rw");
    }

    private byte[] toFixedBytes(String dato, int tamano) {
        byte[] datos = new byte[tamano];
        byte[] temp = dato.getBytes();
        for (int i = 0; i < tamano; i++) {
            datos[i] = (byte) ((i < temp.length) ? temp[i] : ' ');
        }
        return datos;
    }

    private String readFixedString(int tamano) throws IOException {
        byte[] datos = new byte[tamano];
        raf.readFully(datos);
        return new String(datos).trim();
    }

    public void insert(Hotel hotel) throws IOException {
        raf.seek(raf.length());
        raf.writeInt(hotel.getNumHotel());
        raf.write(toFixedBytes(hotel.getHotelName(), TAMANO_NOMBRE));
        raf.write(toFixedBytes(hotel.getHotelLocation(), TAMANO_UBICACION));
    }

    public ArrayList<Hotel> findAll() throws IOException {
        ArrayList<Hotel> hoteles = new ArrayList<>();
        long totalRegistros = raf.length() / TAMANO_REGISTRO;

        for (int i = 0; i < totalRegistros; i++) {
            raf.seek(i * TAMANO_REGISTRO);
            int id = raf.readInt();
            String nombre = readFixedString(TAMANO_NOMBRE);
            String ubicacion = readFixedString(TAMANO_UBICACION);
            hoteles.add(new Hotel(id, nombre, ubicacion));
        }
        return hoteles;
    }

    public Hotel findById(int idBuscado) throws IOException {
        long totalRegistros = raf.length() / TAMANO_REGISTRO;

        for (int i = 0; i < totalRegistros; i++) {
            raf.seek(i * TAMANO_REGISTRO);
            int idActual = raf.readInt();
            if (idActual == idBuscado) {
                String nombre = readFixedString(TAMANO_NOMBRE);
                String ubicacion = readFixedString(TAMANO_UBICACION);
                return new Hotel(idActual, nombre, ubicacion);
            } else {
                raf.skipBytes(TAMANO_NOMBRE + TAMANO_UBICACION);
            }
        }
        return null;
    }

    public boolean update(Hotel hotelToUpdate) throws IOException {
        ArrayList<Hotel> hoteles = findAll();
        boolean found = false;

        for (int i = 0; i < hoteles.size(); i++) {
            if (hoteles.get(i).getNumHotel() == hotelToUpdate.getNumHotel()) {
                hoteles.set(i, hotelToUpdate);
                found = true;
                break;
            }
        }

        if (found) {
            raf.setLength(0); // Borra todo el archivo
            for (Hotel h : hoteles) {
                insert(h);     // Vuelve a escribir cada hotel
            }
        }

        return found;
    }

    public boolean delete(int hotelId) throws IOException {
        ArrayList<Hotel> hoteles = findAll();
        boolean removed = hoteles.removeIf(h -> h.getNumHotel() == hotelId);

        if (removed) {
            raf.setLength(0); // Borra todo el archivo
            for (Hotel h : hoteles) {
                insert(h);
            }
        }

        return removed;
    }


    public void close() throws IOException {
        if (raf != null) raf.close();
    }
}
