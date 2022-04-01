package controllers;

import coreclasses.RequestTypes;
import coreclasses.dataclasses.*;
import coreclasses.thread_classes.UDPAudioClient;
import coreclasses.thread_classes.TCPClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class StreamController implements Initializable, CurrentData.Refreshable, Closable {

    @FXML
    private Label eventTitle;

    @FXML
    private Label eventDesc;

    @FXML
    private VBox chatBox;

    @FXML
    private TextField chatField;

    @FXML
    private Button chatSend;

    @FXML
    private VBox channelBox;

    @FXML
    private Button channelButton;

    @FXML
    private Label isBackTrackOn;

    @FXML
    private Label isFreestyleOn;

    @FXML
    private Button startStream;

    private static boolean connected;

    private final CurrentData currentData = CurrentData.getInstance();

    private final TCPClient client = TCPClient.getInstance();

    private final UDPAudioClient channel = UDPAudioClient.getInstance();

    private final List<UserEntity> channelUsers = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        client.query(new Request(RequestTypes.GET_CHANNEL_USERS.getName()));

        final MusicInfos currentMusic = currentData.getCurrentMusic();
        final Event currentEvent = currentData.getCurrentEvent();

        if(currentEvent != null){
            onChangingEvent(currentEvent);
        } else if(currentMusic != null){
            onNewPlaying(currentMusic);
        } else {
            changeCurrentEvent("Diffusion en cours inconnue", "la diffusion en cours n'a pas pu être récupérée" +
                    " (si vous êtes bien connecté au serveur, ce message sera remplacé dès que le serveur changera de diffusion" +
                    " (en jouant la prochaine musique ou en commençant le prochain évènement)");
        }

        currentData.addListener(this);

        isBackTrackOn.setText(currentData.isBackTrackOn() ? "ON" : "OFF");
        isFreestyleOn.setText(currentData.isFreestyleOn() ? "ON" : "OFF");

        chatSend.setOnAction(e -> {
            sendMessage(chatField.getText());
            chatField.clear();
        });

        chatField.setOnAction(e -> chatSend.fire());

        channelButton.setOnAction(e -> {
            if(connected){
                new Thread(this::disconnect).start();
            } else {
                new Thread(this::connect).start();
            }
            channelButton.setDisable(true);
        });

        startStream.setOnAction(e -> {
            try {
                Parent addWindow = FXMLLoader.load(getClass().getResource("/gui/startstreamview.fxml"));
                Stage stage = new Stage();
                stage.setScene(new Scene(addWindow));
                stage.setHeight(500);
                stage.setWidth(800);
                stage.setTitle("Prendre l'antenne");
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.show();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });

        /* TODO
         * - fetch current users in channel
         * - button pour prendre l'antenne
         * - formatter pseudo channel vocal ?
         */
    }

    @Override
    public void onNewChatMessage(String name, String message) {
        final HBox hbox = new HBox(new Label(name.concat(" : ")), new Label(message));
        chatBox.getChildren().add(hbox);
    }

    @Override
    public void onNewPlaying(MusicInfos newPlayingMusic) {
        changeCurrentEvent("Diffusion de la radio", newPlayingMusic.getTitle().concat(" : ").concat(newPlayingMusic.getAuthors()));
    }

    @Override
    public void onChangingEvent(Event newCurrentEvent) {
        changeCurrentEvent(newCurrentEvent.getTitle(), newCurrentEvent.getDescription());
    }

    @Override
    public void onFreestyleStatusChange(boolean newValue) {
        isFreestyleOn.setText(newValue ? "ON" : "OFF");
    }

    @Override
    public void onBackTrackStatusChange(boolean newValue) {
        isBackTrackOn.setText(newValue ? "ON" : "OFF");
    }

    @Override
    public void onUserConnectingToChannel(UserEntity connectingUser) {
        channelBox.getChildren().add(new HBox(new Label(connectingUser.getNickname())));
        channelUsers.add(connectingUser);
    }

    @Override
    public void onUserDisconnectingToChannel(UserEntity disconnectingUser) {
        final List<UserEntity> usersToRemove = channelUsers.stream()
                .filter(u -> u.getNickname().equals(disconnectingUser.getNickname())).collect(Collectors.toList());

        for(UserEntity u : usersToRemove){
            channelBox.getChildren().remove(channelUsers.indexOf(u));
        }

        channelUsers.removeAll(usersToRemove);
    }

    @Override
    public void onGettingChannelUserList(List<UserEntity> connectedUsers) {
        channelBox.getChildren().clear();
        channelUsers.clear();
        for(UserEntity u : connectedUsers){
            channelBox.getChildren().add(new HBox(new Label(u.getNickname())));
        }
        channelUsers.addAll(connectedUsers);
    }

    @Override
    public void shutdown() {
        currentData.removeListener(this);
    }


    private void changeCurrentEvent(@NotNull final String title, @NotNull final String desc){
        eventTitle.setText(title);
        eventDesc.setText(desc);
    }

    private void sendMessage(String text){
        if(text != null) {
            if (text.isEmpty() || text.trim().isEmpty()) {
                AlertBox.showErrorBox("Chat - Erreur", "Message vide", "vous devez entrer le message à envoyer dans la zone de texte");
                return;
            }
        } else {
            AlertBox.showErrorBox("Chat - Erreur", "Message nul", "vous devez entrer le message à envoyer dans la zone de texte");
            return;
        }

        final String userName = currentData.getUsername();

        if(userName == null){
            AlertBox.showErrorBox("Chat - Erreur", "Pseudo nul", "Votre pseudo n'a pas pu etre récupéré," +
                    " ce qui ne doit pas arriver (penser a me virer si vous voyez ce message");
        }

        final Request r = new Request(RequestTypes.CHAT_MSG.getName());
        r.addStringArgs(userName, text);
        client.query(r);
    }

    private void disconnect(){
        channel.disconnect();
        Platform.runLater(() -> {
            connected = false;
            channelButton.setText("Se connecter");
            channelButton.setDisable(false);
        });

    }

    private void connect() {
        channel.connect();
        Platform.runLater(() -> {
            connected = true;
            channelButton.setText("Se déconnecter");
            channelButton.setDisable(false);
        });
    }
}
