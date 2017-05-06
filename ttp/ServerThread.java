package ttp;
import javax.crypto.BadPaddingException;
import java.io.*;
import java.net.Socket;
import java.util.*;

import application.TTP;
import com.sun.org.apache.xpath.internal.SourceTree;
import essentials.*;

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

    static void handleMessage(Message m)
    {
        int messageType = m.getMessageType();
        if(messageType == 1)
        {
            System.out.println("> "+m.getMessage());
            StringTokenizer slasher = new StringTokenizer(m.getMessage());

            String user = slasher.nextToken();
            int port = Integer.parseInt(slasher.nextToken());

            //Update the users list on the server
            TTP.users.put(user,port);
//            printOnlineUsers();
            if(TTP.users.size()==1)
            {
                sendMessage(new Message("Waiting for the other user to join."),port);
            }
            else
            {
                sendToAll(new Message("Updated user list\n"+TTP.users.keySet()));
            }
        }
        else if(messageType == 2)
        {
            //Just display it on screen
            System.out.println("> "+m.getMessage());
        }
        else if(messageType == 3)
        {
            //This message should be sent to the other node
            System.out.println("# "+m.getMessage());
            sendMessage(m,TTP.users.get(m.getReceiver()));
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
        for (String key : TTP.users.keySet()) {
            int port = TTP.users.get(key);
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
}
