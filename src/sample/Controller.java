package sample;
import com.sun.org.apache.xpath.internal.operations.Mod;
import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point3D;
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
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import javafx.scene.image.*;
import javafx.util.converter.NumberStringConverter;

import javax.xml.soap.Text;
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
    private CheckBox showTrack;
    @FXML
    private TextField heightField;
    @FXML
    private TextField angleField;
    @FXML
    private TextField timeField;
    @FXML
    private TextField initialVField;
    @FXML
    private TextField diameterField;
    @FXML
    private TextField gravityField;
    @FXML
    private BorderPane border;
    @FXML
    private ImageView target;
    @FXML
    private ImageView backgroundViewer;
    @FXML
    private ImageView canonViewer;
    @FXML
    private Pane centralPane;
    @FXML
    private Circle canonBackCircle;
    @FXML
    private Circle canonBarrel;

    private Canvas bg,trajectoryLayer,projectileLayer;
    private AnimationTimer timer;
    private GraphicsContext gc,gcTraj;
    private Model circle;
    private boolean isAnimating;
    private double barrelOriginalX,barrelOriginalY,barrelLength,originalAngle;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //initialize itemList
        itemList.getItems().addAll("ball", "ziqi", "Mr.O","piano");
        Image background = new Image("file:plain-farm-background.png");
        backgroundViewer.setImage(background);
        Image img = new Image("file:target-round.png");
        Image canon = new Image("file:canon.png");
        canonViewer.setImage(canon);
        makeDrag2(canonViewer,canonBackCircle);
        target.setImage(img);
        makeDrag(target);
        angleField.setOnKeyReleased(e -> {
            if(!isDouble(angleField))
            e.consume();
            else
                canonViewer.setRotate(-Double.parseDouble(angleField.getText()));
        });
        makeRotate2(canonBackCircle, canonViewer);
        barrelOriginalX = canonBarrel.getLayoutX();
        barrelOriginalY = canonBarrel.getLayoutY();
        double centerX = canonViewer.getLayoutX()+canonViewer.getFitWidth()/2;
        double centerY = canonViewer.getLayoutY()+ canonViewer.getFitHeight()/2;
        barrelLength = Math.sqrt((barrelOriginalX-centerX)*(barrelOriginalX-centerX)+(barrelOriginalY-centerY)*(barrelOriginalY-centerY));
        originalAngle = Math.toDegrees(Math.atan((centerY-barrelOriginalY)/(barrelOriginalX-centerX)));

    }


    public void aboutButtonClicked(){
        AboutViewer.display();
    }
    public void eraseButtonClicked() {
        isAnimating = false;
        timer.stop();
        centralPane.getChildren().removeAll(trajectoryLayer, projectileLayer);
        gc.clearRect(0, 0, projectileLayer.getWidth(), projectileLayer.getHeight());
        gcTraj.clearRect(0, 0, projectileLayer.getWidth(), projectileLayer.getHeight());

    }
    public void fireButtonClicked() {
        shiftBarrel();
        String selected = itemList.getSelectionModel().getSelectedItem();
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
        circle = new Model(canonBarrel.getLayoutX(), canonBarrel.getLayoutY(), Double.parseDouble(angleField.getText()), Double.parseDouble(initialVField.getText()));

        circle.setG(Double.parseDouble(gravityField.getText()));
        circle.initialize();
        circle.fire();
        isAnimating = true;
        trajectoryLayer = new Canvas(centralPane.getWidth(), centralPane.getHeight());
        projectileLayer = new Canvas(centralPane.getWidth(), centralPane.getHeight());
        gc = projectileLayer.getGraphicsContext2D();
        gcTraj = trajectoryLayer.getGraphicsContext2D();
        gc.setFill(Color.RED);
        Image mrO = new Image("file:mro.jpg");
        Image ziqi = new Image("file:ziqi.jpg");


        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {

                gc.clearRect(0, 0, projectileLayer.getWidth(), projectileLayer.getHeight());
                circle.step(30);
                if (selected.equals("Mr.O"))
                    gc.drawImage(mrO, circle.getX() - 50, circle.getY() - 62.5);
                else if (selected.equals("ball")) {
                    if (!isDouble(diameterField)) {
                        ErrorMessage.showMessage("please enter diameter for ball");
                        return;
                    }
                    double diameterSize = Double.parseDouble(diameterField.getText());
                    gc.fillOval(circle.getX() - diameterSize / 2, circle.getY() - diameterSize / 2, diameterSize, diameterSize);
                } else if (selected.equals("ziqi"))
                    gc.drawImage(ziqi, circle.getX() - 50, circle.getY() - 60, 100, 120);
                if (showTrack.isSelected())
                    gcTraj.fillOval(circle.getX(), circle.getY(), 5, 5);
                if (now % 100 == 0)
                    updateFields();
            }
        };
        timer.start();
        centralPane.getChildren().addAll(trajectoryLayer, projectileLayer);
    }

    public void updateFields()
    {
        heightField.setText(String.valueOf(circle.getAltitude()));
    }

    private void shiftBarrel(){
        double centerX = canonViewer.getLayoutX()+canonViewer.getFitWidth()/2;
        double centerY = canonViewer.getLayoutY()+canonViewer.getFitHeight()/2;
        double newAngle = originalAngle-canonViewer.getRotate();
        double newX = centerX+barrelLength*Math.cos(Math.toRadians(newAngle));
        double newY = centerY-barrelLength*Math.sin(Math.toRadians(newAngle));
        canonBarrel.relocate(newX, newY-15);
    }
    private void makeDrag(Node n){
        final Delta dragDelta = new Delta();
        n.setOnMousePressed(mouseEvent-> {
                // record a delta distance for the drag and drop operation.
                dragDelta.x = n.getLayoutX() - mouseEvent.getSceneX();
                dragDelta.y = n.getLayoutY() - mouseEvent.getSceneY();
                n.setCursor(Cursor.MOVE);
            });
        n.setOnMouseReleased(e -> n.setCursor(Cursor.HAND));
        n.setOnMouseDragged(mouseEvent-> {
                n.setLayoutX(mouseEvent.getSceneX() + dragDelta.x);
                n.setLayoutY(mouseEvent.getSceneY() + dragDelta.y);
            });
        n.setOnMouseEntered(e -> n.setCursor(Cursor.HAND));
    }

    private void makeDrag2(Node n,Node m){
        final Delta dragDelta = new Delta();
        final Delta dragDelta2 = new Delta();
        n.setOnMousePressed(mouseEvent-> {
            // record a delta distance for the drag and drop operation.
            dragDelta.x = n.getLayoutX() - mouseEvent.getSceneX();
            dragDelta.y = n.getLayoutY() - mouseEvent.getSceneY();
            dragDelta2.x = m.getLayoutX() - mouseEvent.getSceneX();
            dragDelta2.y = m.getLayoutY() - mouseEvent.getSceneY();
            n.setCursor(Cursor.MOVE);
        });
        n.setOnMouseReleased(e -> {
            n.setCursor(Cursor.HAND);
            shiftBarrel();
        });
        n.setOnMouseDragged(mouseEvent -> {
            n.setLayoutX(mouseEvent.getSceneX() + dragDelta.x);
            n.setLayoutY(mouseEvent.getSceneY() + dragDelta.y);
            m.setLayoutX(mouseEvent.getSceneX() + dragDelta2.x);
            m.setLayoutY(mouseEvent.getSceneY() + dragDelta2.y);
        });
        n.setOnMouseEntered(e -> n.setCursor(Cursor.HAND));
    }

    private void makeRotate2(Node n,Node m){
        final Delta dragDelta = new Delta();
        n.setOnMousePressed(mouseEvent-> {
            // record a delta distance for the drag and drop operation.
            dragDelta.x = canonViewer.getLayoutX()+canonViewer.getFitWidth()/2;
            dragDelta.y = canonViewer.getLayoutY()+canonViewer.getFitHeight()/2;
            n.setCursor(Cursor.MOVE);
        });
        n.setOnMouseReleased(e -> n.setCursor(Cursor.HAND));
        n.setOnMouseDragged(mouseEvent-> {
            double changex = mouseEvent.getSceneX()-dragDelta.x;
            double changey = mouseEvent.getSceneY()-dragDelta.y;
            double angle = Math.toDegrees(Math.atan(changey / changex));
            if(changex<0)
                angle+=180;
            m.setRotate(angle);
            if(angle<90)
                angle = -angle;
            else
                angle = 360-angle;
            angleField.setText(""+angle);
        });
        n.setOnMouseEntered(e -> n.setCursor(Cursor.HAND));
    }

    private static final class Delta{
        double x,y;
    }

    private boolean isDouble(TextField input){
        if(input.getText().isEmpty()||input.getText().equals("-"))
            return false;
        try{
            double age = Double.parseDouble(input.getText());
            return true;
        }catch(NumberFormatException e){
            ErrorMessage.showMessage("Please enter valid number");
            return false;
        }
    }
}
