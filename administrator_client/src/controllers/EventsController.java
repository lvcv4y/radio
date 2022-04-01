package controllers;

import coreclasses.dataclasses.CurrentData;
import coreclasses.dataclasses.Event;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableView;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class EventsController implements Initializable, Closable, CurrentData.Refreshable {

    @FXML
    private TableView<Event> eventsTableView;

    private final CurrentData currentData = CurrentData.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentData.addListener(this);
        eventsTableView.setItems(FXCollections.observableArrayList(currentData.getEvents()));
    }


    @Override
    public void onAddEvent(Event event) {
        eventsTableView.getItems().add(event);
    }

    @Override
    public void onDeleteEvent(Event event) {
        eventsTableView.getItems().removeIf(e -> {
            try {
                return e.getTitle().equals(event.getTitle()) || e.getDescription().equals(event.getDescription());
            } catch (NullPointerException ignored){ return false; }
        }
        );
    }

    @Override
    public void onEventsGotten(List<Event> currentEvents) {
        eventsTableView.setItems(FXCollections.observableArrayList(currentEvents));
    }

    @Override
    public void shutdown() {
        currentData.removeListener(this);
    }
}
