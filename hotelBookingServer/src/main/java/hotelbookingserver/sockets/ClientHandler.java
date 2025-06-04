package hotelbookingserver.sockets;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import hotelbookingcommon.domain.Request;
import hotelbookingcommon.domain.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private static final Logger logger = LogManager.getLogger(ClientHandler.class);
    private final Socket socket;
    private final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .create();
    private final ProtocolHandler handler = new ProtocolHandler();

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true)
        ) {
            String line;
            while ((line = in.readLine()) != null) {
                logger.debug("Recibido del cliente {}: {}", socket.getInetAddress(), line);
                try {
                    Request req = gson.fromJson(line, Request.class);
                    Response res = handler.handle(req);
                    String resJson = gson.toJson(res);
                    out.println(resJson);
                    logger.debug("Enviado al cliente {}: {}", socket.getInetAddress(), resJson);
                } catch (JsonParseException e) {
                    Response errorResponse = new Response("ERROR", "Invalid JSON request", null);
                    String errorJson = gson.toJson(errorResponse);
                    out.println(errorJson);
                    logger.error("Error parsing JSON from client {}: {}", socket.getInetAddress(), e.getMessage());
                }
            }
        } catch (IOException e) {
            logger.error("Error con cliente {}: {}", socket.getInetAddress(), e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                logger.error("Error al cerrar el socket del cliente {}: {}", socket.getInetAddress(), e.getMessage());
            }
        }
    }

}
