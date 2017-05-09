package securechat;/**
 * Created by gideon on 08/05/17.
 */

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;

public class UserClient extends Application {

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
        Parent root = FXMLLoader.load(getClass().getResource("userinterface/userWelcomeScreen.fxml"));
        primaryStage.setTitle("Connect to TTP");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

        primaryStage.setOnCloseRequest(event -> {
            System.out.println("Stage is closing");
            Platform.exit();
            // Save file
        });
    }

    @FXML
    void onConnectButtonClick(MouseEvent event) throws IOException {
        System.out.println("The host name is "+hostNameBox.getText());
        System.out.println("The port number is "+portNumberBox.getText());

        FXMLLoader loader = new FXMLLoader(getClass().getResource("userinterface/NodeUI.fxml"));
//        Parent p = FXMLLoader.load(getClass().getResource("NodeUI.fxml"));
//        Scene sc = new Scene(p);
        Stage stage = new Stage();
        stage.setScene(new Scene((Pane) loader.load()));
        Node controller = loader.<Node>getController();
        controller.initData(hostNameBox.getText(),portNumberBox.getText());
        stage.show();

        Stage primaryStage = (Stage) ((javafx.scene.Node)event.getSource()).getScene().getWindow();
        primaryStage.close();
    }
}
