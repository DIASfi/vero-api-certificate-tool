package fi.dias.tools.vero.xml;

import fi.vero.certificates._2017._10.certificateservices.RenewCertificateRequest;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.w3._2000._09.xmldsig_.SignatureType;
import org.w3c.dom.Document;

import java.io.ByteArrayOutputStream;

public class Marshaller {
    public static byte[] marshall(JAXBElement<RenewCertificateRequest> renewCertificateRequestJAXBElement) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance("fi.vero.certificates._2017._10.certificateservices");
        jakarta.xml.bind.Marshaller marshaller = jaxbContext.createMarshaller();

        ByteArrayOutputStream marshalledXMLOutputStream = new ByteArrayOutputStream();

        marshaller.setProperty(jakarta.xml.bind.Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
        marshaller.marshal(renewCertificateRequestJAXBElement, marshalledXMLOutputStream);

        return marshalledXMLOutputStream.toByteArray();
    }
    
    public static SignatureType unmarshall(Document document) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance("fi.vero.certificates._2017._10.certificateservices");
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

        JAXBElement<SignatureType> signedInfoTypeJAXBElement = unmarshaller.unmarshal(document.getDocumentElement().getLastChild(), SignatureType.class);
        return signedInfoTypeJAXBElement.getValue();
    }
}
