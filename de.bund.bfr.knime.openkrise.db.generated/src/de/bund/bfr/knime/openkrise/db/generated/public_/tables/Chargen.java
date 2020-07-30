/*******************************************************************************
 * Copyright (c) 2019 German Federal Institute for Risk Assessment (BfR)
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
import de.bund.bfr.knime.openkrise.db.generated.public_.tables.records.ChargenRecord;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.ForeignKey;
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
public class Chargen extends TableImpl<ChargenRecord> {

	private static final long serialVersionUID = 1820516645;

	/**
	 * The reference instance of <code>PUBLIC.Chargen</code>
	 */
	public static final Chargen CHARGEN = new Chargen();

	/**
	 * The class holding records for this type
	 */
	@Override
	public Class<ChargenRecord> getRecordType() {
		return ChargenRecord.class;
	}

	/**
	 * The column <code>PUBLIC.Chargen.ID</code>.
	 */
	public final TableField<ChargenRecord, Integer> ID = createField("ID", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>PUBLIC.Chargen.Artikel</code>.
	 */
	public final TableField<ChargenRecord, Integer> ARTIKEL = createField("Artikel", org.jooq.impl.SQLDataType.INTEGER, this, "");

	/**
	 * The column <code>PUBLIC.Chargen.Zutaten</code>.
	 */
	public final TableField<ChargenRecord, Integer> ZUTATEN = createField("Zutaten", org.jooq.impl.SQLDataType.INTEGER, this, "");

	/**
	 * The column <code>PUBLIC.Chargen.ChargenNr</code>.
	 */
	public final TableField<ChargenRecord, String> CHARGENNR = createField("ChargenNr", org.jooq.impl.SQLDataType.VARCHAR.length(255), this, "");

	/**
	 * The column <code>PUBLIC.Chargen.Menge</code>.
	 */
	public final TableField<ChargenRecord, Double> MENGE = createField("Menge", org.jooq.impl.SQLDataType.DOUBLE, this, "");

	/**
	 * The column <code>PUBLIC.Chargen.Einheit</code>.
	 */
	public final TableField<ChargenRecord, String> EINHEIT = createField("Einheit", org.jooq.impl.SQLDataType.VARCHAR.length(50), this, "");

	/**
	 * The column <code>PUBLIC.Chargen.Lieferungen</code>.
	 */
	public final TableField<ChargenRecord, Integer> LIEFERUNGEN = createField("Lieferungen", org.jooq.impl.SQLDataType.INTEGER, this, "");

	/**
	 * The column <code>PUBLIC.Chargen.MHD_day</code>.
	 */
	public final TableField<ChargenRecord, Integer> MHD_DAY = createField("MHD_day", org.jooq.impl.SQLDataType.INTEGER, this, "");

	/**
	 * The column <code>PUBLIC.Chargen.MHD_month</code>.
	 */
	public final TableField<ChargenRecord, Integer> MHD_MONTH = createField("MHD_month", org.jooq.impl.SQLDataType.INTEGER, this, "");

	/**
	 * The column <code>PUBLIC.Chargen.MHD_year</code>.
	 */
	public final TableField<ChargenRecord, Integer> MHD_YEAR = createField("MHD_year", org.jooq.impl.SQLDataType.INTEGER, this, "");

	/**
	 * The column <code>PUBLIC.Chargen.pd_day</code>.
	 */
	public final TableField<ChargenRecord, Integer> PD_DAY = createField("pd_day", org.jooq.impl.SQLDataType.INTEGER, this, "");

	/**
	 * The column <code>PUBLIC.Chargen.pd_month</code>.
	 */
	public final TableField<ChargenRecord, Integer> PD_MONTH = createField("pd_month", org.jooq.impl.SQLDataType.INTEGER, this, "");

	/**
	 * The column <code>PUBLIC.Chargen.pd_year</code>.
	 */
	public final TableField<ChargenRecord, Integer> PD_YEAR = createField("pd_year", org.jooq.impl.SQLDataType.INTEGER, this, "");

	/**
	 * The column <code>PUBLIC.Chargen.Serial</code>.
	 */
	public final TableField<ChargenRecord, String> SERIAL = createField("Serial", org.jooq.impl.SQLDataType.VARCHAR.length(16383), this, "");

	/**
	 * The column <code>PUBLIC.Chargen.OriginCountry</code>.
	 */
	public final TableField<ChargenRecord, String> ORIGINCOUNTRY = createField("OriginCountry", org.jooq.impl.SQLDataType.VARCHAR.length(255), this, "");

	/**
	 * The column <code>PUBLIC.Chargen.MicrobioSample</code>.
	 */
	public final TableField<ChargenRecord, String> MICROBIOSAMPLE = createField("MicrobioSample", org.jooq.impl.SQLDataType.VARCHAR.length(255), this, "");

	/**
	 * The column <code>PUBLIC.Chargen.ImportSources</code>.
	 */
	public final TableField<ChargenRecord, String> IMPORTSOURCES = createField("ImportSources", org.jooq.impl.SQLDataType.VARCHAR.length(16383), this, "");

	/**
	 * The column <code>PUBLIC.Chargen.Kommentar</code>.
	 */
	public final TableField<ChargenRecord, String> KOMMENTAR = createField("Kommentar", org.jooq.impl.SQLDataType.VARCHAR.length(1023), this, "");

	/**
	 * Create a <code>PUBLIC.Chargen</code> table reference
	 */
	public Chargen() {
		this("Chargen", null);
	}

	/**
	 * Create an aliased <code>PUBLIC.Chargen</code> table reference
	 */
	public Chargen(String alias) {
		this(alias, CHARGEN);
	}

	private Chargen(String alias, Table<ChargenRecord> aliased) {
		this(alias, aliased, null);
	}

	private Chargen(String alias, Table<ChargenRecord> aliased, Field<?>[] parameters) {
		super(alias, Public.PUBLIC, aliased, parameters, "");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Identity<ChargenRecord, Integer> getIdentity() {
		return Keys.IDENTITY_CHARGEN;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UniqueKey<ChargenRecord> getPrimaryKey() {
		return Keys.SYS_PK_10175;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<UniqueKey<ChargenRecord>> getKeys() {
		return Arrays.<UniqueKey<ChargenRecord>>asList(Keys.SYS_PK_10175);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ForeignKey<ChargenRecord, ?>> getReferences() {
		return Arrays.<ForeignKey<ChargenRecord, ?>>asList(Keys.CHARGEN_FK_ARTIKEL_0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Chargen as(String alias) {
		return new Chargen(alias, this);
	}

	/**
	 * Rename this table
	 */
	public Chargen rename(String name) {
		return new Chargen(name, null);
	}
}
