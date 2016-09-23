package de.bund.bfr.knime.openkrise.db.imports.custom.nrw.out;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import de.nrw.verbraucherschutz.idv.daten.*;

public class NRW_Exporter {
		
	public static void main(String[] args) throws JAXBException {
		new NRW_Exporter().doExport(new Analyseergebnis());
	}
	
	public boolean doExport(Analyseergebnis ae) {
		boolean result = true;
		Marshaller writer;
		try {
			JAXBContext context = JAXBContext.newInstance(Analyseergebnis.class);
			writer = context.createMarshaller();
			writer.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
			writer.marshal(ae, System.out);
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		return result;
	}
}
