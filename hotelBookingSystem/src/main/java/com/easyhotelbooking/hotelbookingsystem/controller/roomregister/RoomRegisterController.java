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
    @FXML
    private ImageView roomImageView;
    private static final Logger logger = LogManager.getLogger(RoomRegisterController.class);
    // Definir un directorio base para guardar las imágenes de las habitaciones
    private static final String ROOM_IMAGES_DIR = "data/images/rooms"; // Ruta relativa al directorio de la aplicación
    private MainInterfaceController mainController;
    private RoomOptionsController roomOptionsController;
    private BorderPane parentBp;
    @FXML
    public void initialize(){

        this.imagePaths= new ArrayList<>();
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
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecciona imagen para la habitación");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );

        File selectedFile = fileChooser.showOpenDialog(primaryStage); // Usar primaryStage
        if (selectedFile != null) {
            if (imagePaths.size() < 5) { // Limitar máximo 5 imágenes
                try {
                    // Copiar la imagen al directorio de la aplicación
                    String relativeImagePath = copyImageToAppDirectory(selectedFile);
                    imagePaths.add(relativeImagePath);
                    refreshImagesDisplay();
                    logger.info("Imagen añadida: {}", relativeImagePath);
                } catch (IOException e) {
                    logger.error("Error al copiar la imagen: {}", e.getMessage(), e);
                    mostrarAlerta("Error", "No se pudo copiar la imagen: " + e.getMessage());
                }
            } else {
                mostrarAlerta("Límite de Imágenes", "Solo se permiten hasta 5 imágenes por habitación.");
            }
        }
    }

    // Nuevo método para copiar la imagen y retornar la ruta relativa
    private String copyImageToAppDirectory(File sourceFile) throws IOException {
        // Asegurarse de que el directorio de destino exista
        Path destinationDir = Paths.get(ROOM_IMAGES_DIR);
        if (!Files.exists(destinationDir)) {
            Files.createDirectories(destinationDir);
            logger.info("Directorio creado: {}", destinationDir.toAbsolutePath());
        }

        // Generar un nombre de archivo único para evitar colisiones
        String fileExtension = "";
        int dotIndex = sourceFile.getName().lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < sourceFile.getName().length() - 1) {
            fileExtension = sourceFile.getName().substring(dotIndex + 1);
        }
        String uniqueFileName = UUID.randomUUID().toString() + "." + fileExtension;

        Path destinationPath = destinationDir.resolve(uniqueFileName);

        // Copiar el archivo
        Files.copy(sourceFile.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);

        // Retornar la ruta relativa que se guardará en Room
        return Paths.get(ROOM_IMAGES_DIR, uniqueFileName).toString();
    }

    private void refreshImagesDisplay() {
        flowPane.getChildren().clear();

        for (String path : imagePaths) {
            try {
                // Importante: Construir el File a partir de la ruta relativa guardada
                // para que siempre busque la imagen en el directorio de la aplicación.
                File imgFile = new File(path);
                if (imgFile.exists()) {
                    Image img = new Image(new FileInputStream(imgFile), 100, 75, true, true);
                    ImageView imageView = new ImageView(img);
                    imageView.setPreserveRatio(true);
                    imageView.setFitWidth(200);
                    imageView.setFitHeight(150);
                    flowPane.getChildren().add(imageView);
                } else {
                    logger.warn("La imagen no se encontró en la ruta esperada: {}", path);
                    // Opcional: mostrar una imagen de placeholder si no se encuentra el archivo
                    // Image placeholder = new Image(getClass().getResourceAsStream("/path/to/placeholder.png"));
                    // flowPane.getChildren().add(new ImageView(placeholder));
                }
            } catch (FileNotFoundException e) {
                logger.error("Error al cargar la imagen desde el archivo: {}", path, e);
                System.err.println("Imagen no encontrada: " + path);
            }
        }
    }

    public void onSave(ActionEvent event) {
        try {
            String numberStr = roomNumberTf.getText();
            String priceStr = priceTf.getText();
            String description = descriptionTf.getText();
            Hotel selectedHotel = hotelComboBox.getSelectionModel().getSelectedItem();

            if (numberStr.isEmpty() || priceStr.isEmpty() || description.isEmpty() || selectedHotel == null) {
                mostrarAlerta("Error", "Por favor, complete todos los campos y seleccione un hotel.");
                return;
            }

            int number = Integer.parseInt(numberStr);
            double price = Double.parseDouble(priceStr);
            RoomStatus status = statusCombo.getValue();
            RoomStyle style = styleCombo.getValue();

            if (status == null || style == null) {
                mostrarAlerta("Error", "Seleccione estado y estilo de la habitación.");
                return;
            }

            // Aquí creas el objeto Room con la lista de imágenes
            Room room = new Room(number, price, description, status, style, new ArrayList<>(imagePaths));
            room.setHotelId(selectedHotel.getNumHotel());
            room.setHotel(selectedHotel);
            // Obtener el Hotel seleccionado del ComboBox

            mainController.registerRoom(room); // Envía al servidor

            if (roomOptionsController != null) {
                roomOptionsController.loadRoomsIntoRegister(); // Recarga tabla
            }
            clearFields();
        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Número de habitación o precio inválido.");
            logger.error("Error de formato al registrar habitación: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Error al registrar habitación: {}", e.getMessage());
            mostrarAlerta("Error", "Error al registrar habitación: " + e.getMessage());
        }
    }



    public void onCancel(ActionEvent event) {
        RoomOptionsController controller = Utility.loadPage2("roomoptions.fxml", parentBp);
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
            mostrarAlerta("Error", "No se pudieron cargar los hoteles para el ComboBox.");
            logger.error("Error al cargar hoteles para ComboBox: {}", response != null ? response.getMessage() : "Desconocido");
        }
    }
    private void mostrarAlerta(String titulo, String contenido) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
    private void clearFields(){
        hotelComboBox.getSelectionModel().clearSelection();
        statusCombo.getSelectionModel().clearSelection();
        styleCombo.getSelectionModel().clearSelection();
        roomNumberTf.clear();
        descriptionTf.clear();
        priceTf.clear();
        flowPane.getChildren().clear();
    }
}


