package fi.dias.tools.vero.tasks;

import com.evanlennick.retry4j.CallExecutorBuilder;
import com.evanlennick.retry4j.Status;
import com.evanlennick.retry4j.config.RetryConfig;
import com.evanlennick.retry4j.config.RetryConfigBuilder;
import fi.vero.certificates._2017._10.certificateservices.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

public class FetchCertificate extends WebServiceTask{
    private static final Logger LOGGER = Logger.getLogger(FetchCertificate.class.getName());
    private String customerId;
    private GetCertificateRequest getCertificateRequest = null;
    private String customerName;
    private String retrievalId;
    private List<String> errors = Arrays.asList();

    public FetchCertificate(TaskBuilder builder) {
        super(builder);
        this.retrievalId = builder.retrievalId;
        this.customerId = builder.customerId;
        this.customerName = builder.customerName;
    }

    private boolean prepare() {
        errors = Arrays.asList();
        getCertificateRequest = null;

        if (this.retrievalId == null) {
            errors.add("Retrieval id is missing");
        }

        if (this.customerName == null) {
            errors.add("Customer name is missing");
        }

        if (this.customerId == null) {
            errors.add("Customer id is missing");
        }

        if (errors.isEmpty()) {
            getCertificateRequest = new GetCertificateRequest();
            getCertificateRequest.setEnvironment(this.environmentType);
            getCertificateRequest.setCustomerId(this.customerId);
            getCertificateRequest.setCustomerName(this.customerName);
            getCertificateRequest.setRetrievalId(this.retrievalId);

            return false;
        } else {
            return true;
        }
    }

    public X509Certificate fetch() throws UnsupportedOperationException, CertificateException {
        if (this.prepare()) {
            throw new UnsupportedOperationException("Trying to fetch certificate while missing necessary information, errors: " + String.join(", ", errors));
        }
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");

        Callable<ByteArrayInputStream> callable = () -> {
            GetCertificateResponse getCertificateResponse = this.port.getCertificate(this.getCertificateRequest);
            Result result = getCertificateResponse.getResult();

            this.handleErrorResult(result, "Failed to fetch certificate with retrieval id:" + this.retrievalId);
            return new ByteArrayInputStream(getCertificateResponse.getCertificate());
        };

        RetryConfig config = new RetryConfigBuilder()
                .retryOnAnyException()
                .withFixedBackoff()
                .withDelayBetweenTries(45, ChronoUnit.SECONDS)
                .withMaxNumberOfTries(10)
                .build();

        Status<ByteArrayInputStream> status = new CallExecutorBuilder().config(config).build().execute(callable);

        if (status.wasSuccessful()) {
            return (X509Certificate) certificateFactory.generateCertificate(status.getResult());
        } else {
            throw new RuntimeException("Failed to fetch certificate after 5 tries in 5 second");
        }
    }
}
