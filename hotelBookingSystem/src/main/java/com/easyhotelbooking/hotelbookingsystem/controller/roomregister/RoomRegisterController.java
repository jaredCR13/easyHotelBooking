package com.easyhotelbooking.hotelbookingsystem.controller.roomregister;


import com.easyhotelbooking.hotelbookingsystem.controller.MainInterfaceController;
import com.easyhotelbooking.hotelbookingsystem.socket.ClientConnectionManager;
import com.easyhotelbooking.hotelbookingsystem.util.Utility;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import hotelbookingcommon.domain.*;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;



import hotelbookingcommon.domain.*;

import javafx.scene.control.*;


import java.io.ByteArrayInputStream; // Necesitarás esta importación
import java.io.File;
import java.io.IOException;


public class RoomRegisterController {

    @FXML
    private TextArea descriptionTf;

    @FXML
    private ComboBox<Hotel> hotelComboBox;

    @FXML
    private TextField priceTf;

    @FXML
    private TextField roomNumberTf;

    @FXML
    private ComboBox<RoomStatus> statusCombo;

    @FXML
    private ComboBox<RoomStyle> styleCombo;

    @FXML
    private Button uploadButton;


    @FXML
    private FlowPane flowPane;

    private static final Logger logger = LogManager.getLogger(RoomRegisterController.class);

    private MainInterfaceController mainController;
    private RoomOptionsController roomOptionsController;
    private BorderPane parentBp;
    private final Gson gson = new Gson();
    private List<String> currentImagePaths = new ArrayList<>();

    private Stage primaryStage;

    @FXML
    public void initialize() {
        statusCombo.getItems().addAll(RoomStatus.values());
        styleCombo.getItems().addAll(RoomStyle.values());
        loadHotelsIntoComboBox();
    }

    public void setParentBp(BorderPane parentBp) {
        this.parentBp = parentBp;
    }

    public void setMainController(MainInterfaceController controller) {
        this.mainController = controller;
    }

    public void setRoomOptionsController(RoomOptionsController roomOptionsController) {
        this.roomOptionsController = roomOptionsController;
    }

    public void setStage(Stage stage) {
        this.primaryStage = stage;
    }

    @FXML
    private void onUploadImage() {
        if (currentImagePaths.size() >= 5) { // Limitar máximo 5 imágenes
            util.FXUtility.alertInfo("Límite de Imágenes", "Solo se permiten hasta 5 imágenes por habitación.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecciona imagen para la habitación");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );

        File selectedFile = fileChooser.showOpenDialog(primaryStage);
        if (selectedFile != null) {
            try {
                byte[] imageData = Files.readAllBytes(selectedFile.toPath());
                logger.info("Imagen local leída a {} bytes.", imageData.length);
                ImageUploadDTO uploadDto = new ImageUploadDTO(
                        0,
                        selectedFile.getName(),
                        imageData
                );

                Request request = new Request("uploadTempRoomImage", uploadDto); // Nueva acción para imágenes temporales
                Response response = ClientConnectionManager.sendRequest(request);

                if ("200".equalsIgnoreCase(response.getStatus()) && response.getData() != null) {

                    String tempImagePath = gson.fromJson(gson.toJson(response.getData()), String.class);
                    currentImagePaths.add(tempImagePath); // Añade la ruta temporal a la lista local
                    util.FXUtility.alertInfo("Éxito", "Imagen subida temporalmente al servidor.");
                    logger.info("Imagen temporal subida: {}", tempImagePath);
                    refreshImagesDisplay(); // Volver a cargar las imágenes
                } else {
                    logger.error("Error al subir la imagen temporal al servidor: {}", response.getMessage());
                    util.FXUtility.alert("Error", "No se pudo subir la imagen: " + response.getMessage());
                }

            } catch (IOException e) {
                logger.error("Error de E/S al leer la imagen o al enviarla al servidor: {}", e.getMessage(), e);
                util.FXUtility.alert("Error", "No se pudo procesar la imagen para subir: " + e.getMessage());
            } catch (Exception e) {
                logger.error("Error inesperado en onUploadImage: {}", e.getMessage(), e);
                util.FXUtility.alert("Error", "Ocurrió un error inesperado al subir la imagen.");
            }
        }
    }

