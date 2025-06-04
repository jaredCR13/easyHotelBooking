package com.easyhotelbooking.hotelbookingsystem.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class Utility {

    public static void loadFullView(String fxmlFileName, Node anyNodeInScene) {
        try {
            FXMLLoader loader = new FXMLLoader(Utility.class.getResource("/" + fxmlFileName));
            Parent root = loader.load();

            Stage stage = (Stage) anyNodeInScene.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Hotel Booking");
        } catch (IOException e) {
            System.out.println("Error loading " + fxmlFileName);
            e.printStackTrace();
        }
    }

    public static void loadPage(String fxmlFileName, javafx.scene.layout.BorderPane bp) {
        try {
            FXMLLoader loader = new FXMLLoader(Utility.class.getResource("/" + fxmlFileName));
            Parent view = loader.load();
            bp.setCenter(view);
        } catch (IOException e) {
            System.out.println("Error loading center content: " + fxmlFileName);
            e.printStackTrace();
        }
    }

    public static <T> T loadPage2(String fxmlFileName, javafx.scene.layout.BorderPane bp) {
        try {
            FXMLLoader loader = new FXMLLoader(Utility.class.getResource("/" + fxmlFileName));
            Parent view = loader.load();
            bp.setTop(view);
            bp.setBottom(null);
            bp.setCenter(null);
            return loader.getController(); // Devuelve el controlador
        } catch (IOException e) {
            System.out.println("Error loading center content: " + fxmlFileName);
            e.printStackTrace();
            return null;
        }
    }

    public static LocalDate convertToLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}


