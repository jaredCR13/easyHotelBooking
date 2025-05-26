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
import javafx.stage.FileChooser; // Necesario para el botón de subir imagen
import javafx.stage.Stage; // Necesario para el FileChooser
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream; // Necesario para cargar imágenes
import java.io.FileNotFoundException; // Necesario para cargar imágenes
import java.io.IOException; // Necesario para copiar imágenes
import java.nio.file.Files; // Necesario para copiar imágenes
import java.nio.file.Path; // Necesario para copiar imágenes
import java.nio.file.Paths; // Necesario para copiar imágenes
import java.nio.file.StandardCopyOption; // Necesario para copiar imágenes
import java.util.ArrayList; // Necesario para manipular la lista de imágenes
import java.util.Collections;
import java.util.List;
import java.util.UUID; // Necesario para generar nombres de archivo únicos

public class ModifyRoomController {

    @FXML
    private TextArea descriptionTf;

    @FXML
    private ComboBox<Hotel> hotelComboBox;

    @FXML
    private TextField priceTf;

    @FXML
    private TextField roomNumberTf; // Asumimos que es de solo lectura para la PK

    @FXML
    private ComboBox<RoomStatus> statusCombo;

    @FXML
    private ComboBox<RoomStyle> styleCombo;

    @FXML
    private FlowPane flowPane; // Aquí mostraremos las imágenes

    @FXML
    private Button uploadButton; // Para subir nuevas imágenes

    private MainInterfaceController mainController;
    private RoomOptionsController roomOptionsController;
    private Room currentRoom; // La habitación que estamos modificando
    private BorderPane parentBp;
    private Stage primaryStage; // Necesario para el FileChooser

    private static final Logger logger = LogManager.getLogger(ModifyRoomController.class);

    // Definir un directorio base para guardar las imágenes de las habitaciones
    private static final String ROOM_IMAGES_DIR = "data/images/rooms"; // Ruta relativa al directorio de la aplicación

    public void initialize() {
        statusCombo.getItems().addAll(RoomStatus.values());
        styleCombo.getItems().addAll(RoomStyle.values());
        loadHotelsIntoComboBox();
        // El roomNumberTf debe ser de solo lectura para la clave primaria
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

    // Método para pasar el Stage al controlador (necesario para FileChooser)
    public void setStage(Stage stage) {
        this.primaryStage = stage;
    }

    // Este método es crucial: recibe la habitación a modificar desde RoomOptionsController
    public void setRoom(Room room) {
        this.currentRoom = room; // Asignamos la habitación a la variable de instancia
        if (currentRoom != null) {
            // Rellenar los campos de la interfaz con los datos de la habitación
            roomNumberTf.setText(String.valueOf(currentRoom.getRoomNumber()));
            priceTf.setText(String.valueOf(currentRoom.getRoomPrice()));
            descriptionTf.setText(currentRoom.getDetailedDescription());
            statusCombo.setValue(currentRoom.getStatus());
            styleCombo.setValue(currentRoom.getStyle());

            // Seleccionar el hotel correcto en el ComboBox
            if (currentRoom.getHotelId() != -1) {
                hotelComboBox.getItems().stream()
                        .filter(h -> h.getNumHotel() == currentRoom.getHotelId())
                        .findFirst()
                        .ifPresent(hotelComboBox::setValue);
            }

            // Llamar a refreshImagesDisplay para mostrar las imágenes existentes
            // currentRoom.getImagesPaths() ya contiene las rutas relativas.
            refreshImagesDisplay();

        } else {
            logger.warn("ModifyRoomController recibió un objeto Room nulo.");
            // Opcional: limpiar los campos o mostrar un mensaje de error
            clearFields();
        }
    }

    // --- Lógica para mostrar y manejar imágenes ---

    // Método para refrescar la visualización de las imágenes en el FlowPane
    private void refreshImagesDisplay() {
        flowPane.getChildren().clear(); // Limpiar cualquier imagen anterior

        // Asegurarse de que currentRoom y sus imágenes no sean nulas
        if (currentRoom == null || currentRoom.getImagesPaths() == null || currentRoom.getImagesPaths().isEmpty()) {
            logger.info("No hay imágenes para mostrar en esta habitación.");
            return;
        }

        // Iterar sobre las rutas de las imágenes de la habitación actual
        for (String path : currentRoom.getImagesPaths()) {
            try {
                // Construir un objeto File a partir de la ruta relativa guardada
                // Esto asumirá que la ruta es relativa al directorio de ejecución de la aplicación.
                File imgFile = new File(path);

                if (imgFile.exists()) {
                    // Cargar la imagen usando FileInputStream para rutas locales
                    Image img = new Image(new FileInputStream(imgFile), 200, 150, true, true); // Ajusta tamaño deseado
                    ImageView imageView = new ImageView(img);
                    imageView.setPreserveRatio(true);
                    imageView.setFitWidth(200); // Ancho deseado de la imagen en la UI
                    imageView.setFitHeight(150); // Alto deseado

                    flowPane.getChildren().add(imageView);
                } else {
                    logger.warn("La imagen no se encontró en la ruta esperada: {}", path);
                    // Opcional: Puedes agregar una imagen de "no disponible" o un mensaje visual
                    // Image placeholder = new Image(getClass().getResourceAsStream("/ruta/a/placeholder.png"));
                    // flowPane.getChildren().add(new ImageView(placeholder));
                }
            } catch (FileNotFoundException e) {
                logger.error("Error al cargar la imagen desde el archivo: {}", path, e);
                // System.err.println("Imagen no encontrada: " + path);
            } catch (Exception e) {
                logger.error("Error inesperado al cargar imagen: {}", path, e);
            }
        }
    }

    @FXML
    void onUploadImage(ActionEvent event) {
        // Asegúrate de que primaryStage se haya seteado (desde la clase principal)
        if (primaryStage == null) {
            mostrarAlerta("Error", "La ventana principal no ha sido inicializada correctamente para subir imágenes.");
            logger.error("Stage no disponible para FileChooser en ModifyRoomController.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecciona imagen para la habitación");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );

        File selectedFile = fileChooser.showOpenDialog(primaryStage);
        if (selectedFile != null) {
            if (currentRoom.getImagesPaths().size() < 5) { // Limitar máximo 5 imágenes
                try {
                    // Copiar la imagen al directorio de la aplicación y obtener la ruta relativa
                    String relativeImagePath = copyImageToAppDirectory(selectedFile);
                    // Añadir la nueva ruta a la lista de imágenes de la habitación actual
                    currentRoom.getImagesPaths().add(relativeImagePath);
                    refreshImagesDisplay(); // Volver a dibujar para mostrar la nueva imagen
                    logger.info("Imagen añadida para modificación: {}", relativeImagePath);
                } catch (IOException e) {
                    logger.error("Error al copiar la imagen para modificación: {}", e.getMessage(), e);
                    mostrarAlerta("Error", "No se pudo copiar la imagen: " + e.getMessage());
                }
            } else {
                mostrarAlerta("Límite de Imágenes", "Solo se permiten hasta 5 imágenes por habitación.");
            }
        }
    }

    // Método auxiliar para copiar la imagen seleccionada a un directorio de la aplicación
    private String copyImageToAppDirectory(File sourceFile) throws IOException {
        Path destinationDir = Paths.get(ROOM_IMAGES_DIR);
        // Crear el directorio si no existe
        if (!Files.exists(destinationDir)) {
            Files.createDirectories(destinationDir);
            logger.info("Directorio de imágenes creado: {}", destinationDir.toAbsolutePath());
        }

        // Generar un nombre de archivo único para evitar colisiones
        String fileExtension = "";
        int dotIndex = sourceFile.getName().lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < sourceFile.getName().length() - 1) {
            fileExtension = sourceFile.getName().substring(dotIndex + 1);
        }
        String uniqueFileName = UUID.randomUUID().toString() + "." + fileExtension;

        Path destinationPath = destinationDir.resolve(uniqueFileName);

        // Copiar el archivo, reemplazando si ya existe (poco probable con UUID)
        Files.copy(sourceFile.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);

        // Retornar la ruta relativa que se guardará en el objeto Room
        return Paths.get(ROOM_IMAGES_DIR, uniqueFileName).toString();
    }

