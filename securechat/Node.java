package securechat; /**
 * Created by gideon on 05/05/17.
 */

import securechat.libs.AES;
import securechat.libs.Message;

import javax.crypto.BadPaddingException;
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

public class Node {
    static int PORT_NUMBER;
    public static int TTP_PORT;
    public static int NODE_NUMBER;
    public static boolean stopChat=true;

    static public KeyPair myDHKeyPair;
    static public KeyAgreement myKeyAgreement;
    static public byte[] sharedSecretKey;

    public static void main(String args[]) throws Exception {
        System.out.println("Choosing a random port number to initialize the securechat.node .. ");

        Random random = new Random();
        PORT_NUMBER = random.nextInt(65535 - 49152+ 1) + 49152;
        System.out.println("Randomly selected "+PORT_NUMBER);
        Scanner in = new Scanner(System.in);
        System.out.print("Enter the port number of the Third Party Server : ");
        TTP_PORT = Integer.parseInt(in.nextLine());

        startServer(PORT_NUMBER);
        contactTTP();

        if(NODE_NUMBER==0)
        {
            System.out.println("Press Enter to start chat");
            in.nextLine();
        }
        else
        {
            while(stopChat)
            {
                in.nextLine();
                System.out.println("Please wait till the shared key is calculated");
            }
        }
        AES aes =  new AES(sharedSecretKey);

        System.out.println("----- CHAT STARTS HERE -----");
        while (true)
        {
            String message = in.nextLine();
            if (message.equals("/quit"))
            {
                break;
            }
            else
            {
                byte[] em;
                em = aes.encrypt(message);
                Message m = new Message("CHAT",em,NODE_NUMBER);
                sendMessage(m,TTP_PORT);
            }

        }
    }

    static void startServer(int p)
    {
        Thread t = new Thread(new ServerTask(p));
        t.setDaemon(true);
        t.start();
    }
    public static void sendMessage(Message message, int port)
    {
        Thread t = new Thread(new ClientTask(message,port));
        t.start();
    }

    static void contactTTP()
    {
        sendMessage(new Message("INITIALIZATION",""+PORT_NUMBER),TTP_PORT);
    }


    public static void startKeyExchange() throws NoSuchAlgorithmException, InvalidParameterSpecException, InvalidAlgorithmParameterException, InvalidKeyException {
        DHParameterSpec dhSkipParamSpec;

        // Create new DH parameters
        System.out.println("Initiating DH Key Exchange . . .");

        AlgorithmParameterGenerator paramGen = AlgorithmParameterGenerator.getInstance("DH");
        paramGen.init(512);
        AlgorithmParameters params = paramGen.generateParameters();
        dhSkipParamSpec = (DHParameterSpec)params.getParameterSpec(DHParameterSpec.class);

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
        sendMessage(new Message("DH1",myDHKeyPair.getPublic().getEncoded(),NODE_NUMBER),TTP_PORT);
    }


