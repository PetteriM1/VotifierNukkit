package petterim1.votifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAKeyGenParameterSpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.xml.bind.DatatypeConverter;

public class RSA {

    static byte[] decrypt(byte[] data, PrivateKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, key);

        return cipher.doFinal(data);
    }

    static KeyPair loadKeys(File directory) throws Exception {
        FileInputStream in = new FileInputStream(directory + "/public.key");
        byte[] encodedPublicKey = new byte[(int) new File(directory + "/public.key").length()];
        in.read(encodedPublicKey);
        encodedPublicKey = DatatypeConverter.parseBase64Binary(new String(encodedPublicKey, StandardCharsets.UTF_8));
        in.close();

        in = new FileInputStream(directory + "/private.key");
        byte[] encodedPrivateKey = new byte[(int) new File(directory + "/private.key").length()];
        in.read(encodedPrivateKey);
        encodedPrivateKey = DatatypeConverter.parseBase64Binary(new String(encodedPrivateKey, StandardCharsets.UTF_8));
        in.close();

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(encodedPublicKey));
        PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(encodedPrivateKey));

        return new KeyPair(publicKey, privateKey);
    }

    static KeyPair generateKeys(File directory) throws Exception {
        directory.mkdir();

        KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
        keygen.initialize(new RSAKeyGenParameterSpec(2048, RSAKeyGenParameterSpec.F4));

        KeyPair keyPair = keygen.generateKeyPair();

        FileOutputStream out = new FileOutputStream(directory + "/public.key");
        out.write(DatatypeConverter.printBase64Binary(new X509EncodedKeySpec(keyPair.getPublic().getEncoded()).getEncoded()).getBytes(StandardCharsets.UTF_8));
        out.close();

        out = new FileOutputStream(directory + "/private.key");
        out.write(DatatypeConverter.printBase64Binary(new PKCS8EncodedKeySpec(keyPair.getPrivate().getEncoded()).getEncoded()).getBytes(StandardCharsets.UTF_8));
        out.close();

        return keyPair;
    }
}
