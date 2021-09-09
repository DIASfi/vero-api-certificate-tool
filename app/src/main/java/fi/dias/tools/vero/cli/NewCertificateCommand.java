package fi.dias.tools.vero.cli;

import fi.dias.tools.vero.pki.CertificateRequestUtils;
import fi.dias.tools.vero.pki.KeyPairUtils;
import fi.dias.tools.vero.pki.KeyStoreUtils;
import fi.dias.tools.vero.tasks.FetchCertificate;
import fi.dias.tools.vero.tasks.RequestNewCertificate;
import fi.dias.tools.vero.tasks.TaskBuilder;
import picocli.CommandLine;

import java.io.File;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Optional;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "new", mixinStandardHelpOptions = true, version = "checksum 4.0",
        description = "Requests new certificate and stores it to JSK")
public class NewCertificateCommand extends CommonCommandOptions implements Callable<Integer> {
    @CommandLine.Parameters(index = "0", description = "The JKS file which is created containing the keys and certificate")
    private String keyStoreFilePath;

    @CommandLine.Option(names = "--transfer-id", description = "Transfer ID")
    private String transferId = null;

    @CommandLine.Option(names = "--transfer-password", description = "Transfer Password")
    private String transferPassword = null;

    @CommandLine.Option(names = "--use-existing-private-key", description = "Use existing private key in PEM format")
    private Optional<File> existingPrivateKeyFile = null;

    @CommandLine.Mixin
    CommandLogger logger;

    @Override
    public Integer call() throws Exception {
        logger.enableDebug();

        KeyPair keyPair = null;
        if (existingPrivateKeyFile.isPresent()) {
            logger.debug("Using existing private key file from " + existingPrivateKeyFile);
            keyPair = KeyPairUtils.fromPrivateKey(existingPrivateKeyFile.get());
        } else {
            logger.debug("Generating new key pair");
            keyPair = KeyPairUtils.generate();
        }

        String subjectName = "CN=" + customerName + ",C=FI";

        byte[] certificateRequest = CertificateRequestUtils.generateCertificateRequest(keyPair, subjectName);

        TaskBuilder taskBuilder = new TaskBuilder()
                .inENV(environment)
                .customerId(customerId)
                .customerName(customerName)
                .transferId(transferId)
                .transferPassword(transferPassword)
                .certificateRequest(certificateRequest);

        RequestNewCertificate requestNewCertificate = taskBuilder.buildRequestNewCertificate();
        String retrievalId = requestNewCertificate.request();

        FetchCertificate fetchCertificate = taskBuilder.retrievalId(retrievalId).buildFetchCertificate();
        X509Certificate x509Certificate = fetchCertificate.fetch();

        KeyStore keyStore = KeyStoreUtils.createEmptyStore(keyStorePassword);
        logger.info("Saving certificate to " + keyStoreFilePath + " with alias: '" + keyStoreAlias + "'");

        KeyStoreUtils.storePrivateKeyAndCertificate(keyStore, keyPair.getPrivate(), x509Certificate, keyStoreAlias, keyStorePassword);
        KeyStoreUtils.saveStore(keyStore, keyStorePassword, keyStoreFilePath);

        return 0;
    }
}
