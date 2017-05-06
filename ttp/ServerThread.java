package ttp;
import javax.crypto.BadPaddingException;
import java.io.*;
import java.net.Socket;
import java.util.StringTokenizer;

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
            System.out.println("> "+message.getMessage());
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

    void handleMessage(Message m)
    {
        int messageType = m.getMessageType();
        if(messageType == 1)
        {
            System.out.println("Message will be handled as the type INTITALIZATION");
            StringTokenizer slasher = new StringTokenizer(m.getMessage());

            String user = slasher.nextToken();
            int port = Integer.parseInt(slasher.nextToken());

            //Update the users list on the server
            TTP.users.put(user,port);

            System.out.println("-----------------------------------");
            System.out.println("Current users list");
            System.out.println(TTP.users.toString());
            System.out.println("-----------------------------------");

        }
        else if(messageType == 2)
        {
            System.out.println("Message will be handle as the type FOR_SERVER");
        }
        else if(messageType == 3)
        {
            System.out.println("Message will be handle as the type IN_TRANSIT");
        }
        else
        {
            System.out.println("Unknown. System FAILURE will occur soon!");
        }
    }
}
