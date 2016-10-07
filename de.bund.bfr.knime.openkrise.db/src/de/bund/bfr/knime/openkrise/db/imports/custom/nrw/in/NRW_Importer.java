package de.bund.bfr.knime.openkrise.db.imports.custom.nrw.in;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.SchemaFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.nrw.verbraucherschutz.idv.daten.*;

public class NRW_Importer {

	private HashMap<String, Fall> faelle = null;

	public static void main(String[] args) throws JAXBException, SOAPException, IOException {
		// new
		// NRW_Importer().doImport("/Users/arminweiser/Desktop/xml_test/bbk/",
		// null, true);
		new NRW_Importer().doImport("/Users/arminweiser/Desktop/xml_test/tst/", null);
	}

	@SuppressWarnings("unchecked")
	public String doImport(String foldername, String fallnummer) throws SOAPException, IOException {
		String lastFallNummer = null;
		Unmarshaller reader;
		try {
			reader = JAXBContext.newInstance(Kontrollpunktmeldung.class.getPackage().getName()).createUnmarshaller();

			reader.setSchema(SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
					.newSchema(Kontrollpunktmeldung.class.getResource(
							"/de/nrw/verbraucherschutz/idv/dienste/de.nrw.verbraucherschutz.idv.dienste.2016.2.warenrueckverfolgung.transport.schema.xsd")));

			File folder = new File(foldername);
			if (folder.exists() && folder.isDirectory()) {
				File[] fs = folder.listFiles();
				if (fs != null && fs.length > 0) {
					faelle = new HashMap<>();
					Arrays.sort(fs);
					for (File f : fs) {
						//System.out.println("----- " + f.getName() + " -----");
						Fall fall = null;
						InputStream template = new FileInputStream(f);
						MessageFactory mf = MessageFactory.newInstance(); // SOAPConstants.SOAP_1_1_PROTOCOL
						SOAPMessage message = mf.createMessage(new MimeHeaders(), template);
						SOAPPart sp = message.getSOAPPart();
						SOAPEnvelope se = sp.getEnvelope();
						SOAPBody body = se.getBody();
						NodeList nl = body.getChildNodes();
						for (int i = 0; i < nl.getLength(); i++) {
							Node nln = nl.item(i);
							String nn = nln.getNodeName();
							if (nn.endsWith(":kontrollpunktmeldung")) {
								DOMSource ds = new DOMSource(nln);
								Kontrollpunktmeldung kpm = ((JAXBElement<Kontrollpunktmeldung>) reader
										.unmarshal(ds)).getValue();

								Meldung meldung = kpm.getMeldung();
								String fn = meldung.getFallNummer();
								lastFallNummer = fn;
								if (fallnummer != null && !fallnummer.equals(fn)) break;
								if (!faelle.containsKey(fn)) faelle.put(fn, new Fall(fn, meldung.getFallBezeichnung()));
								fall = faelle.get(fn);

								//System.out.println(kpm.getBetrieb().getBetriebsname());
								fall.addKPM(kpm);
							}
						}
					}
				}
			}
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return lastFallNummer;
	}

	public HashMap<String, Fall> getFaelle() {
		return faelle;
	}
}
