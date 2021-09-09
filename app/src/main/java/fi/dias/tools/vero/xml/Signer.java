package fi.dias.tools.vero.xml;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Signer {
    private static final String EXC_C_14_N = "http://www.w3.org/2001/10/xml-exc-c14n#";

    private static XMLSignatureFactory configuredSignatureFactory() {
        XMLSignatureFactory signatureFactory = XMLSignatureFactory.getInstance("DOM");

        return signatureFactory;
    }

    private static SignedInfo generateSignedInfo(XMLSignatureFactory xmlSignatureFactory) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {
        Transform envelopedTransform = xmlSignatureFactory.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null);
        Transform c14NEXCTransform = xmlSignatureFactory.newTransform(EXC_C_14_N, (TransformParameterSpec) null);
        List<Transform> transforms = Arrays.asList(envelopedTransform, c14NEXCTransform);

        // REVIEW: FROM DOCUMENTATION: Allekirjoitusta muodostettaessa laskettavan tiivisteen (Digest) muodostamiseen tulee käyttää SHA256 -algoritmia.
        DigestMethod digestMethod = xmlSignatureFactory.newDigestMethod(DigestMethod.SHA256, null);
        Reference ref = xmlSignatureFactory.newReference("", digestMethod, transforms, null, null);

        // REVIEW: FROM DOCUMENTATION: Allekirjoituksessa tulee käyttää kanonikalisointia "Exclusive XML Canonicalization Version 1.0".
        CanonicalizationMethod canonicalizationMethod = xmlSignatureFactory.newCanonicalizationMethod(CanonicalizationMethod.EXCLUSIVE, (C14NMethodParameterSpec) null);
        SignatureMethod signatureMethod = xmlSignatureFactory.newSignatureMethod(SignatureMethod.RSA_SHA256, null);
        SignedInfo signedInfo = xmlSignatureFactory.newSignedInfo(canonicalizationMethod, signatureMethod, Collections.singletonList(ref));

        return signedInfo;
    }

    public static Document signXML(byte[] xml, PrivateKey privateKey, X509Certificate certificate) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, IOException, KeyStoreException, CertificateException, UnrecoverableEntryException, ParserConfigurationException, MarshalException, XMLSignatureException, SAXException, TransformerConfigurationException {
        XMLSignatureFactory xmlSignatureFactory = configuredSignatureFactory();
        SignedInfo signedInfo = generateSignedInfo(xmlSignatureFactory);

        KeyInfoFactory keyInfoFactory = xmlSignatureFactory.getKeyInfoFactory();

        X509Data newX509Data = keyInfoFactory.newX509Data(Arrays.asList(certificate));
        List<XMLStructure> data = Arrays.asList(newX509Data);

        KeyInfo keyInfo = keyInfoFactory.newKeyInfo(data);
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);

        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(new ByteArrayInputStream(xml));

        DOMSignContext domSignContext = new DOMSignContext(privateKey, document.getDocumentElement());

        // REVIEW: Hack to add default namespace so that JAX won't fucd things up - @jaakkos
        domSignContext.setDefaultNamespacePrefix("ns2");

        XMLSignature xmlSignature = xmlSignatureFactory.newXMLSignature(signedInfo, keyInfo);

        xmlSignature.sign(domSignContext);

        return document;
    }
}
