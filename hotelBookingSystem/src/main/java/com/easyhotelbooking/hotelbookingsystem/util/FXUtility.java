package util;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;

import java.io.IOException;

public class FXUtility {

    public static void loadPage(String className, String page, BorderPane bp) {
        try {
            Class cl = Class.forName(className);
            FXMLLoader fxmlLoader = new FXMLLoader(cl.getResource(page));
            cl.getResource("bp");
            bp.setCenter(fxmlLoader.load());
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Alert alert(String title, String header){
        Alert myalert = new Alert(Alert.AlertType.NONE);
        myalert.setAlertType(Alert.AlertType.ERROR);
        myalert.setTitle(title);
        myalert.setHeaderText(header);
        return myalert;
    }

    public static Alert alertInfo(String title, String header){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE); //no estoy seguro si es necesario
        alert.getDialogPane().setMinWidth(400); //no estoy seguro si es necesario
        alert.setHeaderText(header);
        //alert.setContentText(header);
        return alert;
    }


    public static TextInputDialog dialog(String title, String header){
        TextInputDialog dialog = new TextInputDialog(title);
        dialog.setHeaderText(header);
        return dialog;
    }

}
