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

@CommandLine.Command(name = "renew", mixinStandardHelpOptions = true,
        description = "Renew certificate using previous certificate and stores it to JSK")
public class RenewCertificateCommand extends CommonCommandOptions implements Callable<Integer> {
    @CommandLine.Parameters(index = "0", description = "The JKS file which containing the keys and certificate for signing renew request")
    private String keyStoreFilePath;

    @CommandLine.Option(names = "--use-existing-private-key", description = "Use existing private key in PEM format")
    private Optional<File> existingPrivateKeyFile = null;

    @CommandLine.Option(names = "--append-existing-jks", description = "Append new private key and certificate to existing JKS file")
    private boolean appendToExistingJKS;

    @CommandLine.Mixin
    CommandLogger logger;

    @Override
    public Integer call() throws Exception {
        logger.enableDebug();

        String keyStoreEntryName = DateTimeFormatter.ofPattern("yyyy-MM-dd-Hms").format(LocalDateTime.now());
        String commandInformationFile = "renew-certificate-information-" + keyStoreEntryName + ".txt";

        String keyStoreForNewPKIFilePath = null;

        KeyStore keyStoreContainingRequestSigningPKI = KeyStoreUtils.loadStore(keyStoreFilePath, keyStorePassword);
        KeyStore keyStoreForStoringNewPKI = null;

        if (appendToExistingJKS) {
            keyStoreForStoringNewPKI = keyStoreContainingRequestSigningPKI;
            keyStoreForNewPKIFilePath = keyStoreFilePath;
        } else {
            keyStoreForStoringNewPKI = KeyStoreUtils.createEmptyStore(keyStorePassword);
            keyStoreForNewPKIFilePath = "renewed-" + keyStoreEntryName + ".jks";
        }

        KeyPair keyPairForNewCertificate = null;
        if (existingPrivateKeyFile.isPresent()) {
            keyPairForNewCertificate = KeyPairUtils.fromPrivateKey(existingPrivateKeyFile.get());
        } else {
            keyPairForNewCertificate = KeyPairUtils.generate();
        }

        KeyStore.PrivateKeyEntry privateKeyEntryForSigningRequest = KeyStoreUtils.readPrivateKeyEntry(keyStoreContainingRequestSigningPKI, keyStoreAlias, keyStorePassword);

        String subjectName = "CN=" + customerName + ",C=FI";
        logger.toFile("Certificate request subject: " + subjectName, commandInformationFile);

        byte[] certificateRequest = CertificateRequestUtils.generateCertificateRequest(keyPairForNewCertificate, subjectName);
        logger.certificateRequestToFile(certificateRequest, commandInformationFile);

        TaskBuilder taskBuilder = new TaskBuilder()
                .inENV(environment)
                .customerId(customerId)
                .customerName(customerName)
                .renewRequestSignKeyPair(privateKeyEntryForSigningRequest)
                .certificateRequest(certificateRequest);

        fi.dias.tools.vero.tasks.RenewCertificate renewCertificate = taskBuilder.buildRenewCertificate();
        String retrievalId = renewCertificate.renew();

        String retrievalMessage = "Retrieval id: " + retrievalId;
        logger.info(retrievalMessage);
        logger.toFile(retrievalMessage, commandInformationFile);

        logger.info("Waiting 30 secs for certificate to be ready before fetching it");
        Thread.sleep(3000);
        logger.info("Fetching certificate");

        FetchCertificate fetchCertificate = taskBuilder.retrievalId(retrievalId).buildFetchCertificate();
        X509Certificate x509Certificate = fetchCertificate.fetch();

        KeyStoreUtils.storePrivateKeyAndCertificate(keyStoreForStoringNewPKI, keyPairForNewCertificate.getPrivate(), x509Certificate, keyStoreEntryName, keyStorePassword);
        KeyStoreUtils.saveStore(keyStoreForStoringNewPKI, keyStorePassword, keyStoreForNewPKIFilePath);

        logger.info("Saving certificate to " + keyStoreForNewPKIFilePath + " with alias: '" + keyStoreAlias + "'");
        logger.certificateToFile(x509Certificate, commandInformationFile);

        logger.toFile("Certificate and Private key are stored to " + keyStoreForNewPKIFilePath + " using alias: " + keyStoreAlias + " and password: " + keyStorePassword, commandInformationFile);
        logger.info("All necessary information about new certificate request can be found from file: " + commandInformationFile);
        return 0;
    }
}
