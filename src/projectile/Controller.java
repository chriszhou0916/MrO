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
    private double barrelOriginalX, barrelOriginalY, barrelLength, originalAngle;
    private ImageView mroView, ziqiView;
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
        itemList.getItems().addAll("ball", "ziqi", "Mr.O");
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
        makeDrag3(canonViewer, canonBackCircle, canonBarrel);
        targetViewer.setImage(img);
        makeDrag(targetViewer);
        //only draggable vertically
        makeDragUpDown(ground,canonViewer,canonBackCircle);

        //another lambda expression, adjusts canon angle whenever the user inputs a new angle in the angleField
        angleField.setOnKeyReleased(e -> {
            //checks if input is valid, if not, the event is consumed
            if (!isDouble(angleField))
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
        barrelLength = Math.sqrt((barrelOriginalX - centerX) * (barrelOriginalX - centerX) + (barrelOriginalY - centerY) * (barrelOriginalY - centerY));
        originalAngle = Math.toDegrees(Math.atan((centerY - barrelOriginalY) / (barrelOriginalX - centerX)));
    }

    /**
     * called whenever the about button is clicked, displays about window
     */
    public void aboutButtonClicked() {
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
        if (selected.equals("Mr.O"))
            mroView.setVisible(false);
        else if (selected.equals("ziqi"))
            ziqiView.setVisible(false);
        else if (selected.equals("ball"))
            canonBarrel.setVisible(false);
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
        if (!isDouble(angleField) || !isDouble(initialVField) || !isDouble(gravityField))
            return;
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
        if (selected.equals("ball")) {
            centralPane.getChildren().remove(canonBarrel);
            centralPane.getChildren().add(canonBarrel);
        }
        if (selected.equals("Mr.O")) {
            Image mrO = new Image("mro.jpg");
            mroView = new ImageView(mrO);
            centralPane.getChildren().add(mroView);
            mroView.setVisible(false);
        } else if (selected.equals("ziqi")) {
            Image ziqi = new Image("ziqi.jpg");
            ziqiView = new ImageView(ziqi);
            ziqiView.setFitWidth(100);
            ziqiView.setFitHeight(120);
            centralPane.getChildren().add(ziqiView);
            ziqiView.setVisible(false);
        }
        timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        //a key frame is executed after a certain amount of time in the timeline
        timeline.getKeyFrames().add(
                new KeyFrame(Duration.millis(30),
                        //lambda expression again, this time notice the body is super long, instead of one line
                        e -> {
                            //checks if the projectile hits the ground
                            if(stopAnimation()){
                                timeline.stop();
                                fieldsTimeLine.stop();
                                return;
                            }
                            //increments model
                            model.step(30);
                            if (selected.equals("Mr.O")) {
                                mroView.setVisible(true);
                                mroView.relocate(model.getX() - 50, model.getY() - 62.5);
                            } else if (selected.equals("ball")) {
                                if (!isDouble(diameterField)) {
                                    ErrorMessage.showMessage("please enter diameter for ball");
                                    return;
                                }
                                //saves diameter of the ball
                                double diameterSize = Double.parseDouble(diameterField.getText());
                                canonBarrel.setVisible(true);
                                canonBarrel.setRadius(diameterSize / 2);
                                canonBarrel.relocate(model.getX() - diameterSize / 2, model.getY() - diameterSize / 2);
                            } else if (selected.equals("ziqi")) {
                                ziqiView.setVisible(true);
                                ziqiView.relocate(model.getX() - 50, model.getY() - 60);
                            }
                            //draws track
                            if (showTrack.isSelected())
                                gcTraj.fillOval(model.getX(), model.getY(), 5, 5);
                            //checks collisions
                            if (!collisionDetected) {
                                if (selected.equals("ball"))
                                    detectCollision(canonBarrel, targetViewer);
                                else if (selected.equals("Mr.O"))
                                    detectCollision(mroView, targetViewer);
                                else if (selected.equals("ziqi"))
                                    detectCollision(ziqiView, targetViewer);
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
        canonBarrel.relocate(centerX+barrelOriginalX,centerY-barrelOriginalY);
        System.out.println(canonBarrel.getLayoutX()+" "+canonBarrel.getLayoutY());
        System.out.println(canonViewer.getRotate());
        Rotate rotation = new Rotate(canonViewer.getRotate(),centerX,centerY);
        Point2D transformed = rotation.transform(canonBarrel.getLayoutX(),canonBarrel.getLayoutY());
        canonBarrel.relocate(transformed.getX(),transformed.getY());
        System.out.println(canonBarrel.getLayoutX()+" "+canonBarrel.getLayoutY());
//        double newAngle = originalAngle - canonViewer.getRotate();
//        double newX = centerX + barrelLength * Math.cos(Math.toRadians(newAngle));
//        double newY = centerY - barrelLength * Math.sin(Math.toRadians(newAngle));
//        canonBarrel.relocate(newX, newY - 15);
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
    public void makeDragUpDown(Node n,Node m, Node k){
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
     * @param k other element to be moved with main element
     */
    private void makeDrag3(Node n, Node m,Node k) {
        final Delta dragD = new Delta();
        final Delta dragD2 = new Delta();
        final Delta dragD3 = new Delta();
        n.setOnMousePressed(e -> {
            // record a delta distance for the drag and drop operation.
            dragD.x = n.getLayoutX() - e.getSceneX();
            dragD.y = n.getLayoutY() - e.getSceneY();
            dragD2.x = m.getLayoutX() - e.getSceneX();
            dragD2.y = m.getLayoutY() - e.getSceneY();
            dragD3.x = k.getLayoutX() - e.getSceneX();
            dragD3.y = k.getLayoutY() - e.getSceneY();
            n.setCursor(Cursor.MOVE);
        });
        n.setOnMouseReleased(e -> {
            n.setCursor(Cursor.HAND);
        });
        n.setOnMouseDragged(e -> {
            n.setLayoutX(e.getSceneX() + dragD.x);
            n.setLayoutY(e.getSceneY() + dragD.y);
            m.setLayoutX(e.getSceneX() + dragD2.x);
            m.setLayoutY(e.getSceneY() + dragD2.y);
            k.setLayoutX(e.getSceneX() + dragD3.x);
            k.setLayoutY(e.getSceneY() + dragD3.y);
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
    private boolean isDouble(TextField input) {
        if (input.getText().isEmpty() || input.getText().equals("-"))
            return false;
        try {
            double age = Double.parseDouble(input.getText());
            return true;
        } catch (NumberFormatException e) {
            ErrorMessage.showMessage("Please enter valid number");
            return false;
        }
    }

    /**
     * detects collision between two Nodes
     * @param a
     * @param b
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