    static class ClientTask extends Thread implements Runnable {
        Message message;
        int port;
        public ClientTask(Message m,int p)
        {
            this.message = m;
            this.port = p;
        }
        @Override
        public void run() {
            Socket socket;
            try {
                socket = new Socket(InetAddress.getLocalHost(),port);
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
    static class ServerTask extends Thread implements Runnable {
        int port;
        public ServerTask(int port)
        {
            this.port = port;
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
            System.out.println("Server Started and listening to the port "+this.port);

            //securechat.node.Server is running always. This is done using this while(true) loop
            while(true) {
                //Reading the message from the client
                try {
                    listener = serverSocket.accept();

                    ObjectInputStream tunnelIn = new ObjectInputStream(listener.getInputStream());
                    Message message = (Message) tunnelIn.readObject();

                    String messageType = message.getMessageType();
                    //Display the message on the standard output
                    if (messageType.equals("CHAT"))
                    {
                        AES aes = new AES(Node.sharedSecretKey);
                        System.out.println("> "+aes.decrypt(message.getEncryptedMessage()));
                    }
                    else if(messageType.equals("UPDATE_NODE_NUMBER"))
                    {
                        Node.NODE_NUMBER = Integer.parseInt(message.getMessage());
                        System.out.println("The securechat.node number is "+Node.NODE_NUMBER);
                    }
                    else if(messageType.equals("DH1"))
                    {
                        //Received Node1's public key
//                System.out.println("> "+toHexString(message.getEncryptedMessage()));
                        KeyFactory node2KeyFac = KeyFactory.getInstance("DH");
                        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(message.getEncryptedMessage());
                        PublicKey node1PubKey = node2KeyFac.generatePublic(x509KeySpec);

                        DHParameterSpec dhParamSpec = ((DHPublicKey)node1PubKey).getParams();

                        // Node2 creates a DH key pair
                        KeyPairGenerator node2KpairGen = KeyPairGenerator.getInstance("DH");
                        node2KpairGen.initialize(dhParamSpec);
                        Node.myDHKeyPair = node2KpairGen.generateKeyPair();

                        // Node2 creates and initializes DH KeyAgreement object
                        Node.myKeyAgreement = KeyAgreement.getInstance("DH");
                        Node.myKeyAgreement.init(Node.myDHKeyPair.getPrivate());

                        // Node2 encodes its public key, and sends it over to Node1.
                        byte[] publicKey = Node.myDHKeyPair.getPublic().getEncoded();
//                System.out.println("Sending this over the network "+Node.myDHKeyPair.getPublic());

                        //Meanwhile calulate the shared secret key
                        Node.myKeyAgreement.doPhase(node1PubKey, true);
                        Node.sharedSecretKey = Node.myKeyAgreement.generateSecret();
//                System.out.println("The shared secret key is "+toHexString(Node.sharedSecretKey));


                        Node.sendMessage(new Message("DH2",publicKey, Node.NODE_NUMBER),Node.TTP_PORT);
                    }
                    else if(messageType.equals("DH2"))
                    {
                        //Node 1 receives public key from Node2
//                System.out.println("> "+toHexString(message.getEncryptedMessage()));

                        KeyFactory node1KeyFac = KeyFactory.getInstance("DH");
                        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(message.getEncryptedMessage());
                        PublicKey node2PublicKey = node1KeyFac.generatePublic(x509KeySpec);

                        Node.myKeyAgreement.doPhase(node2PublicKey, true);
                        Node.sharedSecretKey = Node.myKeyAgreement.generateSecret();
//                System.out.println("The shared secret key is "+toHexString(Node.sharedSecretKey));
                        Node.sendMessage(new Message("START_CHAT",""),Node.TTP_PORT);
                    }
                    else if(messageType.equals("START_CHAT"))
                    {
                        Node.stopChat = false;
                        System.out.println("Key calculation successful. Press Enter to proceed to chat.");
                    }
                    else if(messageType.equals("EXCHANGE_KEYS"))
                    {
                        Node.startKeyExchange();
                    }
                    else
                    {
                        System.out.println("> "+message.getMessage());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (BadPaddingException e)
                {
                    System.err.println("Incorrect key used. Please update the keys.");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }


        private  void byte2hex(byte b, StringBuffer buf) {
            char[] hexChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
                    '9', 'A', 'B', 'C', 'D', 'E', 'F' };
            int high = ((b & 0xf0) >> 4);
            int low = (b & 0x0f);
            buf.append(hexChars[high]);
            buf.append(hexChars[low]);
        }

        /*
         * Converts a byte array to hex string
         */
        private  String toHexString(byte[] block) {
            StringBuffer buf = new StringBuffer();

            int len = block.length;

            for (int i = 0; i < len; i++) {
                byte2hex(block[i], buf);
                if (i < len-1) {
                    buf.append("");
                }
            }
            return buf.toString();
        }


    }

}
