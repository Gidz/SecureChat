package securechat;
/**
 * Created by gideon on 08/05/17.
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

public class Node extends Application {

    private static int PORT_NUMBER;
    private static int TTP_PORT;
    private static int NODE_NUMBER;
    private static boolean stopChat = true;

    private static KeyPair myDHKeyPair;
    private static KeyAgreement myKeyAgreement;
    private static byte[] sharedSecretKey;
    private static AES aes;

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
        Parent root = FXMLLoader.load(getClass().getResource("userinterface/NodeUI.fxml"));
        primaryStage.setTitle("User Chat Client");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

        primaryStage.setOnCloseRequest(event -> {
            System.out.println("Stage is closing");
            Platform.exit();
            System.exit(0);
        });
    }

    @FXML
    public void updateDisplay(String text) {
        String toDisplay;
        try {
            if (text == null) {
                toDisplay = "";
            } else {
                toDisplay = text;
            }
            userDisplay.appendText(toDisplay);
        } catch (NullPointerException e) {
            //            updateDisplay("Some text cannot be displayed.");
        }
    }



    @FXML
    public void onClickstartChat() {
        updateDisplay("Starting the chat!\n");
        startChat.setVisible(false);
        //        updateDisplay("Trying to instantiate an User object\n");
        new Thread(() -> {
            try {
                user = new User("localhost", "8888");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        //        updateDisplay("Instantiation successful\n");
        sendMessageButton.setDisable(false);
        userInputBox.setDisable(false);
    }

    @FXML
    public void onClicksendMessageButton() {
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
            System.out.println("\nStart encryption");
            cipher.init(Cipher.ENCRYPT_MODE, RSAPrivateKey);
            finalEncryptedHash = cipher.doFinal(md);

        } catch (Exception e) {
            e.printStackTrace();
        }

        Message m = new Message("CHAT", em, finalEncryptedHash, NODE_NUMBER);
        new User().sendMessage(m, TTP_PORT);

    }

    @FXML
    public void initData(String hostname, String portNumber) {
        TTP_PORT = Integer.parseInt(portNumber);
        System.out.println("Set the port number to" + TTP_PORT);
    }

    public class User {

        private String hostname;
        private int port;

        public User(String hostname, String port) throws Exception {
            this.hostname = hostname;
            this.port = Integer.parseInt(port);
            runChat();
        }
        public User() {
            //Nothing here
        }

        public void runChat() throws Exception {
            System.out.print("Choosing a random port number to initialize the securechat.node .. \n");
            updateDisplay("Choosing a random port number to initialize the securechat.node .. \n");

            Random random = new Random();
            PORT_NUMBER = random.nextInt(65535 - 49152 + 1) + 49152;
            updateDisplay("Randomly selected " + PORT_NUMBER + "\n");
            System.out.println("Randomly selected " + PORT_NUMBER + "\n");
            //
            //            System.out.print("Enter the port number of the Third Party Server : ");
            //            TTP_PORT = port;

            //TODO: Implement DSA
            // generate an RSA keypair
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(1024);
            KeyPair key = keyGen.generateKeyPair();
            RSAPrivateKey = key.getPrivate();
            RSAPublicKey = key.getPublic();

            startServer(PORT_NUMBER);
            contactTTP();



            System.out.println("My public key is " + RSAPublicKey);

            Scanner in = new Scanner(System.in);

            if (NODE_NUMBER == 0) {
                //                System.out.println("Press Enter to start chat");
                in .nextLine();
            } else {
                while (stopChat) { in .nextLine();
                    System.out.println("Please wait till the shared key is calculated");
                }
            }

            while (sharedSecretKey == null) {
                //Be busy waiting
                continue;
            }
            aes = new AES(sharedSecretKey);

            System.out.println("----- CHAT STARTS HERE -----");
            while (true) {
                String message = in .nextLine();
                if (message.equals("/quit")) {
                    break;
                } else {
                    byte[] em;
                    em = aes.encrypt(message);
                    Message m = new Message("CHAT", em, NODE_NUMBER);
                    sendMessage(m, TTP_PORT);
                }

            }
        }

        void startServer(int p) {
            Thread t = new Thread(new ServerTask(p));
            t.setDaemon(true);
            t.start();
        }
        public void sendMessage(Message message, int port) {
            Thread t = new Thread(new ClientTask(message, port));
            t.start();
        }

        void contactTTP() {
            sendMessage(new Message("INITIALIZATION", "" + PORT_NUMBER), TTP_PORT);
        }

        public void exchangeRSAPublicKeys() {
            System.out.println("Initiating RSA Key Exchange");
        }

        public void startKeyExchange() throws NoSuchAlgorithmException, InvalidParameterSpecException, InvalidAlgorithmParameterException, InvalidKeyException {
            DHParameterSpec dhSkipParamSpec;

            // Create new DH parameters
            System.out.print("Initiating DH Key Exchange . . .\n");

            AlgorithmParameterGenerator paramGen = AlgorithmParameterGenerator.getInstance("DH");
            paramGen.init(512);
            AlgorithmParameters params = paramGen.generateParameters();
            dhSkipParamSpec = (DHParameterSpec) params.getParameterSpec(DHParameterSpec.class);

            //Create a DH key pair, using the DH parameters above
            KeyPairGenerator keysGenerator = KeyPairGenerator.getInstance("DH");
            keysGenerator.initialize(dhSkipParamSpec);
            myDHKeyPair = keysGenerator.generateKeyPair();

            // Create and initializes her DH KeyAgreement object
            myKeyAgreement = KeyAgreement.getInstance("DH");
            myKeyAgreement.init(myDHKeyPair.getPrivate());

            // Encodes the public key, and sends it over the network.
            byte[] publicKey = myDHKeyPair.getPublic().getEncoded();

            //        System.out.println("Sending this over the network "+myDHKeyPair.getPublic());

            //Sending Node1's public key
            sendMessage(new Message("DH1", myDHKeyPair.getPublic().getEncoded(), NODE_NUMBER), TTP_PORT);
        }


        class ClientTask extends Thread implements Runnable {
            Message message;
            int port;
            public ClientTask(Message m, int p) {
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


        /**
         * Created by gideon on 05/05/17.
         */
        class ServerTask extends Thread implements Runnable {
            int port;
            public ServerTask(int port) {
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
                //                System.out.print("Server Started and listening to the port " + this.port);
                //                updateDisplay("Server Started and listening to the port " + this.port);

                //securechat.node.Server is running always. This is done using this while(true) loop
                while (true) {
                    //Reading the message from the client
                    try {
                        listener = serverSocket.accept();

                        ObjectInputStream tunnelIn = new ObjectInputStream(listener.getInputStream());
                        Message message = (Message) tunnelIn.readObject();

                        String messageType = message.getMessageType();
                        //Display the message on the standard output
                        if (messageType.equals("CHAT")) {
                            AES aes = new AES(sharedSecretKey);
                            String plainText = aes.decrypt(message.getEncryptedMessage());
                            System.out.println("> " + plainText);
                            updateDisplay("Node " + toggleNumber(NODE_NUMBER) + ": " + plainText + "\n");

                            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                            cipher.init(Cipher.DECRYPT_MODE, otherNodesPublicKey);
                            byte[] md = cipher.doFinal(message.getHash());

                            MessageDigest messageDigest = MessageDigest.getInstance("SHA");
                            messageDigest.update(plainText.getBytes());
                            byte[] calculated_md = messageDigest.digest();

                            updateDisplay(toHexString(md) + "\n");
                            updateDisplay(toHexString(calculated_md) + "\n");

                            if (toHexString(calculated_md).equals(toHexString(md))) {
                                System.out.println("Message is Authenticated and its Integrity is verified!\n");
                                updateDisplay("Message is Authenticated and its Integrity is verified!\n");
                            } else {
                                updateDisplay("Authentication is not verified :( ");
                            }

                        } else if (messageType.equals("UPDATE_NODE_NUMBER")) {
                            NODE_NUMBER = Integer.parseInt(message.getMessage());
                            System.out.println("The securechat.node number is " + NODE_NUMBER);
                            updateDisplay("The securechat.node number is " + NODE_NUMBER + "\n");
                        } else if (messageType.equals("DH1")) {
                            //Received Node1's public key
                            //                System.out.println("> "+toHexString(message.getEncryptedMessage()));
                            KeyFactory node2KeyFac = KeyFactory.getInstance("DH");
                            X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(message.getEncryptedMessage());
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
                            //                System.out.println("Sending this over the network "+Node.myDHKeyPair.getPublic());

                            //Meanwhile calulate the shared secret key
                            myKeyAgreement.doPhase(node1PubKey, true);
                            sharedSecretKey = myKeyAgreement.generateSecret();
                            //                System.out.println("The shared secret key is "+toHexString(Node.sharedSecretKey));


                            sendMessage(new Message("DH2", publicKey, NODE_NUMBER), TTP_PORT);
                        } else if (messageType.equals("DH2")) {
                            //Node 1 receives public key from Node2
                            //                System.out.println("> "+toHexString(message.getEncryptedMessage()));

                            KeyFactory node1KeyFac = KeyFactory.getInstance("DH");
                            X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(message.getEncryptedMessage());
                            PublicKey node2PublicKey = node1KeyFac.generatePublic(x509KeySpec);

                            myKeyAgreement.doPhase(node2PublicKey, true);
                            sharedSecretKey = myKeyAgreement.generateSecret();
                            //                System.out.println("The shared secret key is "+toHexString(Node.sharedSecretKey));
                            sendMessage(new Message("START_CHAT", ""), TTP_PORT);
                        } else if (messageType.equals("START_CHAT")) {
                            stopChat = false;
                            System.out.print("Key calculation successful. Press Enter to proceed to chat.");
                        } else if (messageType.equals("EXCHANGE_RSA_PUBLIC_KEY")) {
                            sendMessage(new Message("RSA_PUBLIC_KEY", RSAPublicKey.getEncoded(), NODE_NUMBER), TTP_PORT);
                        } else if (messageType.equals("RSA_PUBLIC_KEY")) {
                            otherNodesPublicKey =
                                    KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(message.getEncryptedMessage()));
                            System.out.println("The other node's public key is " + otherNodesPublicKey);
                            if (NODE_NUMBER == 0) {
                                startKeyExchange();
                            }

                        } else if (messageType.equals("EXCHANGE_KEYS")) {
                            startKeyExchange();
                        } else {
                            System.out.print("> " + message.getMessage());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } catch (BadPaddingException e) {
                        System.err.println("Incorrect key used. Please update the keys.");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }


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

            /*
             * Converts a byte array to hex string
             */
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