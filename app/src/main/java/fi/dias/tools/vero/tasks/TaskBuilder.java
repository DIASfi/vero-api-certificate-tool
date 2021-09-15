package fi.dias.tools.vero.tasks;

import fi.vero.certificates._2017._10.certificateservices.EnvironmentTypes;

import java.security.KeyPair;
import java.security.KeyStore;

public class TaskBuilder {
    public enum Environment {
        DEV,
        TEST,
        PROD
    };
    private final String devServiceURL = "https://pkiws-testi.vero.fi/DEV/2017/10/CertificateServices";
    private final String testServiceURL = "https://pkiws-testi.vero.fi/2017/10/CertificateServices";
    private final String prodServiceURL = "https://pkiws.vero.fi/2017/10/CertificateServices";
    private TaskBuilder.Environment env;
    protected String serviceURL = null;
    protected EnvironmentTypes environmentType;
    protected String customerId = null;
    protected String customerName = null;
    protected String transferId = null;
    protected String transferPassword = null;
    protected String retrievalId = null;
    protected byte[] certificateRequest = null;
    protected KeyStore.PrivateKeyEntry renewRequestSignKeyEntry = null;

    public TaskBuilder customerId(String customerId) {
        this.customerId = customerId;
        return this;
    }

    public TaskBuilder customerName(String customerName) {
        this.customerName = customerName;
        return this;
    }

    public TaskBuilder transferId(String transferId) {
        this.transferId = transferId;
        return this;
    }

    public TaskBuilder transferPassword(String transferPassword) {
        this.transferPassword = transferPassword;
        return this;
    }

    public TaskBuilder retrievalId(String retrievalId) {
        this.retrievalId = retrievalId;
        return this;
    }

    public TaskBuilder certificateRequest(byte[] certificateRequest) {
        this.certificateRequest = certificateRequest;
        return this;
    }

    public TaskBuilder inENV(Environment env) {
        this.env = env;
        return this;
    }

    public TaskBuilder renewRequestSignKeyPair(KeyStore.PrivateKeyEntry privateKeyEntry) {
        this.renewRequestSignKeyEntry = privateKeyEntry;
        return this;
    }

    private void setEnvironmentSpecific() {
        switch (this.env) {
            case DEV:
                this.serviceURL = this.devServiceURL;
                this.environmentType = EnvironmentTypes.TEST;
                break;
            case TEST:
                this.serviceURL = this.testServiceURL;
                this.environmentType = EnvironmentTypes.TEST;
                break;
            case PROD:
                this.serviceURL = this.prodServiceURL;
                this.environmentType = EnvironmentTypes.PRODUCTION;
                break;
        }
    }

    public FetchCertificate buildFetchCertificate() {
        this.setEnvironmentSpecific();
        return new FetchCertificate(this);
    }

    public RequestNewCertificate buildRequestNewCertificate() {
        this.setEnvironmentSpecific();
        return new RequestNewCertificate(this);
    }

    public RenewCertificate buildRenewCertificate() {
        this.setEnvironmentSpecific();
        return new RenewCertificate(this);
    }
}
