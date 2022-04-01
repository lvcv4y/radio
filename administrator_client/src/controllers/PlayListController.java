package controllers;

import coreclasses.dataclasses.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableView;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class PlayListController implements Initializable, CurrentData.Refreshable, Closable {

    @FXML
    private TableView<MusicEntity> playListTableView;

    private final CurrentData currentData = CurrentData.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentData.addListener(this);

        final ObservableList<MusicEntity> currentPlaylist = FXCollections.observableArrayList();

        for(MusicInfos infos : currentData.getPlaylist()){
            currentPlaylist.add(new MusicEntity(currentPlaylist.size(), infos));
        }

        playListTableView.setItems(currentPlaylist);
    }

    @Override
    public void onAddInPlayList(MusicInfos infos) {
        playListTableView.getItems().add(new MusicEntity(playListTableView.getItems().size(), infos));
    }

    @Override
    public void onDeleteInPlayList(MusicInfos infos) {
        final ObservableList<MusicEntity> items = playListTableView.getItems();
        items.removeIf(m -> {
            try {
                return m.getTitle().equals(infos.getTitle()) || m.getAuthors().equals(infos.getAuthors());
            } catch (NullPointerException ignored) { return false; }
        });

        for(MusicEntity entity : items){
            entity.setId(items.indexOf(entity));
        }
    }

    @Override
    public void onPlaylistGotten(List<MusicInfos> currentPlaylist) {

        final ObservableList<MusicEntity> observable = FXCollections.observableArrayList();

        for(MusicInfos infos : currentData.getHistorique()){
            observable.add(new MusicEntity(currentPlaylist.size(), infos));
        }

        playListTableView.setItems(observable);
    }

    @Override
    public void shutdown() {
        currentData.removeListener(this);
    }
}
