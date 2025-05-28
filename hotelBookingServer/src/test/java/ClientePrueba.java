import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientePrueba {

    @Test
    public void test_cliente() {
        try (Socket socket = new Socket("10.59.56.133", 5000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            String jsonRequest = "{\"action\":\"getHotels\",\"data\":null}";
            out.println(jsonRequest);

            String response;
            response = in.readLine();
            System.out.println("Respuesta del servidor: " + response);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
