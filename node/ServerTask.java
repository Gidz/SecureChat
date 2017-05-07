package node;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import application.Node;
import essentials.*;

import javax.crypto.BadPaddingException;
import javax.crypto.KeyAgreement;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;

/**
 * Created by gideon on 05/05/17.
 */
public class ServerTask extends Thread implements Runnable {
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

        //node.Server is running always. This is done using this while(true) loop
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
                System.out.println("The node number is "+Node.NODE_NUMBER);
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


    private static void byte2hex(byte b, StringBuffer buf) {
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
    private static String toHexString(byte[] block) {
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
