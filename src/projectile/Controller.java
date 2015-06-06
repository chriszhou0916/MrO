package projectile;

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

import java.awt.*;
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
       // border.minWidthProperty().bind(border.heightProperty().multiply(965 / 600));
        centralPane.prefHeightProperty().bind(border.heightProperty());
        rightMenu.prefHeightProperty().bind(border.heightProperty());
        ground.widthProperty().bind(backgroundViewer.fitWidthProperty());
        makeDragUpDown(ground,canonViewer,canonBackCircle);
        //initialize itemList
        itemList.getItems().addAll("ball", "ziqi", "Mr.O", "piano");
        itemList.getSelectionModel().selectFirst();
        Image background = new Image("plain-farm-background.png");
        backgroundViewer.setImage(background);
        backgroundViewer.fitHeightProperty().bind(rightMenu.heightProperty());
        backgroundViewer.fitWidthProperty().bind(border.widthProperty());

        Image img = new Image("target-round.png");

        Image canon = new Image("canon.png");
        canonViewer.setImage(canon);
        makeDrag3(canonViewer, canonBackCircle, canonBarrel);
        targetViewer.setImage(img);
        makeDrag(targetViewer);
        angleField.setOnKeyReleased(e -> {
            if (!isDouble(angleField))
                e.consume();
            else
                canonViewer.setRotate(-Double.parseDouble(angleField.getText()));
        });
        makeRotate2(canonBackCircle, canonViewer);

        double centerX = canonViewer.getLayoutX() + canonViewer.getFitWidth() / 2;
        double centerY = canonViewer.getLayoutY() + canonViewer.getFitHeight() / 2;
        barrelOriginalX = canonBarrel.getLayoutX() - centerX;
        barrelOriginalY = centerY - canonBarrel.getLayoutY();
        barrelLength = Math.sqrt((barrelOriginalX - centerX) * (barrelOriginalX - centerX) + (barrelOriginalY - centerY) * (barrelOriginalY - centerY));
        originalAngle = Math.toDegrees(Math.atan((centerY - barrelOriginalY) / (barrelOriginalX - centerX)));
    }

    public void aboutButtonClicked() {
        AboutViewer.display();
    }

    public void eraseButtonClicked() {
        if (selected == null || model == null)
            return;
        isAnimating = false;
        timeline.stop();
        fieldsTimeLine.stop();
        centralPane.getChildren().remove(trajectoryLayer);
        gcTraj.clearRect(0, 0, trajectoryLayer.getWidth(), trajectoryLayer.getHeight());
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
    }

    public void fireButtonClicked() {
        shiftBarrel();
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
        model = new ProjectileModel(canonBarrel.getLayoutX(), canonBarrel.getLayoutY(), Double.parseDouble(angleField.getText()), Double.parseDouble(initialVField.getText()));

        model.setG(Double.parseDouble(gravityField.getText()));
        model.initialize();
        model.fire();
        isAnimating = true;
        trajectoryLayer = new Canvas(backgroundViewer.getFitWidth(), backgroundViewer.getFitHeight());
        gcTraj = trajectoryLayer.getGraphicsContext2D();
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
        timeline.getKeyFrames().add(
                new KeyFrame(Duration.millis(30),
                        e -> {
                            if(stopAnimation()){
                                System.out.println(1);
                                timeline.stop();
                                fieldsTimeLine.stop();
                                return;
                            }
                            model.step(30);
                            if (selected.equals("Mr.O")) {
                                mroView.setVisible(true);
                                mroView.relocate(model.getX() - 50, model.getY() - 62.5);
                            } else if (selected.equals("ball")) {
                                if (!isDouble(diameterField)) {
                                    ErrorMessage.showMessage("please enter diameter for ball");
                                    return;
                                }
                                double diameterSize = Double.parseDouble(diameterField.getText());
                                canonBarrel.setVisible(true);
                                canonBarrel.setRadius(diameterSize / 2);
                                canonBarrel.relocate(model.getX() - diameterSize / 2, model.getY() - diameterSize / 2);
                            } else if (selected.equals("ziqi")) {
                                ziqiView.setVisible(true);
                                ziqiView.relocate(model.getX() - 50, model.getY() - 60);
                            }
                            if (showTrack.isSelected())
                                gcTraj.fillOval(model.getX(), model.getY(), 5, 5);
                            if (!collisionDetected) {
                                if (selected.equals("ball"))
                                    detectCollision(canonBarrel, targetViewer);
                                else if (selected.equals("Mr.O"))
                                    detectCollision(mroView, targetViewer);
                                else if (selected.equals("ziqi"))
                                    detectCollision(ziqiView, targetViewer);
                            } else if (!textPrinted) {
                                scoreText.setVisible(true);

                            }

                        }));
        fieldsTimeLine = new Timeline();
        fieldsTimeLine.setCycleCount(Timeline.INDEFINITE);
        fieldsTimeLine.getKeyFrames().add(new KeyFrame(Duration.seconds(1),
                e -> updateFields()));

        timeline.playFromStart();
        fieldsTimeLine.playFromStart();
    }

    private void updateFields() {
        heightField.setText(round(model.getAltitude())+"");
        timeField.setText(round(model.getTimeElapsed())+"");
        rangeField.setText(round(model.getRange())+"");
    }

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

    private void makeDrag(Node n) {
        final Delta dragDelta = new Delta();
        n.setOnMousePressed(mouseEvent -> {
            // record a delta distance for the drag and drop operation.
            dragDelta.x = n.getLayoutX() - mouseEvent.getSceneX();
            dragDelta.y = n.getLayoutY() - mouseEvent.getSceneY();
            n.setCursor(Cursor.MOVE);
        });
        n.setOnMouseReleased(e -> n.setCursor(Cursor.HAND));
        n.setOnMouseDragged(mouseEvent -> {
            n.setLayoutX(mouseEvent.getSceneX() + dragDelta.x);
            n.setLayoutY(mouseEvent.getSceneY() + dragDelta.y);
        });
        n.setOnMouseEntered(e -> n.setCursor(Cursor.HAND));
    }

    public void makeDragUpDown(Node n,Node m, Node k){
        final Delta dragDelta = new Delta();
        final Delta dragDelta2 = new Delta();
        final Delta dragDelta3 = new Delta();
        n.setOnMousePressed(mouseEvent -> {
            // record a delta distance for the drag and drop operation.
            dragDelta.y = n.getLayoutY() - mouseEvent.getSceneY();
            dragDelta2.y = m.getLayoutY() - mouseEvent.getSceneY();
            dragDelta3.y = k.getLayoutY() - mouseEvent.getSceneY();
            n.setCursor(Cursor.MOVE);
        });
        n.setOnMouseReleased(e -> n.setCursor(Cursor.HAND));
        n.setOnMouseDragged(mouseEvent -> {
            n.setLayoutY(mouseEvent.getSceneY() + dragDelta.y);
            m.setLayoutY(mouseEvent.getSceneY() + dragDelta2.y);
            k.setLayoutY(mouseEvent.getSceneY() + dragDelta3.y);
        });
        n.setOnMouseEntered(e -> n.setCursor(Cursor.HAND));
    }

    private void makeDrag3(Node n, Node m,Node k) {
        final Delta dragDelta = new Delta();
        final Delta dragDelta2 = new Delta();
        final Delta dragDelta3 = new Delta();
        n.setOnMousePressed(mouseEvent -> {
            // record a delta distance for the drag and drop operation.
            dragDelta.x = n.getLayoutX() - mouseEvent.getSceneX();
            dragDelta.y = n.getLayoutY() - mouseEvent.getSceneY();
            dragDelta2.x = m.getLayoutX() - mouseEvent.getSceneX();
            dragDelta2.y = m.getLayoutY() - mouseEvent.getSceneY();
            dragDelta3.x = k.getLayoutX() - mouseEvent.getSceneX();
            dragDelta3.y = k.getLayoutY() - mouseEvent.getSceneY();
            n.setCursor(Cursor.MOVE);
        });
        n.setOnMouseReleased(e -> {
            n.setCursor(Cursor.HAND);
        });
        n.setOnMouseDragged(mouseEvent -> {
            n.setLayoutX(mouseEvent.getSceneX() + dragDelta.x);
            n.setLayoutY(mouseEvent.getSceneY() + dragDelta.y);
            m.setLayoutX(mouseEvent.getSceneX() + dragDelta2.x);
            m.setLayoutY(mouseEvent.getSceneY() + dragDelta2.y);
            k.setLayoutX(mouseEvent.getSceneX() + dragDelta3.x);
            k.setLayoutY(mouseEvent.getSceneY() + dragDelta3.y);
        });
        n.setOnMouseEntered(e -> n.setCursor(Cursor.HAND));
    }

    private void makeRotate2(Node n, Node m) {
        final Delta dragDelta = new Delta();
        n.setOnMousePressed(mouseEvent -> {
            // record a delta distance for the drag and drop operation.
            dragDelta.x = canonViewer.getLayoutX() + canonViewer.getFitWidth() / 2;
            dragDelta.y = canonViewer.getLayoutY() + canonViewer.getFitHeight() / 2;
            n.setCursor(Cursor.MOVE);
        });
        n.setOnMouseReleased(e -> n.setCursor(Cursor.HAND));
        n.setOnMouseDragged(mouseEvent -> {
            double changex = mouseEvent.getSceneX() - dragDelta.x;
            double changey = mouseEvent.getSceneY() - dragDelta.y;
            double angle = Math.toDegrees(Math.atan(changey / changex));
            if (changex < 0)
                angle += 180;
            m.setRotate(angle);
            if (angle < 90)
                angle = -angle;
            else
                angle = 360 - angle;
            angleField.setText("" + round(angle));
        });
        n.setOnMouseEntered(e -> n.setCursor(Cursor.HAND));
    }

    private boolean stopAnimation(){
        return (model.getY()>ground.getLayoutY());
    }

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

    private void detectCollision(Node a, Node b) {
        if (a.getBoundsInParent().intersects(b.getBoundsInParent()))
            this.collisionDetected = true;
    }
    private double round(double a){
        return Math.round(a*100)/100.0;
    }
    private static final class Delta {
        double x, y;
    }
}
