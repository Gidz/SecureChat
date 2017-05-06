package essentials;

/**
 * Created by gideon on 06/05/17.
 */

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES {
    private static String IV;
    private static String plaintext;
    private static String encryptionKey;
    private static byte[] cipher;
    public AES()
    {
        //Use these by default if none specified
        this.IV = "Az5AAAAAAAAAAAAA";
        this.encryptionKey="07B820B5A579C74D32D5F5F4B6EA9081";
    }

    public AES(String key)
    {
        this.encryptionKey = key;
        this.IV = "Az5AAAAAAAAAAAAA";
    }

    public byte[] encrypt(String plainText) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding", "SunJCE");
        SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes("UTF-8"), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, key,new IvParameterSpec(IV.getBytes("UTF-8")));
        return cipher.doFinal(plainText.getBytes("UTF-8"));
    }

    public String decrypt(byte[] cipherText) throws Exception{
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding", "SunJCE");
        SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes("UTF-8"), "AES");
        cipher.init(Cipher.DECRYPT_MODE, key,new IvParameterSpec(IV.getBytes("UTF-8")));
        return new String(cipher.doFinal(cipherText),"UTF-8");
    }
}