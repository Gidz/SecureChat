package securechat;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import securechat.libs.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;

/**
 * Created by gideon on 07/05/17.
 * The TTP Server class
 _   _
| |_| |_ _ __  ___  ___ _ ____   _____ _ __
| __| __| '_ \/ __|/ _ | '__\ \ / / _ | '__|
| |_| |_| |_) \__ |  __| |   \ V |  __| |
 \__|\__| .__/|___/\___|_|    \_/ \___|_|
        |_|

 */

public class Server extends Application {
    @FXML
    public Button startButton;
    @FXML
    public TextArea serverOutput;
    @FXML
    public TextField serverPortNumber;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("userinterface/ServerUI.fxml"));
        primaryStage.setTitle("TTP Server");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

        primaryStage.setOnCloseRequest(event -> {
            System.out.println("Server is closing");
            Platform.exit();
            System.exit(0);
        });
    }

    @FXML
    public void handleStartButton() {
        new Thread(() -> {
            try
            {
                TTP ttpServer = new TTP(Integer.parseInt(serverPortNumber.getText()));
            }
            catch(NullPointerException e)
            {
                updateDisplay(">>>>>>>> An Exception Occured <<<<<<<<");
            }
        }).start();
        startButton.setDisable(true);
        serverPortNumber.setDisable(true);
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
            serverOutput.appendText(toDisplay);
        } catch (NullPointerException e) {
            updateDisplay("Some text cannot be displayed.");
        }
    }

    //The class which is responsible for the TTP Server
    class TTP {
        int PORT_NUMBER;
        public ArrayList < Integer > users = new ArrayList < > ();

        public TTP(int p) {
            ServerSocket serverSocket = null;
            try
            {
                PORT_NUMBER = p;
                try {
                    serverSocket = new ServerSocket(PORT_NUMBER);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                updateDisplay("The Server is up and running!\n");

                while (true) {
                    Socket socket = null;
                    try {
                        socket = serverSocket.accept();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Thread thread = new Listener(socket);
                    thread.start();
                }
            }
            finally {
                if (serverSocket != null) {
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                        // log error just in case
                    }
                }
            }

        }

        //Handling the message. Depending on what the type of the message is, the server does different things
        void handleMessage(Message m) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidAlgorithmParameterException, InvalidKeyException {
            String messageType = m.getMessageType();
            if (messageType.equals("INITIALIZATION")) {
                int port = Integer.parseInt(m.getStringMessage());
                //Update the users list on the server
                users.add(port);
                if (users.size() == 1) {
                    sendMessage(new Message("UPDATE_NODE_NUMBER", "0"), port);
                    sendMessage(new Message("INFO", "TTP Server : Waiting for the other user to join."), port);
                } else if(users.size() == 2){
                    sendMessage(new Message("UPDATE_NODE_NUMBER", "1"), port);
                    sendToAll(new Message("INFO", "TTP Server : All the users joined. You can begin chat now"));
                    sendToAll(new Message("EXCHANGE_RSA_PUBLIC_KEY", ""));
                }
                else
                {
                    sendMessage(new Message("UPDATE_NODE_NUMBER", "-1"), port);
                    sendMessage(new Message("INFO", "TTP Server : Unfortunately the Server is handling connections at its maximum capacity. Please try again later\n"), port);
                }
            } else if (messageType.equals("INFO")) {
                //Just display it on screen
                updateDisplay("> " + m.getMessage() + "\n");
            } else if (messageType.equals("CHAT")) {
                byte[] cipher;
                cipher = m.getMessage();
                //Printing out the encrypted message
//                for (int i = 0; i < cipher.length; i++)
//                    updateDisplay(new Integer(cipher[i]) + " ");
//                updateDisplay("\n");
                updateDisplay("Transferring the message to the destination . . .\n");
                //Sending the message to another securechat.node
                invokeToggleSender(m);
            }
            //The server does not store the public key anywhere, it just passes it over to the other node
            else if(messageType.equals("RSA_PUBLIC_KEY"))
            {
                invokeToggleSender(m);
            }
            //The key exchange protocol
            else if (messageType.equals("DH1") || messageType.equals("DH2")) {
                //The server does not interfere with the key exchange, it just passes the message on to the other node
                invokeToggleSender(m);
            } else if (messageType.equals("START_CHAT")) {
                invokeToggleSender(m);
            }else if(messageType.equals("QUIT")) {
                updateDisplay("Node "+m.getSender()+" quit\n");
                //Remove both the users so that server can continue if new users send requests
                if(!users.isEmpty())
                {
                    invokeToggleSender(m);
                    users.clear();
                }
            }else {
                updateDisplay("Unknown. System FAILURE will occur soon!\n");
            }
        }

        //Send message
        void sendMessage(Message message, int port) {
            Thread t = new Thread(new Sender(message, port));
            t.start();
        }

        //Sends message to all the nodes
        void sendToAll(Message message) {
            for (int i = 0; i < users.size(); i++) {
                int port = users.get(i);
                Thread t = new Thread(new Sender(message, port));
                t.start();
            }
        }

        //If message came from node1, then it will be forwarded to node0 and vice versa
        void invokeToggleSender(Message m) {
            if (m.getSender() == 0) {
                sendMessage(m, users.get(1));
            } else if (m.getSender() == 1) {
                sendMessage(m, users.get(0));
            }
        }

        class Sender extends Thread implements Runnable {
            Message message;
            int port;
            public Sender(Message m, int p) {
                this.message = m;
                this.port = p;
            }
            @Override
            public void run() {
                Socket socket;
                try {
                    socket = new Socket(InetAddress.getLocalHost(), port);
                    ObjectOutputStream tunnelOut = new ObjectOutputStream(socket.getOutputStream());
                    tunnelOut.writeObject(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        class Listener extends Thread {
            Socket socket;
            public Listener(Socket socket) {
                this.socket = socket;
            }
            public void run() {
                try {
                    ObjectInputStream tunnelIn = new ObjectInputStream(socket.getInputStream());
                    ObjectOutputStream tunnelOut = new ObjectOutputStream(socket.getOutputStream());

                    Message message = (Message) tunnelIn.readObject();
                    //Display the message on the screen
                    handleMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }


        }

    }
}