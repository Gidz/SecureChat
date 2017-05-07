package essentials; /**
 * Created by gideon on 06/05/17.
 * The essentials.Message class is used to trasmit an actual message over the network.
 * essentials.Message type can be one of the following
 * 1. Initialization
 * 2. In Transit
 * 3. For Server
 **/

import java.io.Serializable;

/**
 * Created by Gideon on 20/04/17.
 */
public class Message implements Serializable{
//    public String resolveMessageCode(int p)
//    {
//        switch (p)
//        {
//            case 1: return "INITIALIZATION";
//            case 2: return "INFO";
//            case 3: return "IN_TRANSIT";
//            default:return "UNKNOWN";
//        }
//    }
    private String receiver,message;
    private int sender;
    private String messageType;
    private byte[] encryptedMessage;

    public Message(String t,byte[] m,int s)
    {
        this.messageType =t;
        this.sender = s;
        this.encryptedMessage = m;
    }

    public Message(String t,String m)
    {
        this.messageType =t;
        this.message = m;
    }

    public Message(String m)
    {
        this.message = m;
    }

    public String getMessage()
    {
        return this.message;
    }

    public byte[] getEncryptedMessage()
    {
        return this.encryptedMessage;
    }

    public int getSender()
    {
        return this.sender;
    }

    public String getReceiver()
    {
        return this.receiver;
    }

    public String getMessageType()
    {
        return messageType;
    }

    public String getDetails()
    {
        return "SENDER : "+this.sender+"\nRECEIVER :"+this.receiver+"\nMESSAGE :"+this.message;
    }
}
