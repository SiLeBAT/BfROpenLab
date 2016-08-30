package de.nrw.verbraucherschutz.idv.daten.test;

import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import de.nrw.verbraucherschutz.idv.daten.Kontrollpunktmeldung;

public class Test {

	public static void main(String[] args) throws JAXBException, IOException {
		new Test();
	}

	@SuppressWarnings("unchecked")
	public Test() throws JAXBException, IOException {
		Unmarshaller reader = JAXBContext.newInstance(Kontrollpunktmeldung.class.getPackage().getName())
				.createUnmarshaller();
		Kontrollpunktmeldung meldung = ((JAXBElement<Kontrollpunktmeldung>) reader.unmarshal(
				getClass().getResource("/de/nrw/verbraucherschutz/idv/daten/test/l2b_kontrollpunktmeldung-v0.2.xml")
						.openStream())).getValue();

		System.out.println(meldung.getBetrieb().getBetriebsname());
	}
}
