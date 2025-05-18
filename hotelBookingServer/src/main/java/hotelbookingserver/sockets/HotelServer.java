package hotelbookingserver.sockets;

import hotelbookingserver.service.HotelService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class HotelServer {
    private static final int PORT=5000;
    private ArrayList<Socket>clients=new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("Hotel server iniciado");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            while (true){

                Socket clientSocket= serverSocket.accept();
                System.out.println("Cliente conectado"+clientSocket.getInetAddress());
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
