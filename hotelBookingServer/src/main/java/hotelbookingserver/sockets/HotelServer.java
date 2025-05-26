package hotelbookingserver.sockets;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HotelServer {
    private static final int PORT = 5000;
    private static final Logger logger = LogManager.getLogger(HotelServer.class);
    private ExecutorService threadPool;
    private ServerSocket serverSocket;


    public HotelServer() {
        this.threadPool = Executors.newFixedThreadPool(10); // Use a thread pool
    }


    public void start() {
        try {
            //serverSocket = new ServerSocket(PORT, 50, InetAddress.getByName("0.0.0.0"));
            serverSocket = new ServerSocket(PORT);
            logger.info("Servidor iniciado en el puerto {}", PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                logger.info("Cliente conectado desde {}", clientSocket.getInetAddress());
                threadPool.execute(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            logger.error("Error al iniciar el servidor: {}", e.getMessage());
            // Handle the error appropriately, maybe try to restart or shutdown
            throw new RuntimeException("Error starting server", e); // Add this
        } finally {
            stop();
        }
    }

    public void stop() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
            threadPool.shutdown();
            logger.info("Servidor detenido");
        } catch (IOException e) {
            logger.error("Error al detener el servidor: {}", e.getMessage());
        }
    }

    public static void main(String[] args) {
        new HotelServer().start();
    }
}