    private void refreshImagesDisplay() {
        flowPane.getChildren().clear();

        if (currentImagePaths == null || currentImagePaths.isEmpty()) {
            logger.info("No hay imágenes para mostrar en esta habitación.");
            return;
        }

        for (String serverImagePath : new ArrayList<>(currentImagePaths)) {
            logger.info("Solicitando imagen al servidor para mostrar: {}", serverImagePath);
            Request request = new Request("downloadRoomImage", serverImagePath);
            Response response = ClientConnectionManager.sendRequest(request);

            if ("200".equalsIgnoreCase(response.getStatus()) && response.getData() != null) {
                try {
                    byte[] imageData;
                    // Manejo de la deserialización de bytes
                    if (response.getData() instanceof List) {
                        List<Double> doubleList = gson.fromJson(gson.toJson(response.getData()), new TypeToken<List<Double>>() {}.getType());
                        imageData = new byte[doubleList.size()];
                        for (int i = 0; i < doubleList.size(); i++) {
                            imageData[i] = doubleList.get(i).byteValue();
                        }
                    } else if (response.getData() instanceof byte[]) {
                        imageData = (byte[]) response.getData();
                    } else {
                        logger.warn("Tipo de dato inesperado para imagen del servidor: {}. Saltando imagen {}.", response.getData().getClass().getName(), serverImagePath);
                        continue;
                    }

                    Image img = new Image(new ByteArrayInputStream(imageData), 200, 150, true, true);
                    ImageView imageView = new ImageView(img);
                    imageView.setPreserveRatio(true);
                    imageView.setFitWidth(200);
                    imageView.setFitHeight(150);

                    //Lógica para eliminar la imagen
                    imageView.setOnMouseClicked(event -> {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Confirmar Eliminación");
                        alert.setHeaderText(null);
                        alert.setContentText("¿Estás seguro de que quieres eliminar esta imagen?");
                        if (this.primaryStage != null) {
                            alert.initOwner(this.primaryStage);
                        }

                        Optional<ButtonType> result = alert.showAndWait();
                        if (result.isPresent() && result.get() == ButtonType.OK) {


                            if (currentImagePaths.remove(serverImagePath)) {
                                util.FXUtility.alertInfo("Éxito", "Imagen eliminada de la lista.");
                                logger.info("Imagen eliminada de la lista local: {}", serverImagePath);
                                refreshImagesDisplay(); // Refrescar la UI
                            } else {
                                util.FXUtility.alert("Error", "No se pudo encontrar la imagen para eliminar.");
                            }
                        }
                    });

                    flowPane.getChildren().add(imageView);
                } catch (Exception e) {
                    logger.error("Error al procesar la imagen descargada del servidor: {}", serverImagePath, e);
                    util.FXUtility.alert("Error", "No se pudo cargar una imagen desde el servidor: " + serverImagePath + " - " + e.getMessage());
                }
            } else {
                logger.warn("No se pudo descargar la imagen del servidor: {}. Estado: {}, Mensaje: {}",
                        serverImagePath, response.getStatus(), response.getMessage());
                util.FXUtility.alert("Advertencia", "No se pudo descargar una imagen desde el servidor: " + serverImagePath + " - " + response.getMessage());
            }
        }
    }

