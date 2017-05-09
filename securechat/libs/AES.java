package securechat.libs;

/**
 * Created by Gideon Paul on 06/05/17.
 */

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class AES {
    private static byte[] IV;
    private static String plaintext;
    private static String encryptionKey;
    private static byte[] cipher;
    private SecretKey key;

    //Constructor for AES class
    public AES(byte[] k) throws NoSuchAlgorithmException {
        byte[] AESSalt,IVSalt;
        MessageDigest sha = MessageDigest.getInstance("SHA-1");
        k = sha.digest(k);

        // use first 128 bits as salt for AES key
        AESSalt = Arrays.copyOf(k, 16);
        SecretKeySpec secretKeySpec = new SecretKeySpec(AESSalt, "AES");
        key = new SecretKeySpec(AESSalt, 0, AESSalt.length, "AES");

        //Use 128 bits to 256 bits for AES IV
        IVSalt = Arrays.copyOfRange(k, 16,32);
        this.IV = IVSalt;
    }

    //The AES encrypt method
    public byte[] encrypt(String plainText) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding", "SunJCE");
        cipher.init(Cipher.ENCRYPT_MODE, key,new IvParameterSpec(IV));
        return cipher.doFinal(plainText.getBytes("UTF-8"));
    }

    //The AES decrypt method
    public String decrypt(byte[] cipherText) throws Exception{
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding", "SunJCE");
        cipher.init(Cipher.DECRYPT_MODE, key,new IvParameterSpec(IV));
        return new String(cipher.doFinal(cipherText),"UTF-8");
    }
}