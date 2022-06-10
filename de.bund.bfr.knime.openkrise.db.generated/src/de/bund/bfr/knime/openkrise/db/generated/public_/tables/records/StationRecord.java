/*******************************************************************************
 * Copyright (c) 2014-2022 German Federal Institute for Risk Assessment (BfR)
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
/**
 * This class is generated by jOOQ
 */
package de.bund.bfr.knime.openkrise.db.generated.public_.tables.records;


import de.bund.bfr.knime.openkrise.db.generated.public_.tables.Station;

import java.sql.Date;

import javax.annotation.Generated;

import org.jooq.Record1;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@Generated(
	value = {
		"http://www.jooq.org",
		"jOOQ version:3.6.2"
	},
	comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class StationRecord extends UpdatableRecordImpl<StationRecord> {

	private static final long serialVersionUID = -2089588068;

	/**
	 * Setter for <code>PUBLIC.Station.ID</code>.
	 */
	public void setId(Integer value) {
		setValue(0, value);
	}

	/**
	 * Getter for <code>PUBLIC.Station.ID</code>.
	 */
	public Integer getId() {
		return (Integer) getValue(0);
	}

	/**
	 * Setter for <code>PUBLIC.Station.Produktkatalog</code>.
	 */
	public void setProduktkatalog(Integer value) {
		setValue(1, value);
	}

	/**
	 * Getter for <code>PUBLIC.Station.Produktkatalog</code>.
	 */
	public Integer getProduktkatalog() {
		return (Integer) getValue(1);
	}

	/**
	 * Setter for <code>PUBLIC.Station.Name</code>.
	 */
	public void setName(String value) {
		setValue(2, value);
	}

	/**
	 * Getter for <code>PUBLIC.Station.Name</code>.
	 */
	public String getName() {
		return (String) getValue(2);
	}

	/**
	 * Setter for <code>PUBLIC.Station.Strasse</code>.
	 */
	public void setStrasse(String value) {
		setValue(3, value);
	}

	/**
	 * Getter for <code>PUBLIC.Station.Strasse</code>.
	 */
	public String getStrasse() {
		return (String) getValue(3);
	}

	/**
	 * Setter for <code>PUBLIC.Station.Hausnummer</code>.
	 */
	public void setHausnummer(String value) {
		setValue(4, value);
	}

	/**
	 * Getter for <code>PUBLIC.Station.Hausnummer</code>.
	 */
	public String getHausnummer() {
		return (String) getValue(4);
	}

	/**
	 * Setter for <code>PUBLIC.Station.Postfach</code>.
	 */
	public void setPostfach(String value) {
		setValue(5, value);
	}

	/**
	 * Getter for <code>PUBLIC.Station.Postfach</code>.
	 */
	public String getPostfach() {
		return (String) getValue(5);
	}

	/**
	 * Setter for <code>PUBLIC.Station.PLZ</code>.
	 */
	public void setPlz(String value) {
		setValue(6, value);
	}

	/**
	 * Getter for <code>PUBLIC.Station.PLZ</code>.
	 */
	public String getPlz() {
		return (String) getValue(6);
	}

	/**
	 * Setter for <code>PUBLIC.Station.Ort</code>.
	 */
	public void setOrt(String value) {
		setValue(7, value);
	}

	/**
	 * Getter for <code>PUBLIC.Station.Ort</code>.
	 */
	public String getOrt() {
		return (String) getValue(7);
	}

	/**
	 * Setter for <code>PUBLIC.Station.District</code>.
	 */
	public void setDistrict(String value) {
		setValue(8, value);
	}

	/**
	 * Getter for <code>PUBLIC.Station.District</code>.
	 */
	public String getDistrict() {
		return (String) getValue(8);
	}

	/**
	 * Setter for <code>PUBLIC.Station.Bundesland</code>.
	 */
	public void setBundesland(String value) {
		setValue(9, value);
	}

	/**
	 * Getter for <code>PUBLIC.Station.Bundesland</code>.
	 */
	public String getBundesland() {
		return (String) getValue(9);
	}

	/**
	 * Setter for <code>PUBLIC.Station.Land</code>.
	 */
	public void setLand(String value) {
		setValue(10, value);
	}

	/**
	 * Getter for <code>PUBLIC.Station.Land</code>.
	 */
	public String getLand() {
		return (String) getValue(10);
	}

	/**
	 * Setter for <code>PUBLIC.Station.Longitude</code>.
	 */
	public void setLongitude(Double value) {
		setValue(11, value);
	}

	/**
	 * Getter for <code>PUBLIC.Station.Longitude</code>.
	 */
	public Double getLongitude() {
		return (Double) getValue(11);
	}

	/**
	 * Setter for <code>PUBLIC.Station.Latitude</code>.
	 */
	public void setLatitude(Double value) {
		setValue(12, value);
	}

	/**
	 * Getter for <code>PUBLIC.Station.Latitude</code>.
	 */
	public Double getLatitude() {
		return (Double) getValue(12);
	}

	/**
	 * Setter for <code>PUBLIC.Station.Ansprechpartner</code>.
	 */
	public void setAnsprechpartner(String value) {
		setValue(13, value);
	}

	/**
	 * Getter for <code>PUBLIC.Station.Ansprechpartner</code>.
	 */
	public String getAnsprechpartner() {
		return (String) getValue(13);
	}

	/**
	 * Setter for <code>PUBLIC.Station.Telefon</code>.
	 */
	public void setTelefon(String value) {
		setValue(14, value);
	}

	/**
	 * Getter for <code>PUBLIC.Station.Telefon</code>.
	 */
	public String getTelefon() {
		return (String) getValue(14);
	}

	/**
	 * Setter for <code>PUBLIC.Station.Fax</code>.
	 */
	public void setFax(String value) {
		setValue(15, value);
	}

	/**
	 * Getter for <code>PUBLIC.Station.Fax</code>.
	 */
	public String getFax() {
		return (String) getValue(15);
	}

	/**
	 * Setter for <code>PUBLIC.Station.EMail</code>.
	 */
	public void setEmail(String value) {
		setValue(16, value);
	}

	/**
	 * Getter for <code>PUBLIC.Station.EMail</code>.
	 */
	public String getEmail() {
		return (String) getValue(16);
	}

	/**
	 * Setter for <code>PUBLIC.Station.Webseite</code>.
	 */
	public void setWebseite(String value) {
		setValue(17, value);
	}

	/**
	 * Getter for <code>PUBLIC.Station.Webseite</code>.
	 */
	public String getWebseite() {
		return (String) getValue(17);
	}

	/**
	 * Setter for <code>PUBLIC.Station.Betriebsnummer</code>.
	 */
	public void setBetriebsnummer(String value) {
		setValue(18, value);
	}

	/**
	 * Getter for <code>PUBLIC.Station.Betriebsnummer</code>.
	 */
	public String getBetriebsnummer() {
		return (String) getValue(18);
	}

	/**
	 * Setter for <code>PUBLIC.Station.Betriebsart</code>.
	 */
	public void setBetriebsart(String value) {
		setValue(19, value);
	}

	/**
	 * Getter for <code>PUBLIC.Station.Betriebsart</code>.
	 */
	public String getBetriebsart() {
		return (String) getValue(19);
	}

	/**
	 * Setter for <code>PUBLIC.Station.VATnumber</code>.
	 */
	public void setVatnumber(String value) {
		setValue(20, value);
	}

	/**
	 * Getter for <code>PUBLIC.Station.VATnumber</code>.
	 */
	public String getVatnumber() {
		return (String) getValue(20);
	}

	/**
	 * Setter for <code>PUBLIC.Station.Code</code>.
	 */
	public void setCode(String value) {
		setValue(21, value);
	}

	/**
	 * Getter for <code>PUBLIC.Station.Code</code>.
	 */
	public String getCode() {
		return (String) getValue(21);
	}

	/**
	 * Setter for <code>PUBLIC.Station.CasePriority</code>.
	 */
	public void setCasepriority(Double value) {
		setValue(22, value);
	}

	/**
	 * Getter for <code>PUBLIC.Station.CasePriority</code>.
	 */
	public Double getCasepriority() {
		return (Double) getValue(22);
	}

	/**
	 * Setter for <code>PUBLIC.Station.AnzahlFaelle</code>.
	 */
	public void setAnzahlfaelle(Integer value) {
		setValue(23, value);
	}

	/**
	 * Getter for <code>PUBLIC.Station.AnzahlFaelle</code>.
	 */
	public Integer getAnzahlfaelle() {
		return (Integer) getValue(23);
	}

	/**
	 * Setter for <code>PUBLIC.Station.AlterMin</code>.
	 */
	public void setAltermin(Integer value) {
		setValue(24, value);
	}

	/**
	 * Getter for <code>PUBLIC.Station.AlterMin</code>.
	 */
	public Integer getAltermin() {
		return (Integer) getValue(24);
	}

	/**
	 * Setter for <code>PUBLIC.Station.AlterMax</code>.
	 */
	public void setAltermax(Integer value) {
		setValue(25, value);
	}

	/**
	 * Getter for <code>PUBLIC.Station.AlterMax</code>.
	 */
	public Integer getAltermax() {
		return (Integer) getValue(25);
	}

	/**
	 * Setter for <code>PUBLIC.Station.DatumBeginn</code>.
	 */
	public void setDatumbeginn(Date value) {
		setValue(26, value);
	}

	/**
	 * Getter for <code>PUBLIC.Station.DatumBeginn</code>.
	 */
	public Date getDatumbeginn() {
		return (Date) getValue(26);
	}

	/**
	 * Setter for <code>PUBLIC.Station.DatumHoehepunkt</code>.
	 */
	public void setDatumhoehepunkt(Date value) {
		setValue(27, value);
	}

	/**
	 * Getter for <code>PUBLIC.Station.DatumHoehepunkt</code>.
	 */
	public Date getDatumhoehepunkt() {
		return (Date) getValue(27);
	}

	/**
	 * Setter for <code>PUBLIC.Station.DatumEnde</code>.
	 */
	public void setDatumende(Date value) {
		setValue(28, value);
	}

	/**
	 * Getter for <code>PUBLIC.Station.DatumEnde</code>.
	 */
	public Date getDatumende() {
		return (Date) getValue(28);
	}

	/**
	 * Setter for <code>PUBLIC.Station.Erregernachweis</code>.
	 */
	public void setErregernachweis(Integer value) {
		setValue(29, value);
	}

	/**
	 * Getter for <code>PUBLIC.Station.Erregernachweis</code>.
	 */
	public Integer getErregernachweis() {
		return (Integer) getValue(29);
	}

	/**
	 * Setter for <code>PUBLIC.Station.Serial</code>.
	 */
	public void setSerial(String value) {
		setValue(30, value);
	}

	/**
	 * Getter for <code>PUBLIC.Station.Serial</code>.
	 */
	public String getSerial() {
		return (String) getValue(30);
	}

	/**
	 * Setter for <code>PUBLIC.Station.ImportSources</code>.
	 */
	public void setImportsources(String value) {
		setValue(31, value);
	}

	/**
	 * Getter for <code>PUBLIC.Station.ImportSources</code>.
	 */
	public String getImportsources() {
		return (String) getValue(31);
	}

	/**
	 * Setter for <code>PUBLIC.Station.Kommentar</code>.
	 */
	public void setKommentar(String value) {
		setValue(32, value);
	}

	/**
	 * Getter for <code>PUBLIC.Station.Kommentar</code>.
	 */
	public String getKommentar() {
		return (String) getValue(32);
	}

	/**
	 * Setter for <code>PUBLIC.Station.Adresse</code>.
	 */
	public void setAdresse(String value) {
		setValue(33, value);
	}

	/**
	 * Getter for <code>PUBLIC.Station.Adresse</code>.
	 */
	public String getAdresse() {
		return (String) getValue(33);
	}

	// -------------------------------------------------------------------------
	// Primary key information
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Record1<Integer> key() {
		return (Record1) super.key();
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * Create a detached StationRecord
	 */
	public StationRecord() {
		super(Station.STATION);
	}

	/**
	 * Create a detached, initialised StationRecord
	 */
	public StationRecord(Integer id, Integer produktkatalog, String name, String strasse, String hausnummer, String postfach, String plz, String ort, String district, String bundesland, String land, Double longitude, Double latitude, String ansprechpartner, String telefon, String fax, String email, String webseite, String betriebsnummer, String betriebsart, String vatnumber, String code, Double casepriority, Integer anzahlfaelle, Integer altermin, Integer altermax, Date datumbeginn, Date datumhoehepunkt, Date datumende, Integer erregernachweis, String serial, String importsources, String kommentar, String adresse) {
		super(Station.STATION);

		setValue(0, id);
		setValue(1, produktkatalog);
		setValue(2, name);
		setValue(3, strasse);
		setValue(4, hausnummer);
		setValue(5, postfach);
		setValue(6, plz);
		setValue(7, ort);
		setValue(8, district);
		setValue(9, bundesland);
		setValue(10, land);
		setValue(11, longitude);
		setValue(12, latitude);
		setValue(13, ansprechpartner);
		setValue(14, telefon);
		setValue(15, fax);
		setValue(16, email);
		setValue(17, webseite);
		setValue(18, betriebsnummer);
		setValue(19, betriebsart);
		setValue(20, vatnumber);
		setValue(21, code);
		setValue(22, casepriority);
		setValue(23, anzahlfaelle);
		setValue(24, altermin);
		setValue(25, altermax);
		setValue(26, datumbeginn);
		setValue(27, datumhoehepunkt);
		setValue(28, datumende);
		setValue(29, erregernachweis);
		setValue(30, serial);
		setValue(31, importsources);
		setValue(32, kommentar);
		setValue(33, adresse);
	}
}