    @FXML
    public void onSave(ActionEvent event) {
        try {
            String numberStr = roomNumberTf.getText();
            String priceStr = priceTf.getText();
            String description = descriptionTf.getText();
            Hotel selectedHotel = hotelComboBox.getSelectionModel().getSelectedItem();

            if (numberStr.isEmpty() || priceStr.isEmpty() || description.isEmpty() || selectedHotel == null) {
                util.FXUtility.alert("Error", "Por favor, complete todos los campos y seleccione un hotel.");
                return;
            }

            int number = Integer.parseInt(numberStr);
            double price = Double.parseDouble(priceStr);
            RoomStatus status = statusCombo.getValue();
            RoomStyle style = styleCombo.getValue();

            if (status == null || style == null) {
                util.FXUtility.alert("Error", "Seleccione estado y estilo de la habitación.");
                return;
            }

            Room room = new Room(number, price, description, status, style, new ArrayList<>(currentImagePaths));
            room.setHotelId(selectedHotel.getNumHotel());
            room.setHotel(selectedHotel);

            Request request = new Request("registerRoom", room); // Esta acción ya existía
            Response response = ClientConnectionManager.sendRequest(request);

            if ("201".equalsIgnoreCase(response.getStatus())) {
                util.FXUtility.alertInfo("Éxito", "Habitación registrada correctamente.");
                if (roomOptionsController != null) {
                    roomOptionsController.loadRoomsIntoRegister();
                }
                clearFields();
            } else if ("409".equalsIgnoreCase(response.getStatus())) {
                util.FXUtility.alert("Error", response.getMessage());
                logger.warn("Intento de registrar habitación duplicada: {}", number);
            } else {
                util.FXUtility.alert("Error", "Error al registrar habitación: " + response.getMessage());
                logger.error("Error del servidor al registrar habitación: Status: {}, Message: {}", response.getStatus(), response.getMessage());
            }

        } catch (NumberFormatException e) {
            util.FXUtility.alert("Error", "Número de habitación o precio inválido.");
            logger.error("Error de formato al registrar habitación: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Error al registrar habitación: {}", e.getMessage());
            util.FXUtility.alert("Error", "Error inesperado al registrar habitación: " + e.getMessage());
        }
    }

    @FXML
    public void onCancel(ActionEvent event) {
        RoomOptionsController controller = Utility.loadPage2("roominterface/roomoptions.fxml", parentBp);
        controller.setMainController(mainController);
        if (this.primaryStage != null) {
            controller.setStage(this.primaryStage);
        }
        roomOptionsController.loadRoomsIntoRegister();
        clearFields();
    }

    private void loadHotelsIntoComboBox() {
        Request request = new Request("getHotels", null);
        Response response = ClientConnectionManager.sendRequest(request);
        if ("200".equalsIgnoreCase(response.getStatus()) && response.getData() != null) {
            List<Hotel> hotels = new Gson().fromJson(new Gson().toJson(response.getData()), new TypeToken<List<Hotel>>() {}.getType());
            hotelComboBox.setItems(FXCollections.observableArrayList(hotels));
            hotelComboBox.setCellFactory(lv -> new ListCell<Hotel>() {
                @Override
                protected void updateItem(Hotel hotel, boolean empty) {
                    super.updateItem(hotel, empty);
                    setText(empty ? "" : hotel.getHotelName() + " (" + hotel.getNumHotel() + ")");
                }
            });
            hotelComboBox.setButtonCell(new ListCell<Hotel>() {
                @Override
                protected void updateItem(Hotel hotel, boolean empty) {
                    super.updateItem(hotel, empty);
                    setText(empty ? "Seleccione un Hotel" : hotel.getHotelName() + " (" + hotel.getNumHotel() + ")");
                }
            });
        } else {
            util.FXUtility.alert("Error", "No se pudieron cargar los hoteles para el ComboBox.");
            logger.error("Error al cargar hoteles para ComboBox: {}", response != null ? response.getMessage() : "Desconocido");
        }
    }

    private void clearFields(){
        hotelComboBox.getSelectionModel().clearSelection();
        statusCombo.getSelectionModel().clearSelection();
        styleCombo.getSelectionModel().clearSelection();
        roomNumberTf.clear();
        descriptionTf.clear();
        priceTf.clear();
        flowPane.getChildren().clear();
        currentImagePaths.clear(); // Limpia también la lista de rutas de imágenes
    }
}

