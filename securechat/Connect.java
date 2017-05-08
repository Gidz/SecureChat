package securechat;/**
 * Created by gideon on 08/05/17.
 */

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class Connect extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @FXML
    TextField hostNameBox;
    @FXML
    TextField portNumberBox;
    @FXML
    Button connectButton;
    @Override
    public void start(Stage primaryStage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("userWelcomeScreen.fxml"));
        primaryStage.setTitle("Connect to TTP");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    @FXML
    void onConnectButtonClick(MouseEvent event) throws IOException {
        System.out.println("The host name is "+hostNameBox.getText());
        System.out.println("The port number is "+portNumberBox.getText());

        Parent p = FXMLLoader.load(getClass().getResource("NodeUI.fxml"));
        Scene sc = new Scene(p);
        Stage newStage = (Stage) ((javafx.scene.Node)event.getSource()).getScene().getWindow();
        newStage.setScene(sc);
        newStage.show();
    }
}
