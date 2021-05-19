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
import de.bund.bfr.knime.openkrise.db.generated.public_.tables.records.ChargenverbindungenRecord;

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
public class Chargenverbindungen extends TableImpl<ChargenverbindungenRecord> {

	private static final long serialVersionUID = 2135761126;

	/**
	 * The reference instance of <code>PUBLIC.ChargenVerbindungen</code>
	 */
	public static final Chargenverbindungen CHARGENVERBINDUNGEN = new Chargenverbindungen();

	/**
	 * The class holding records for this type
	 */
	@Override
	public Class<ChargenverbindungenRecord> getRecordType() {
		return ChargenverbindungenRecord.class;
	}

	/**
	 * The column <code>PUBLIC.ChargenVerbindungen.ID</code>.
	 */
	public final TableField<ChargenverbindungenRecord, Integer> ID = createField("ID", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>PUBLIC.ChargenVerbindungen.Zutat</code>.
	 */
	public final TableField<ChargenverbindungenRecord, Integer> ZUTAT = createField("Zutat", org.jooq.impl.SQLDataType.INTEGER, this, "");

	/**
	 * The column <code>PUBLIC.ChargenVerbindungen.Produkt</code>.
	 */
	public final TableField<ChargenverbindungenRecord, Integer> PRODUKT = createField("Produkt", org.jooq.impl.SQLDataType.INTEGER, this, "");

	/**
	 * The column <code>PUBLIC.ChargenVerbindungen.MixtureRatio</code>.
	 */
	public final TableField<ChargenverbindungenRecord, Double> MIXTURERATIO = createField("MixtureRatio", org.jooq.impl.SQLDataType.DOUBLE, this, "");

	/**
	 * The column <code>PUBLIC.ChargenVerbindungen.ImportSources</code>.
	 */
	public final TableField<ChargenverbindungenRecord, String> IMPORTSOURCES = createField("ImportSources", org.jooq.impl.SQLDataType.VARCHAR.length(16383), this, "");

	/**
	 * The column <code>PUBLIC.ChargenVerbindungen.Kommentar</code>.
	 */
	public final TableField<ChargenverbindungenRecord, String> KOMMENTAR = createField("Kommentar", org.jooq.impl.SQLDataType.VARCHAR.length(1023), this, "");

	/**
	 * Create a <code>PUBLIC.ChargenVerbindungen</code> table reference
	 */
	public Chargenverbindungen() {
		this("ChargenVerbindungen", null);
	}

	/**
	 * Create an aliased <code>PUBLIC.ChargenVerbindungen</code> table reference
	 */
	public Chargenverbindungen(String alias) {
		this(alias, CHARGENVERBINDUNGEN);
	}

	private Chargenverbindungen(String alias, Table<ChargenverbindungenRecord> aliased) {
		this(alias, aliased, null);
	}

	private Chargenverbindungen(String alias, Table<ChargenverbindungenRecord> aliased, Field<?>[] parameters) {
		super(alias, Public.PUBLIC, aliased, parameters, "");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Identity<ChargenverbindungenRecord, Integer> getIdentity() {
		return Keys.IDENTITY_CHARGENVERBINDUNGEN;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UniqueKey<ChargenverbindungenRecord> getPrimaryKey() {
		return Keys.SYS_PK_10189;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<UniqueKey<ChargenverbindungenRecord>> getKeys() {
		return Arrays.<UniqueKey<ChargenverbindungenRecord>>asList(Keys.SYS_PK_10189);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ForeignKey<ChargenverbindungenRecord, ?>> getReferences() {
		return Arrays.<ForeignKey<ChargenverbindungenRecord, ?>>asList(Keys.CHARGENVERBINDUNGEN_FK_ZUTAT_0, Keys.CHARGENVERBINDUNGEN_FK_PRODUKT_1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Chargenverbindungen as(String alias) {
		return new Chargenverbindungen(alias, this);
	}

	/**
	 * Rename this table
	 */
	public Chargenverbindungen rename(String name) {
		return new Chargenverbindungen(name, null);
	}
}
