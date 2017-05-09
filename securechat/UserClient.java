package securechat;
/**
 * Created by gideon on 08/05/17.
                         _ _            _
 _   _ ___  ___ _ __ ___| (_) ___ _ __ | |_
| | | / __|/ _ | '__/ __| | |/ _ | '_ \| __|
| |_| \__ |  __| | | (__| | |  __| | | | |_
 \__,_|___/\___|_|  \___|_|_|\___|_| |_|\__|

 * This class is what greets the user on application start
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
import securechat.libs.Message;

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
            Platform.exit();
            System.exit(0);
        });
    }

    /* When the Connect button is clicked the input is taken from the input boxes and is passed on to the UserNode controller
     */
    @FXML
    void onConnectButtonClick(MouseEvent event) throws IOException {
        String hostName = hostNameBox.getText();
        String portNumber = portNumberBox.getText();

        //Sanitize the data if possible
        if (hostName == null || hostName.isEmpty() || portNumber == null || portNumber.isEmpty())
        {
            hostNameBox.setText("");
            portNumberBox.setText("");
            hostNameBox.setPromptText("Enter valid hostname");
            portNumberBox.setPromptText("Enter valid port number");
        }
        else
        {
            //Load the UI for Chat box
            FXMLLoader loader = new FXMLLoader(getClass().getResource("userinterface/NodeUI.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene((Pane) loader.load()));

            //Get and instance of UserNode controller
            UserNode controller = loader. <UserNode> getController();
            controller.initData(hostName,portNumber,stage);
            stage.show();

            //Display the Chat box
            Stage primaryStage = (Stage)((javafx.scene.Node) event.getSource()).getScene().getWindow();
            primaryStage.close();
        }
    }
}