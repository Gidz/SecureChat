package securechat.ttp;

import securechat.essentials.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.*;
import java.security.spec.InvalidKeySpecException;

/**
 * Created by gideon on 20/04/17.
 */
public class ServerThread extends Thread {
    Socket socket;
    public ServerThread(Socket socket)
    {
        this.socket = socket;
    }
    public void run()
    {
        try {
            ObjectInputStream tunnelIn = new ObjectInputStream (socket.getInputStream());
            ObjectOutputStream tunnelOut = new ObjectOutputStream(socket.getOutputStream());

            Message message= (Message) tunnelIn.readObject();
            //Display the message on the screen
            handleMessage(message);

        }
        catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static void handleMessage(Message m) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidAlgorithmParameterException, InvalidKeyException {
        String messageType = m.getMessageType();
        if(messageType.equals("INITIALIZATION"))
        {
            System.out.println("> "+m.getMessage());
            int port = Integer.parseInt(m.getMessage());

            //Update the users list on the server
            TTP.users.add(port);
//            printOnlineUsers();
            if(TTP.users.size()==1)
            {
                sendMessage(new Message("UPDATE_NODE_NUMBER","0"),port);
                sendMessage(new Message("INFO","Waiting for the other user to join."),port);
            }
            else
            {
                sendMessage(new Message("UPDATE_NODE_NUMBER","1"),port);
                sendToAll(new Message("INFO","All the users joined. You can begin chat now"));
                sendMessage(new Message("EXCHANGE_KEYS",""),TTP.users.get(0));
            }
        }
        else if(messageType.equals("INFO"))
        {
            //Just display it on screen
            System.out.println("> "+m.getMessage());
        }
        else if(messageType.equals("CHAT"))
        {
            byte[] cipher;
            cipher = m.getEncryptedMessage();
            //Printing out the encrypted message
            for (int i=0; i<cipher.length; i++)
                System.out.print(new Integer(cipher[i])+" ");
            System.out.println("");

            //Sending the message to another securechat.node
            invokeToggleSender(m);
        }
        //The key exchange protocol
        else if(messageType.equals("DH1") || messageType.equals("DH2"))
        {
            //Sending the message to another securechat.node
            invokeToggleSender(m);
        }
        else if(messageType.equals("START_CHAT"))
        {
            invokeToggleSender(m);
        }
        else
        {
            System.out.println("Unknown. System FAILURE will occur soon!");
        }
    }

    static void sendMessage(Message message, int port)
    {
        Thread t = new Thread(new ClientThread(message,port));
        t.start();
    }

    static void sendToAll(Message message)
    {
        for (int i=0;i<TTP.users.size();i++) {
            int port = TTP.users.get(i);
            Thread t = new Thread(new ClientThread(message,port));
            t.start();
        }
    }

    static void printOnlineUsers()
    {
        System.out.println("-----------------------------------");
        System.out.println("Current users list");
        System.out.println(TTP.users.toString());
        System.out.println("-----------------------------------");
    }

    static void invokeToggleSender(Message m)
    {
        if(m.getSender() == 0)
        {
            sendMessage(m,TTP.users.get(1));
        }
        else if(m.getSender() == 1)
        {
            sendMessage(m,TTP.users.get(0));
        }
    }
}
