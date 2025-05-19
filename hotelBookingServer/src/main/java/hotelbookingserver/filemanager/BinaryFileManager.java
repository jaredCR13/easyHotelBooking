package hotelbookingserver.filemanager;

import java.io.*;
import java.util.List;

public class BinaryFileManager {
    public static void writeData(String filename, List<? extends Serializable> data) {
        File file = new File(filename);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(data);
        } catch (IOException e) {
            throw new RuntimeException("Error writing to file: " + filename, e); // Mejor manejo de excepciones
        }
    }

    public static List<? extends Serializable> readData(String filename) {
        File file = new File(filename);
        if (!file.exists()) {
            return List.of(); // Devuelve una lista vacía si el archivo no existe
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            @SuppressWarnings("unchecked") // Suprimir la advertencia de tipo
            List<? extends Serializable> data = (List<? extends Serializable>) ois.readObject();
            return data;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Error reading from file: " + filename, e); //  manejo de excepciones
        }
    }
}
