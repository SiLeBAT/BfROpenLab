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
package de.bund.bfr.knime.openkrise.db.generated.public_.tables;


import de.bund.bfr.knime.openkrise.db.generated.public_.Keys;
import de.bund.bfr.knime.openkrise.db.generated.public_.Public;
import de.bund.bfr.knime.openkrise.db.generated.public_.tables.records.LieferungenRecord;

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
public class Lieferungen extends TableImpl<LieferungenRecord> {

	private static final long serialVersionUID = 159186169;

	/**
	 * The reference instance of <code>PUBLIC.Lieferungen</code>
	 */
	public static final Lieferungen LIEFERUNGEN = new Lieferungen();

	/**
	 * The class holding records for this type
	 */
	@Override
	public Class<LieferungenRecord> getRecordType() {
		return LieferungenRecord.class;
	}

	/**
	 * The column <code>PUBLIC.Lieferungen.ID</code>.
	 */
	public final TableField<LieferungenRecord, Integer> ID = createField("ID", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>PUBLIC.Lieferungen.Charge</code>.
	 */
	public final TableField<LieferungenRecord, Integer> CHARGE = createField("Charge", org.jooq.impl.SQLDataType.INTEGER, this, "");

	/**
	 * The column <code>PUBLIC.Lieferungen.dd_day</code>.
	 */
	public final TableField<LieferungenRecord, Integer> DD_DAY = createField("dd_day", org.jooq.impl.SQLDataType.INTEGER, this, "");

	/**
	 * The column <code>PUBLIC.Lieferungen.dd_month</code>.
	 */
	public final TableField<LieferungenRecord, Integer> DD_MONTH = createField("dd_month", org.jooq.impl.SQLDataType.INTEGER, this, "");

	/**
	 * The column <code>PUBLIC.Lieferungen.dd_year</code>.
	 */
	public final TableField<LieferungenRecord, Integer> DD_YEAR = createField("dd_year", org.jooq.impl.SQLDataType.INTEGER, this, "");

	/**
	 * The column <code>PUBLIC.Lieferungen.ad_day</code>.
	 */
	public final TableField<LieferungenRecord, Integer> AD_DAY = createField("ad_day", org.jooq.impl.SQLDataType.INTEGER, this, "");

	/**
	 * The column <code>PUBLIC.Lieferungen.ad_month</code>.
	 */
	public final TableField<LieferungenRecord, Integer> AD_MONTH = createField("ad_month", org.jooq.impl.SQLDataType.INTEGER, this, "");

	/**
	 * The column <code>PUBLIC.Lieferungen.ad_year</code>.
	 */
	public final TableField<LieferungenRecord, Integer> AD_YEAR = createField("ad_year", org.jooq.impl.SQLDataType.INTEGER, this, "");

	/**
	 * The column <code>PUBLIC.Lieferungen.numPU</code>.
	 */
	public final TableField<LieferungenRecord, Double> NUMPU = createField("numPU", org.jooq.impl.SQLDataType.DOUBLE, this, "");

	/**
	 * The column <code>PUBLIC.Lieferungen.typePU</code>.
	 */
	public final TableField<LieferungenRecord, String> TYPEPU = createField("typePU", org.jooq.impl.SQLDataType.VARCHAR.length(255), this, "");

	/**
	 * The column <code>PUBLIC.Lieferungen.Unitmenge</code>.
	 */
	public final TableField<LieferungenRecord, Double> UNITMENGE = createField("Unitmenge", org.jooq.impl.SQLDataType.DOUBLE, this, "");

	/**
	 * The column <code>PUBLIC.Lieferungen.UnitEinheit</code>.
	 */
	public final TableField<LieferungenRecord, String> UNITEINHEIT = createField("UnitEinheit", org.jooq.impl.SQLDataType.VARCHAR.length(50), this, "");

	/**
	 * The column <code>PUBLIC.Lieferungen.Empfänger</code>.
	 */
	public final TableField<LieferungenRecord, Integer> EMPFÄNGER = createField("Empfänger", org.jooq.impl.SQLDataType.INTEGER, this, "");

	/**
	 * The column <code>PUBLIC.Lieferungen.Serial</code>.
	 */
	public final TableField<LieferungenRecord, String> SERIAL = createField("Serial", org.jooq.impl.SQLDataType.VARCHAR.length(16383), this, "");

	/**
	 * The column <code>PUBLIC.Lieferungen.EndChain</code>.
	 */
	public final TableField<LieferungenRecord, String> ENDCHAIN = createField("EndChain", org.jooq.impl.SQLDataType.VARCHAR.length(255), this, "");

	/**
	 * The column <code>PUBLIC.Lieferungen.Explanation_EndChain</code>.
	 */
	public final TableField<LieferungenRecord, String> EXPLANATION_ENDCHAIN = createField("Explanation_EndChain", org.jooq.impl.SQLDataType.VARCHAR.length(16383), this, "");

	/**
	 * The column <code>PUBLIC.Lieferungen.Contact_Questions_Remarks</code>.
	 */
	public final TableField<LieferungenRecord, String> CONTACT_QUESTIONS_REMARKS = createField("Contact_Questions_Remarks", org.jooq.impl.SQLDataType.VARCHAR.length(16383), this, "");

	/**
	 * The column <code>PUBLIC.Lieferungen.Further_Traceback</code>.
	 */
	public final TableField<LieferungenRecord, String> FURTHER_TRACEBACK = createField("Further_Traceback", org.jooq.impl.SQLDataType.VARCHAR.length(255), this, "");

	/**
	 * The column <code>PUBLIC.Lieferungen.ImportSources</code>.
	 */
	public final TableField<LieferungenRecord, String> IMPORTSOURCES = createField("ImportSources", org.jooq.impl.SQLDataType.VARCHAR.length(16383), this, "");

	/**
	 * The column <code>PUBLIC.Lieferungen.Kommentar</code>.
	 */
	public final TableField<LieferungenRecord, String> KOMMENTAR = createField("Kommentar", org.jooq.impl.SQLDataType.VARCHAR.length(1023), this, "");

	/**
	 * Create a <code>PUBLIC.Lieferungen</code> table reference
	 */
	public Lieferungen() {
		this("Lieferungen", null);
	}

	/**
	 * Create an aliased <code>PUBLIC.Lieferungen</code> table reference
	 */
	public Lieferungen(String alias) {
		this(alias, LIEFERUNGEN);
	}

	private Lieferungen(String alias, Table<LieferungenRecord> aliased) {
		this(alias, aliased, null);
	}

	private Lieferungen(String alias, Table<LieferungenRecord> aliased, Field<?>[] parameters) {
		super(alias, Public.PUBLIC, aliased, parameters, "");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Identity<LieferungenRecord, Integer> getIdentity() {
		return Keys.IDENTITY_LIEFERUNGEN;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UniqueKey<LieferungenRecord> getPrimaryKey() {
		return Keys.SYS_PK_10181;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<UniqueKey<LieferungenRecord>> getKeys() {
		return Arrays.<UniqueKey<LieferungenRecord>>asList(Keys.SYS_PK_10181);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ForeignKey<LieferungenRecord, ?>> getReferences() {
		return Arrays.<ForeignKey<LieferungenRecord, ?>>asList(Keys.LIEFERUNGEN_FK_CHARGE_0, Keys.LIEFERUNGEN_FK_EMPFÄNGER_8);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Lieferungen as(String alias) {
		return new Lieferungen(alias, this);
	}

	/**
	 * Rename this table
	 */
	public Lieferungen rename(String name) {
		return new Lieferungen(name, null);
	}
}
