package sample;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Created by chriszhou1 on 5/24/15.
 */
public class ErrorMessage {
    public static void showMessage(String s){
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("ERROR");
        window.setMinWidth(200);
        window.setMinHeight(100);

        Label label = new Label();
        label.setText(s);
        label.setFont(new Font("Arial",12));
        VBox layout = new VBox(10);
        Button closeButton = new Button("close");
        closeButton.setOnAction(e -> window.close());
        layout.getChildren().addAll(label,closeButton);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout);
        window.setScene(scene);
        window.showAndWait();
    }
}
