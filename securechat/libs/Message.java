package securechat.libs;
import java.io.Serializable;

/**
 * Created by Gideon Paul on 20/04/17.
 */

/*
* The Message class is what we use for the communication between the nodes.
* The objects of these class are transported over the network.
*/
public class Message implements Serializable {
    private int sender;
    private String messageType;
    private byte[] message;
    private byte[] hash;
    private String stringMessage;

    //This constructor takes in Type of the message, a byte array of the message and the sender node id
    public Message(String t, byte[] m, int s) {
        this.messageType = t;
        this.sender = s;
        this.message = m;
    }

    //This constructor takes in Type of the message, a byte array of the message, byte array of signed hash and the sender node id
    public Message(String t, byte[] m, byte[] h, int s) {
        this.messageType = t;
        this.sender = s;
        this.message = m;
        this.hash = h;
    }

    //A more plain version which takes in type of the message and a message in String format
    public Message(String t, String m)
    {
        this.messageType = t;
        this.stringMessage = m;
    }

    //All the getter functions

    public byte[] getMessage() {
        return this.message;
    }

    public byte[] getHash() {
        return this.hash;
    }

    public int getSender() {
        return this.sender;
    }

    public String getMessageType() {
        return this.messageType;
    }

    public String getStringMessage() {
        return this.stringMessage;
    }

}