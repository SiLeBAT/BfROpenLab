package de.bund.bfr.knime.openkrise.db.imports.custom.nrw.out;

import java.io.File;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import de.nrw.verbraucherschutz.idv.daten.*;

public class NRW_Exporter {
		
	public static void main(String[] args) throws JAXBException, SAXException {
		new NRW_Exporter().doExport(new Analyseergebnis(), null);
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
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		return result;
	}
}
