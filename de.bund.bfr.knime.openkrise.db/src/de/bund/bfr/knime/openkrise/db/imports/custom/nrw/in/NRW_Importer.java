package de.bund.bfr.knime.openkrise.db.imports.custom.nrw.in;

import java.io.File;
import java.util.HashMap;

import javax.swing.JProgressBar;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import de.nrw.verbraucherschutz.idv.daten.*;
import de.bund.bfr.knime.openkrise.db.imports.MyImporter;

public class NRW_Importer implements MyImporter {
	
	private HashMap<String, Kontrollpunktmeldung> kpms = null;
	private HashMap<String, Betrieb> betriebe = null;
	
	public static void main(String[] args) throws JAXBException {
		//new NRW_Importer().doImport("/Users/arminweiser/Desktop/xml_test/bbk/", null, true);
		new NRW_Importer().doImport("/Users/arminweiser/Desktop/xml_test/tst/", null, true);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public boolean doImport(String foldername, JProgressBar progress, boolean showResults) {
		boolean result = true;
		Unmarshaller reader;
		try {
			reader = JAXBContext.newInstance(Kontrollpunktmeldung.class.getPackage().getName())
					.createUnmarshaller();

			reader.setSchema(SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
					.newSchema(Kontrollpunktmeldung.class.getResource(
							"/de/nrw/verbraucherschutz/idv/dienste/de.nrw.verbraucherschutz.idv.dienste.2016.2.warenrueckverfolgung.transport.schema.xsd")));
	
			File folder = new File(foldername);
			if (folder.exists() && folder.isDirectory()) {
				File[] fs = folder.listFiles();
				if (fs != null && fs.length > 0) {
					kpms = new HashMap<>();
					betriebe = new HashMap<>();
					for (File f : fs) {
						if (f.getName().endsWith(".xml")) {
							System.out.println("----- " + f.getName() + " -----");

							try {
								Kontrollpunktmeldung meldung = ((JAXBElement<Kontrollpunktmeldung>) reader.unmarshal(f)).getValue();
								
								System.out.println(meldung.getBetrieb().getBetriebsname());
								if (!betriebe.containsKey(meldung.getBetrieb().getBetriebsnummer())) {
									betriebe.put(meldung.getBetrieb().getBetriebsnummer(), meldung.getBetrieb());
								}
								kpms.put(meldung.getBetrieb().getBetriebsnummer(), meldung);
								if (meldung.getWareneingaenge() != null) {
									for (Wareneingang we : meldung.getWareneingaenge().getWareneingang()) {
										for (Betrieb b : we.getBetrieb()) {
											if (b.getTyp().equals("LIEFERANT")) {
												if (!betriebe.containsKey(b.getBetriebsnummer())) {
													betriebe.put(b.getBetriebsnummer(), b);
												}
											}
										}
									}									
								}
								if (meldung.getWarenausgaenge() != null) {
									for (Warenausgang wa : meldung.getWarenausgaenge().getWarenausgang()) {
										for (Betrieb b : wa.getBetrieb()) {
											if (b.getTyp().equals("KUNDE")) {
												if (!betriebe.containsKey(b.getBetriebsnummer())) {
													betriebe.put(b.getBetriebsnummer(), b);
												}
											}
										}
									}									
								}
								/*
								// erstmal egal:
								meldung.getMeldung();
								meldung.getAusloeser();
								meldung.getWarenbestaende();
								
								// erstmal wichtig:
								meldung.getBetrieb();
								meldung.getWareneingaenge();
								meldung.getProduktionen();
								meldung.getWarenausgaenge();
								*/
							}
							catch (Exception e) {}
														
						}
					}
					System.out.println("Anzahl Stationen: " + betriebe.size());
				}
			}
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	public HashMap<String, Kontrollpunktmeldung> getKontrollpunktmeldungen() {
		return kpms;
	}
	public HashMap<String, Betrieb> getBetriebe() {
		return betriebe;
	}
}