    // --- Lógica de la interfaz y comunicación con el servidor ---

    @FXML
    void onCancel(ActionEvent event) {
        // Carga la vista de RoomOptions de nuevo
        RoomOptionsController controller = Utility.loadPage2("roomoptions.fxml", parentBp);
        if (controller != null) {
            controller.setMainController(mainController);
            // Asegúrate de que roomOptionsController ya esté cargado antes de llamar a loadRoomsIntoRegister
            // si la navegación directa a "roomoptions.fxml" es el objetivo principal.
            // Si el controlador devuelto por loadPage2 es el nuevo RoomOptionsController,
            // entonces ese nuevo controller necesita ser el que llame a loadRoomsIntoRegister().
            controller.loadRoomsIntoRegister(); // Llamar a loadRoomsIntoRegister en el nuevo controlador
        }
    }

    @FXML
    void onModify(ActionEvent event) {
        try {
            // currentRoom.getRoomNumber() mantiene el número de habitación original
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

            // Crear un nuevo objeto Room con los datos actualizados y la lista de imágenes actualizadas
            // Es importante pasar una nueva instancia de ArrayList para evitar modificaciones inesperadas
            Room updatedRoom = new Room(number, price, description, status, style, new ArrayList<>(currentRoom.getImagesPaths()));
            updatedRoom.setHotelId(selectedHotel.getNumHotel());
            updatedRoom.setHotel(selectedHotel); // Importante para la lógica en memoria si se usa

            // Enviar la habitación actualizada al servidor a través del MainInterfaceController
            mainController.updateRoom(updatedRoom); // Asume que tienes un método updateRoom en MainInterfaceController

            // Refrescar la tabla en RoomOptionsController para mostrar los cambios
            if (roomOptionsController != null) {
                roomOptionsController.loadRoomsIntoRegister();
            }

            mostrarAlerta("Éxito", "Habitación modificada correctamente.");
            // Volver a la vista de opciones después de guardar
            onCancel(event); // Reutilizar el método onCancel para volver a la vista anterior

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
        flowPane.getChildren().clear(); // Limpiar las imágenes también
    }
}

