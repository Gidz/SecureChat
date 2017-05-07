package essentials;

/**
 * Created by gideon on 06/05/17.
 */

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class AES {
    private static String IV;
    private static String plaintext;
    private static String encryptionKey;
    private static byte[] cipher;
    private SecretKey key;
    public AES()
    {

    }

    public AES(byte[] k) throws NoSuchAlgorithmException {
        MessageDigest sha = MessageDigest.getInstance("SHA-1");
        k = sha.digest(k);
        k = Arrays.copyOf(k, 16); // use only first 128 bit
        SecretKeySpec secretKeySpec = new SecretKeySpec(k, "AES");
        key = new SecretKeySpec(k, 0, k.length, "AES");

        //TODO: Change the IV based on the byte[] array
        this.IV = "AAAAAAAAAAAAAAAA";
    }

    public byte[] encrypt(String plainText) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding", "SunJCE");
//        SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes("UTF-8"), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, key,new IvParameterSpec(IV.getBytes("UTF-8")));
        return cipher.doFinal(plainText.getBytes("UTF-8"));
    }

    public String decrypt(byte[] cipherText) throws Exception{
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding", "SunJCE");
//        SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes("UTF-8"), "AES");
        cipher.init(Cipher.DECRYPT_MODE, key,new IvParameterSpec(IV.getBytes("UTF-8")));
        return new String(cipher.doFinal(cipherText),"UTF-8");
    }
}