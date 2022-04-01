package controllers;

import coreclasses.RequestTypes;
import coreclasses.dataclasses.CurrentData;
import coreclasses.dataclasses.Request;
import coreclasses.thread_classes.TCPClient;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;


import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;


public class ChatController implements Initializable, Closable, CurrentData.Refreshable {

    @FXML
    private TextArea messageTextArea;

    @FXML
    private Button sendButton;

    @FXML
    private VBox chatVBox;

    private final TCPClient client = TCPClient.getInstance();

    private final CurrentData currentData = CurrentData.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        currentData.addListener(this);

        for(Map.Entry<String, String> messages : currentData.getMessages().entrySet()){
            onNewChatMessage(messages.getKey(), messages.getValue());
        }

        messageTextArea.setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.ENTER){
                if(!(event.isShiftDown() || event.isAltDown() || event.isControlDown())) {
                    send();
                    event.consume();
                } else {
                    messageTextArea.appendText("\n");
                }
            }
        });

        sendButton.setOnAction(e -> send());
    }

    public void send(){

        final String msg = messageTextArea.getText();
        messageTextArea.clear();

        if(msg != null) {
            if (msg.isEmpty() || msg.trim().isEmpty()) {
                AlertBox.showErrorBox("Chat - Erreur", "Message vide", "vous devez entrer le message à envoyer dans la zone de texte");
                return;
            }
        } else {
            AlertBox.showErrorBox("Chat - Erreur", "Message null", "vous devez entrer le message à envoyer dans la zone de texte");
            return;
        }

        final String userName = currentData.getUsername();

        if(userName == null){
            AlertBox.showErrorBox("Chat - Erreur", "Pseudo nul", "Votre pseudo n'a pas pu etre récupéré," +
                    " ce qui ne doit pas arriver (penser a me virer si vous voyez ce message");
        }

        final Request r = new Request(RequestTypes.CHAT_MSG.getName());
        r.addStringArgs(userName, msg);
        client.query(r);

    }

    @Override
    public void onNewChatMessage(String name, String message) {
        final HBox hbox = new HBox(new Label(name.concat(" : ")), new Label(message));
        chatVBox.getChildren().add(hbox);
    }

    @Override
    public void shutdown() {
        currentData.removeListener(this);
    }
}
