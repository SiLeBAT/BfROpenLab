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
/**
 * This class is generated by jOOQ
 */
package de.bund.bfr.knime.openkrise.db.generated.public_.tables.records;


import de.bund.bfr.knime.openkrise.db.generated.public_.tables.Importmetadata;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record5;
import org.jooq.Row5;
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
public class ImportmetadataRecord extends UpdatableRecordImpl<ImportmetadataRecord> implements Record5<Integer, String, String, String, String> {

	private static final long serialVersionUID = -1256470200;

	/**
	 * Setter for <code>PUBLIC.ImportMetadata.ID</code>.
	 */
	public void setId(Integer value) {
		setValue(0, value);
	}

	/**
	 * Getter for <code>PUBLIC.ImportMetadata.ID</code>.
	 */
	public Integer getId() {
		return (Integer) getValue(0);
	}

	/**
	 * Setter for <code>PUBLIC.ImportMetadata.filename</code>.
	 */
	public void setFilename(String value) {
		setValue(1, value);
	}

	/**
	 * Getter for <code>PUBLIC.ImportMetadata.filename</code>.
	 */
	public String getFilename() {
		return (String) getValue(1);
	}

	/**
	 * Setter for <code>PUBLIC.ImportMetadata.reporter</code>.
	 */
	public void setReporter(String value) {
		setValue(2, value);
	}

	/**
	 * Getter for <code>PUBLIC.ImportMetadata.reporter</code>.
	 */
	public String getReporter() {
		return (String) getValue(2);
	}

	/**
	 * Setter for <code>PUBLIC.ImportMetadata.date</code>.
	 */
	public void setDate(String value) {
		setValue(3, value);
	}

	/**
	 * Getter for <code>PUBLIC.ImportMetadata.date</code>.
	 */
	public String getDate() {
		return (String) getValue(3);
	}

	/**
	 * Setter for <code>PUBLIC.ImportMetadata.remarks</code>.
	 */
	public void setRemarks(String value) {
		setValue(4, value);
	}

	/**
	 * Getter for <code>PUBLIC.ImportMetadata.remarks</code>.
	 */
	public String getRemarks() {
		return (String) getValue(4);
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
	// Record5 type implementation
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Row5<Integer, String, String, String, String> fieldsRow() {
		return (Row5) super.fieldsRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Row5<Integer, String, String, String, String> valuesRow() {
		return (Row5) super.valuesRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Integer> field1() {
		return Importmetadata.IMPORTMETADATA.ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<String> field2() {
		return Importmetadata.IMPORTMETADATA.FILENAME;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<String> field3() {
		return Importmetadata.IMPORTMETADATA.REPORTER;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<String> field4() {
		return Importmetadata.IMPORTMETADATA.DATE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<String> field5() {
		return Importmetadata.IMPORTMETADATA.REMARKS;
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
	public String value2() {
		return getFilename();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String value3() {
		return getReporter();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String value4() {
		return getDate();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String value5() {
		return getRemarks();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ImportmetadataRecord value1(Integer value) {
		setId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ImportmetadataRecord value2(String value) {
		setFilename(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ImportmetadataRecord value3(String value) {
		setReporter(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ImportmetadataRecord value4(String value) {
		setDate(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ImportmetadataRecord value5(String value) {
		setRemarks(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ImportmetadataRecord values(Integer value1, String value2, String value3, String value4, String value5) {
		value1(value1);
		value2(value2);
		value3(value3);
		value4(value4);
		value5(value5);
		return this;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * Create a detached ImportmetadataRecord
	 */
	public ImportmetadataRecord() {
		super(Importmetadata.IMPORTMETADATA);
	}

	/**
	 * Create a detached, initialised ImportmetadataRecord
	 */
	public ImportmetadataRecord(Integer id, String filename, String reporter, String date, String remarks) {
		super(Importmetadata.IMPORTMETADATA);

		setValue(0, id);
		setValue(1, filename);
		setValue(2, reporter);
		setValue(3, date);
		setValue(4, remarks);
	}
}
