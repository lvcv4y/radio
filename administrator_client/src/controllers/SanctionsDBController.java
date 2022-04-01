package controllers;

import coreclasses.dataclasses.CurrentData;
import coreclasses.dataclasses.SanctionEntity;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;


public class SanctionsDBController implements Initializable, CurrentData.Refreshable, Closable {

    @FXML
    private TableView<SanctionEntity> sanctionsTableView;

    @FXML
    private TextField SQLCmdField;

    @FXML
    private Button executeButton;

    @FXML
    private Button addButton;

    private final CurrentData currentData = CurrentData.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        currentData.addListener(this);
        currentData.fetchSanctions();

        sanctionsTableView.setItems(FXCollections.observableArrayList(currentData.getSanctions()));

        executeButton.setOnAction(e -> {
            fetchRequest(SQLCmdField.getText());
            SQLCmdField.clear();
        });

        SQLCmdField.setOnAction(e -> {
            executeButton.fire();
            e.consume();
        });

        addButton.setOnAction(e -> {
            try {
                Parent addWindow = FXMLLoader.load(getClass().getResource("/gui/addsanctionsview.fxml"));
                Stage stage = new Stage();
                stage.setScene(new Scene(addWindow));
                stage.setTitle("Sanctions - Ajouter une sanction");
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.show();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });
    }

    @Override
    public void onAddSanction(SanctionEntity sanction) {
        sanctionsTableView.getItems().add(sanction);
    }

    @Override
    public void onDeleteSanction(SanctionEntity sanction) {
        sanctionsTableView.getItems().removeIf(s -> {
            try {
                return s.getId() == sanction.getId();
            } catch (NullPointerException ignored) { return false; }
        });
    }

    @Override
    public void onSanctionsGotten(List<SanctionEntity> currentSanctions) {
        sanctionsTableView.setItems(FXCollections.observableArrayList(currentSanctions));
    }

    @Override
    public void shutdown(){ currentData.addListener(this); }

    private void fetchRequest(String res) {
        // TODO fetch from server and update usertableview with results
        System.out.println("fetching res (-> \"".concat(res).concat("\")"));
    }
}
