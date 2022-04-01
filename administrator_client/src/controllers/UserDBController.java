package controllers;

import coreclasses.dataclasses.CurrentData;
import coreclasses.dataclasses.UserEntity;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;


public class UserDBController implements Initializable, CurrentData.Refreshable, Closable {

    @FXML
    private TableView<UserEntity> userTableView;

    @FXML
    private TextField SQLCmdField;

    @FXML
    private Button executeButton;

    private final CurrentData currentData = CurrentData.getInstance();


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentData.fetchUsers();
        currentData.addListener(this);

        userTableView.setItems(FXCollections.observableArrayList(currentData.getUsers()));

        executeButton.setOnAction(e -> {
            fetchRequest(SQLCmdField.getText());
            SQLCmdField.clear();
        });

        SQLCmdField.setOnAction(e -> {
            executeButton.fire();
            e.consume();
        });
    }

    @Override
    public void onAddUser(UserEntity user) {
        userTableView.getItems().add(user);
    }

    @Override
    public void onDeleteUser(UserEntity user) {
        userTableView.getItems().removeIf(u -> {
                    try {
                        return u.getId() == user.getId();
                    } catch (NullPointerException ignored){ return false; }
                }
        );
    }

    @Override
    public void onUsersGotten(List<UserEntity> currentUsers) {
        userTableView.setItems(FXCollections.observableArrayList(currentUsers));
    }

    @Override
    public void shutdown() {
        currentData.removeListener(this);
    }


    private void fetchRequest(String res){
        // TODO fetch from server and update usertableview with results
        System.out.println("fetching res (-> \"".concat(res).concat("\")"));
    }
}
