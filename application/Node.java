package application; /**
 * Created by gideon on 05/05/17.
 */
import essentials.Message;
import node.*;

import java.util.Random;
import java.util.Scanner;

public class Node {
    static int PORT_NUMBER;
    static int TTP_PORT;
    static String NAME;
    public static void main(String args[])
    {
        System.out.println("Choosing a random port number to initialize the node .. ");

        Random random = new Random();
        PORT_NUMBER = random.nextInt(65535 - 49152+ 1) + 49152;
        System.out.println("Randomly selected "+PORT_NUMBER);
        Scanner in = new Scanner(System.in);
        System.out.print("Enter the port number of the Third Party Server : ");
        TTP_PORT = Integer.parseInt(in.nextLine());
        System.out.println("Enter your identifier");
        NAME = in.nextLine();
        //Start the server thread
        startServer(PORT_NUMBER);
        contactTTP();


        System.out.println("----- CHAT STARTS HERE -----");
        Scanner messageReader = new Scanner(System.in);
        while (true)
        {
            String message = in.nextLine();
            if (message.equals("/quit"))
            {
                break;
            }
            else
            {
                Message m = new Message(message);
                sendMessage(m,PORT_NUMBER);
            }

        }
    }

    static void startServer(int p)
    {
        Thread t = new Thread(new ServerTask(p));
        t.setDaemon(true);
        t.start();
    }

    static void sendMessage(Message message,int port)
    {
        Thread t = new Thread(new ClientTask(message,port));
        t.start();
    }

    static void contactTTP()
    {
        sendMessage(new Message(1,NAME+" "+PORT_NUMBER+""),TTP_PORT);
    }
}
