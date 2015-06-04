package sample;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {


    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Projectile-Motion");
        Scene scene = new Scene(root, 965 ,600);
        primaryStage.setScene(scene);
        primaryStage.minWidthProperty().bind(scene.heightProperty().multiply(965.0/600));
        primaryStage.maxWidthProperty().bind(scene.heightProperty().multiply(965.0/600));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
