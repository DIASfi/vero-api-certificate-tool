package fi.dias.tools.vero.pki;

import junit.framework.TestCase;

import java.io.File;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

public class KeyPairUtilsTest extends TestCase {

    public void testFromPrivateKey() throws Exception {
        KeyPair keyPair = KeyPairUtils.fromPrivateKey(new File("../test/RenewCertificate_Private.key"));
        assertEquals("PKCS#8", keyPair.getPrivate().getFormat());
        assertEquals("RSA", keyPair.getPrivate().getAlgorithm());
    }

    public void testGenerate() throws NoSuchAlgorithmException {
        KeyPair keyPair = KeyPairUtils.generate();
        assertEquals("PKCS#8", keyPair.getPrivate().getFormat());
        assertEquals("RSA", keyPair.getPrivate().getAlgorithm());
    }
}