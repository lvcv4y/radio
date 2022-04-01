package controllers;

import coreclasses.dataclasses.CurrentData;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class BackTrackController implements Initializable {

    @FXML
    private Button backTrackBrowser;

    @FXML
    private TextField backTrackDir;

    @FXML
    private ChoiceBox<String> backTrackChoiceBox;

    @FXML
    private CheckBox backTrackLoop;

    @FXML
    private Button uploadButton;

    private final CurrentData currentData = CurrentData.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        if(currentData.getBackTrackDir() != null)
            backTrackDir.setText(currentData.getBackTrackDir());

        backTrackChoiceBox.setValue(currentData.isBackTrackOn() ? "ON" : "OFF");

        backTrackLoop.setSelected(currentData.isBackTrackLoopOn());

        backTrackBrowser.setOnAction(e -> {
            final FileChooser fc = new FileChooser();
            fc.setTitle("B-Rap Radio Admin Client - Choix du fond sonore");
            fc.setInitialDirectory(new File("C:/"));
            final File dir = fc.showOpenDialog(((Node) e.getTarget()).getScene().getWindow());
            if(dir != null) {
                currentData.setBackTrackDir(dir.getPath());
                backTrackDir.setText(dir.getPath());
            }
        });

        backTrackChoiceBox.setOnAction(e -> currentData.setBackTrackOn("ON".equals(backTrackChoiceBox.getValue())));

        backTrackLoop.setOnAction(e -> currentData.setBackTrackLoopOn(backTrackLoop.isSelected()));

        uploadButton.setOnAction(e -> AlertBox.notImplemented());
    }
}
