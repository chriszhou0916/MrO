package projectile;
/**
 * Starts application and binds height and width properties*
 * @author Chris
 * @version 6/6/15
 */
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    /**
     * launches javafx application, loads fxml (UI layout instructions), binds key values
     * @param primaryStage stud
     * @throws Exception we don't want any exceptions right?
     */
    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("UILayout.fxml"));
        primaryStage.setTitle("Projectile-Motion");
        //specifies initial window size
        Scene scene = new Scene(root, 965 ,600);
        primaryStage.setScene(scene);
        //binding properties: whenever the second value changes, the first value changes
        //as well. This is known as one directional binding
        //you can also do bi-directional binding as well
        //here, the width changes with respect to the height, while preserving the initial
        //rato of the window
        primaryStage.minWidthProperty().bind(scene.heightProperty().multiply(965.0/600));
        primaryStage.maxWidthProperty().bind(scene.heightProperty().multiply(965.0/600));
        primaryStage.show();
    }

    /**
     * starts the app
     * @param args stud
     */
    public static void main(String[] args) {
        launch(args);
    }
}
