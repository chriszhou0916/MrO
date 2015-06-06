package projectile;
/**
 * Generates "about this app" window *
 * @author Varsha
 * @version 6/6/15
 */
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
public class AboutViewer  {
    /**
     * displays the window
     */
    public static void display(){
        //creates stage object, equivalent of JFrame in javax.swing
        Stage window = new Stage();
        //prevents the user from clicking outside the window before this current window
        //is resolved
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("About this App");
        window.setMinWidth(200);
        window.setMinHeight(250);
        // a Label is equivalent of a JLabel
        Label label = new Label();
        label.setText("For Mr.O\nDeveloped by:\nChris Zhou,\nVarsha Sundar\nJune 2015");
        label.setFont(new Font("Arial",20));
        //A VBox is an example of a window layout, it aligns all elements vertically
        VBox layout = new VBox(10);
        //add the label to the VBox, since there is only one element, it's centered
        layout.getChildren().add(label);
        layout.setAlignment(Pos.CENTER);
        //each stage needs a scene. The scene is what's moving inside the stage
        Scene scene = new Scene(layout);
        //fit the scene in the stage
        window.setScene(scene);
        //this part works together with Modality to prevent the user from clicking outside
        window.showAndWait();
    }
}
