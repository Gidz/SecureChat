package securechat.ttp;
/**
 * Created by gideon on 06/05/17.
 */

import securechat.ttp.ServerThread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class TTP {
    static int PORT_NUMBER;
    public static ArrayList<Integer> users = new ArrayList<>();

    public TTP(int p){
        PORT_NUMBER = p;
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(PORT_NUMBER);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("The Server is up and running!");

        while(true)
        {
            Socket socket = null;
            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Thread thread = new ServerThread(socket);
            thread.start();
        }
    }
}
