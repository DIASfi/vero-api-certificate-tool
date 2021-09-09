package fi.dias.tools.vero.tasks;

import fi.dias.tools.vero.pki.CertificateRequestUtils;
import fi.dias.tools.vero.pki.KeyPairUtils;
import fi.dias.tools.vero.pki.KeyStoreUtils;
import fi.dias.tools.vero.xml.Marshaller;
import fi.vero.certificates._2017._10.certificateservices.*;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import org.w3._2000._09.xmldsig_.SignatureType;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

public class RenewCertificate extends WebServiceTask {
    private final String customerId;
    private final String customerName;
    private byte[] certificateRequest;
    private RenewCertificateRequest renewCertificateRequest;
    private KeyStore.PrivateKeyEntry renewRequestSignKeyEntry;
    private KeyPair signNewCertificateKeyPair = null;
    private KeyStore signNewKeyStore = null;
    private String retrievalId = null;
    private List<String> errors = Arrays.asList();

    public RenewCertificate(TaskBuilder builder) {
        super(builder);
        this.customerId = builder.customerId;
        this.customerName = builder.customerName;
        this.certificateRequest = builder.certificateRequest;
        this.renewRequestSignKeyEntry = builder.renewRequestSignKeyEntry;
    }

    private boolean prepare() {
        this.errors = Arrays.asList();
        this.renewCertificateRequest = null;

        if (this.certificateRequest == null) {
            this.errors.add("Certificate Request is missing");
        }

        if (this.customerName == null) {
            this.errors.add("Customer name is missing");
        }

        if (this.customerId == null) {
            this.errors.add("Customer id is missing");
        }

        if (this.renewRequestSignKeyEntry == null) {
            this.errors.add("Request signing key pair is missing");
        }

        if (errors.isEmpty()) {
            this.renewCertificateRequest = new RenewCertificateRequest();
            this.renewCertificateRequest.setEnvironment(this.environmentType);
            this.renewCertificateRequest.setCustomerId(this.customerId);
            this.renewCertificateRequest.setCustomerName(this.customerName);
            this.renewCertificateRequest.setCertificateRequest(this.certificateRequest);

            return false;
        } else {
            return true;
        }
    }

    private void sign() throws JAXBException, MarshalException, InvalidAlgorithmParameterException, TransformerConfigurationException, UnrecoverableEntryException, CertificateException, NoSuchAlgorithmException, IOException, KeyStoreException, ParserConfigurationException, XMLSignatureException, SAXException {
        PrivateKey privateKey = this.renewRequestSignKeyEntry.getPrivateKey();
        X509Certificate certificate = (X509Certificate) this.renewRequestSignKeyEntry.getCertificate();
        JAXBElement<RenewCertificateRequest> requestJAXBElement = new ObjectFactory().createRenewCertificateRequest(this.renewCertificateRequest);
        byte[] marshalledXML = Marshaller.marshall(requestJAXBElement);

        Document doc = fi.dias.tools.vero.xml.Signer.signXML(marshalledXML, privateKey, certificate);
        SignatureType signatureType = Marshaller.unmarshall(doc);

        this.renewCertificateRequest.setSignature(signatureType);
    }

    public String renew() throws MarshalException, InvalidAlgorithmParameterException, TransformerConfigurationException, JAXBException, CertificateException, NoSuchAlgorithmException, IOException, KeyStoreException, SAXException, UnrecoverableEntryException, ParserConfigurationException, XMLSignatureException {
        if (this.prepare()) {
            throw new UnsupportedOperationException("Trying renew certificate while missing necessary information, errors: " + String.join(", ", errors));
        }

        this.sign();

        RenewCertificateResponse renewCertificateResponse = this.port.renewCertificate(renewCertificateRequest);
        Result result = renewCertificateResponse.getResult();

        this.handleErrorResult(result, "Failed to fetch certificate with retrieval id:" + this.retrievalId);

        return renewCertificateResponse.getRetrievalId();
    }
}
