package de.bund.bfr.knime.openkrise.db.imports.custom.nrw.out;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.nrw.verbraucherschutz.idv.daten.*;

public class NRW_Exporter {
		
	public static void main(String[] args) throws JAXBException, SAXException {
		Analyseergebnis ae = new Analyseergebnis();
		ae.setBewertung(new Bewertung());
		new NRW_Exporter().doExport(ae, null, true);
	}
	
	public boolean doExport(Analyseergebnis ae, String filename, boolean xmlStyleOutput) throws SAXException {
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
							"/de/nrw/verbraucherschutz/idv/dienste/de.nrw.verbraucherschutz.idv.dienste.2016.2.warenrueckverfolgung.transport.schema.xsd")));
							*/
			writer.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
			writer.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
			writer.setProperty("com.sun.xml.internal.bind.namespacePrefixMapper", new MyNamespaceMapper());
			/*
			writer.marshal(jaxbWrappedHeader, System.out);
						
			if (filename != null) {
				writer.marshal(jaxbWrappedHeader, new File(filename));
			}
	*/
			// write SOAP
	        // Create the Document
	        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	        DocumentBuilder db = dbf.newDocumentBuilder();
	        Document document = db.newDocument();
			writer.marshal(jaxbWrappedHeader, document);

			MessageFactory factory = MessageFactory.newInstance();
	        SOAPMessage soapMsg = factory.createMessage();
			SOAPPart part = soapMsg.getSOAPPart();

			SOAPEnvelope envelope = part.getEnvelope();
			envelope.setPrefix("soapenv");
			SOAPHeader header = envelope.getHeader();
			header.setPrefix("soapenv");
			PropertyList pl = new PropertyList();
			Property p = new Property();
			p.setKey(PropertyKeys.CORRELATIONID); p.setValue("C123456789@BfR");
			pl.getKey().add(p);
			p = new Property(); p.setKey(PropertyKeys.SENDER); p.setValue("BFR");
			pl.getKey().add(p);
			p = new Property(); p.setKey(PropertyKeys.ORGID); p.setValue("BFR");
			pl.getKey().add(p);
			p = new Property(); p.setKey(PropertyKeys.RECEIVER); p.setValue("LANUV");
			pl.getKey().add(p);
			p = new Property(); p.setKey(PropertyKeys.MSGID); p.setValue("ffdeab2449c99c335a55f804918ade5f844edce6@IDV-SFDP");
			pl.getKey().add(p);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String formattedDate = sdf.format(new Date());
			p = new Property(); p.setKey(PropertyKeys.LASTMODIFIED); p.setValue(formattedDate);
			pl.getKey().add(p);
			JAXBElement<PropertyList> jaxbWrappedCommHeader =  objectFactory.createCommHeader(pl);
			context = JAXBContext.newInstance(PropertyList.class);
			writer = context.createMarshaller();
			writer.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
			writer.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
			writer.setProperty("com.sun.xml.internal.bind.namespacePrefixMapper", new MyNamespaceMapper());
			writer.marshal(jaxbWrappedCommHeader, header);
			header.removeNamespaceDeclaration(envelope.getPrefix());
			
			SOAPBody body = envelope.getBody();
			body.setPrefix("soapenv");
			envelope.addNamespaceDeclaration("dok", "http://verbraucherschutz.nrw.de/idv/daten/2016.1/dokument");
			envelope.addNamespaceDeclaration("tran", "http://verbraucherschutz.nrw.de/idv/dienste/2016.2/warenrueckverfolgung/transport");
			envelope.addNamespaceDeclaration("xsi", "http://www.w3.org/2001/XMLSchema-instance");
			
			body.addDocument(document);
			
			soapMsg.setProperty(SOAPMessage.CHARACTER_SET_ENCODING, "UTF-8");
			//soapMsg.setProperty(SOAPMessage.WRITE_XML_DECLARATION, Boolean.TRUE);
			
			removeXmlns(header.getElementsByTagName("*"));
			removeXmlns(body.getElementsByTagName("*"));
			
			if (xmlStyleOutput) {
			    Source source = part.getContent();

			    Node root = null;
			    if (source instanceof DOMSource) {
			      root = ((DOMSource) source).getNode();
			    } else if (source instanceof SAXSource) {
			      org.xml.sax.InputSource inSource = ((SAXSource) source).getInputSource();
			      DocumentBuilderFactory dbff = DocumentBuilderFactory.newInstance();
			      dbff.setNamespaceAware(true);
			      DocumentBuilder dbb = null;

			      dbb = dbff.newDocumentBuilder();

			      Document doc = dbb.parse(inSource);
			      root = (Node) doc.getDocumentElement();
			    }
			    
			    Transformer transformer = TransformerFactory.newInstance().newTransformer();
			    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			    transformer.transform(new DOMSource(root), new StreamResult(System.out));    				
			}
			else {
				soapMsg.writeTo(System.out);				
			}
		  
			//FileOutputStream fOut = new FileOutputStream("SoapMessage.xml");
			//soapMsg.writeTo(fOut);
			
		} catch (JAXBException | ParserConfigurationException | SOAPException | IOException | TransformerFactoryConfigurationError | TransformerException e) {
			e.printStackTrace();
		}
		return result;
	}
	private void removeXmlns(NodeList nodeList) {
		String[] nss = MyNamespaceMapper.getPreDeclaredNamespacePrefixes();
		for (int i = 0; i < nodeList.getLength(); i++) {
		    Node node = nodeList.item(i);
		    if (node.getNodeType() == Node.ELEMENT_NODE) {
	            SOAPElement childX = (SOAPElement) node;
	            
	            for (String ns : nss) {
			    	childX.removeNamespaceDeclaration(ns);	            	
	            }
	            
	            /*
		    	childX.removeNamespaceDeclaration("tran");
		    	childX.removeNamespaceDeclaration("dok");
		    	childX.removeNamespaceDeclaration("wrv");
		    	childX.removeNamespaceDeclaration("kat");
		    	childX.removeNamespaceDeclaration("kom");
		    	*/
		    }
		}			
	}
}
