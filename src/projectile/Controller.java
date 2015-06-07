package projectile;
/**
 * Controls all the components, serves as a link between fxml and the app
 * @author Chris
 * @version 6/6/15
 */
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    private final int ANIMATION_INTERVAL = 30;
    @FXML
    private ListView<String> itemList;
    @FXML
    private CheckBox showTrack;
    @FXML
    private TextField rangeField,heightField, angleField, timeField, initialVField, diameterField, gravityField;
    @FXML
    private BorderPane border;
    @FXML
    private VBox rightMenu;
    @FXML
    private ImageView targetViewer, backgroundViewer, canonViewer;
    @FXML
    private Pane centralPane;
    @FXML
    private Circle canonBackCircle, canonBarrel;
    @FXML
    private Rectangle ground;
    @FXML
    private Text scoreText;

    private Canvas trajectoryLayer;
    private GraphicsContext gcTraj;
    private ProjectileModel model;
    private boolean isAnimating, collisionDetected, textPrinted;
    private double barrelOriginalX, barrelOriginalY;
    private ImageView mroView, ziqiView, pianoView,tankView,pizzaView,humanView,tomatoView;
    private String selected;
    private Timeline timeline, fieldsTimeLine;

    /**
     * this method is called whenever the program starts. it includes essential information
     * that were not defined in the fxml, such as bindings, image locations, and some initial
     * calculations
     * @param location stud
     * @param resources stud
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //here are a bunch of bindings. I want every container in the program to automatically resize
        //as the window is resized
        centralPane.prefHeightProperty().bind(border.heightProperty());
        rightMenu.prefHeightProperty().bind(border.heightProperty());
        ground.widthProperty().bind(backgroundViewer.fitWidthProperty());

        //initialize itemList for projectiles
        itemList.getItems().addAll("ball", "ziqi", "Mr.O","piano","tank","pizza","adult human","mystery");
        //sets ball as default projectile
        itemList.getSelectionModel().selectFirst();
        //imports the background picture and sets it into the ImageViewer
        Image background = new Image("plain-farm-background.png");
        backgroundViewer.setImage(background);
        //binds image to the window size
        backgroundViewer.fitHeightProperty().bind(rightMenu.heightProperty());
        backgroundViewer.fitWidthProperty().bind(border.widthProperty());

        Image img = new Image("target-round.png");
        Image canon = new Image("canon.png");
        canonViewer.setImage(canon);
        //makes these things draggable as a group
        makeDrag2(canonViewer, canonBackCircle);
        targetViewer.setImage(img);
        makeDrag(targetViewer);
        //only draggable vertically
        makeDragUpDown(ground,canonViewer,canonBackCircle);

        //another lambda expression, adjusts canon angle whenever the user inputs a new angle in the angleField
        angleField.setOnKeyReleased(e -> {
            //checks if input is valid, if not, the event is consumed
            if (notDouble(angleField))
                e.consume();
            else
                canonViewer.setRotate(-Double.parseDouble(angleField.getText()));
        });
        //make these two buddies rotate together with respect to the center as a group
        makeRotate2(canonBackCircle, canonViewer);

        double centerX = canonViewer.getLayoutX() + canonViewer.getFitWidth() / 2;
        double centerY = canonViewer.getLayoutY() + canonViewer.getFitHeight() / 2;
        barrelOriginalX = canonBarrel.getLayoutX() - centerX;
        barrelOriginalY = centerY - canonBarrel.getLayoutY();
    }

    /**
     * called whenever the about button is clicked, displays about window
     */
    public void aboutButtonClicked() {
        try {
            int key = Integer.parseInt(diameterField.getText());
            if(key==923)
                targetViewer.setImage(new Image("persons.jpg"));
        }catch (NumberFormatException e){
            ErrorMessage.showMessage("hint: easter egg hides here");
        }
        AboutViewer.display();
    }

    /**
     * called whenever the erase button is clicked, clears everything
     */
    public void eraseButtonClicked() {
        //false proof steps, if the user didn't fire the projectile first, there is nothing to erase
        if (selected == null || model == null)
            return;
        isAnimating = false;
        timeline.stop();
        fieldsTimeLine.stop();
        //clears out the canvas so you can drag and rotate components again, or else the canvas covers up everything
        centralPane.getChildren().remove(trajectoryLayer);
        //clears canvas
        gcTraj.clearRect(0, 0, trajectoryLayer.getWidth(), trajectoryLayer.getHeight());
        //get rid of score text
        scoreText.setVisible(false);
        textPrinted = false;
        collisionDetected = false;
        switch (selected) {
            case "Mr.O":
                mroView.setVisible(false);
                break;
            case "ziqi":
                ziqiView.setVisible(false);
                break;
            case "ball":
                canonBarrel.setVisible(false);
                break;
            case "piano":
                pianoView.setVisible(false);
                break;
            case "tank":
                tankView.setVisible(false);
                break;
            case "pizza":
                pizzaView.setVisible(false);
                break;
            case "adult human":
                humanView.setVisible(false);
                break;
            case "mystery":
                tomatoView.setVisible(false);
                break;
        }
        heightField.clear();
        timeField.clear();
        rangeField.clear();
    }

    /**
     * called whenever the fire button is clicked, generates animations
     */
    public void fireButtonClicked() {
        //shifts the initial location of the projectile to a correct place
        shiftBarrel();
        //stores the selected projectile from the list in a string
        selected = itemList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            ErrorMessage.showMessage("please select a projectile");
            return;
        }
        if (isAnimating) {
            ErrorMessage.showMessage("one animation is running already\nplease click erase");
            return;
        }
        if (notDouble(angleField) || notDouble(initialVField) || notDouble(gravityField))
            return;
        if(selected.equals("ball")&& notDouble(diameterField)){
                return;
        }
        //creates mathematical model
        model = new ProjectileModel(canonBarrel.getLayoutX(), canonBarrel.getLayoutY(), Double.parseDouble(angleField.getText()), Double.parseDouble(initialVField.getText()));
        model.setG(Double.parseDouble(gravityField.getText()));
        model.initialize();
        model.fire();
        isAnimating = true;
        //a canvas is drawable, meaning that you can draw various geometric shapes on it
        trajectoryLayer = new Canvas(backgroundViewer.getFitWidth(), backgroundViewer.getFitHeight());
        gcTraj = trajectoryLayer.getGraphicsContext2D();
        //add canvas to the display
        centralPane.getChildren().add(trajectoryLayer);
        switch (selected) {
            case "ball":
                centralPane.getChildren().remove(canonBarrel);
                centralPane.getChildren().add(canonBarrel);
                break;
            case "Mr.O":
                Image mrO = new Image("mro.jpg");
                mroView = new ImageView(mrO);
                centralPane.getChildren().add(mroView);
                mroView.setVisible(false);
                break;
            case "ziqi":
                Image ziqi = new Image("ziqi.jpg");
                ziqiView = new ImageView(ziqi);
                centralPane.getChildren().add(ziqiView);
                ziqiView.setVisible(false);
                break;
            case "piano":
                Image piano = new Image("grand-piano.png");
                pianoView = new ImageView(piano);
                centralPane.getChildren().add(pianoView);
                pianoView.setVisible(false);
                break;
            case "tank":
                Image tank = new Image("tank3.png");
                tankView = new ImageView(tank);
                centralPane.getChildren().add(tankView);
                tankView.setVisible(false);
                break;
            case "pizza":
                Image pizza = new Image("pizza6.png");
                pizzaView = new ImageView(pizza);
                centralPane.getChildren().add(pizzaView);
                pizzaView.setVisible(false);
                break;
            case "adult human": {
                Image human = new Image("human.png");
                humanView = new ImageView(human);
                centralPane.getChildren().add(humanView);
                humanView.setVisible(false);
                break;
            }
            case "mystery": {
                Image human = new Image("tomato5.png");
                tomatoView = new ImageView(human);
                centralPane.getChildren().add(tomatoView);
                tomatoView.setVisible(false);
                break;
            }
        }

        timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        //a key frame is executed after a certain amount of time in the timeline
        timeline.getKeyFrames().add(
                new KeyFrame(Duration.millis(ANIMATION_INTERVAL),
                        //lambda expression again, this time notice the body is super long, instead of one line
                        e -> {
                            //checks if the projectile hits the ground
                            if(stopAnimation()){
                                timeline.stop();
                                fieldsTimeLine.stop();
                                return;
                            }
                            //increments model
                            model.step(ANIMATION_INTERVAL);
                            switch (selected) {
                                case "Mr.O":
                                    mroView.setVisible(true);
                                    mroView.relocate(model.getX() - 50, model.getY() - 62.5);
                                    break;
                                case "ball":
                                    //saves diameter of the ball
                                    double diameterSize = Double.parseDouble(diameterField.getText());
                                    canonBarrel.setVisible(true);
                                    canonBarrel.setRadius(diameterSize / 2);
                                    canonBarrel.relocate(model.getX() - diameterSize / 2, model.getY() - diameterSize / 2);
                                    break;
                                case "ziqi":
                                    ziqiView.setVisible(true);
                                    ziqiView.relocate(model.getX() - 50, model.getY() - 55.5);
                                    break;
                                case "piano":
                                    pianoView.setVisible(true);
                                    pianoView.relocate(model.getX() - 50, model.getY() - 51.5);
                                    break;
                                case "tank":
                                    tankView.setVisible(true);
                                    tankView.relocate(model.getX() - 75, model.getY() - 48);
                                    break;
                                case "pizza":
                                    pizzaView.setVisible(true);
                                    pizzaView.relocate(model.getX() - 50, model.getY() - 53.5);
                                    break;
                                case "adult human":
                                    humanView.setVisible(true);
                                    humanView.relocate(model.getX() - 50, model.getY() - 51.5);
                                    break;
                                case "mystery":
                                    tomatoView.setVisible(true);
                                    tomatoView.relocate(model.getX() - 50, model.getY() - 50);
                                    break;
                            }
                            //draws track
                            if (showTrack.isSelected())
                                gcTraj.fillOval(model.getX(), model.getY(), 5, 5);
                            //checks collisions
                            if (!collisionDetected) {
                                switch (selected) {
                                    case "ball":
                                        detectCollision(canonBarrel, targetViewer);
                                        break;
                                    case "Mr.O":
                                        detectCollision(mroView, targetViewer);
                                        break;
                                    case "ziqi":
                                        detectCollision(ziqiView, targetViewer);
                                        break;
                                    case "piano":
                                        detectCollision(pianoView, targetViewer);
                                        break;
                                    case "tank":
                                        detectCollision(tankView, targetViewer);
                                        break;
                                    case "pizza":
                                        detectCollision(pizzaView, targetViewer);
                                        break;
                                    case "adult human":
                                        detectCollision(humanView, targetViewer);
                                        break;
                                    case "mystery":
                                        detectCollision(tomatoView, targetViewer);
                                        break;
                                }
                            } else if (!textPrinted) {
                                scoreText.setVisible(true);
                                textPrinted = true;
                            }
                        }));
        //second timeline used to monitor data fields
        fieldsTimeLine = new Timeline();
        fieldsTimeLine.setCycleCount(Timeline.INDEFINITE);
        //this timeline updates every 1 second instead of every 30 milisec
        fieldsTimeLine.getKeyFrames().add(new KeyFrame(Duration.seconds(1),
                e -> updateFields()));

        timeline.playFromStart();
        fieldsTimeLine.playFromStart();
    }

    /**
     * update data fields from model, everything rounded to two decimal places
     */
    private void updateFields() {
        heightField.setText(round(model.getAltitude())+"");
        timeField.setText(round(model.getTimeElapsed())+"");
        rangeField.setText(round(model.getRange())+"");
    }

    /**
     * updates location of barrel(initial starting point of projectile), according to angle of rotation of the canon
     */
    private void shiftBarrel() {
        double centerX = canonViewer.getLayoutX() + canonViewer.getFitWidth() / 2;
        double centerY = canonViewer.getLayoutY() + canonViewer.getFitHeight() / 2;
        canonBarrel.setLayoutX(centerX+barrelOriginalX);
        canonBarrel.setLayoutY(centerY-barrelOriginalY);
        Rotate rotation = new Rotate(canonViewer.getRotate(),centerX,centerY);
        Point2D transformed = rotation.transform(canonBarrel.getLayoutX(),canonBarrel.getLayoutY());
        canonBarrel.setLayoutX(transformed.getX());
        canonBarrel.setLayoutY(transformed.getY());
    }

    /**
     * adds dragging functionality to a node
     * @param n Node (element) to be dragged
     */
    /*
    Partially taken from StackOverFlow
    Author's Name: ItachiUchiha
    Date Created: March 3, 2014
    URL: http://stackoverflow.com/questions/22139615/dragging-buttons-in-javafx
     */
    private void makeDrag(Node n) {
        final Delta dragD = new Delta();
        n.setOnMousePressed(e -> {
            // record a delta distance for the drag and drop operation.
            dragD.x = n.getLayoutX() - e.getSceneX();
            dragD.y = n.getLayoutY() - e.getSceneY();
            n.setCursor(Cursor.MOVE);
        });
        n.setOnMouseReleased(e -> n.setCursor(Cursor.HAND));
        n.setOnMouseDragged(e -> {
            n.setLayoutX(e.getSceneX() + dragD.x);
            n.setLayoutY(e.getSceneY() + dragD.y);
        });
        n.setOnMouseEntered(e -> n.setCursor(Cursor.HAND));
    }

    /**
     * similar to makeDrag, this one is only vertical. drags multiply elements at once
     * @param n main element
     * @param m other element to be moved with main element
     * @param k other element to be moved with main element
     */
    private void makeDragUpDown(Node n, Node m, Node k){
        final Delta dragD = new Delta();
        final Delta dragD2 = new Delta();
        final Delta dragD3 = new Delta();
        n.setOnMousePressed(e -> {
            // record a delta distance for the drag and drop operation.
            dragD.y = n.getLayoutY() - e.getSceneY();
            dragD2.y = m.getLayoutY() - e.getSceneY();
            dragD3.y = k.getLayoutY() - e.getSceneY();
            n.setCursor(Cursor.MOVE);
        });
        n.setOnMouseReleased(e -> n.setCursor(Cursor.HAND));
        n.setOnMouseDragged(e -> {
            n.setLayoutY(e.getSceneY() + dragD.y);
            m.setLayoutY(e.getSceneY() + dragD2.y);
            k.setLayoutY(e.getSceneY() + dragD3.y);
        });
        n.setOnMouseEntered(e -> n.setCursor(Cursor.HAND));
    }
    /**
     * similar to makeDrag, but drags multiply elements at once
     * @param n main element
     * @param m other element to be moved with main element
     */
    private void makeDrag2(Node n, Node m) {
        final Delta dragD = new Delta();
        final Delta dragD2 = new Delta();
        n.setOnMousePressed(e -> {
            // record a delta distance for the drag and drop operation.
            dragD.x = n.getLayoutX() - e.getSceneX();
            dragD.y = n.getLayoutY() - e.getSceneY();
            dragD2.x = m.getLayoutX() - e.getSceneX();
            dragD2.y = m.getLayoutY() - e.getSceneY();
            n.setCursor(Cursor.MOVE);
        });
        n.setOnMouseReleased(e -> n.setCursor(Cursor.HAND));
        n.setOnMouseDragged(e -> {
            n.setLayoutX(e.getSceneX() + dragD.x);
            n.setLayoutY(e.getSceneY() + dragD.y);
            m.setLayoutX(e.getSceneX() + dragD2.x);
            m.setLayoutY(e.getSceneY() + dragD2.y);
        });
        n.setOnMouseEntered(e -> n.setCursor(Cursor.HAND));
    }

    /**
     * Rotates two elements, with respect to the center of the first element
     * @param n main element
     * @param m element moving with the main element
     */
    private void makeRotate2(Node n, Node m) {
        final Delta dragD = new Delta();
        n.setOnMousePressed(e -> {
            // record a delta distance for the drag and drop operation.
            dragD.x = canonViewer.getLayoutX() + canonViewer.getFitWidth() / 2;
            dragD.y = canonViewer.getLayoutY() + canonViewer.getFitHeight() / 2;
            n.setCursor(Cursor.MOVE);
        });
        n.setOnMouseReleased(e -> n.setCursor(Cursor.HAND));
        n.setOnMouseDragged(e -> {
            double changex = e.getSceneX() - dragD.x;
            double changey = e.getSceneY() - dragD.y;
            double angle = Math.toDegrees(Math.atan(changey / changex));
            if (changex < 0)
                angle += 180;
            m.setRotate(angle);
            if (angle < 90)
                angle = -angle;
            else
                angle = 360 - angle;
            //updates angle field
            angleField.setText("" + round(angle));
        });
        n.setOnMouseEntered(e -> n.setCursor(Cursor.HAND));
    }

    /**
     * stops the animation if the projectile touches the ground
     * @return result
     */
    private boolean stopAnimation(){
        return (model.getY()>ground.getLayoutY());
    }

    /**
     * checks if a textfield contains a double
     * @param input target textfield to be checked
     * @return result
     */
    private boolean notDouble(TextField input) {
        if (input.getText().isEmpty() || input.getText().equals("-"))
            return true;
        try {
            @SuppressWarnings("UnusedAssignment") double age = Double.parseDouble(input.getText());
            return false;
        } catch (NumberFormatException e) {
            ErrorMessage.showMessage("Please enter valid number");
            return true;
        }
    }

    /**
     * detects collision between two Nodes
     * @param a first component
     * @param b second component
     */
    private void detectCollision(Node a, Node b) {
        if (a.getBoundsInParent().intersects(b.getBoundsInParent()))
            collisionDetected = true;
    }

    /**
     * rounds double to two decimal places
     * @param a number to be rounded
     * @return result
     */
    private double round(double a){
        return Math.round(a*100)/100.0;
    }

    /**
     * helper class for drag functionality
     */
    private static final class Delta {
        double x, y;
    }
}
