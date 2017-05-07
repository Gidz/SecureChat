import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
public class DHKeyAgreement2 {

    public static void main(String argv[]) {
        try {
            String mode = "GENERATE_DH_PARAMS";
            DHKeyAgreement2 keyAgree = new DHKeyAgreement2();
            keyAgree.run(mode);
        } catch (Exception e) {
            System.err.println("Error: " + e);
            System.exit(1);
        }
    }

    private void run(String mode) throws Exception {

        DHParameterSpec dhSkipParamSpec;

            // Some central authority creates new DH parameters
            System.out.println("Creating Diffie-Hellman parameters...");
            AlgorithmParameterGenerator paramGen = AlgorithmParameterGenerator.getInstance("DH");
            paramGen.init(1024);
            AlgorithmParameters params = paramGen.generateParameters();
            dhSkipParamSpec = (DHParameterSpec)params.getParameterSpec(DHParameterSpec.class);

        //TODO: Step 1
        //Alice creates her own DH key pair, using the DH parameters above

        System.out.println("ALICE: Generate DH keypair ...");
        KeyPairGenerator aliceKpairGen = KeyPairGenerator.getInstance("DH");
        aliceKpairGen.initialize(dhSkipParamSpec);
        KeyPair aliceKpair = aliceKpairGen.generateKeyPair();

        // Alice creates and initializes her DH KeyAgreement object
        System.out.println("ALICE: Initialization ...");
        KeyAgreement aliceKeyAgree = KeyAgreement.getInstance("DH");
        aliceKeyAgree.init(aliceKpair.getPrivate());

        // Alice encodes her public key, and sends it over to Bob.
        byte[] alicePubKeyEnc = aliceKpair.getPublic().getEncoded();

        System.out.println("Sending this over the network "+toHexString(alicePubKeyEnc));

        /*
         * Let's turn over to Bob. Bob has received Alice's public key
         * in encoded format.
         * He instantiates a DH public key from the encoded key material.
         */
        //TODO: Step 2
        KeyFactory bobKeyFac = KeyFactory.getInstance("DH");
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec
                (alicePubKeyEnc);
        PublicKey alicePubKey = bobKeyFac.generatePublic(x509KeySpec);

        /*
         * Bob gets the DH parameters associated with Alice's public key.
         * He must use the same parameters when he generates his own key
         * pair.
         */
        DHParameterSpec dhParamSpec = ((DHPublicKey)alicePubKey).getParams();

        // Bob creates his own DH key pair
        System.out.println("BOB: Generate DH keypair ...");
        KeyPairGenerator bobKpairGen = KeyPairGenerator.getInstance("DH");
        bobKpairGen.initialize(dhParamSpec);
        KeyPair bobKpair = bobKpairGen.generateKeyPair();

        // Bob creates and initializes his DH KeyAgreement object
        System.out.println("BOB: Initialization ...");
        KeyAgreement bobKeyAgree = KeyAgreement.getInstance("DH");
        bobKeyAgree.init(bobKpair.getPrivate());

        // Bob encodes his public key, and sends it over to Alice.
        byte[] bobPubKeyEnc = bobKpair.getPublic().getEncoded();
        System.out.println("Sending this over the network "+toHexString(bobPubKeyEnc));

        /*
         * Alice uses Bob's public key for the first (and only) phase
         * of her version of the DH
         * protocol.
         * Before she can do so, she has to instantiate a DH public key
         * from Bob's encoded key material.
         */
        //TODO: Step 3
        KeyFactory aliceKeyFac = KeyFactory.getInstance("DH");
        x509KeySpec = new X509EncodedKeySpec(bobPubKeyEnc);
        PublicKey bobPubKey = aliceKeyFac.generatePublic(x509KeySpec);
        System.out.println("ALICE: Execute PHASE1 ...");
        aliceKeyAgree.doPhase(bobPubKey, true);

        /*
         * Bob uses Alice's public key for the first (and only) phase
         * of his version of the DH
         * protocol.
         */
        System.out.println("BOB: Execute PHASE1 ...");
        bobKeyAgree.doPhase(alicePubKey, true);





        /*
         * At this stage, both Alice and Bob have completed the DH key
         * agreement protocol.
         * Both generate the (same) shared secret.
         */
        byte[] aliceSharedSecret = aliceKeyAgree.generateSecret();
        int aliceLen = aliceSharedSecret.length;

        byte[] bobSharedSecret = new byte[aliceLen];
        int bobLen;
        // provide output buffer of required size
        bobLen = bobKeyAgree.generateSecret(bobSharedSecret, 0);

        System.out.println("Alice secret: " +
                toHexString(aliceSharedSecret));
        System.out.println("Bob secret: " +
                toHexString(bobSharedSecret));

        if (!java.util.Arrays.equals(aliceSharedSecret, bobSharedSecret))
            throw new Exception("Shared secrets differ");
        System.out.println("Shared secrets are the same");
    }

    /*
     * Converts a byte to hex digit and writes to the supplied buffer
     */
    private void byte2hex(byte b, StringBuffer buf) {
        char[] hexChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
                '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        int high = ((b & 0xf0) >> 4);
        int low = (b & 0x0f);
        buf.append(hexChars[high]);
        buf.append(hexChars[low]);
    }

    /*
     * Converts a byte array to hex string
     */
    private String toHexString(byte[] block) {
        StringBuffer buf = new StringBuffer();

        int len = block.length;

        for (int i = 0; i < len; i++) {
            byte2hex(block[i], buf);
            if (i < len-1) {
                buf.append(":");
            }
        }
        return buf.toString();
    }
}