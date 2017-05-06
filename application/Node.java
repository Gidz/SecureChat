package application; /**
 * Created by gideon on 05/05/17.
 */

import essentials.AES;
import essentials.Message;
import node.ClientTask;
import node.ServerTask;

import java.util.Random;
import java.util.Scanner;

public class Node {
    static int PORT_NUMBER;
    static int TTP_PORT;
    static String NAME;
    static String CHATTING_WITH;
    static String AES_KEY;
    public static void main(String args[]) throws Exception {
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

        System.out.println("Whom do you want to chat with ? ");
        Scanner messageReader = new Scanner(System.in);
        CHATTING_WITH = messageReader.nextLine();
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
                Message m = new Message(3,em,NAME,CHATTING_WITH);
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
