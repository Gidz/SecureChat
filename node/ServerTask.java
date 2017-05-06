package node;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import essentials.*;

import javax.crypto.BadPaddingException;

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

            //Display the message on the standard output
            if (message.getMessageType()==3)
            {
                AES aes = new AES();
                System.out.println("> "+aes.decrypt(message.getEncryptedMessage()));
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
}
