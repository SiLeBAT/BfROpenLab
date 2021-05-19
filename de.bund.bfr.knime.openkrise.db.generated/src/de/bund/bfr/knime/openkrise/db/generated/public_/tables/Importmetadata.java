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
package de.bund.bfr.knime.openkrise.db.generated.public_.tables;


import de.bund.bfr.knime.openkrise.db.generated.public_.Keys;
import de.bund.bfr.knime.openkrise.db.generated.public_.Public;
import de.bund.bfr.knime.openkrise.db.generated.public_.tables.records.ImportmetadataRecord;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Identity;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.TableImpl;


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
public class Importmetadata extends TableImpl<ImportmetadataRecord> {

	private static final long serialVersionUID = 376981295;

	/**
	 * The reference instance of <code>PUBLIC.ImportMetadata</code>
	 */
	public static final Importmetadata IMPORTMETADATA = new Importmetadata();

	/**
	 * The class holding records for this type
	 */
	@Override
	public Class<ImportmetadataRecord> getRecordType() {
		return ImportmetadataRecord.class;
	}

	/**
	 * The column <code>PUBLIC.ImportMetadata.ID</code>.
	 */
	public final TableField<ImportmetadataRecord, Integer> ID = createField("ID", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>PUBLIC.ImportMetadata.filename</code>.
	 */
	public final TableField<ImportmetadataRecord, String> FILENAME = createField("filename", org.jooq.impl.SQLDataType.VARCHAR.length(2048), this, "");

	/**
	 * The column <code>PUBLIC.ImportMetadata.reporter</code>.
	 */
	public final TableField<ImportmetadataRecord, String> REPORTER = createField("reporter", org.jooq.impl.SQLDataType.VARCHAR.length(255), this, "");

	/**
	 * The column <code>PUBLIC.ImportMetadata.date</code>.
	 */
	public final TableField<ImportmetadataRecord, String> DATE = createField("date", org.jooq.impl.SQLDataType.VARCHAR.length(255), this, "");

	/**
	 * The column <code>PUBLIC.ImportMetadata.remarks</code>.
	 */
	public final TableField<ImportmetadataRecord, String> REMARKS = createField("remarks", org.jooq.impl.SQLDataType.VARCHAR.length(2048), this, "");

	/**
	 * Create a <code>PUBLIC.ImportMetadata</code> table reference
	 */
	public Importmetadata() {
		this("ImportMetadata", null);
	}

	/**
	 * Create an aliased <code>PUBLIC.ImportMetadata</code> table reference
	 */
	public Importmetadata(String alias) {
		this(alias, IMPORTMETADATA);
	}

	private Importmetadata(String alias, Table<ImportmetadataRecord> aliased) {
		this(alias, aliased, null);
	}

	private Importmetadata(String alias, Table<ImportmetadataRecord> aliased, Field<?>[] parameters) {
		super(alias, Public.PUBLIC, aliased, parameters, "");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Identity<ImportmetadataRecord, Integer> getIdentity() {
		return Keys.IDENTITY_IMPORTMETADATA;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UniqueKey<ImportmetadataRecord> getPrimaryKey() {
		return Keys.SYS_PK_10202;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<UniqueKey<ImportmetadataRecord>> getKeys() {
		return Arrays.<UniqueKey<ImportmetadataRecord>>asList(Keys.SYS_PK_10202, Keys.IMPORTMETADATA_UNI_0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Importmetadata as(String alias) {
		return new Importmetadata(alias, this);
	}

	/**
	 * Rename this table
	 */
	public Importmetadata rename(String name) {
		return new Importmetadata(name, null);
	}
}
