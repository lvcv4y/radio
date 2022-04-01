package controllers;

import coreclasses.RequestListener;
import coreclasses.RequestTypes;
import coreclasses.dataclasses.CurrentData;
import coreclasses.dataclasses.Request;
import coreclasses.thread_classes.TCPClient;
import coreclasses.thread_classes.UDPAudioClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable, RequestListener {

    @FXML
    private TextField nicknameField;

    @FXML
    private TextField passwordField;

    @FXML
    private TextField keyField;

    @FXML
    private Button connectButton;

    private final CurrentData currentData = CurrentData.getInstance();
    private final TCPClient client = TCPClient.getInstance();

    private void globalSetDisable(boolean disable){
        nicknameField.setDisable(disable);
        passwordField.setDisable(disable);
        keyField.setDisable(disable);
        connectButton.setDisable(disable);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        client.registerListener(this);
        connectButton.setOnAction(e -> {
            globalSetDisable(true);
            final String nickname = nicknameField.getText();
            final String password = passwordField.getText();
            final String key = keyField.getText();

            if(nickname == null || password == null || key == null){
                AlertBox.showErrorBox("Erreur - argument manquant", "Argument manquant",
                        "Vous devez remplir tous les champs pour vous connecter");
                return;
            }

            if(nickname.isEmpty() || password.isEmpty() || key.isEmpty()){
                AlertBox.showErrorBox("Erreur - argument manquant", "Argument manquant",
                        "Vous devez remplir tous les champs pour vous connecter");
                return;
            }

            if(!client.isRunning()){
                    new Thread(() -> {
                        try {
                            client.connect();
                        } catch(IOException ex){
                            Platform.runLater(() -> {
                                AlertBox.ExceptionError("Erreur - Reseau", "Erreur lors de la connexion au serveur",
                                    "Une erreur est apparue lors de la connexion au serveur. Veuillez vérifier que " +
                                            "vous etes bien connecté à internet. Il se peut aussi l'erreur vienne du serveur.", ex);
                                globalSetDisable(false);
                            });
                        }
                    }).start();
            }

            final Request loginRequest = new Request(RequestTypes.LOGIN.getName());
            loginRequest.addStringArgs(nickname, password, key);
            client.query(loginRequest);
        });
    }

    @Override
    public void onReceived(Request req) {
        if(RequestTypes.LOGIN.getName().equals(req.getType())){
            if(req.isError()){
                switch(req.getErrorMessage()){

                    case "NOT FOUND":
                        Platform.runLater(() -> AlertBox.showErrorBox("Erreur - Connexion impossible", "Utilisateur non trouvé",
                                "les informations rentrées ne correspondent à aucun utilisateur."));
                        break;

                    case "BAD PWD":
                        Platform.runLater(() -> AlertBox.showErrorBox("Erreur - Connexion impossible", "Mot de passe erroné",
                                "le mot de passe entré est erroné."));
                        break;

                    case "BAD KEY":
                        Platform.runLater(() -> AlertBox.showErrorBox("Erreur - Connexion impossible", "Clé erronée",
                                "La clé entrée est erronée."));
                        break;

                    default:
                        Platform.runLater(() -> AlertBox.showErrorBox("Erreur - Connexion impossible", "Erreur inconnue",
                                "Une erreur inconnue a été renvoyée par le serveur : ".concat(req.getErrorMessage())));
                        break;
                }

                Platform.runLater(() -> globalSetDisable(false));

            } else if(req.getStringArgs() != null && req.getStringArgs().size() > 0){
                if("LOGGED".equals(req.getStringArgs().get(0))){
                    currentData.login(nicknameField.getText(), passwordField.getText(), keyField.getText());
                    Platform.runLater(() -> {
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/mainview.fxml"));
                            Parent root = loader.load();
                            MainController controller = loader.getController();
                            final Stage stage = new Stage();
                            stage.setTitle("B-Rap Radio - Admin Client");
                            stage.setScene(new Scene(root));
                            stage.setMinHeight(700);
                            stage.setMinWidth(900);
                            stage.setOnHidden(e -> controller.shutdown());
                            stage.show();
                            ((Stage) connectButton.getScene().getWindow()).close();
                            client.unregisterListener(this);
                        } catch (IOException e) {
                            AlertBox.ExceptionError("Erreur - lancement de l'application", "Erreur lors du lancement" +
                                    " de l'application", "Une erreur s'est produite lors du lancement de la fenetre principale", e);
                        }
                    });
                }
            } else {
                final String details = "\ntype : LOGIN\nisError : false\nStringArgs : ".concat(String.valueOf(req.getStringArgs()));
                Platform.runLater(() -> {
                        AlertBox.showErrorBox("Erreur - réponse inconnue", "Réponse inconnue recue",
                        "Une réponse inconnue a été renvoyée par le serveur (virez moi). Details de la réponse recue : ".concat(details));
                        globalSetDisable(false);
                });
            }
        }
    }

    public void shutdown(){
        client.shutdown();
        UDPAudioClient.getInstance().disconnect();
        // TODO save currentData
    }
}
