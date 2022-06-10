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


import de.bund.bfr.knime.openkrise.db.generated.public_.tables.Chargen;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record18;
import org.jooq.Row18;
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
public class ChargenRecord extends UpdatableRecordImpl<ChargenRecord> implements Record18<Integer, Integer, Integer, String, Double, String, Integer, Integer, Integer, Integer, Integer, Integer, Integer, String, String, String, String, String> {

	private static final long serialVersionUID = -101681596;

	/**
	 * Setter for <code>PUBLIC.Chargen.ID</code>.
	 */
	public void setId(Integer value) {
		setValue(0, value);
	}

	/**
	 * Getter for <code>PUBLIC.Chargen.ID</code>.
	 */
	public Integer getId() {
		return (Integer) getValue(0);
	}

	/**
	 * Setter for <code>PUBLIC.Chargen.Artikel</code>.
	 */
	public void setArtikel(Integer value) {
		setValue(1, value);
	}

	/**
	 * Getter for <code>PUBLIC.Chargen.Artikel</code>.
	 */
	public Integer getArtikel() {
		return (Integer) getValue(1);
	}

	/**
	 * Setter for <code>PUBLIC.Chargen.Zutaten</code>.
	 */
	public void setZutaten(Integer value) {
		setValue(2, value);
	}

	/**
	 * Getter for <code>PUBLIC.Chargen.Zutaten</code>.
	 */
	public Integer getZutaten() {
		return (Integer) getValue(2);
	}

	/**
	 * Setter for <code>PUBLIC.Chargen.ChargenNr</code>.
	 */
	public void setChargennr(String value) {
		setValue(3, value);
	}

	/**
	 * Getter for <code>PUBLIC.Chargen.ChargenNr</code>.
	 */
	public String getChargennr() {
		return (String) getValue(3);
	}

	/**
	 * Setter for <code>PUBLIC.Chargen.Menge</code>.
	 */
	public void setMenge(Double value) {
		setValue(4, value);
	}

	/**
	 * Getter for <code>PUBLIC.Chargen.Menge</code>.
	 */
	public Double getMenge() {
		return (Double) getValue(4);
	}

	/**
	 * Setter for <code>PUBLIC.Chargen.Einheit</code>.
	 */
	public void setEinheit(String value) {
		setValue(5, value);
	}

	/**
	 * Getter for <code>PUBLIC.Chargen.Einheit</code>.
	 */
	public String getEinheit() {
		return (String) getValue(5);
	}

	/**
	 * Setter for <code>PUBLIC.Chargen.Lieferungen</code>.
	 */
	public void setLieferungen(Integer value) {
		setValue(6, value);
	}

	/**
	 * Getter for <code>PUBLIC.Chargen.Lieferungen</code>.
	 */
	public Integer getLieferungen() {
		return (Integer) getValue(6);
	}

	/**
	 * Setter for <code>PUBLIC.Chargen.MHD_day</code>.
	 */
	public void setMhdDay(Integer value) {
		setValue(7, value);
	}

	/**
	 * Getter for <code>PUBLIC.Chargen.MHD_day</code>.
	 */
	public Integer getMhdDay() {
		return (Integer) getValue(7);
	}

	/**
	 * Setter for <code>PUBLIC.Chargen.MHD_month</code>.
	 */
	public void setMhdMonth(Integer value) {
		setValue(8, value);
	}

	/**
	 * Getter for <code>PUBLIC.Chargen.MHD_month</code>.
	 */
	public Integer getMhdMonth() {
		return (Integer) getValue(8);
	}

	/**
	 * Setter for <code>PUBLIC.Chargen.MHD_year</code>.
	 */
	public void setMhdYear(Integer value) {
		setValue(9, value);
	}

	/**
	 * Getter for <code>PUBLIC.Chargen.MHD_year</code>.
	 */
	public Integer getMhdYear() {
		return (Integer) getValue(9);
	}

	/**
	 * Setter for <code>PUBLIC.Chargen.pd_day</code>.
	 */
	public void setPdDay(Integer value) {
		setValue(10, value);
	}

