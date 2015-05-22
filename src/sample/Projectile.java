package sample;

import javafx.animation.AnimationTimer;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.scene.effect.Lighting;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.beans.EventHandler;

/**
 * Created by chriszhou1 on 5/21/15.
 */
public class Projectile {}
    /*private Timeline timeline;
    private AnimationTimer timer;
    private int i = 0;

    public Projectile() {
        initialize();
    }

    public void initialize(){
        Circle circle = new Circle(20, Color.RED);
        circle.setEffect(new Lighting());
        StackPane stack = new StackPane();
        stack.getChildren().add(circle);
        stack.setLayoutX(30);
        stack.setLayoutY(30);

        timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);

        Model ball = new Model(100,300,45,50);
        ball.initialize();
        ball.fire();
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                ball.step(25);
            }
        };

        Duration duration = Duration.millis(1000);
        EventHandler onFinished = new EventHandler() {
            public void handle(ActionEvent t) {
                stack.setTranslateX(java.lang.Math.random()*200-100);
                //reset counter
                i = 0;
            }
        };


        }
    }*/

