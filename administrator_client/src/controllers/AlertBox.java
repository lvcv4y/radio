package controllers;

import coreclasses.dataclasses.Request;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;


public class AlertBox {

    private static Stage stage;

    public static void showErrorBox(String windowTitle, String title, String desc){

        if(windowTitle == null)
            return;

        if(stage == null){
            try {
                Parent root = FXMLLoader.load(AlertBox.class.getResource("/gui/errorboxview.fxml"));
                stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setResizable(false);
                stage.setTitle(windowTitle);

                if(title == null)
                    root.lookup("#titleLabel").setVisible(false);
                else
                    ((Label) root.lookup("#titleLabel")).setText(title);

                if(desc == null)
                    root.lookup("#descriptionLabel").setVisible(false);
                else
                    ((Label) root.lookup("#descriptionLabel")).setText(desc);

                stage.setScene(new Scene(root));
                stage.showAndWait();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void notImplemented(){
        showErrorBox("Erreur - Non Implémenté", "Non Implémenté", "Cette fonction n'a pas encore été implémentée " +
                "(c'est un joli mot pour dire qu'il manque un (gros ?) bout de code pour que le bouton sur lequel vous avez cliqué fasse quelque chose).");
    }

    public static void requestError(@NotNull Request res){
        if(res.isError()) {
            String details = "type : ".concat(res.getType()).concat("\nDescription de l'erreur : ").concat(res.getErrorMessage());
            showErrorBox("Erreur - Requête d'erreur", "Erreur reçue", "Une requête d'erreur provenant du serveur a été reçue. " +
                    "Cette dernière est sûrement à la dernière action que vous avez effectuer. Voici les détails de la requête en question : \n".concat(details));
        }
    }

    public static void ExceptionError(final String windowTitle, final String title, final String startDesc, final Exception e){
        String details = "\n*****EXCEPTION DETAILS*****\nEXCEPTION CLASS = ".concat(e.getClass().getName());
        if(e.getMessage() != null)
            details = details.concat("\nERROR MESSAGE = ".concat(e.getMessage()));

        if(e.getCause() != null)
            details = details.concat("\nERROR CAUSE = ".concat(e.getCause().toString()));

        if(e.getStackTrace() != null){
            details = details.concat("\nSTACKTRACE : \n");
            for(StackTraceElement el : e.getStackTrace())
                details = details.concat(el.toString().concat("\n"));
        }

        showErrorBox(windowTitle, title, startDesc.concat(details));
    }

    public void close() {
        if (stage != null) {
            stage.close();
            stage = null;
        }
    }
}
