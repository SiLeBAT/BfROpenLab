package de.bund.bfr.knime.openkrise.db.imports.custom.nrw.in;

/*
 Fragen:
 - sind wareneingangs ids identisch mit warenausgangs ids aus der anderen sicht, d.h. sind diese IDs global gültig oder gelten sie nur innerhalb des betrachteten Kontrollpunktes?
 - was bedeutet GÜLTIG / UNGÜLTIG in einer Meldung?
 - wann wird eine xml ungültig? Welche Regeln gibt es da? Z.B. xml wird aus Bushaltestelle gelöscht oder eine neue Meldung desselben Kontrollpunktes wird in die Bushaltestelle gelegt oder der Status der Meldung wird auf UNGÜLTIG gesetzt oder...?
 - gibt es nur eine meldung pro Betrieb/Kontrollpunkt?
 */
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
	
	public static void main(String[] args) throws JAXBException {
		new NRW_Importer().doImport("/Users/arminweiser/Desktop/xml_test/bbk/", null, true); // /de/nrw/verbraucherschutz/idv/daten/test/kpm_xmls/l2b_kontrollpunktmeldung-v0.2.xml
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
							"/de/nrw/verbraucherschutz/idv/daten/main/de.nrw.verbraucherschutz.idv.daten.2016.2.20160818.warenrueckverfolgung.transport.schema.xsd")));
	
			File folder = new File(foldername);
			if (folder.exists() && folder.isDirectory()) {
				File[] fs = folder.listFiles();
				if (fs != null && fs.length > 0) {
					HashMap<String, Kontrollpunktmeldung> kpms = new HashMap<>();
					for (File f : fs) {
						if (f.getName().endsWith(".xml")) {
							System.out.println("----- " + f.getName() + " -----");
							/*
							Kontrollpunktmeldung meldung = ((JAXBElement<Kontrollpunktmeldung>) reader.unmarshal(
									getClass().getResource(f.getAbsolutePath())
											.openStream())).getValue();
					*/
							Kontrollpunktmeldung meldung = ((JAXBElement<Kontrollpunktmeldung>) reader.unmarshal(f)).getValue();
							System.out.println(meldung.getBetrieb().getBetriebsname());
							kpms.put(meldung.getBetrieb().getBetriebsnummer(), meldung);
							if (meldung.getWareneingaenge() != null) {
								for (Wareneingang we : meldung.getWareneingaenge().getWareneingang()) {
									for (Betrieb b : we.getBetrieb()) {
										if (b.getTyp().equals("LIEFERANT")) {
											if (!kpms.containsKey(b.getBetriebsnummer())) {
												kpms.put(b.getBetriebsnummer(), null);
											}
										}
									}
								}									
							}
							if (meldung.getWarenausgaenge() != null) {
								for (Warenausgang wa : meldung.getWarenausgaenge().getWarenausgang()) {
									for (Betrieb b : wa.getBetrieb()) {
										if (b.getTyp().equals("KUNDE")) {
											if (!kpms.containsKey(b.getBetriebsnummer())) {
												kpms.put(b.getBetriebsnummer(), null);
											}
										}
									}
								}									
							}
							
							// erstmal egal:
							meldung.getMeldung();
							meldung.getAusloeser();
							meldung.getWarenbestaende();
							
							// erstmal wichtig:
							meldung.getBetrieb();
							meldung.getWareneingaenge();
							meldung.getProduktionen();
							meldung.getWarenausgaenge();
							
						}
					}
					System.out.println("Anzahl Stationen: " + kpms.size());
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
}
