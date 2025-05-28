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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    private  List<String>imagePaths;

    @FXML
    private FlowPane flowPane;

    private static final Logger logger = LogManager.getLogger(RoomRegisterController.class);


    private MainInterfaceController mainController;
    private RoomOptionsController roomOptionsController;
    private BorderPane parentBp;
    // AÑADE TU PROPIA INSTANCIA DE GSON
    private final Gson gson = new Gson();

    // AÑADE ESTA LISTA DE RUTAS DE IMAGEN (VACÍA AL INICIO)
    private List<String> currentImagePaths = new ArrayList<>(); // Nueva lista para almacenar las rutas relativas del servidor

    @FXML
    public void initialize(){

        statusCombo.getItems().addAll(RoomStatus.values());
        styleCombo.getItems().addAll(RoomStyle.values());

        // Cargar los hoteles disponibles en el ComboBox al inicializar
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
    // Necesitas referencia al Stage para el FileChooser
    private Stage primaryStage;

    public void setStage(Stage stage) {
        this.primaryStage = stage;
    }

    @FXML
    private void onUploadImage() {
        // Usa 'currentImagePaths' en lugar de 'imagePaths'
        if (currentImagePaths.size() >= 5) { // Limitar máximo 5 imágenes
            util.FXUtility.alertInfo("Límite de Imágenes", "Solo se permiten hasta 5 imágenes por habitación.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecciona imagen para la habitación");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );

        File selectedFile = fileChooser.showOpenDialog(primaryStage); // Usar primaryStage
        if (selectedFile != null) {
            try {
                // 1. CONVERSIÓN CLAVE: Leer el archivo de imagen local a un arreglo de bytes
                byte[] imageData = Files.readAllBytes(selectedFile.toPath());
                logger.info("Imagen local leída a {} bytes.", imageData.length);

                // ** IMPORTANTE: Antes de subir una imagen, la habitación debe tener un número.
                // En un registro de habitación, el número es típicamente asignado al guardar,
                // no al subir imágenes. Hay dos enfoques aquí:
                // A) Subir la imagen *después* de que la habitación sea registrada (más simple, pero menos flexible UI).
                // B) Subir la imagen y asociarla a un ID TEMPORAL, o asociarla al nombre de la habitación,
                //    y luego en el servidor, cuando se guarde la habitación, reasignar las imágenes.
                //
                // Para simplificar, asumiremos que el roomNumberTf.getText() ya tiene un número
                // válido que el usuario introdujo. Si no, necesitarías un flujo diferente
                // (ej. guardar la habitación primero, luego permitir la subida de imágenes).
                // Dado que ya tienes un campo roomNumberTf, lo usaremos.

                int roomNumber;
                try {
                    roomNumber = Integer.parseInt(roomNumberTf.getText());
                } catch (NumberFormatException e) {
                    util.FXUtility.alert("Error", "Por favor, introduce un número de habitación válido antes de subir imágenes.");
                    logger.warn("Intento de subir imagen sin número de habitación válido.");
                    return;
                }


                // 2. Crear el DTO para enviar al servidor
                ImageUploadDTO uploadDto = new ImageUploadDTO(
                        roomNumber,                   // Número de la habitación actual (obtenido del campo)
                        selectedFile.getName(),       // Nombre original del archivo (para la extensión)
                        imageData                     // ¡Los bytes de la imagen!
                );

                // 3. Enviar la Request al servidor para subir la imagen
                Request request = new Request("uploadRoomImage", uploadDto); // Acción: "uploadRoomImage"
                Response response = ClientConnectionManager.sendRequest(request);

                if ("200".equalsIgnoreCase(response.getStatus()) && response.getData() != null) {
                    util.FXUtility.alertInfo("Éxito", "Imagen subida exitosamente al servidor.");
                    logger.info("Respuesta del servidor al subir imagen: {}", response.getMessage());

                    // El servidor debería devolver la habitación actualizada con la nueva ruta de imagen
                    // Deserializamos la habitación actualizada que viene en el campo 'data' de la respuesta
                    Room updatedRoom = gson.fromJson(gson.toJson(response.getData()), Room.class);
                    // Actualiza la lista local de paths con los paths de la habitación que el servidor acaba de actualizar
                    currentImagePaths = new ArrayList<>(updatedRoom.getImagesPaths());

                    logger.info("Habitación actualizada con los nuevos paths de imagen.");
                    refreshImagesDisplay(); // Volver a cargar las imágenes (ahora se pedirán del servidor)
                } else {
                    logger.error("Error al subir la imagen al servidor: {}", response.getMessage());
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

        // Usa 'currentImagePaths'
        if (currentImagePaths == null || currentImagePaths.isEmpty()) {
            logger.info("No hay imágenes para mostrar en esta habitación.");
            return;
        }

        for (String serverImagePath : new ArrayList<>(currentImagePaths)) { // Iterar sobre las rutas relativas
            logger.info("Solicitando imagen al servidor para mostrar: {}", serverImagePath);
            Request request = new Request("downloadRoomImage", serverImagePath);
            Response response = ClientConnectionManager.sendRequest(request);

            if ("200".equalsIgnoreCase(response.getStatus()) && response.getData() != null) {
                try {
                    byte[] imageData;
                    if (response.getData() instanceof List) {
                        List<Double> doubleList = gson.fromJson(gson.toJson(response.getData()), new TypeToken<List<Double>>() {}.getType());
                        imageData = new byte[doubleList.size()];
                        for (int i = 0; i < doubleList.size(); i++) {
                            imageData[i] = doubleList.get(i).byteValue();
                        }
                        logger.info("Convertido List<Double> a byte[] para mostrar imagen: {}", serverImagePath);
                    } else if (response.getData() instanceof byte[]) {
                        imageData = (byte[]) response.getData();
                        logger.info("Recibido byte[] directamente para mostrar imagen: {}", serverImagePath);
                    } else {
                        logger.warn("Tipo de dato inesperado para imagen del servidor: {}. Saltando imagen {}.", response.getData().getClass().getName(), serverImagePath);
                        continue;
                    }

                    Image img = new Image(new ByteArrayInputStream(imageData), 200, 150, true, true);
                    ImageView imageView = new ImageView(img);
                    imageView.setPreserveRatio(true);
                    imageView.setFitWidth(200);
                    imageView.setFitHeight(150);

                    // Lógica para eliminar la imagen (ahora debe comunicar al servidor)
                    imageView.setOnMouseClicked(event -> {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Confirmar Eliminación");
                        alert.setHeaderText(null);
                        alert.setContentText("¿Estás seguro de que quieres eliminar esta imagen?");

                        Optional<ButtonType> result = alert.showAndWait();
                        if (result.isPresent() && result.get() == ButtonType.OK) {
                            // Obtén el número de habitación actual del campo de texto
                            int roomNumberToDeleteFrom;
                            try {
                                roomNumberToDeleteFrom = Integer.parseInt(roomNumberTf.getText());
                            } catch (NumberFormatException e) {
                                util.FXUtility.alert("Error", "Número de habitación inválido para eliminar imagen.");
                                logger.error("Error al intentar eliminar imagen: número de habitación no válido.");
                                return;
                            }

                            // 1. Obtener la habitación completa del servidor
                            Request getRoomRequest = new Request("getRoom", roomNumberToDeleteFrom);
                            Response getRoomResponse = ClientConnectionManager.sendRequest(getRoomRequest);

                            if ("200".equalsIgnoreCase(getRoomResponse.getStatus()) && getRoomResponse.getData() != null) {
                                Room roomToUpdate = gson.fromJson(gson.toJson(getRoomResponse.getData()), Room.class);
                                if (roomToUpdate.getImagesPaths() != null) {
                                    roomToUpdate.getImagesPaths().remove(serverImagePath); // Eliminar la ruta de la lista
                                    logger.info("Ruta de imagen eliminada de la lista local de la habitación {}: {}", roomNumberToDeleteFrom, serverImagePath);

                                    // 2. Enviar la habitación actualizada (sin esa ruta) al servidor para que la guarde en la DB
                                    Request updateRoomRequest = new Request("updateRoom", roomToUpdate);
                                    Response updateRoomResponse = ClientConnectionManager.sendRequest(updateRoomRequest);

                                    if ("200".equalsIgnoreCase(updateRoomResponse.getStatus())) {
                                        util.FXUtility.alertInfo("Éxito", "Imagen eliminada y habitación actualizada.");
                                        logger.info("Habitación actualizada en el servidor después de eliminar imagen.");
                                        currentImagePaths = new ArrayList<>(roomToUpdate.getImagesPaths()); // Actualiza la lista local de paths
                                        refreshImagesDisplay(); // Refrescar la UI para reflejar el cambio
                                    } else {
                                        roomToUpdate.getImagesPaths().add(serverImagePath); // Revertir si hubo error en la actualización
                                        util.FXUtility.alert("Error", "No se pudo actualizar la habitación en el servidor: " + updateRoomResponse.getMessage());
                                        logger.error("Error al actualizar habitación en servidor tras eliminar imagen: {}", updateRoomResponse.getMessage());
                                    }
                                } else {
                                    logger.warn("La lista de imágenes de la habitación {} es nula al intentar eliminar.", roomNumberToDeleteFrom);
                                }
                            } else {
                                util.FXUtility.alert("Error", "No se pudo obtener la habitación del servidor para eliminar imagen: " + getRoomResponse.getMessage());
                                logger.error("Error al obtener habitación del servidor para eliminar imagen: {}", getRoomResponse.getMessage());
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

            // Pasa la lista de rutas de imagen (currentImagePaths)
            Room room = new Room(number, price, description, status, style, new ArrayList<>(currentImagePaths));
            room.setHotelId(selectedHotel.getNumHotel());
            room.setHotel(selectedHotel);

            // Envía la solicitud al servidor para registrar la habitación
            Request request = new Request("registerRoom", room);
            Response response = ClientConnectionManager.sendRequest(request);

            // Procesar la respuesta del servidor
            if ("201".equalsIgnoreCase(response.getStatus())) {
                util.FXUtility.alertInfo("Éxito", "Habitación registrada correctamente.");
                if (roomOptionsController != null) {
                    roomOptionsController.loadRoomsIntoRegister();
                }
                clearFields();
                currentImagePaths.clear(); // Limpia la lista de imágenes después de un registro exitoso
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

    public void onCancel(ActionEvent event) {
        RoomOptionsController controller = Utility.loadPage2("roominterface/roomoptions.fxml", parentBp);
        controller.setMainController(mainController); // Muy importante
        roomOptionsController.loadRoomsIntoRegister();
    }

    private void loadHotelsIntoComboBox() {
        Request request = new Request("getHotels", null);
        Response response = ClientConnectionManager.sendRequest(request);
        if ("200".equalsIgnoreCase(response.getStatus()) && response.getData() != null) {
            List<Hotel> hotels = new Gson().fromJson(new Gson().toJson(response.getData()), new TypeToken<List<Hotel>>() {}.getType());
            hotelComboBox.setItems(FXCollections.observableArrayList(hotels));
            // Importante: Necesitas un CellFactory si quieres que el ComboBox muestre el nombre del hotel
            // en lugar del toString() por defecto de Hotel.
            hotelComboBox.setCellFactory(lv -> new ListCell<Hotel>() {
                @Override
                protected void updateItem(Hotel hotel, boolean empty) {
                    super.updateItem(hotel, empty);
                    setText(empty ? "" : hotel.getHotelName() + " (" + hotel.getNumHotel() + ")");
                }
            });
            // Esto también se aplica al botón de selección (representation)
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


