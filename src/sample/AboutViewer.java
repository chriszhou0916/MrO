package sample;/**
 * Created by chriszhou1 on 5/20/15.
 */

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AboutViewer  {

    public static void display(){
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("About this App");
        window.setMinWidth(200);
        window.setMinHeight(250);

        Label label = new Label();
        label.setText("For Mr.O\nDeveloped by:\nChris Zhou,\nRachel Lee, \nVarsha Sundar\nJune 2015");
        label.setFont(new Font("Arial",20));
        VBox layout = new VBox(10);
        layout.getChildren().add(label);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout);
        window.setScene(scene);
        window.showAndWait();
    }
}
