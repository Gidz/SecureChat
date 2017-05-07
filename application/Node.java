package application; /**
 * Created by gideon on 05/05/17.
 */

import essentials.AES;
import essentials.Message;
import node.ClientTask;
import node.ServerTask;

import javax.crypto.KeyAgreement;
import javax.crypto.spec.DHParameterSpec;
import java.security.*;
import java.security.spec.InvalidParameterSpecException;
import java.util.Random;
import java.util.Scanner;

public class Node {
    static int PORT_NUMBER;
    public static int TTP_PORT;
    public static int NODE_NUMBER;
    static String NAME;
    static String CHATTING_WITH;
    static String AES_KEY;
    public static boolean canChat=false;

    static public KeyPair myDHKeyPair;
    static public KeyAgreement myKeyAgreement;
    static public byte[] sharedSecretKey;

    public static void main(String args[]) throws Exception {
        System.out.println("Choosing a random port number to initialize the node .. ");

        Random random = new Random();
        PORT_NUMBER = random.nextInt(65535 - 49152+ 1) + 49152;
        System.out.println("Randomly selected "+PORT_NUMBER);
        Scanner in = new Scanner(System.in);
        System.out.print("Enter the port number of the Third Party Server : ");
        TTP_PORT = Integer.parseInt(in.nextLine());

        startServer(PORT_NUMBER);
        contactTTP();


        System.out.println("Enter yes to exhange keys");
        in.nextLine();
        //TODO: Exchange the AES key
        if(NODE_NUMBER==0)
        {
            startKeyExchange();
        }

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
                AES aes = new AES();
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
    public static void sendMessage(Message message,int port)
    {
        Thread t = new Thread(new ClientTask(message,port));
        t.start();
    }

    static void contactTTP()
    {
        sendMessage(new Message("INITIALIZATION",""+PORT_NUMBER),TTP_PORT);
    }


    static void startKeyExchange() throws NoSuchAlgorithmException, InvalidParameterSpecException, InvalidAlgorithmParameterException, InvalidKeyException {
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
}
