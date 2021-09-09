package fi.dias.tools.vero.pki;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KeyStoreUtils {
    public static final String DEFAULT_PASSWORD = "JKSDefaultPassword";
    private static Logger LOGGER = Logger.getLogger(KeyStoreUtils.class.getName());

    private static String privateKeyPrefix(String prefix) {
        return "private-key-" + prefix;
    }

    private static String certificatePrefix(String prefix) {
        return "certificate-" + prefix;
    }

    private static char[] fallbackToDefaultPassword(String password) {
        if (password != null && !password.isEmpty()) {
            return password.toCharArray();
        } else {
            LOGGER.log(Level.WARNING, "Password not set for KeyStore, using default: " + DEFAULT_PASSWORD);
            return DEFAULT_PASSWORD.toCharArray();
        }
    }

    public static KeyStore storePrivateKeyAndCertificate(KeyStore keyStore, PrivateKey privateKey, X509Certificate certificate, String name, String password) throws KeyStoreException {
        keyStore.setKeyEntry(privateKeyPrefix(name), privateKey, fallbackToDefaultPassword(password), new Certificate[]{certificate});
        keyStore.setCertificateEntry(certificatePrefix(name), certificate);
        return keyStore;
    }

    public static KeyStore.PrivateKeyEntry readPrivateKeyEntry(KeyStore keyStore, String name, String password) throws UnrecoverableEntryException, NoSuchAlgorithmException, KeyStoreException {
        return (KeyStore.PrivateKeyEntry) keyStore.getEntry(privateKeyPrefix(name), new KeyStore.PasswordProtection(password.toCharArray()));
    }

    public static KeyStore createEmptyStore(String password) throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(null, fallbackToDefaultPassword(password));

        return keyStore;
    }

    public static KeyStore loadStore(String filePath, String password) throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException {
        KeyStore keyStore = KeyStore.getInstance("JKS");
        InputStream keyStoreData = new FileInputStream(filePath);
        keyStore.load(keyStoreData, fallbackToDefaultPassword(password));
        keyStoreData.close();

        return keyStore;
    }

    public static void saveStore(KeyStore keyStore, String password, String filePath) throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException {
        FileOutputStream fileOutputStream = new FileOutputStream(filePath);
        keyStore.store(fileOutputStream, fallbackToDefaultPassword(password));
        fileOutputStream.close();
    }
}
