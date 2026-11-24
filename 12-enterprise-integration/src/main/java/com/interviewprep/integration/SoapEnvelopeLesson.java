package com.interviewprep.integration;

import java.io.StringReader;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public final class SoapEnvelopeLesson {
    private static final String SOAP_NAMESPACE =
            "http://schemas.xmlsoap.org/soap/envelope/";

    private SoapEnvelopeLesson() {
    }

    public static String readOperation(String xml) throws Exception {
        DocumentBuilderFactory factory = secureFactory();
        Document document = factory.newDocumentBuilder()
                .parse(new InputSource(new StringReader(xml)));
        var xpath = XPathFactory.newInstance().newXPath();
        var body = (org.w3c.dom.Node) xpath.evaluate(
                "/*[local-name()='Envelope' and namespace-uri()='" + SOAP_NAMESPACE
                        + "']/*[local-name()='Body' and namespace-uri()='" + SOAP_NAMESPACE + "']/*[1]",
                document,
                XPathConstants.NODE);
        if (body == null) {
            throw new IllegalArgumentException("SOAP body operation is missing");
        }
        return body.getLocalName();
    }

    private static DocumentBuilderFactory secureFactory()
            throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        return factory;
    }
}
