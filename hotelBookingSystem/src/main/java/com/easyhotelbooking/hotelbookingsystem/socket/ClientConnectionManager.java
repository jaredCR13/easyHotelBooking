package com.easyhotelbooking.hotelbookingsystem.socket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import hotelbookingcommon.domain.Request;
import hotelbookingcommon.domain.Response;
import java.io.*;
import java.net.Socket;
public class ClientConnectionManager {



    private static final String HOST = "192.168.50.130";
    private static final int PORT = 5000;
    private static final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .create();

    public static Response sendRequest(Request request) {

        try (Socket socket = new Socket(HOST, PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            out.println(gson.toJson(request));
            String responseLine = in.readLine();
            return gson.fromJson(responseLine, Response.class);

        } catch (IOException e) {
            e.printStackTrace();
            return new Response("error", "No connection to server", null);
        }

    }

}
