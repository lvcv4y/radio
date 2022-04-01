package controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.Calendar;
import java.util.ResourceBundle;

public class StartStreamController implements Initializable {

    @FXML
    private TextField eventTitle;

    @FXML
    private TextArea eventDesc;

    @FXML
    private VBox channelBox;

    @FXML
    private ComboBox<Integer> startHour;

    @FXML
    private ComboBox<Integer> startMinute;

    @FXML
    private ComboBox<Integer> endHour;

    @FXML
    private ComboBox<Integer> endMinute;

    @FXML
    private Button showHourStart;

    @FXML
    private Button showHourEnd;

    @FXML
    private ComboBox<String> stopMode;

    @FXML
    private Button streamButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        for (int i = 1; i <= 24; i++) {
            startHour.getItems().add(i);
            endHour.getItems().add(i);
        }

        for (int i = 0; i < 60; i++) {
            startMinute.getItems().add(i);
            endMinute.getItems().add(i);
        }

        showHourStart.setOnAction(e -> {
            final Calendar c = Calendar.getInstance();
            startHour.setValue(c.get(Calendar.HOUR_OF_DAY));
            startMinute.setValue(c.get(Calendar.MINUTE));
        });

        showHourEnd.setOnAction(e -> {
            final Calendar c = Calendar.getInstance();
            endHour.setValue(c.get(Calendar.HOUR_OF_DAY));
            endMinute.setValue(c.get(Calendar.MINUTE));
        });


    }
}
