package application; /**
 * Created by gideon on 06/05/17.
 */

import ttp.ServerThread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;

public class TTP {
    static int PORT_NUMBER;

    public static HashMap<String,Integer> users = new HashMap<String, Integer>();

    public static void main(String args[]) throws IOException {
        System.out.print("Choose a port number to start the Server: ");
        Scanner in = new Scanner(System.in);
        PORT_NUMBER = Integer.parseInt(in.nextLine());
        ServerSocket serverSocket = new ServerSocket(PORT_NUMBER);
        System.out.println("The Server is up and running!");

        while(true)
        {
            Socket socket = serverSocket.accept();
            Thread thread = new ServerThread(socket);
            thread.start();
        }
    }
}
