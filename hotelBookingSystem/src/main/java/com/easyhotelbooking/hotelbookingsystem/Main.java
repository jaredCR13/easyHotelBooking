package com.easyhotelbooking.hotelbookingsystem;

import com.easyhotelbooking.hotelbookingsystem.controller.logincontroller.LogInController;
import com.easyhotelbooking.hotelbookingsystem.controller.maininterface.MainInterfaceController;
import com.easyhotelbooking.hotelbookingsystem.util.FXUtility;
import hotelbookingcommon.domain.LogIn.FrontDeskClerkDTO;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class Main extends Application {

    private Stage primaryStage;
    private static final Logger logger = LogManager.getLogger(Main.class);


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


                logger.info("Main.java: Referencia de Main y Stage pasadas al LogInController en start().");
            } else {
                logger.error("Error: LogInController es null después de cargar el FXML de login. Revisa 'login.fxml'.");
                FXUtility.alert("Error Fatal", "No se pudo cargar la pantalla de inicio de sesión. La aplicación se cerrará.");
                System.exit(1);
            }

            Scene loginScene = new Scene(loginRoot);
            primaryStage.setScene(loginScene);
            primaryStage.setResizable(false);
            primaryStage.show();

        } catch (IOException e) {
            logger.fatal("Error crítico al iniciar la aplicación: No se pudo cargar el FXML de login.", e);
            FXUtility.alert("Error Fatal", "No se pudo iniciar la aplicación debido a un error de carga. " + e.getMessage());
            System.exit(1);
        }
    }


    public void loadMainInterface(FrontDeskClerkDTO loggedInClerk) {
        try {

            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/maininterface.fxml"));
            Parent root = fxmlLoader.load();
            MainInterfaceController mainController = fxmlLoader.getController();

            if (mainController != null) {
                mainController.setLoggedInClerk(loggedInClerk);
                logger.info("Main.java: DTO del recepcionista logueado pasado al MainInterfaceController.");
                mainController.setMainApp(this);


                mainController.setStage(primaryStage);
                logger.info("Main.java: Stage principal pasado al MainInterfaceController.");
            } else {
                logger.error("Error: MainInterfaceController es null después de cargar el FXML. Revisa tu FXML '/maininterface/maininterface.fxml'.");
                FXUtility.alert("Error de Carga", "No se pudo cargar la interfaz principal. Revisa el archivo FXML.");
                return;
            }

            Scene scene = new Scene(root);
            primaryStage.setTitle("Easy Hotel Booking System - Main Interface");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();

            logger.info("Main.java: Interfaz principal cargada exitosamente en el primaryStage.");

        } catch (IOException e) {
            logger.error("Error al cargar la interfaz principal: {}", e.getMessage(), e);
            FXUtility.alert("Error de Carga", "No se pudo cargar la interfaz principal. Por favor, reinicie la aplicación.");
        }
    }


    public void showLoginScreenAfterLogout(Stage currentMainAppStage) {
        try {

            if (currentMainAppStage != null) {
                currentMainAppStage.close();
                logger.info("Main.java: Stage de la interfaz principal cerrado después de logout.");
            }


            FXMLLoader loginLoader = new FXMLLoader(Main.class.getResource("/logininterface/login.fxml"));
            Parent loginRoot = loginLoader.load();

            LogInController loginController = loginLoader.getController();

            if (loginController != null) {
                loginController.setMainApp(this);

                logger.info("Main.java: Referencia de Main y Stage pasadas al LogInController al volver del logout.");
            } else {
                logger.error("Error: LogInController es null al volver al login después de logout. Revisa 'login.fxml'.");
                FXUtility.alert("Error", "No se pudo recargar la pantalla de inicio de sesión.");
            }


            Scene loginScene = new Scene(loginRoot);
            primaryStage.setScene(loginScene);
            primaryStage.setTitle("Easy Hotel Booking System - Login");
            primaryStage.setResizable(false);
            primaryStage.show();
            logger.info("Main.java: Pantalla de login mostrada de nuevo en el primaryStage.");

        } catch (IOException e) {
            logger.error("Error al recargar la pantalla de login después de logout: {}", e.getMessage(), e);
            FXUtility.alert("Error", "No se pudo volver a la pantalla de inicio de sesión.");
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}