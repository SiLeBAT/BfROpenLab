package de.bund.bfr.knime.openkrise.db.imports.custom.nrw.out;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import de.nrw.verbraucherschutz.idv.daten.*;

public class NRW_Exporter {
		
	public static void main(String[] args) throws JAXBException, SAXException {
		Analyseergebnis ae = new Analyseergebnis();
		ae.setBewertung(new Bewertung());
		new NRW_Exporter().doExport(ae, null);
	}
	
	public boolean doExport(Analyseergebnis ae, String filename) throws SAXException {
		boolean result = true;
		Marshaller writer;
		try {
			ObjectFactory objectFactory = new ObjectFactory();
			JAXBElement<Analyseergebnis> jaxbWrappedHeader =  objectFactory.createAnalyseergebnis(ae);
			JAXBContext context = JAXBContext.newInstance(Analyseergebnis.class);
			writer = context.createMarshaller();
			/*
			writer.setSchema(SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
					.newSchema(Analyseergebnis.class.getResource(
							"/de/nrw/verbraucherschutz/idv/daten/main/de.nrw.verbraucherschutz.idv.dienste.2016.2.warenrueckverfolgung.transport.schema.xsd")));
							*/
			writer.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
			writer.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
			writer.setProperty("com.sun.xml.internal.bind.namespacePrefixMapper", new MyNamespaceMapper());
			writer.marshal(jaxbWrappedHeader, System.out);
						
			if (filename != null) {
				writer.marshal(jaxbWrappedHeader, new File(filename));
			}

			// write SOAP
	        // Create the Document
	        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	        DocumentBuilder db = dbf.newDocumentBuilder();
	        Document document = db.newDocument();
			writer.marshal(jaxbWrappedHeader, document);
// minOccurs="0"
			MessageFactory factory = MessageFactory.newInstance();
	        SOAPMessage soapMsg = factory.createMessage();
			SOAPPart part = soapMsg.getSOAPPart();

			SOAPEnvelope envelope = part.getEnvelope();
			SOAPHeader header = envelope.getHeader();
			SOAPBody body = envelope.getBody();
			envelope.addNamespaceDeclaration("dok", "http://verbraucherschutz.nrw.de/idv/daten/2016.1/dokument");
			envelope.addNamespaceDeclaration("tran", "http://verbraucherschutz.nrw.de/idv/dienste/2016.2/warenrueckverfolgung/transport");
			
			QName qn = new QName("CommHeader");
			//SOAPElement she = header.addChildElement(qn);
			body.addDocument(document);
			
			soapMsg.writeTo(System.out);
			//FileOutputStream fOut = new FileOutputStream("SoapMessage.xml");
			//soapMsg.writeTo(fOut);
			
		} catch (JAXBException | ParserConfigurationException | SOAPException | IOException e) {
			e.printStackTrace();
		}
		return result;
	}
}
