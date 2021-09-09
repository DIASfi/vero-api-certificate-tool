package fi.dias.tools.vero.cli;

import fi.dias.tools.vero.pki.CertificateRequestUtils;
import fi.dias.tools.vero.pki.KeyPairUtils;
import fi.dias.tools.vero.pki.KeyStoreUtils;
import fi.dias.tools.vero.tasks.FetchCertificate;
import fi.dias.tools.vero.tasks.TaskBuilder;
import picocli.CommandLine;

import java.io.File;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "renew", mixinStandardHelpOptions = true, version = "checksum 4.0",
        description = "Renew certificate using previous certificate and stores it to JSK")
public class RenewCertificateCommand extends CommonCommandOptions implements Callable<Integer> {
    @CommandLine.Parameters(index = "0", description = "The JKS file which is created containing the keys and certificate")
    private String keyStoreFilePath;

    @CommandLine.Option(names = "--use-existing-private-key", description = "Use existing private key in PEM format")
    private Optional<File> existingPrivateKeyFile = null;

    @CommandLine.Mixin
    CommandLogger logger;

    @Override
    public Integer call() throws Exception {
        logger.enableDebug();

        String keyStoreEntryName = DateTimeFormatter.ofPattern("yyyy-MM-dd-Hms").format(LocalDateTime.now());

        KeyStore keyStore = KeyStoreUtils.loadStore(keyStoreFilePath, keyStorePassword);

        KeyPair keyPairForNewCertificate = null;
        if (existingPrivateKeyFile.isPresent()) {
            keyPairForNewCertificate = KeyPairUtils.fromPrivateKey(existingPrivateKeyFile.get());
        } else {
            keyPairForNewCertificate = KeyPairUtils.generate();
        }

        KeyStore.PrivateKeyEntry privateKeyEntryForSigningRequest = KeyStoreUtils.readPrivateKeyEntry(keyStore, keyStoreAlias, keyStorePassword);

        String subjectName = "CN=" + customerName + ",C=FI";
        byte[] certificateRequest = CertificateRequestUtils.generateCertificateRequest(keyPairForNewCertificate, subjectName);

        TaskBuilder taskBuilder = new TaskBuilder()
                .inENV(environment)
                .customerId(customerId)
                .customerName(customerName)
                .renewRequestSignKeyPair(privateKeyEntryForSigningRequest)
                .certificateRequest(certificateRequest);

        fi.dias.tools.vero.tasks.RenewCertificate renewCertificate = taskBuilder.buildRenewCertificate();
        String retrievalId = renewCertificate.renew();

        FetchCertificate fetchCertificate = taskBuilder.retrievalId(retrievalId).buildFetchCertificate();
        X509Certificate x509Certificate = fetchCertificate.fetch();

        KeyStoreUtils.storePrivateKeyAndCertificate(keyStore, keyPairForNewCertificate.getPrivate(), x509Certificate, keyStoreEntryName, keyStorePassword);
        KeyStoreUtils.saveStore(keyStore, keyStorePassword, keyStoreFilePath);

        return 0;
    }
}
