package de.nrw.verbraucherschutz.idv.daten.test;

import java.io.IOException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import de.nrw.verbraucherschutz.idv.daten.Kontrollpunktmeldung;

public class Test {

	public static void main(String[] args) throws JAXBException, IOException, SAXException {
		new Test();
	}

	@SuppressWarnings("unchecked")
	public Test() throws JAXBException, IOException, SAXException {
		Unmarshaller reader = JAXBContext.newInstance(Kontrollpunktmeldung.class.getPackage().getName())
				.createUnmarshaller();

		reader.setSchema(SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
				.newSchema(Kontrollpunktmeldung.class.getResource(
						"/de/nrw/verbraucherschutz/idv/daten/main/de.nrw.verbraucherschutz.idv.dienste.2016.2.warenrueckverfolgung.transport.schema.xsd")));

		Kontrollpunktmeldung meldung = ((JAXBElement<Kontrollpunktmeldung>) reader.unmarshal(
				getClass().getResource("/de/nrw/verbraucherschutz/idv/daten/test/l2b_kontrollpunktmeldung-v0.2.xml")
						.openStream())).getValue();

		System.out.println(meldung.getBetrieb().getBetriebsname());
	}
}
