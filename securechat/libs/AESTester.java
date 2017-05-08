package securechat.libs;
import java.util.Scanner;

/**
 * Created by gideon on 05/05/17.
 */
public class AESTester {
    public static void main(String [] args) {
        try {
            System.out.println("Enter a string to be encrypted: ");
            Scanner in = new Scanner(System.in);
            String plaintext = in.nextLine();
            AES myCipher = new AES(hexStringToByteArray("404B436B389F0CB16131866E770FC0D6761773857579DC62DC942AFD1615E75E6332E9B000E55A1FEB38562EAAA3F350A352DB62E290C283C4CC616A140CCDFD"));

            byte[] cipher = myCipher.encrypt(plaintext);

            System.out.println("Encryption successful. The cipher test is: ");
            for (int i=0; i<cipher.length; i++)
                System.out.print(new Integer(cipher[i])+" ");
            System.out.println("");

            String decrypted = myCipher.decrypt(cipher);

            System.out.println("Decryption successful. The plain text is: " + decrypted);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }


}
