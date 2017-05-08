package securechat;/**
 * Created by gideon on 08/05/17.
 */

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;

public class Node extends Application {

    @FXML
    public Button sendMessageButton;

    @FXML
    public TextField userInputBox;

    @FXML
    public TextArea userDisplay;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("NodeUI.fxml"));
        primaryStage.setTitle("My Application");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    @FXML
    public void updateDisplay(String text) {
        String toDisplay;
        try {
            if(text == null)
            {
                toDisplay="";
            }
            else
            {
                toDisplay = text;
            }
            userDisplay.appendText(toDisplay);
        } catch (NullPointerException e) {
            updateDisplay("Some text cannot be displayed.");
        }
    }
}
