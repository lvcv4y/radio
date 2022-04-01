package controllers;

import coreclasses.dataclasses.CurrentData;
import coreclasses.dataclasses.UserEntity;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableView;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;


public class StaffController implements Initializable, CurrentData.Refreshable, Closable {

    @FXML
    private TableView<UserEntity> tableView;

    private final CurrentData currentData = CurrentData.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentData.addListener(this);
        currentData.fetchStaff();
        tableView.setItems(FXCollections.observableArrayList(currentData.getStaff()));
    }


    @Override
    public void onAddStaff(UserEntity user) {
        tableView.getItems().add(user);
    }

    @Override
    public void onDeleteStaff(UserEntity user) {
        tableView.getItems().removeIf(u -> {
                    try {
                        return u.getId() == user.getId();
                    } catch (NullPointerException ignored) { return false; }
                }
        );
    }

    @Override
    public void onStaffGotten(List<UserEntity> currentStaffs) {
        tableView.setItems(FXCollections.observableArrayList(currentStaffs));
    }

    @Override
    public void shutdown() {
        currentData.removeListener(this);
    }
}
