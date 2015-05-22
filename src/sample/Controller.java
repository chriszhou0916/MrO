package sample;
import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
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
    private TextField angleField;
    @FXML
    private TextField initialVField;
    @FXML
    private BorderPane border;

    @FXML
    private Pane centralPane;

    private Canvas bg,trajectoryLayer,projectileLayer;
    private AnimationTimer timer;
    private GraphicsContext gc,gcTraj;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //initialize itemList
        itemList.getItems().addAll("ball", "car", "human","piano");

    }


    public void aboutButtonClicked(){
        AboutViewer.display();
    }
    public void eraseButtonClicked() {
        timer.stop();
        gc.clearRect(0,0,projectileLayer.getWidth(),projectileLayer.getHeight());
        gcTraj.clearRect(0,0,projectileLayer.getWidth(),projectileLayer.getHeight());

    }
    public void fireButtonClicked(){

        Model circle = new Model (10,300,Integer.parseInt(angleField.getText()),Integer.parseInt(initialVField.getText()));
        circle.setG(5);
        circle.initialize();
        circle.fire();
//        Circle shape = new Circle(10,10,10);
//        shape.setFill(Color.RED);
//        shape.setLayoutY(circle.getY());
//        shape.setLayoutX(circle.getX());
        trajectoryLayer = new Canvas(centralPane.getWidth(),centralPane.getHeight());
        projectileLayer = new Canvas(centralPane.getWidth(),centralPane.getHeight());


         gc = projectileLayer.getGraphicsContext2D();
         gcTraj = trajectoryLayer.getGraphicsContext2D();
        gc.setFill(Color.RED);
        gc.fillOval(circle.getX(),circle.getY(),20,20);


//        border.setCenter(shape);
//        centralPane.getChildren().add(shape);
//        Timeline timeline = new Timeline();
//        timeline.setCycleCount(Timeline.INDEFINITE);

        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                gc.clearRect(0,0,projectileLayer.getWidth(),projectileLayer.getHeight());
                circle.step(50);
                gc.fillOval(circle.getX(),circle.getY(),30,30);
                gcTraj.fillOval(circle.getX()+10,circle.getY()+10,5,5);
            }
        };
        timer.start();
        centralPane.getChildren().addAll(trajectoryLayer,projectileLayer);
//        KeyFrame moveBall = new KeyFrame(Duration.seconds(.02),e -> {
//            shape.setTranslateX(1);
//        });
//        timeline.getKeyFrames().add(moveBall);
//        timeline.play();



    }
}
