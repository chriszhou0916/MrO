package projectile;
/**
 * Generates error window with parameters*
 * @author Varsha
 * @version 6/6/15
 */
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ErrorMessage {
    /**
     * displays error window
     * @param s message to be displayed
     */
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
        // a button is the equivalent of a JButton
        Button closeButton = new Button("close");
        //this is called a lambda expression
        //oracle added lambda expression in Java 8 to simplify code when
        //creating anonymous classes
        //here, e is the parameter of the action listener, and the thing after ->
        //is the code that needs to be executed
        closeButton.setOnAction(e -> window.close());
        //add both the label and the button to the VBox
        //since it's a Vertical Box, the components should line up vertically
        layout.getChildren().addAll(label,closeButton);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout);
        window.setScene(scene);
        window.showAndWait();
    }
}
