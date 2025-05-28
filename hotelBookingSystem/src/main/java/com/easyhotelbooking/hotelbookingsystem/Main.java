package com.easyhotelbooking.hotelbookingsystem;

import com.easyhotelbooking.hotelbookingsystem.controller.MainInterfaceController; // ¡Importa tu controlador!
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent; // No siempre necesario si usas el tipo específico como BorderPane

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/maininterface.fxml"));
        Parent root = fxmlLoader.load();
        MainInterfaceController mainController = fxmlLoader.getController();

        // **** ¡AÑADE ESTA LÍNEA CLAVE! ****
        // Pasa el Stage principal al MainInterfaceController
        if (mainController != null) {
            mainController.setStage(stage); // Asegúrate de que MainInterfaceController tenga un método setStage(Stage stage)
        } else {
            // Este log de error te ayudará a depurar si algo va mal al cargar el FXML
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

