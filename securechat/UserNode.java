package securechat;
/**
 * Created by gideon on 08/05/17.
 *
 * The UserNode is what the user genreally interacts with.
 * This deal with the UI for Sending and Receiving messages.
 */

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import securechat.libs.AES;
import securechat.libs.Message;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Random;
import java.util.Scanner;

public class UserNode extends Application {

    //The port on which the Client node listens
    private static int PORT_NUMBER;

    //The server address
    private static int TTP_PORT;
    private static int NODE_NUMBER;
    private static boolean stopChat = true;
    private static AES aes;

    //DH Keys
    private static KeyPair myDHKeyPair;
    private static KeyAgreement myKeyAgreement;
    private static byte[] sharedSecretKey;

    //RSA KEYS
    public static Key RSAPublicKey;
    private static Key RSAPrivateKey;
    private static PublicKey otherNodesPublicKey;


    @FXML
    public Button sendMessageButton;

    @FXML
    public TextField userInputBox;

    @FXML
    public TextArea userDisplay;

    @FXML
    public Button startChat;

    public static User user;
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {

        //Load the UI from the userinterface package
        Parent root = FXMLLoader.load(getClass().getResource("userinterface/NodeUI.fxml"));
        primaryStage.setTitle("User Chat Client");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

        //Define functionality when close button is pressed
        primaryStage.setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0);
        });
    }

    //A function to update the user display
    @FXML
    public void updateDisplay(String text) {
        String toDisplay;
        try {
            if (text == null || text =="null") {
                toDisplay = "";
            } else {
                toDisplay = text;
            }
            userDisplay.appendText(toDisplay);
        } catch (NullPointerException e) {
            System.err.println("Some messages cannot be displayed.");
        }
    }



    //This is called when "Start Chat" button is clicked
    @FXML
    public void onClickstartChat() {

            updateDisplay("You can start the chat . . .\n");
            startChat.setVisible(false);

            //Run this on a separate thread so that UI thread won't be blocked
            new Thread(() -> {
                try {
                    //Create an instance of the User
                    user = new User("localhost", "8888");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

            //Enable the Chat box and Send button
            sendMessageButton.setDisable(false);
            userInputBox.setDisable(false);
    }

    //What happens when send message button is clicked
    @FXML
    public void onClicksendMessageButton() {
        if (stopChat)
        {
            updateDisplay("Please wait until the key exchange is complete\n");
        }
        else
        {
            String message = userInputBox.getText();
            updateDisplay("> " + message + "\n");
            userInputBox.setText("");
            byte[] em = null;
            byte[] md = null;
            byte[] finalEncryptedHash = null;
            try {
                em = new AES(sharedSecretKey).encrypt(message);

                //Calculate the message disgest
                MessageDigest messageDigest = MessageDigest.getInstance("SHA");
                messageDigest.update(message.getBytes("UTF8"));
                md = messageDigest.digest();

                // get an instance of RSA cipher
                Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                cipher.init(Cipher.ENCRYPT_MODE, RSAPrivateKey);
                finalEncryptedHash = cipher.doFinal(md);

            } catch (Exception e) {
                e.printStackTrace();
            }

            Message m = new Message("CHAT", em, finalEncryptedHash, NODE_NUMBER);
            new User().sendMessage(m, TTP_PORT);
        }
    }

    //Get the data from the previous screen
    @FXML
    public void initData(String hostname, String portNumber) {
        //Update the address of the TTP Server
        TTP_PORT = Integer.parseInt(portNumber);
    }

    public class User {
        private String hostname;
        private int port;

        public User()
        {
            //An empty constructor
        }
        public User(String hostname, String port) throws Exception {
            this.hostname = hostname;
            this.port = Integer.parseInt(port);
            runChat();
        }

        public void runChat() throws Exception {
            Random random = new Random();
            PORT_NUMBER = random.nextInt(65535 - 49152 + 1) + 49152;

            // Generate an RSA keypair
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(1024);
            KeyPair key = keyGen.generateKeyPair();
            RSAPrivateKey = key.getPrivate();
            RSAPublicKey = key.getPublic();

            //Start listening on this port for incoming messages
            startListening(PORT_NUMBER);

            //Contact the TTP Server that the node is alive
            contactTTP();
        }

        //This method starts a new thread and listens to the incoming messages.
        void startListening(int p) {
            Thread t = new Thread(new Listener(p));
            //Set the thread to be a daemon so that it runs in the background without obstructing the user
            t.setDaemon(true);
            t.start();
        }

        //This method starts a new thread ans sends the message over the network
        public void sendMessage(Message message, int port) {
            Thread t = new Thread(new Sender(message, port));
            t.start();
        }

        //This method is initially used to contact the server about the details
        void contactTTP() {
            sendMessage(new Message("INITIALIZATION", "" + PORT_NUMBER), TTP_PORT);
        }

        //The Diffie Hellman Key exchange
        public void startKeyExchange() throws NoSuchAlgorithmException, InvalidParameterSpecException, InvalidAlgorithmParameterException, InvalidKeyException {
            DHParameterSpec dhSkipParamSpec;

            // Create new DH parameters
            AlgorithmParameterGenerator paramGen = AlgorithmParameterGenerator.getInstance("DH");
            paramGen.init(1024);
            AlgorithmParameters params = paramGen.generateParameters();
            dhSkipParamSpec = (DHParameterSpec) params.getParameterSpec(DHParameterSpec.class);

            //Create a DH key pair, using the DH parameters above
            KeyPairGenerator keysGenerator = KeyPairGenerator.getInstance("DH");
            keysGenerator.initialize(dhSkipParamSpec);
            myDHKeyPair = keysGenerator.generateKeyPair();

            // Create and initializes a DH KeyAgreement object
            myKeyAgreement = KeyAgreement.getInstance("DH");
            myKeyAgreement.init(myDHKeyPair.getPrivate());

            // Encodes the public key, and sends it over the network.
            byte[] publicKey = myDHKeyPair.getPublic().getEncoded();
            sendMessage(new Message("DH1", myDHKeyPair.getPublic().getEncoded(), NODE_NUMBER), TTP_PORT);
        }


        //This class is in charge of sending out the messages. All of its instances are run as Threads
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

        //This class is in charge of listening to the incoming messages. Even its instance runs as a thread.
        class Listener extends Thread implements Runnable {
            int port;
            public Listener(int port) {
                this.port = port;
            }
            public int toggleNumber(int num) {
                if (num == 0) {
                    return 1;
                } else {
                    return 0;
                }
            }
            @Override
            public void run() {
                ServerSocket serverSocket = null;
                try {
                    serverSocket = new ServerSocket(port);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Socket listener = null;

                //securechat.node.Server is listening to the incoming messages always. This is done using this while(true) loop
                while (true) {
                    try {
                        listener = serverSocket.accept();

                        ObjectInputStream tunnelIn = new ObjectInputStream(listener.getInputStream());
                        Message message = (Message) tunnelIn.readObject();

                        String messageType = message.getMessageType();

                        //All the cases

                        if (messageType.equals("CHAT")) {
                            AES aes = new AES(sharedSecretKey);
                            String plainText = aes.decrypt(message.getMessage());
                            System.out.println("> " + plainText);
                            updateDisplay("Node " + toggleNumber(NODE_NUMBER) + ": " + plainText + "\n");

                            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                            cipher.init(Cipher.DECRYPT_MODE, otherNodesPublicKey);
                            byte[] md = cipher.doFinal(message.getHash());

                            MessageDigest messageDigest = MessageDigest.getInstance("SHA");
                            messageDigest.update(plainText.getBytes());
                            byte[] calculated_md = messageDigest.digest();
                        } else if (messageType.equals("UPDATE_NODE_NUMBER")) {
                            NODE_NUMBER = Integer.parseInt(message.getStringMessage());
                        } else if (messageType.equals("DH1")) {
                            KeyFactory node2KeyFac = KeyFactory.getInstance("DH");
                            X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(message.getMessage());
                            PublicKey node1PubKey = node2KeyFac.generatePublic(x509KeySpec);

                            DHParameterSpec dhParamSpec = ((DHPublicKey) node1PubKey).getParams();

                            // Node2 creates a DH key pair
                            KeyPairGenerator node2KpairGen = KeyPairGenerator.getInstance("DH");
                            node2KpairGen.initialize(dhParamSpec);
                            myDHKeyPair = node2KpairGen.generateKeyPair();

                            // Node2 creates and initializes DH KeyAgreement object
                            myKeyAgreement = KeyAgreement.getInstance("DH");
                            myKeyAgreement.init(myDHKeyPair.getPrivate());

                            // Node2 encodes its public key, and sends it over to Node1.
                            byte[] publicKey = myDHKeyPair.getPublic().getEncoded();

                            //Meanwhile calculate the shared secret key
                            myKeyAgreement.doPhase(node1PubKey, true);
                            sharedSecretKey = myKeyAgreement.generateSecret();

                            sendMessage(new Message("DH2", publicKey, NODE_NUMBER), TTP_PORT);
                            stopChat = false;
                        } else if (messageType.equals("DH2")) {
                            KeyFactory node1KeyFac = KeyFactory.getInstance("DH");
                            X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(message.getMessage());
                            PublicKey node2PublicKey = node1KeyFac.generatePublic(x509KeySpec);

                            myKeyAgreement.doPhase(node2PublicKey, true);
                            sharedSecretKey = myKeyAgreement.generateSecret();
                            sendMessage(new Message("START_CHAT", ""), TTP_PORT);
                            stopChat = false;
                        } else if (messageType.equals("START_CHAT")) {
                            stopChat = false;
                        } else if (messageType.equals("EXCHANGE_RSA_PUBLIC_KEY")) {
                            sendMessage(new Message("RSA_PUBLIC_KEY", RSAPublicKey.getEncoded(), NODE_NUMBER), TTP_PORT);
                        } else if (messageType.equals("RSA_PUBLIC_KEY")) {
                            otherNodesPublicKey =
                                    KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(message.getMessage()));
                            if (NODE_NUMBER == 0) {
                                startKeyExchange();
                            }
                        } else if (messageType.equals("EXCHANGE_KEYS")) {
                            startKeyExchange();
                        } else {
                            //Just drop the message to the console
                            updateDisplay("> " + message.getMessage()+"\n");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } catch (BadPaddingException e) {
                        updateDisplay("Incorrect key used. Please update the keys."+"\n");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }


            //These methods come in handy when attempting to display the byte[] arrays on the screen.

            //This method converts a byte to hex code
            private void byte2hex(byte b, StringBuffer buf) {
                char[] hexChars = {
                        '0',
                        '1',
                        '2',
                        '3',
                        '4',
                        '5',
                        '6',
                        '7',
                        '8',
                        '9',
                        'A',
                        'B',
                        'C',
                        'D',
                        'E',
                        'F'
                };
                int high = ((b & 0xf0) >> 4);
                int low = (b & 0x0f);
                buf.append(hexChars[high]);
                buf.append(hexChars[low]);
            }

            //This method converts a byte array to hex string
            private String toHexString(byte[] block) {
                StringBuffer buf = new StringBuffer();

                int len = block.length;

                for (int i = 0; i < len; i++) {
                    byte2hex(block[i], buf);
                    if (i < len - 1) {
                        buf.append("");
                    }
                }
                return buf.toString();
            }
        }
    }
}