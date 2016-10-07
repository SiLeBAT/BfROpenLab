package de.bund.bfr.knime.openkrise.db.imports.custom.nrw.in;

import java.util.Collection;
import java.util.HashMap;

import de.nrw.verbraucherschutz.idv.daten.Betrieb;
import de.nrw.verbraucherschutz.idv.daten.Kontrollpunktmeldung;
import de.nrw.verbraucherschutz.idv.daten.Meldung;
import de.nrw.verbraucherschutz.idv.daten.Warenausgang;
import de.nrw.verbraucherschutz.idv.daten.Wareneingang;

public class Fall {
	private String fallNummer = null;
	private String fallBezeichnung = null;
	private HashMap<String, Kontrollpunktmeldung> kpmsB = null;
	private HashMap<String, Kontrollpunktmeldung> kpmsM = null;
	
	public Fall(String fallNummer, String fallBezeichnung) {
		this.fallNummer = fallNummer; 
		this.fallBezeichnung = fallBezeichnung; 
		kpmsB = new HashMap<>();
		kpmsM = new HashMap<>();
	}

	public String getFallNummer() {
		return fallNummer;
	}
	public String getFallBezeichnung() {
		return fallBezeichnung;
	}
	
	public void addKPM(Kontrollpunktmeldung kpm) {
		Meldung meldung = kpm.getMeldung();
		String bn = kpm.getBetrieb().getBetriebsnummer();
		//System.out.println(meldung.getNummer());
		if (meldung.getStatus().equals("UNGUELTIG")) {
			kpmsM.remove(meldung.getNummer());
			kpmsB.remove(bn);
		}
		else {
			kpmsM.put(meldung.getNummer(), kpm);			
		}
		if (kpmsB.containsKey(bn)) {
			kpmsM.remove(kpm.getMeldung().getNummer());
		}
		kpmsB.put(bn, kpm);	
	}
	
	
	public Collection<Kontrollpunktmeldung> getKpms() {
		return kpmsM.values();
	}

	public Collection<Betrieb> getBetriebe() {
		HashMap <String, Betrieb> betriebe = new HashMap<>();
		for (Kontrollpunktmeldung kpm : getKpms()) {
			if (!betriebe.containsKey(kpm.getBetrieb().getBetriebsnummer())) {
				betriebe.put(kpm.getBetrieb().getBetriebsnummer(), kpm.getBetrieb());
			}	

			if (kpm.getWareneingaenge() != null) {
				for (Wareneingang we : kpm.getWareneingaenge().getWareneingang()) {
					for (Betrieb b : we.getBetrieb()) {
						if (b.getTyp().equals("LIEFERANT")) {
							if (!betriebe.containsKey(b.getBetriebsnummer())) {
								betriebe.put(b.getBetriebsnummer(), b);
							}
						}
					}
				}
			}
			
			if (kpm.getWarenausgaenge() != null) {
				for (Warenausgang wa : kpm.getWarenausgaenge().getWarenausgang()) {
					for (Betrieb b : wa.getBetrieb()) {
						if (b.getTyp().equals("KUNDE")) {
							if (!betriebe.containsKey(b.getBetriebsnummer())) {
								betriebe.put(b.getBetriebsnummer(), b);
							}
						}
					}
				}
			}
		}
		
		return betriebe.values();
	}
}
