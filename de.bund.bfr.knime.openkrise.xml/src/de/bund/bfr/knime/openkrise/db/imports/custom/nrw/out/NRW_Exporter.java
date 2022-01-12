/*******************************************************************************
 * Copyright (c) 2021 German Federal Institute for Risk Assessment (BfR)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Department Biological Safety - BfR
 *******************************************************************************/
package de.bund.bfr.knime.openkrise.db.imports.custom.nrw.out;

import java.io.ByteArrayOutputStream;
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
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.xmlbeans.impl.soap.MessageFactory;
import org.apache.xmlbeans.impl.soap.SOAPBody;
import org.apache.xmlbeans.impl.soap.SOAPElement;
import org.apache.xmlbeans.impl.soap.SOAPEnvelope;
import org.apache.xmlbeans.impl.soap.SOAPException;
import org.apache.xmlbeans.impl.soap.SOAPHeader;
import org.apache.xmlbeans.impl.soap.SOAPMessage;
import org.apache.xmlbeans.impl.soap.SOAPPart;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.nrw.verbraucherschutz.idv.daten.Analyseergebnis;
import de.nrw.verbraucherschutz.idv.daten.Bewertung;
import de.nrw.verbraucherschutz.idv.daten.ObjectFactory;
import de.nrw.verbraucherschutz.idv.daten.Property;
import de.nrw.verbraucherschutz.idv.daten.PropertyKeys;
import de.nrw.verbraucherschutz.idv.daten.PropertyList;

public class NRW_Exporter {
		
	public static void main(String[] args) throws JAXBException, SAXException {
		Analyseergebnis ae = new Analyseergebnis();
		ae.setBewertung(new Bewertung());
		new NRW_Exporter().doExport(ae, true);
	}
	
	public ByteArrayOutputStream doExport(Analyseergebnis ae, boolean xmlStyleOutput) throws SAXException {
		ByteArrayOutputStream result = null;
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
			p.setKey(PropertyKeys.CORRELATIONID); p.setValue(System.currentTimeMillis() + "@BfR");
			pl.getKey().add(p);
			p = new Property(); p.setKey(PropertyKeys.SENDER); p.setValue("BFR");
			pl.getKey().add(p);
			p = new Property(); p.setKey(PropertyKeys.ORGID); p.setValue("BFR");
			pl.getKey().add(p);
			p = new Property(); p.setKey(PropertyKeys.RECEIVER); p.setValue("LANUV");
			pl.getKey().add(p);
			p = new Property(); p.setKey(PropertyKeys.MSGID); p.setValue((System.currentTimeMillis() + 1) + "@BfR");
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
			//soapMsg.setProperty(SOAPMessage.WRITE_XML_DECLARATION, "true");
			
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
			    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			    transformer.transform(new DOMSource(root), new StreamResult(System.out));    
			    result = new ByteArrayOutputStream();
			    transformer.transform(new DOMSource(root), new StreamResult(result));    				
			}
			else {
				//soapMsg.writeTo(System.out);				
				soapMsg.writeTo(result);				
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
