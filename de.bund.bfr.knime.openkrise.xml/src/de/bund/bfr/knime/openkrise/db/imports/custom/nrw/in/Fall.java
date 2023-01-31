/*******************************************************************************
 * Copyright (c) 2014-2023 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.openkrise.db.imports.custom.nrw.in;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

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
			if (kpmsB.containsKey(bn)) {
				kpmsM.remove(kpmsB.get(bn).getMeldung().getNummer());
			}
			kpmsM.put(meldung.getNummer(), kpm);			
			kpmsB.put(bn, kpm);	
		}
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
						if (b.getTyp().equals("LIEFERANT") || b.getTyp().equals("ORT_ABHOLUNG")) {
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
						if (b.getTyp().equals("KUNDE") || b.getTyp().equals("ORT_ANLIEFERUNG")) {
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
	public Set<String> getAuftragsnummern() {
		Set<String> nummern = new HashSet<>();
		for (Kontrollpunktmeldung kpm : getKpms()) {
			nummern.add(kpm.getMeldung().getNummer());
		}		
		return nummern;
	}
}