	/**
	 * Getter for <code>PUBLIC.Chargen.pd_day</code>.
	 */
	public Integer getPdDay() {
		return (Integer) getValue(10);
	}

	/**
	 * Setter for <code>PUBLIC.Chargen.pd_month</code>.
	 */
	public void setPdMonth(Integer value) {
		setValue(11, value);
	}

	/**
	 * Getter for <code>PUBLIC.Chargen.pd_month</code>.
	 */
	public Integer getPdMonth() {
		return (Integer) getValue(11);
	}

	/**
	 * Setter for <code>PUBLIC.Chargen.pd_year</code>.
	 */
	public void setPdYear(Integer value) {
		setValue(12, value);
	}

	/**
	 * Getter for <code>PUBLIC.Chargen.pd_year</code>.
	 */
	public Integer getPdYear() {
		return (Integer) getValue(12);
	}

	/**
	 * Setter for <code>PUBLIC.Chargen.Serial</code>.
	 */
	public void setSerial(String value) {
		setValue(13, value);
	}

	/**
	 * Getter for <code>PUBLIC.Chargen.Serial</code>.
	 */
	public String getSerial() {
		return (String) getValue(13);
	}

	/**
	 * Setter for <code>PUBLIC.Chargen.OriginCountry</code>.
	 */
	public void setOrigincountry(String value) {
		setValue(14, value);
	}

	/**
	 * Getter for <code>PUBLIC.Chargen.OriginCountry</code>.
	 */
	public String getOrigincountry() {
		return (String) getValue(14);
	}

	/**
	 * Setter for <code>PUBLIC.Chargen.MicrobioSample</code>.
	 */
	public void setMicrobiosample(String value) {
		setValue(15, value);
	}

	/**
	 * Getter for <code>PUBLIC.Chargen.MicrobioSample</code>.
	 */
	public String getMicrobiosample() {
		return (String) getValue(15);
	}

	/**
	 * Setter for <code>PUBLIC.Chargen.ImportSources</code>.
	 */
	public void setImportsources(String value) {
		setValue(16, value);
	}

	/**
	 * Getter for <code>PUBLIC.Chargen.ImportSources</code>.
	 */
	public String getImportsources() {
		return (String) getValue(16);
	}

	/**
	 * Setter for <code>PUBLIC.Chargen.Kommentar</code>.
	 */
	public void setKommentar(String value) {
		setValue(17, value);
	}

