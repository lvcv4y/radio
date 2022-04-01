package controllers;

import coreclasses.RequestTypes;
import coreclasses.TreeViewNames;
import coreclasses.dataclasses.CurrentData;
import coreclasses.dataclasses.Request;
import coreclasses.thread_classes.TCPClient;
import coreclasses.thread_classes.UDPAudioClient;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable, CurrentData.Refreshable {

    @FXML
    private TreeView<String> treeMenu;

    @FXML
    private TabPane tabPane;

    @FXML
    private VBox chatVBox;

    @FXML
    private Button sendChat;

    @FXML
    private TextField messageTextField;

    private final CurrentData currentData = CurrentData.getInstance();
    private final TCPClient client = TCPClient.getInstance();

    private TreeItem<String> freestyleBeatItem, backtrackBeatItem;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        currentData.addListener(this);

        TreeItem<String> antenne, radio, users, root;
        root = new TreeItem<>();
        root.setExpanded(true);

        antenne = makeTreeItem("Antenne", root);
        freestyleBeatItem = makeTreeItem("Instru - ".concat(currentData.isFreestyleOn() ? "ON" : "OFF"), antenne);
        backtrackBeatItem = makeTreeItem("Fond sonore - ".concat(currentData.isBackTrackOn() ? "ON" : "OFF"), antenne);
        makeTreeItem("Prendre l'antenne", antenne);


        radio = makeTreeItem("Radio", root);
        makeTreeItem("Playlist", radio);
        makeTreeItem("Historique", radio);
        makeTreeItem("Chat", radio);
        makeTreeItem("Events", radio);


        users = makeTreeItem("Bases de données", root);
        makeTreeItem("Utilisateurs", users);
        makeTreeItem("Sanctions", users);
        makeTreeItem("Admins", users);

        treeMenu.setRoot(root);
        treeMenu.setShowRoot(false);
        treeMenu.setCellFactory(tree -> {
            TreeCell<String> cell = new TreeCell<String>() {
                @Override
                public void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty) ;
                    if (empty) {
                        setText(null);
                    } else {
                        setText(item);
                    }
                }
            };

            cell.setOnMouseClicked(event -> {
                if (!cell.isEmpty() && event.getClickCount() >= 2) {
                    event.consume();
                    TreeItem<String> treeItem = cell.getTreeItem();
                    final TreeViewNames tvn = TreeViewNames.getObjectFromName(treeItem.getValue());
                    if(tvn != null){
                        try {
                            final FXMLLoader loader = new FXMLLoader(getClass().getResource(tvn.getFileName()));
                            final Tab tab = new Tab(tvn.getName(), loader.load());
                            final Object controller = loader.getController();
                            tab.setOnClosed(e -> {
                                if(controller instanceof Closable)
                                    ((Closable) controller).shutdown();

                            });
                            tabPane.getTabs().add(tab);

                        } catch(IOException | NullPointerException e){
                            // e.printStackTrace();
                            System.out.println(e.getCause() == null ? "cause null" : e.getCause());
                            System.out.println(e.getMessage());
                        }
                    }
                }
            });

            return cell;
        });

        sendChat.setOnAction(e -> send());
        messageTextField.setOnAction(e -> send());
    }

    private TreeItem<String> makeTreeItem(String title, TreeItem<String> parent){
        TreeItem<String> item = new TreeItem<>(title);
        parent.getChildren().add(item);
        return item;
    }

    public void send(){

        final String msg = messageTextField.getText();
        messageTextField.clear();

        if(msg != null) {
            if (msg.isEmpty() || msg.trim().isEmpty()) {
                AlertBox.showErrorBox("Chat - Erreur", "Message vide", "vous devez entrer le message à envoyer dans la zone de texte");
                return;
            }
        } else {
            AlertBox.showErrorBox("Chat - Erreur", "Message nul", "vous devez entrer le message à envoyer dans la zone de texte");
            return;
        }

        final String userName = currentData.getUsername();

        if(userName == null){
            AlertBox.showErrorBox("Chat - Erreur", "Pseudo null", "Votre pseudo n'a pas pu être récupéré," +
                    " ce qui ne doit pas arriver (penser à me virer si vous voyez ce message)");
            return;
        }

        final Request r = new Request(RequestTypes.ADMIN_CHAT_MSG.getName());
        r.addStringArgs(userName, msg);
        client.query(r);
    }

    @Override
    public void onNewAdminChatMessage(String name, String message) {
        final HBox hbox = new HBox(new Label(name.concat(" : ")), new Label(message));
        chatVBox.getChildren().add(hbox);
    }

    @Override
    public void onFreestyleStatusChange(boolean newValue) {
        final String newText = "Instru - ".concat(newValue ? "ON" : "OFF");
        freestyleBeatItem.setValue(newText);
    }

    @Override
    public void onBackTrackStatusChange(boolean newValue) {
        final String newText = "Fond sonore - ".concat(newValue ? "ON" : "OFF");
        backtrackBeatItem.setValue(newText);
    }

    public void shutdown(){
        client.shutdown();
        UDPAudioClient.getInstance().disconnect();
        // TODO save currentData
    }
}
