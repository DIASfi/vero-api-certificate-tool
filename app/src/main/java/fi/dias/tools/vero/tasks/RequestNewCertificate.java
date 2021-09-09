package fi.dias.tools.vero.tasks;

import fi.vero.certificates._2017._10.certificateservices.Result;
import fi.vero.certificates._2017._10.certificateservices.SignNewCertificateRequest;
import fi.vero.certificates._2017._10.certificateservices.SignNewCertificateResponse;

import java.util.Arrays;
import java.util.List;

public class RequestNewCertificate extends WebServiceTask {

    private final String customerId;
    private final String customerName;
    private final String transferId;
    private final String transferPassword;
    private final byte[] certificateRequest;
    private List<String> errors = Arrays.asList();
    private SignNewCertificateRequest signNewCertificateRequest = null;


    public RequestNewCertificate(TaskBuilder builder) {
        super(builder);
        this.customerId = builder.customerId;
        this.customerName = builder.customerName;
        this.transferId = builder.transferId;
        this.transferPassword = builder.transferPassword;
        this.certificateRequest = builder.certificateRequest;
    }

    private boolean prepare() {
        errors = Arrays.asList();

        if (this.customerName == null) {
            errors.add("Customer name is missing");
        }

        if (this.customerId == null) {
            errors.add("Customer id is missing");
        }

        if (this.transferId == null) {
            errors.add("Transfer ID is missing");
        }

        if (this.transferPassword == null) {
            errors.add("Transfer password is missing");
        }

        if (this.certificateRequest == null) {
            errors.add("Certificate request is missing");
        }

        if (errors.isEmpty()) {
            this.signNewCertificateRequest = new SignNewCertificateRequest();
            this.signNewCertificateRequest.setEnvironment(this.environmentType);
            this.signNewCertificateRequest.setCustomerId(this.customerId);
            this.signNewCertificateRequest.setCustomerName(this.customerName);
            this.signNewCertificateRequest.setTransferId(this.transferId);
            this.signNewCertificateRequest.setTransferPassword(this.transferPassword);
            this.signNewCertificateRequest.setCertificateRequest(this.certificateRequest);

            return false;
        } else {
            return true;
        }
    }

    public String request() {
        if (this.prepare()) {
            throw new UnsupportedOperationException("Trying to request new certificate while missing necessary information, errors: " + String.join(", ", errors));
        }

        SignNewCertificateResponse signNewCertificateResponse = this.port.signNewCertificate(signNewCertificateRequest);
        Result result = signNewCertificateResponse.getResult();

        this.handleErrorResult(result, "Failed to request new certificate");

        return signNewCertificateResponse.getRetrievalId();
    }


}
