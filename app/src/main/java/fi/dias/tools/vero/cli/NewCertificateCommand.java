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

@CommandLine.Command(name = "new", mixinStandardHelpOptions = true,
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
        String commandInformationFile = "new-certificate-information-" + keyStoreAlias + ".txt";
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
        logger.debug("Certificate request subject: " + subjectName);
        logger.toFile("Certificate request subject: " + subjectName, commandInformationFile);

        byte[] certificateRequest = CertificateRequestUtils.generateCertificateRequest(keyPair, subjectName);
        logger.certificateRequestToFile(certificateRequest, commandInformationFile);

        TaskBuilder taskBuilder = new TaskBuilder()
                .inENV(environment)
                .customerId(customerId)
                .customerName(customerName)
                .transferId(transferId)
                .transferPassword(transferPassword)
                .certificateRequest(certificateRequest);

        // REVIEW:
        // Varmenteen hakemisessa on otettava huomioon, että allekirjoituspyynnön (SignNewCertificate) saa tehdä siirtotunnuksilla
        // (TransferId ja TransferPassword) vain yhden kerran. Varmenteen noudon (GetCertificate) voi tehdä samalla noutotunnuksella
        // (RetrievalId) useita kertoja. Allekirjoituspyynnön ja noudon välillä on viive eli kannattaa odottaa jonkin aikaa.
        // https://www.vero.fi/tulorekisteri/ohjelmistokehitt%C3%A4j%C3%A4t/varmennepalvelu/kysymyksi%C3%A4-ja-vastauksia-varmennepalvelusta/#nouto

        RequestNewCertificate requestNewCertificate = taskBuilder.buildRequestNewCertificate();
        String retrievalId = requestNewCertificate.request();

        String retrievalMessage = "Retrieval id: " + retrievalId;
        logger.info(retrievalMessage);
        logger.toFile(retrievalMessage, commandInformationFile);

        // REVIEW:
        // Mikä on käsittelyaika, kun noudetaan pyydettyä tai uusittua varmennetta GetCertificateRequest-pyynnöllä?
        // Dokumentaatiossa on määritelty varoaika 5 minuutiksi, mutta käytännössä aika on alle 30 sekuntia. Usein aika on selkeästi lyhyempi.
        // https://www.vero.fi/tulorekisteri/ohjelmistokehitt%C3%A4j%C3%A4t/varmennepalvelu/kysymyksi%C3%A4-ja-vastauksia-varmennepalvelusta/#nouto

        logger.info("Waiting 30 secs for certificate to be ready before fetching it");
        Thread.sleep(3000);
        logger.info("Fetching certificate");

        FetchCertificate fetchCertificate = taskBuilder.retrievalId(retrievalId).buildFetchCertificate();
        X509Certificate x509Certificate = fetchCertificate.fetch();

        KeyStore keyStore = KeyStoreUtils.createEmptyStore(keyStorePassword);
        logger.info("Saving certificate to " + keyStoreFilePath + " with alias: '" + keyStoreAlias + "'");

        logger.certificateToFile(x509Certificate, commandInformationFile);

        KeyStoreUtils.storePrivateKeyAndCertificate(keyStore, keyPair.getPrivate(), x509Certificate, keyStoreAlias, keyStorePassword);
        KeyStoreUtils.saveStore(keyStore, keyStorePassword, keyStoreFilePath);

        logger.toFile("Certificate and Private key are stored to " + keyStoreFilePath + " using alias: " + keyStoreAlias + " and password: " + keyStorePassword, commandInformationFile);

        logger.info("All necessary information about new certificate request can be found from file: " + commandInformationFile);

        return 0;
    }
}
