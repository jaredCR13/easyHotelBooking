package com.easyhotelbooking.hotelbookingsystem;

import com.easyhotelbooking.hotelbookingsystem.controller.MainInterfaceController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/maininterface.fxml"));
        Parent root = fxmlLoader.load();
        MainInterfaceController mainController = fxmlLoader.getController();

        if (mainController != null) {
            mainController.setStage(stage);
        } else {
            System.err.println("Error: MainInterfaceController es null después de cargar el FXML. Revisa tu FXML.");
        }

        Scene scene = new Scene(root); // Usa la vista cargada como raíz de la escena
        stage.setTitle("Hotel booking");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

