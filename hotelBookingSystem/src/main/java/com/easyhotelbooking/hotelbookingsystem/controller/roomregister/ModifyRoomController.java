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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.Optional; // Importar para Alert.showAndWait() con botones

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

    private static final Logger logger = LogManager.getLogger(ModifyRoomController.class);

    private static final String ROOM_IMAGES_DIR = "data\\images\\rooms";

    public void initialize() {
        statusCombo.getItems().addAll(RoomStatus.values());
        styleCombo.getItems().addAll(RoomStyle.values());
        loadHotelsIntoComboBox();
        roomNumberTf.setEditable(false);

        Path currentRelativePath = Paths.get("");
        String s = currentRelativePath.toAbsolutePath().toString();
        logger.info("Directorio de trabajo actual: " + s);
        logger.info("Ruta ROOM_IMAGES_DIR utilizada para guardar/leer: " + Paths.get(ROOM_IMAGES_DIR).toAbsolutePath().toString());
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

        // Iterar sobre una copia de la lista para evitar ConcurrentModificationException
        // si se elimina una imagen mientras se itera
        for (String path : new ArrayList<>(currentRoom.getImagesPaths())) {
            try {
                File imgFile = new File(path);

                if (imgFile.exists()) {
                    Image img = new Image(new FileInputStream(imgFile), 200, 150, true, true);
                    ImageView imageView = new ImageView(img);
                    imageView.setPreserveRatio(true);
                    imageView.setFitWidth(200);
                    imageView.setFitHeight(150);

                    // Añadir evento para eliminar la imagen al hacer clic
                    imageView.setOnMouseClicked(event -> {
                        // Confirmación antes de eliminar
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Confirmar Eliminación");
                        alert.setHeaderText(null);
                        alert.setContentText("¿Estás seguro de que quieres eliminar esta imagen?");

                        Optional<ButtonType> result = alert.showAndWait();
                        if (result.isPresent() && result.get() == ButtonType.OK) {
                            currentRoom.getImagesPaths().remove(path); // Eliminar de la lista de la habitación
                            // Opcional: Eliminar el archivo físico si ya no está referenciado por ninguna otra habitación
                            // (Esto requiere una lógica más compleja para rastrear referencias, así que por ahora solo eliminamos de la lista)
                            // Si solo lo eliminas de la lista, el archivo físico permanecerá en el disco.
                            logger.info("Imagen eliminada de la lista: {}", path);
                            refreshImagesDisplay(); // Volver a dibujar para actualizar la UI
                        }
                    });

                    flowPane.getChildren().add(imageView);
                } else {
                    logger.warn("La imagen no se encontró en la ruta esperada: {}", path);
                }
            } catch (FileNotFoundException e) {
                logger.error("Error al cargar la imagen desde el archivo: {}", path, e);
            } catch (Exception e) {
                logger.error("Error inesperado al cargar imagen: {}", path, e);
            }
        }
    }

    @FXML
    void onUploadImage(ActionEvent event) {


        if (currentRoom.getImagesPaths().size() >= 5) { // Verificar el límite antes de abrir el FileChooser
            mostrarAlerta("Límite de Imágenes", "Solo se permiten hasta 5 imágenes por habitación. Elimina alguna para añadir más.");
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
                String relativeImagePath = copyImageToAppDirectory(selectedFile);
                currentRoom.getImagesPaths().add(relativeImagePath);
                refreshImagesDisplay();
                logger.info("Imagen añadida para modificación: {}", relativeImagePath);
            } catch (IOException e) {
                logger.error("Error al copiar la imagen para modificación: {}", e.getMessage(), e);
                mostrarAlerta("Error", "No se pudo copiar la imagen: " + e.getMessage());
            }
        }
    }

    private String copyImageToAppDirectory(File sourceFile) throws IOException {
        Path destinationDir = Paths.get(ROOM_IMAGES_DIR);
        if (!Files.exists(destinationDir)) {
            Files.createDirectories(destinationDir);
            logger.info("Directorio de imágenes creado: {}", destinationDir.toAbsolutePath());
        }

        String fileExtension = "";
        int dotIndex = sourceFile.getName().lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < sourceFile.getName().length() - 1) {
            fileExtension = sourceFile.getName().substring(dotIndex + 1);
        }
        String uniqueFileName = UUID.randomUUID().toString() + "." + fileExtension;

        Path destinationPath = destinationDir.resolve(uniqueFileName);
        Files.copy(sourceFile.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);

        return Paths.get(ROOM_IMAGES_DIR, uniqueFileName).toString();
    }

    @FXML
    void onCancel(ActionEvent event) {
        RoomOptionsController controller = Utility.loadPage2("roomoptions.fxml", parentBp);
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
                mostrarAlerta("Error", "Por favor, complete todos los campos y seleccione un hotel.");
                return;
            }

            RoomStatus status = statusCombo.getValue();
            RoomStyle style = styleCombo.getValue();

            if (status == null || style == null) {
                mostrarAlerta("Error", "Seleccione estado y estilo de la habitación.");
                return;
            }

            Room updatedRoom = new Room(number, price, description, status, style, new ArrayList<>(currentRoom.getImagesPaths()));
            updatedRoom.setHotelId(selectedHotel.getNumHotel());
            updatedRoom.setHotel(selectedHotel);

            mainController.updateRoom(updatedRoom);

            if (roomOptionsController != null) {
                roomOptionsController.loadRoomsIntoRegister();
            }

            mostrarAlerta("Éxito", "Habitación modificada correctamente.");
            onCancel(event);

        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Precio inválido.");
            logger.error("Error de formato al modificar habitación: {}", e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Error al modificar habitación: {}", e.getMessage(), e);
            mostrarAlerta("Error", "Error al modificar habitación: " + e.getMessage());
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
            mostrarAlerta("Error", "No se pudieron cargar los hoteles para el ComboBox.");
            logger.error("Error al cargar hoteles para ComboBox en ModifyRoom: {}", response != null ? response.getMessage() : "Desconocido");
        }
    }

    private void mostrarAlerta(String titulo, String contenido) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setContentText(contenido);
        alert.showAndWait();
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
