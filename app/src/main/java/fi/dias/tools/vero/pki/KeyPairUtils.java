package fi.dias.tools.vero.pki;

import java.io.File;
import java.security.*;
import java.security.interfaces.RSAKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.RSAPublicKeySpec;

public class KeyPairUtils {
    private static PublicKey generateFromPrivateKey(PrivateKey privateKey) throws Exception {
        RSAPrivateCrtKey rsaPrivateCrtKey = (RSAPrivateCrtKey)privateKey;

        RSAPublicKeySpec publicKeySpec = new java.security.spec.RSAPublicKeySpec(rsaPrivateCrtKey.getModulus(), rsaPrivateCrtKey.getPublicExponent());

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey myPublicKey = keyFactory.generatePublic(publicKeySpec);
        return  myPublicKey;
    }

    public static KeyPair fromPrivateKey(File file) throws Exception {
        RSAKey privateKey = (RSAKey) PemUtils.readPrivateKeyFromFile(file.getAbsolutePath(), "RSA");
        PublicKey publicKey = generateFromPrivateKey( (PrivateKey) privateKey);

        return new KeyPair(publicKey, (PrivateKey) privateKey);
    }

    public static KeyPair generate() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);

        return keyPairGenerator.generateKeyPair();
    }

}
