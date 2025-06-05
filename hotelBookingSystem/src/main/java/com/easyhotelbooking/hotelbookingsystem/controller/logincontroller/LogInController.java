package com.easyhotelbooking.hotelbookingsystem.controller.logincontroller;

import com.easyhotelbooking.hotelbookingsystem.Main;
import com.easyhotelbooking.hotelbookingsystem.socket.ClientConnectionManager;
import com.easyhotelbooking.hotelbookingsystem.util.FXUtility;
import com.google.gson.Gson;
import hotelbookingcommon.domain.LogIn.FrontDeskClerkDTO;
import hotelbookingcommon.domain.LogIn.LoginRequestDTO; // <-- Make sure this is imported
import hotelbookingcommon.domain.Request;
import hotelbookingcommon.domain.Response;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LogInController {

    @FXML private TextField userTextField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;

    private static final Logger logger = LogManager.getLogger(LogInController.class);
    private Gson gson = new Gson();

    private Main mainAppReference;

    public void setMainApp(Main mainAppReference) {
        this.mainAppReference = mainAppReference;
    }


    @FXML
    public void logInOnAction() {
        String username = userTextField.getText();
        String password = passwordField.getText(); // This is the plain-text password from the user

        if (username.isEmpty() || password.isEmpty()) {
            FXUtility.alert("Error de Login", "Por favor, introduce usuario y contraseña.");
            return;
        }
        LoginRequestDTO loginCredentials = new LoginRequestDTO(username, password);

        Request request = new Request("login", loginCredentials);

        Response response = ClientConnectionManager.sendRequest(request);

        if ("200".equalsIgnoreCase(response.getStatus()) && response.getData() != null) {
            FrontDeskClerkDTO loggedInClerk = gson.fromJson(gson.toJson(response.getData()), FrontDeskClerkDTO.class);
            logger.info("Login exitoso para usuario: {}", loggedInClerk.getUser());
            FXUtility.alertInfo("Login Exitoso", "¡Bienvenido, " + loggedInClerk.getName() + "!");

            if (mainAppReference != null) {
                mainAppReference.loadMainInterface(loggedInClerk);
            } else {
                logger.error("Referencia a la aplicación principal (Main) no establecida en LogInController.");
                FXUtility.alert("Error Fatal", "La aplicación no pudo cargar la interfaz principal debido a un error interno.");
            }
        } else {
            String message = response != null ? response.getMessage() : "Credenciales inválidas o error de conexión.";
            logger.warn("Login fallido para usuario {}: {}", username, message);
            FXUtility.alert("Error de Login", message);
        }
    }
}