package com.easyhotelbooking.hotelbookingsystem;

import com.easyhotelbooking.hotelbookingsystem.controller.logincontroller.LogInController;
import com.easyhotelbooking.hotelbookingsystem.controller.maininterface.MainInterfaceController;
import com.easyhotelbooking.hotelbookingsystem.controller.search.SearchController;
import com.easyhotelbooking.hotelbookingsystem.socket.ClientConnectionManager;
import com.easyhotelbooking.hotelbookingsystem.util.FXUtility;
import hotelbookingcommon.domain.FrontDeskClerk;
import hotelbookingcommon.domain.FrontDeskClerkRole;
import hotelbookingcommon.domain.LogIn.FrontDeskClerkDTO;
import hotelbookingcommon.domain.Request;
import hotelbookingcommon.domain.Response;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;

import java.io.IOException;
// Importa las clases de logging
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class Main extends Application {

    private Stage primaryStage;
    private static final Logger logger = LogManager.getLogger(Main.class); // Agrega el logger


    @Override
    public void start(Stage stage) throws Exception {
        this.primaryStage = stage;
        this.primaryStage.setTitle("Easy Hotel Booking System - Login");

        try {
            FXMLLoader loginLoader = new FXMLLoader(Main.class.getResource("/logininterface/login.fxml"));
            Parent loginRoot = loginLoader.load();

            LogInController loginController = loginLoader.getController();

            if (loginController != null) {
                loginController.setMainApp(this);
                logger.info("Main.java: Referencia de Main pasada al LogInController.");
            } else {
                logger.error("Error: LogInController es null después de cargar el FXML de login. Revisa 'login.fxml'.");
                // Puedes mostrar una alerta al usuario aquí si es un error crítico
                FXUtility.alert("Error Fatal", "No se pudo cargar la pantalla de inicio de sesión. La aplicación se cerrará.");
                System.exit(1); // Sale de la aplicación si no se puede cargar la pantalla de login
            }

            Scene loginScene = new Scene(loginRoot);
            primaryStage.setScene(loginScene);
            primaryStage.setResizable(false);
            primaryStage.show();

        } catch (IOException e) {
            logger.fatal("Error crítico al iniciar la aplicación: No se pudo cargar el FXML de login.", e);
            FXUtility.alert("Error Fatal", "No se pudo iniciar la aplicación debido a un error de carga. " + e.getMessage());
            System.exit(1); // Sale de la aplicación
        }
    }

    /**
     * Carga y muestra la interfaz principal.
     */
    public void loadMainInterface(FrontDeskClerkDTO loggedInClerk) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/maininterface.fxml"));
            Parent root = fxmlLoader.load();
            MainInterfaceController mainController = fxmlLoader.getController();

            if (mainController != null) {
                // *** ¡ESTA ES LA LÍNEA CRÍTICA QUE FALTABA! ***
                mainController.setLoggedInClerk(loggedInClerk);
                logger.info("Main.java: DTO del recepcionista loggeado pasado al MainInterfaceController.");
                mainController.setMainApp(this);
                mainController.setStage(primaryStage); // Pasa la referencia del Stage
                logger.info("Main.java: Stage principal pasado al MainInterfaceController.");
            } else {
                logger.error("Error: MainInterfaceController es null después de cargar el FXML. Revisa tu FXML 'main-interface-view.fxml'.");
                FXUtility.alert("Error de Carga", "No se pudo cargar la interfaz principal. Revisa el archivo FXML.");
                return; // No intentar mostrar una escena si el controlador es null
            }

            Scene scene = new Scene(root);
            primaryStage.setTitle("Easy Hotel Booking System - Main Interface"); // Título más descriptivo
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();
            logger.info("Main.java: Interfaz principal cargada exitosamente.");

        } catch (IOException e) {
            logger.error("Error al cargar la interfaz principal: {}", e.getMessage(), e); // Usa {} para parámetros
            FXUtility.alert("Error de Carga", "No se pudo cargar la interfaz principal. Por favor, reinicie la aplicación.");
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}