	/**
	 * Getter for <code>PUBLIC.Chargen.Kommentar</code>.
	 */
	public String getKommentar() {
		return (String) getValue(17);
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
	// Record18 type implementation
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Row18<Integer, Integer, Integer, String, Double, String, Integer, Integer, Integer, Integer, Integer, Integer, Integer, String, String, String, String, String> fieldsRow() {
		return (Row18) super.fieldsRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Row18<Integer, Integer, Integer, String, Double, String, Integer, Integer, Integer, Integer, Integer, Integer, Integer, String, String, String, String, String> valuesRow() {
		return (Row18) super.valuesRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Integer> field1() {
		return Chargen.CHARGEN.ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Integer> field2() {
		return Chargen.CHARGEN.ARTIKEL;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Integer> field3() {
		return Chargen.CHARGEN.ZUTATEN;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<String> field4() {
		return Chargen.CHARGEN.CHARGENNR;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Double> field5() {
		return Chargen.CHARGEN.MENGE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<String> field6() {
		return Chargen.CHARGEN.EINHEIT;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Integer> field7() {
		return Chargen.CHARGEN.LIEFERUNGEN;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Integer> field8() {
		return Chargen.CHARGEN.MHD_DAY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Integer> field9() {
		return Chargen.CHARGEN.MHD_MONTH;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Integer> field10() {
		return Chargen.CHARGEN.MHD_YEAR;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Integer> field11() {
		return Chargen.CHARGEN.PD_DAY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Integer> field12() {
		return Chargen.CHARGEN.PD_MONTH;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Integer> field13() {
		return Chargen.CHARGEN.PD_YEAR;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<String> field14() {
		return Chargen.CHARGEN.SERIAL;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<String> field15() {
		return Chargen.CHARGEN.ORIGINCOUNTRY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<String> field16() {
		return Chargen.CHARGEN.MICROBIOSAMPLE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<String> field17() {
		return Chargen.CHARGEN.IMPORTSOURCES;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<String> field18() {
		return Chargen.CHARGEN.KOMMENTAR;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer value1() {
		return getId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer value2() {
		return getArtikel();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer value3() {
		return getZutaten();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String value4() {
		return getChargennr();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Double value5() {
		return getMenge();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String value6() {
		return getEinheit();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer value7() {
		return getLieferungen();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer value8() {
		return getMhdDay();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer value9() {
		return getMhdMonth();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer value10() {
		return getMhdYear();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer value11() {
		return getPdDay();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer value12() {
		return getPdMonth();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer value13() {
		return getPdYear();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String value14() {
		return getSerial();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String value15() {
		return getOrigincountry();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String value16() {
		return getMicrobiosample();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String value17() {
		return getImportsources();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String value18() {
		return getKommentar();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ChargenRecord value1(Integer value) {
		setId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ChargenRecord value2(Integer value) {
		setArtikel(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ChargenRecord value3(Integer value) {
		setZutaten(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ChargenRecord value4(String value) {
		setChargennr(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ChargenRecord value5(Double value) {
		setMenge(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ChargenRecord value6(String value) {
		setEinheit(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ChargenRecord value7(Integer value) {
		setLieferungen(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ChargenRecord value8(Integer value) {
		setMhdDay(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ChargenRecord value9(Integer value) {
		setMhdMonth(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ChargenRecord value10(Integer value) {
		setMhdYear(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ChargenRecord value11(Integer value) {
		setPdDay(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ChargenRecord value12(Integer value) {
		setPdMonth(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ChargenRecord value13(Integer value) {
		setPdYear(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ChargenRecord value14(String value) {
		setSerial(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ChargenRecord value15(String value) {
		setOrigincountry(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ChargenRecord value16(String value) {
		setMicrobiosample(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ChargenRecord value17(String value) {
		setImportsources(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ChargenRecord value18(String value) {
		setKommentar(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ChargenRecord values(Integer value1, Integer value2, Integer value3, String value4, Double value5, String value6, Integer value7, Integer value8, Integer value9, Integer value10, Integer value11, Integer value12, Integer value13, String value14, String value15, String value16, String value17, String value18) {
		value1(value1);
		value2(value2);
		value3(value3);
		value4(value4);
		value5(value5);
		value6(value6);
		value7(value7);
		value8(value8);
		value9(value9);
		value10(value10);
		value11(value11);
		value12(value12);
		value13(value13);
		value14(value14);
		value15(value15);
		value16(value16);
		value17(value17);
		value18(value18);
		return this;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * Create a detached ChargenRecord
	 */
	public ChargenRecord() {
		super(Chargen.CHARGEN);
	}

	/**
	 * Create a detached, initialised ChargenRecord
	 */
	public ChargenRecord(Integer id, Integer artikel, Integer zutaten, String chargennr, Double menge, String einheit, Integer lieferungen, Integer mhdDay, Integer mhdMonth, Integer mhdYear, Integer pdDay, Integer pdMonth, Integer pdYear, String serial, String origincountry, String microbiosample, String importsources, String kommentar) {
		super(Chargen.CHARGEN);

		setValue(0, id);
		setValue(1, artikel);
		setValue(2, zutaten);
		setValue(3, chargennr);
		setValue(4, menge);
		setValue(5, einheit);
		setValue(6, lieferungen);
		setValue(7, mhdDay);
		setValue(8, mhdMonth);
		setValue(9, mhdYear);
		setValue(10, pdDay);
		setValue(11, pdMonth);
		setValue(12, pdYear);
		setValue(13, serial);
		setValue(14, origincountry);
		setValue(15, microbiosample);
		setValue(16, importsources);
		setValue(17, kommentar);
	}
}
