package com.easyhotelbooking.hotelbookingsystem.controller.roomregister;

import com.easyhotelbooking.hotelbookingsystem.controller.maininterface.MainInterfaceController;
import com.easyhotelbooking.hotelbookingsystem.socket.ClientConnectionManager;
import com.easyhotelbooking.hotelbookingsystem.util.FXUtility;
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
import hotelbookingcommon.domain.ImageUploadDTO;


public class ModifyRoomController {

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
    private FlowPane flowPane;

    @FXML
    private Button uploadButton;

    private MainInterfaceController mainController;
    private RoomOptionsController roomOptionsController;
    private Room currentRoom;
    private BorderPane parentBp;
    private Stage primaryStage;
    private final Gson gson = new Gson();
    private static final Logger logger = LogManager.getLogger(ModifyRoomController.class);



    public void initialize() {
        statusCombo.getItems().addAll(RoomStatus.values());
        styleCombo.getItems().addAll(RoomStyle.values());
        loadHotelsIntoComboBox();
        roomNumberTf.setEditable(false);

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

    public void setRoom(Room room) {
        this.currentRoom = room;
        if (currentRoom != null) {
            roomNumberTf.setText(String.valueOf(currentRoom.getRoomNumber()));
            priceTf.setText(String.valueOf(currentRoom.getRoomPrice()));
            descriptionTf.setText(currentRoom.getDetailedDescription());
            statusCombo.setValue(currentRoom.getStatus());
            styleCombo.setValue(currentRoom.getStyle());

            if (currentRoom.getHotelId() != -1) {
                hotelComboBox.getItems().stream()
                        .filter(h -> h.getNumHotel() == currentRoom.getHotelId())
                        .findFirst()
                        .ifPresent(hotelComboBox::setValue);
            }
            refreshImagesDisplay();
        } else {
            logger.warn("ModifyRoomController recibió un objeto Room nulo.");
            clearFields();
        }
    }

    // --- Lógica para mostrar y manejar imágenes ---

    private void refreshImagesDisplay() {
        flowPane.getChildren().clear();

        if (currentRoom == null || currentRoom.getImagesPaths() == null || currentRoom.getImagesPaths().isEmpty()) {
            logger.info("No hay imágenes para mostrar en esta habitación.");
            return;
        }


        for (String serverImagePath : new ArrayList<>(currentRoom.getImagesPaths())) {
            logger.info("Solicitando imagen al servidor para mostrar: {}", serverImagePath);
            // Enviar una Request al servidor para descargar los bytes de la imagen
            Request request = new Request("downloadRoomImage", serverImagePath);
            Response response = ClientConnectionManager.sendRequest(request);

            if ("200".equalsIgnoreCase(response.getStatus()) && response.getData() != null) {
                try {
                    byte[] imageData;
                    // Manejar la deserialización de Gson: byte[] a menudo llega como List<Double>
                    if (response.getData() instanceof List) {

                        List<Double> doubleList = new Gson().fromJson(new Gson().toJson(response.getData()), new TypeToken<List<Double>>() {}.getType());
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

                    //Crear la Image de JavaFX desde el array de bytes
                    Image img = new Image(new ByteArrayInputStream(imageData), 200, 150, true, true);
                    ImageView imageView = new ImageView(img);
                    imageView.setPreserveRatio(true);
                    imageView.setFitWidth(200);
                    imageView.setFitHeight(150);

                    // Lógica para eliminar la imagen
                    imageView.setOnMouseClicked(event -> {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Confirmar Eliminación");
                        alert.setHeaderText(null);
                        alert.setContentText("¿Estás seguro de que quieres eliminar esta imagen?");

                        Optional<ButtonType> result = alert.showAndWait();
                        if (result.isPresent() && result.get() == ButtonType.OK) {
                            // Eliminar la ruta de la imagen de la lista local
                            currentRoom.getImagesPaths().remove(serverImagePath);
                            logger.info("Ruta de imagen eliminada de la lista local: {}", serverImagePath);

                            //Enviar la habitación actualizada (sin esa ruta) al servidor para que la guarde en la DB

                            Request updateRoomRequest = new Request("updateRoom", currentRoom);
                            Response updateRoomResponse = ClientConnectionManager.sendRequest(updateRoomRequest);

                            if ("200".equalsIgnoreCase(updateRoomResponse.getStatus())) {
                                logger.info("Habitación actualizada en el servidor después de eliminar imagen.");
                                refreshImagesDisplay(); //Refrescar la interfaz para reflejar el cambio
                            } else {

                                currentRoom.getImagesPaths().add(serverImagePath); // Revertir si hubo error
                                FXUtility.alert("Error", "No se pudo actualizar la habitación en el servidor: " + updateRoomResponse.getMessage());
                                logger.error("Error al actualizar habitación en servidor tras eliminar imagen: {}", updateRoomResponse.getMessage());
                            }
                        }
                    });

                    flowPane.getChildren().add(imageView);
                } catch (Exception e) {
                    logger.error("Error al procesar la imagen descargada del servidor: {}", serverImagePath, e);
                    FXUtility.alert("Error", "No se pudo cargar una imagen desde el servidor: " + serverImagePath + " - " + e.getMessage());
                }
            } else {
                logger.warn("No se pudo descargar la imagen del servidor: {}. Estado: {}, Mensaje: {}",
                        serverImagePath, response.getStatus(), response.getMessage());
                FXUtility.alert("Advertencia", "No se pudo descargar una imagen desde el servidor: " + serverImagePath + " - " + response.getMessage());
            }
        }
    }

    @FXML
    void onUploadImage(ActionEvent event) {
        if (currentRoom == null || currentRoom.getImagesPaths().size() >= 5) {
            FXUtility.alertInfo("Límite de Imágenes", "Solo se permiten hasta 5 imágenes por habitación. Elimina alguna para añadir más.");
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

                // Crear el DTO para enviar al servidor
                ImageUploadDTO uploadDto = new ImageUploadDTO(
                        currentRoom.getRoomNumber(),
                        selectedFile.getName(),
                        imageData
                );

                //Enviar la Request al servidor para subir la imagen
                Request request = new Request("uploadRoomImage", uploadDto); // Acción: "uploadRoomImage"
                Response response = ClientConnectionManager.sendRequest(request);

                if ("200".equalsIgnoreCase(response.getStatus()) && response.getData() != null) {
                    FXUtility.alertInfo("Éxito", "Imagen subida exitosamente al servidor.");
                    logger.info("Respuesta del servidor al subir imagen: {}", response.getMessage());

                    //El servidor debería devolver la habitación actualizada con la nueva ruta de imagen

                    Room updatedRoom = gson.fromJson(gson.toJson(response.getData()), Room.class);
                    currentRoom.setImagesPaths(updatedRoom.getImagesPaths()); // Actualiza la lista local de paths

                    logger.info("Habitación actualizada con los nuevos paths de imagen.");
                    refreshImagesDisplay(); // Volver a cargar las imágenes
                } else {
                    logger.error("Error al subir la imagen al servidor: {}", response.getMessage());
                    FXUtility.alert("Error", "No se pudo subir la imagen: " + response.getMessage());
                }

            } catch (IOException e) {
                logger.error("Error de E/S al leer la imagen o al enviarla al servidor: {}", e.getMessage(), e);
                FXUtility.alert("Error", "No se pudo procesar la imagen para subir: " + e.getMessage());
            } catch (Exception e) {
                logger.error("Error inesperado en onUploadImage: {}", e.getMessage(), e);
                FXUtility.alert("Error", "Ocurrió un error inesperado al subir la imagen.");
            }
        }

    }


    @FXML
    void onCancel(ActionEvent event) {
        RoomOptionsController controller = Utility.loadPage2("roominterface/roomoptions.fxml", parentBp);
        if (controller != null) {
            controller.setMainController(mainController);
            controller.loadRoomsIntoRegister();
        }
    }

    @FXML
    void onModify(ActionEvent event) {
        try {
            int number = currentRoom.getRoomNumber();
            double price = Double.parseDouble(priceTf.getText());
            String description = descriptionTf.getText();
            Hotel selectedHotel = hotelComboBox.getSelectionModel().getSelectedItem();

            if (priceTf.getText().isEmpty() || description.isEmpty() || selectedHotel == null) {
                FXUtility.alert("Error", "Por favor, complete todos los campos y seleccione un hotel.");
                return;
            }

            RoomStatus status = statusCombo.getValue();
            RoomStyle style = styleCombo.getValue();

            if (status == null || style == null) {
                FXUtility.alert("Error", "Seleccione estado y estilo de la habitación.");
                return;
            }

            Room updatedRoom = new Room(number, price, description, status, style, new ArrayList<>(currentRoom.getImagesPaths()));
            updatedRoom.setHotelId(selectedHotel.getNumHotel());
            updatedRoom.setHotel(selectedHotel);

            mainController.updateRoom(updatedRoom);

            if (roomOptionsController != null) {
                roomOptionsController.loadRoomsIntoRegister();
            }

            FXUtility.alertInfo("Éxito", "Habitación modificada correctamente.");
            onCancel(event);

        } catch (NumberFormatException e) {
            FXUtility.alert("Error", "Precio inválido.");
            logger.error("Error de formato al modificar habitación: {}", e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Error al modificar habitación: {}", e.getMessage(), e);
            FXUtility.alert("Error", "Error al modificar habitación: " + e.getMessage());
        }
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
            FXUtility.alert("Error", "No se pudieron cargar los hoteles para el ComboBox.");
            logger.error("Error al cargar hoteles para ComboBox en ModifyRoom: {}", response != null ? response.getMessage() : "Desconocido");
        }
    }


    private void clearFields() {
        hotelComboBox.getSelectionModel().clearSelection();
        statusCombo.getSelectionModel().clearSelection();
        styleCombo.getSelectionModel().clearSelection();
        roomNumberTf.clear();
        descriptionTf.clear();
        priceTf.clear();
        flowPane.getChildren().clear();
    }
}
