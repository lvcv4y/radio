import controllers.LoginController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class App extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("gui/loginview.fxml"));
        Parent root = loader.load();
        LoginController controller = loader.getController();
        primaryStage.setOnHidden(e -> controller.shutdown());
        primaryStage.setTitle("B-Rap Radio - Connexion");
        primaryStage.setScene(new Scene(root));
        primaryStage.setMinHeight(400);
        primaryStage.setMinWidth(400);
        primaryStage.show();
    }
}
