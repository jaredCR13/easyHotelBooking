package hotelbookingserver.sockets;

import java.io.*;
import java.net.Socket;

public class ClientePrueba {
    public static void main(String[] args) {
        try (Socket socket = new Socket("192.168.18.61", 5000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            String jsonRequest = "{\"action\":\"getHotels\",\"data\":null}";
            out.println(jsonRequest);

            String response = in.readLine();
            System.out.println("Respuesta del servidor: " + response);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

