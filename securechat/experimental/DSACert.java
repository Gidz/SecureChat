package securechat.experimental;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.*;


public class DSACert {

    public static void main (String[] args) throws Exception {

        byte[] plainText = "s".getBytes("UTF8");

        MessageDigest messageDigest = MessageDigest.getInstance("SHA");
        messageDigest.update(plainText);
        byte[] md = messageDigest.digest();
        System.out.print( "\nDigest: " );
        System.out.println(toHexString(md));

        // generate an RSA keypair
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(1024);
        KeyPair key = keyGen.generateKeyPair();
        Key privateKey = key.getPrivate();
        Key publicKey = key.getPublic();

        byte[] temp = publicKey.getEncoded();
        System.out.println("Public key is: "+temp);
        System.out.println("Public key is: "+publicKey);
        System.out.println("Private key is: "+privateKey);

        PublicKey blewah =
                KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(temp));
        System.out.println("Decode public key is: "+blewah);

        // get an RSA cipher and list the provider
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");

        System.out.println( "\nStart encryption" );
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        byte[] cipherText = cipher.doFinal(md);
        System.out.print( "Cipher: " );
        System.out.println( toHexString(cipherText) );



        System.out.println( "\nStart decryption" );
        cipher.init(Cipher.DECRYPT_MODE, publicKey);
        byte[] newMD = cipher.doFinal(cipherText);
        System.out.println( "Finish decryption: " );
        System.out.println(toHexString(newMD) );


        System.out.println( "\nStart signature verification" );
        messageDigest.reset();
        messageDigest.update(plainText);
        byte[] oldMD = messageDigest.digest();



        int len = newMD.length;
        if (len > oldMD.length) {
            System.out.println( "Signature failed, length error");
            System.exit(1);
        }
        for (int i = 0; i < len; ++i)
            if (oldMD[i] != newMD[i]) {
                System.out.println( "Signature failed, element error" );
                System.exit(1);
            }
        System.out.println( "Signature verified" );
    }


    public static void byte2hex(byte b, StringBuffer buf) {
        char[] hexChars = {
                '0',
                '1',
                '2',
                '3',
                '4',
                '5',
                '6',
                '7',
                '8',
                '9',
                'A',
                'B',
                'C',
                'D',
                'E',
                'F'
        };
        int high = ((b & 0xf0) >> 4);
        int low = (b & 0x0f);
        buf.append(hexChars[high]);
        buf.append(hexChars[low]);
    }

    /*
     * Converts a byte array to hex string
     */
    public static String toHexString(byte[] block) {
        StringBuffer buf = new StringBuffer();

        int len = block.length;

        for (int i = 0; i < len; i++) {
            byte2hex(block[i], buf);
            if (i < len - 1) {
                buf.append("");
            }
        }
        return buf.toString();
    }

}


