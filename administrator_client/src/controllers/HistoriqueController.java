package controllers;

import coreclasses.dataclasses.CurrentData;
import coreclasses.dataclasses.MusicEntity;
import coreclasses.dataclasses.MusicInfos;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableView;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class HistoriqueController implements Initializable, Closable, CurrentData.Refreshable {

    @FXML
    private TableView<MusicEntity> historiqueTableView;

    private final CurrentData currentData = CurrentData.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentData.addListener(this);
        final ObservableList<MusicEntity> currentHistorique = FXCollections.observableArrayList();

        for(MusicInfos infos : currentData.getHistorique()){
            currentHistorique.add(new MusicEntity(currentHistorique.size(), infos));
        }

        historiqueTableView.setItems(currentHistorique);
    }

    @Override
    public void onDeleteInHistorique(MusicInfos infos) {

        ObservableList<MusicEntity> items = historiqueTableView.getItems();

        items.removeIf(m -> {
                    try {
                        return m.getTitle().equals(infos.getTitle()) || m.getAuthors().equals(infos.getAuthors());
                    } catch (NullPointerException ignored){ return false; }
                }
        );

        for(MusicEntity entity : items){
            entity.setId(items.indexOf(entity));
        }
    }

    @Override
    public void onAddInHistorique(MusicInfos infos) {
        historiqueTableView.getItems().add(new MusicEntity(historiqueTableView.getItems().size(), infos));

        while(historiqueTableView.getItems().size() > CurrentData.HISTORIQUE_SIZE){
            historiqueTableView.getItems().remove(CurrentData.HISTORIQUE_SIZE);
        }
    }

    @Override
    public void onHistoriqueGotten(List<MusicInfos> currentHistorique) {

        final ObservableList<MusicEntity> observable = FXCollections.observableArrayList();

        for(MusicInfos infos : currentData.getHistorique()){
            observable.add(new MusicEntity(currentHistorique.size(), infos));
        }


        historiqueTableView.setItems(observable);
    }

    @Override
    public void shutdown() {
        currentData.removeListener(this);
    }
}
