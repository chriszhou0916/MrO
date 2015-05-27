package sample;
import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import javafx.scene.image.*;
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
    private ImageView target;
    @FXML
    private Pane centralPane;

    private Canvas bg,trajectoryLayer,projectileLayer;
    private AnimationTimer timer;
    private GraphicsContext gc,gcTraj;
    private DragC dc;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //initialize itemList
        itemList.getItems().addAll("ball", "car", "human","piano");
        Image img = new Image("file:persons.jpg");
        target.setImage(img);
        makeDrag(target);
        //centralPane.getChildren().add(makeDraggable(target));

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
        if(!isInt(angleField)||!isInt(initialVField))
            return;
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
        Image mrO = new Image("file:mro.jpg");



//        border.setCenter(shape);
//        centralPane.getChildren().add(shape);
//        Timeline timeline = new Timeline();
//        timeline.setCycleCount(Timeline.INDEFINITE);

<<<<<<< HEAD
        String selected = itemList.getSelectionModel().getSelectedItem();
        if(selected == null){
            ErrorMessage.showMessage("please select a projectile");
            return;}
=======
>>>>>>> origin/master
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                gc.clearRect(0,0,projectileLayer.getWidth(),projectileLayer.getHeight());
                circle.step(50);
                gc.drawImage(mrO,circle.getX(),circle.getY());
                //gc.fillOval(circle.getX(),circle.getY(),30,30);
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
    private Node makeDraggable(final Node node) {
        final DragC dragContext = new DragC();
        final Group wrapGroup = new Group(node);


        wrapGroup.addEventFilter(
                MouseEvent.MOUSE_PRESSED,
                mouseEvent -> {

                        // remember initial mouse cursor coordinates
                        // and node position
                        dragContext.mouseAnchorX = mouseEvent.getX();
                        dragContext.mouseAnchorY = mouseEvent.getY();
                        dragContext.initialTranslateX =
                                node.getTranslateX();
                        dragContext.initialTranslateY =
                                node.getTranslateY();
                }
        );

        wrapGroup.addEventFilter(
                MouseEvent.MOUSE_DRAGGED,
                mouseEvent -> {
                        // shift node from its initial position by delta
                        // calculated from mouse cursor movement
                        node.setTranslateX(
                                dragContext.initialTranslateX
                                        + mouseEvent.getX()
                                        - dragContext.mouseAnchorX);
                        node.setTranslateY(
                                dragContext.initialTranslateY
                                        + mouseEvent.getY()
                                        - dragContext.mouseAnchorY);
                }
        );
        return wrapGroup;
    }

    private void makeDrag(Node n){
        final Delta dragDelta = new Delta();
        n.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent mouseEvent) {
                // record a delta distance for the drag and drop operation.
                dragDelta.x = n.getLayoutX() - mouseEvent.getSceneX();
                dragDelta.y = n.getLayoutY() - mouseEvent.getSceneY();
                n.setCursor(Cursor.MOVE);
            }
        });
        n.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent mouseEvent) {
                n.setCursor(Cursor.HAND);
            }
        });
        n.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent mouseEvent) {
                n.setLayoutX(mouseEvent.getSceneX() + dragDelta.x);
                n.setLayoutY(mouseEvent.getSceneY() + dragDelta.y);
            }
        });
        n.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent mouseEvent) {
                n.setCursor(Cursor.HAND);
            }
        });
    }

    private static final class DragC {
        public double mouseAnchorX;
        public double mouseAnchorY;
        public double initialTranslateX;
        public double initialTranslateY;
    }

    private static final class Delta{
        double x,y;
    }

    private boolean isInt(TextField input){
        try{
            int age = Integer.parseInt(input.getText());
            return true;
        }catch(NumberFormatException e){
            ErrorMessage.showMessage("Please enter valid number");
            return false;
        }
    }
}
