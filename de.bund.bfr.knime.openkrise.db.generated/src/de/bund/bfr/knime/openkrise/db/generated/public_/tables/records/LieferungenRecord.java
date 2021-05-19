/*******************************************************************************
 * Copyright (c) 2021 German Federal Institute for Risk Assessment (BfR)
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


import de.bund.bfr.knime.openkrise.db.generated.public_.tables.Lieferungen;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record20;
import org.jooq.Row20;
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
public class LieferungenRecord extends UpdatableRecordImpl<LieferungenRecord> implements Record20<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Double, String, Double, String, Integer, String, String, String, String, String, String, String> {

	private static final long serialVersionUID = -2064900508;

	/**
	 * Setter for <code>PUBLIC.Lieferungen.ID</code>.
	 */
	public void setId(Integer value) {
		setValue(0, value);
	}

	/**
	 * Getter for <code>PUBLIC.Lieferungen.ID</code>.
	 */
	public Integer getId() {
		return (Integer) getValue(0);
	}

	/**
	 * Setter for <code>PUBLIC.Lieferungen.Charge</code>.
	 */
	public void setCharge(Integer value) {
		setValue(1, value);
	}

	/**
	 * Getter for <code>PUBLIC.Lieferungen.Charge</code>.
	 */
	public Integer getCharge() {
		return (Integer) getValue(1);
	}

	/**
	 * Setter for <code>PUBLIC.Lieferungen.dd_day</code>.
	 */
	public void setDdDay(Integer value) {
		setValue(2, value);
	}

	/**
	 * Getter for <code>PUBLIC.Lieferungen.dd_day</code>.
	 */
	public Integer getDdDay() {
		return (Integer) getValue(2);
	}

	/**
	 * Setter for <code>PUBLIC.Lieferungen.dd_month</code>.
	 */
	public void setDdMonth(Integer value) {
		setValue(3, value);
	}

	/**
	 * Getter for <code>PUBLIC.Lieferungen.dd_month</code>.
	 */
	public Integer getDdMonth() {
		return (Integer) getValue(3);
	}

	/**
	 * Setter for <code>PUBLIC.Lieferungen.dd_year</code>.
	 */
	public void setDdYear(Integer value) {
		setValue(4, value);
	}

	/**
	 * Getter for <code>PUBLIC.Lieferungen.dd_year</code>.
	 */
	public Integer getDdYear() {
		return (Integer) getValue(4);
	}

	/**
	 * Setter for <code>PUBLIC.Lieferungen.ad_day</code>.
	 */
	public void setAdDay(Integer value) {
		setValue(5, value);
	}

	/**
	 * Getter for <code>PUBLIC.Lieferungen.ad_day</code>.
	 */
	public Integer getAdDay() {
		return (Integer) getValue(5);
	}

	/**
	 * Setter for <code>PUBLIC.Lieferungen.ad_month</code>.
	 */
	public void setAdMonth(Integer value) {
		setValue(6, value);
	}

	/**
	 * Getter for <code>PUBLIC.Lieferungen.ad_month</code>.
	 */
	public Integer getAdMonth() {
		return (Integer) getValue(6);
	}

	/**
	 * Setter for <code>PUBLIC.Lieferungen.ad_year</code>.
	 */
	public void setAdYear(Integer value) {
		setValue(7, value);
	}

	/**
	 * Getter for <code>PUBLIC.Lieferungen.ad_year</code>.
	 */
	public Integer getAdYear() {
		return (Integer) getValue(7);
	}

	/**
	 * Setter for <code>PUBLIC.Lieferungen.numPU</code>.
	 */
	public void setNumpu(Double value) {
		setValue(8, value);
	}

	/**
	 * Getter for <code>PUBLIC.Lieferungen.numPU</code>.
	 */
	public Double getNumpu() {
		return (Double) getValue(8);
	}

	/**
	 * Setter for <code>PUBLIC.Lieferungen.typePU</code>.
	 */
	public void setTypepu(String value) {
		setValue(9, value);
	}

	/**
	 * Getter for <code>PUBLIC.Lieferungen.typePU</code>.
	 */
	public String getTypepu() {
		return (String) getValue(9);
	}

	/**
	 * Setter for <code>PUBLIC.Lieferungen.Unitmenge</code>.
	 */
	public void setUnitmenge(Double value) {
		setValue(10, value);
	}

	/**
	 * Getter for <code>PUBLIC.Lieferungen.Unitmenge</code>.
	 */
	public Double getUnitmenge() {
		return (Double) getValue(10);
	}

	/**
	 * Setter for <code>PUBLIC.Lieferungen.UnitEinheit</code>.
	 */
	public void setUniteinheit(String value) {
		setValue(11, value);
	}

	/**
	 * Getter for <code>PUBLIC.Lieferungen.UnitEinheit</code>.
	 */
	public String getUniteinheit() {
		return (String) getValue(11);
	}

	/**
	 * Setter for <code>PUBLIC.Lieferungen.Empfänger</code>.
	 */
	public void setEmpfänger(Integer value) {
		setValue(12, value);
	}

	/**
	 * Getter for <code>PUBLIC.Lieferungen.Empfänger</code>.
	 */
	public Integer getEmpfänger() {
		return (Integer) getValue(12);
	}

	/**
	 * Setter for <code>PUBLIC.Lieferungen.Serial</code>.
	 */
	public void setSerial(String value) {
		setValue(13, value);
	}

	/**
	 * Getter for <code>PUBLIC.Lieferungen.Serial</code>.
	 */
	public String getSerial() {
		return (String) getValue(13);
	}

	/**
	 * Setter for <code>PUBLIC.Lieferungen.EndChain</code>.
	 */
	public void setEndchain(String value) {
		setValue(14, value);
	}

	/**
	 * Getter for <code>PUBLIC.Lieferungen.EndChain</code>.
	 */
	public String getEndchain() {
		return (String) getValue(14);
	}

	/**
	 * Setter for <code>PUBLIC.Lieferungen.Explanation_EndChain</code>.
	 */
	public void setExplanationEndchain(String value) {
		setValue(15, value);
	}

	/**
	 * Getter for <code>PUBLIC.Lieferungen.Explanation_EndChain</code>.
	 */
	public String getExplanationEndchain() {
		return (String) getValue(15);
	}

	/**
	 * Setter for <code>PUBLIC.Lieferungen.Contact_Questions_Remarks</code>.
	 */
	public void setContactQuestionsRemarks(String value) {
		setValue(16, value);
	}

	/**
	 * Getter for <code>PUBLIC.Lieferungen.Contact_Questions_Remarks</code>.
	 */
	public String getContactQuestionsRemarks() {
		return (String) getValue(16);
	}

	/**
	 * Setter for <code>PUBLIC.Lieferungen.Further_Traceback</code>.
	 */
	public void setFurtherTraceback(String value) {
		setValue(17, value);
	}

	/**
	 * Getter for <code>PUBLIC.Lieferungen.Further_Traceback</code>.
	 */
	public String getFurtherTraceback() {
		return (String) getValue(17);
	}

	/**
	 * Setter for <code>PUBLIC.Lieferungen.ImportSources</code>.
	 */
	public void setImportsources(String value) {
		setValue(18, value);
	}

	/**
	 * Getter for <code>PUBLIC.Lieferungen.ImportSources</code>.
	 */
	public String getImportsources() {
		return (String) getValue(18);
	}

	/**
	 * Setter for <code>PUBLIC.Lieferungen.Kommentar</code>.
	 */
	public void setKommentar(String value) {
		setValue(19, value);
	}

	/**
	 * Getter for <code>PUBLIC.Lieferungen.Kommentar</code>.
	 */
	public String getKommentar() {
		return (String) getValue(19);
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
	// Record20 type implementation
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Row20<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Double, String, Double, String, Integer, String, String, String, String, String, String, String> fieldsRow() {
		return (Row20) super.fieldsRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Row20<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Double, String, Double, String, Integer, String, String, String, String, String, String, String> valuesRow() {
		return (Row20) super.valuesRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Integer> field1() {
		return Lieferungen.LIEFERUNGEN.ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Integer> field2() {
		return Lieferungen.LIEFERUNGEN.CHARGE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Integer> field3() {
		return Lieferungen.LIEFERUNGEN.DD_DAY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Integer> field4() {
		return Lieferungen.LIEFERUNGEN.DD_MONTH;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Integer> field5() {
		return Lieferungen.LIEFERUNGEN.DD_YEAR;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Integer> field6() {
		return Lieferungen.LIEFERUNGEN.AD_DAY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Integer> field7() {
		return Lieferungen.LIEFERUNGEN.AD_MONTH;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Integer> field8() {
		return Lieferungen.LIEFERUNGEN.AD_YEAR;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Double> field9() {
		return Lieferungen.LIEFERUNGEN.NUMPU;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<String> field10() {
		return Lieferungen.LIEFERUNGEN.TYPEPU;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Double> field11() {
		return Lieferungen.LIEFERUNGEN.UNITMENGE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<String> field12() {
		return Lieferungen.LIEFERUNGEN.UNITEINHEIT;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Integer> field13() {
		return Lieferungen.LIEFERUNGEN.EMPFÄNGER;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<String> field14() {
		return Lieferungen.LIEFERUNGEN.SERIAL;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<String> field15() {
		return Lieferungen.LIEFERUNGEN.ENDCHAIN;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<String> field16() {
		return Lieferungen.LIEFERUNGEN.EXPLANATION_ENDCHAIN;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<String> field17() {
		return Lieferungen.LIEFERUNGEN.CONTACT_QUESTIONS_REMARKS;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<String> field18() {
		return Lieferungen.LIEFERUNGEN.FURTHER_TRACEBACK;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<String> field19() {
		return Lieferungen.LIEFERUNGEN.IMPORTSOURCES;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<String> field20() {
		return Lieferungen.LIEFERUNGEN.KOMMENTAR;
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
		return getCharge();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer value3() {
		return getDdDay();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer value4() {
		return getDdMonth();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer value5() {
		return getDdYear();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer value6() {
		return getAdDay();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer value7() {
		return getAdMonth();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer value8() {
		return getAdYear();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Double value9() {
		return getNumpu();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String value10() {
		return getTypepu();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Double value11() {
		return getUnitmenge();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String value12() {
		return getUniteinheit();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer value13() {
		return getEmpfänger();
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
		return getEndchain();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String value16() {
		return getExplanationEndchain();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String value17() {
		return getContactQuestionsRemarks();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String value18() {
		return getFurtherTraceback();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String value19() {
		return getImportsources();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String value20() {
		return getKommentar();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LieferungenRecord value1(Integer value) {
		setId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LieferungenRecord value2(Integer value) {
		setCharge(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LieferungenRecord value3(Integer value) {
		setDdDay(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LieferungenRecord value4(Integer value) {
		setDdMonth(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LieferungenRecord value5(Integer value) {
		setDdYear(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LieferungenRecord value6(Integer value) {
		setAdDay(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LieferungenRecord value7(Integer value) {
		setAdMonth(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LieferungenRecord value8(Integer value) {
		setAdYear(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LieferungenRecord value9(Double value) {
		setNumpu(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LieferungenRecord value10(String value) {
		setTypepu(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LieferungenRecord value11(Double value) {
		setUnitmenge(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LieferungenRecord value12(String value) {
		setUniteinheit(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LieferungenRecord value13(Integer value) {
		setEmpfänger(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LieferungenRecord value14(String value) {
		setSerial(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LieferungenRecord value15(String value) {
		setEndchain(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LieferungenRecord value16(String value) {
		setExplanationEndchain(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LieferungenRecord value17(String value) {
		setContactQuestionsRemarks(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LieferungenRecord value18(String value) {
		setFurtherTraceback(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LieferungenRecord value19(String value) {
		setImportsources(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LieferungenRecord value20(String value) {
		setKommentar(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LieferungenRecord values(Integer value1, Integer value2, Integer value3, Integer value4, Integer value5, Integer value6, Integer value7, Integer value8, Double value9, String value10, Double value11, String value12, Integer value13, String value14, String value15, String value16, String value17, String value18, String value19, String value20) {
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
		value19(value19);
		value20(value20);
		return this;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * Create a detached LieferungenRecord
	 */
	public LieferungenRecord() {
		super(Lieferungen.LIEFERUNGEN);
	}

	/**
	 * Create a detached, initialised LieferungenRecord
	 */
	public LieferungenRecord(Integer id, Integer charge, Integer ddDay, Integer ddMonth, Integer ddYear, Integer adDay, Integer adMonth, Integer adYear, Double numpu, String typepu, Double unitmenge, String uniteinheit, Integer empfänger, String serial, String endchain, String explanationEndchain, String contactQuestionsRemarks, String furtherTraceback, String importsources, String kommentar) {
		super(Lieferungen.LIEFERUNGEN);

		setValue(0, id);
		setValue(1, charge);
		setValue(2, ddDay);
		setValue(3, ddMonth);
		setValue(4, ddYear);
		setValue(5, adDay);
		setValue(6, adMonth);
		setValue(7, adYear);
		setValue(8, numpu);
		setValue(9, typepu);
		setValue(10, unitmenge);
		setValue(11, uniteinheit);
		setValue(12, empfänger);
		setValue(13, serial);
		setValue(14, endchain);
		setValue(15, explanationEndchain);
		setValue(16, contactQuestionsRemarks);
		setValue(17, furtherTraceback);
		setValue(18, importsources);
		setValue(19, kommentar);
	}
}
