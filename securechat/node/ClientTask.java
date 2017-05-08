package securechat.node;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import securechat.essentials.*;

/**
 * Created by gideon on 05/05/17.
 */
public class ClientTask extends Thread implements Runnable {
    Message message;
    int port;
    public ClientTask(Message m,int p)
    {
        this.message = m;
        this.port = p;
    }
    @Override
    public void run() {
        Socket socket;
        try {
            socket = new Socket(InetAddress.getLocalHost(),port);
            ObjectOutputStream tunnelOut = new ObjectOutputStream(socket.getOutputStream());
            tunnelOut.writeObject(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
