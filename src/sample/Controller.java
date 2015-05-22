package sample;
import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;


public class Controller implements Initializable{
    @FXML
    private ListView<String> itemList;

    @FXML
    private Button aboutButton;

    @FXML
    private Button fireButton;

    @FXML
    private TextField timeField;


    @FXML
    private Group centralPane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //initialize itemList
        itemList.getItems().addAll("ball", "car", "human","piano");
    }


    public void aboutButtonClicked(){
        AboutViewer.display();
    }
    public void fireButtonClicked(){
        centralPane = new Group();
        Model circle = new Model (200,300,45,20);

        circle.initialize();
        circle.fire();
        Circle shape = new Circle(20, Color.RED);
        shape.setLayoutY(circle.getY());
        shape.setLayoutX(circle.getX());
        centralPane.getChildren().add(shape);
        Timeline timeline = new Timeline();
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(20),
                e -> {
                    circle.step(20);
                    shape.setLayoutY(circle.getY());
                    shape.setLayoutX(circle.getX());
                }));



    }
}
