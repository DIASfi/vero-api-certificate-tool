package fi.dias.tools.vero.pki;

import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;

import javax.security.auth.x500.X500Principal;
import java.io.IOException;
import java.security.KeyPair;

public class CertificateRequestUtils {
    public static byte[] generateCertificateRequest(KeyPair keyPair, String subjectName) throws OperatorCreationException, IOException {
        String algorithm = "SHA256WithRSA";
        X500Principal certificateSubject = new X500Principal(subjectName);
        PKCS10CertificationRequestBuilder pkcs10CertificationRequestBuilder = new JcaPKCS10CertificationRequestBuilder(certificateSubject, keyPair.getPublic());
        JcaContentSignerBuilder jcaContentSignerBuilder = new JcaContentSignerBuilder(algorithm);
        ContentSigner signer = jcaContentSignerBuilder.build(keyPair.getPrivate());
        PKCS10CertificationRequest pkcs10CertificationRequest = pkcs10CertificationRequestBuilder.build(signer);

        return pkcs10CertificationRequest.getEncoded();
    }
}
