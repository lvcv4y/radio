package controllers;

import coreclasses.dataclasses.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class InstruController implements Initializable {

    @FXML
    private Button freestyleBrowser;

    @FXML
    private TextField freestyleDir;

    @FXML
    private ChoiceBox<String> freestyleChoiceBox;

    @FXML
    private Button uploadButton;

    private final CurrentData currentData = CurrentData.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        if(currentData.getFreestyleBeatDir() != null)
            freestyleDir.setText(currentData.getFreestyleBeatDir());

        freestyleChoiceBox.setValue(currentData.isFreestyleOn() ? "ON" : "OFF");

        freestyleBrowser.setOnAction(e -> {
            final FileChooser fc = new FileChooser();
            fc.setTitle("B-Rap Radio Admin Client - Instru freestyle");
            fc.setInitialDirectory(new File("C:/"));
            final File dir = fc.showOpenDialog(((Node) e.getTarget()).getScene().getWindow());

            if(dir != null){
                freestyleDir.setText(dir.getPath());
                currentData.setFreestyleBeatDir(dir.getPath());
            }
        });

        freestyleChoiceBox.setOnAction(e -> currentData.setFreestyleOn("ON".equals(freestyleChoiceBox.getValue())));


        uploadButton.setOnAction(e -> AlertBox.notImplemented());
    }

}
