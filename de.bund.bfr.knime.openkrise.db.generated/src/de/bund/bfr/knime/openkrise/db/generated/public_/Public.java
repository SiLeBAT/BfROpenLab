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
package de.bund.bfr.knime.openkrise.db.generated.public_;


import de.bund.bfr.knime.openkrise.db.generated.public_.tables.Chargen;
import de.bund.bfr.knime.openkrise.db.generated.public_.tables.Chargenverbindungen;
import de.bund.bfr.knime.openkrise.db.generated.public_.tables.Extrafields;
import de.bund.bfr.knime.openkrise.db.generated.public_.tables.Importmetadata;
import de.bund.bfr.knime.openkrise.db.generated.public_.tables.Lieferungen;
import de.bund.bfr.knime.openkrise.db.generated.public_.tables.Lookups;
import de.bund.bfr.knime.openkrise.db.generated.public_.tables.Produktkatalog;
import de.bund.bfr.knime.openkrise.db.generated.public_.tables.Station;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import org.jooq.Table;
import org.jooq.impl.SchemaImpl;


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
public class Public extends SchemaImpl {

	private static final long serialVersionUID = -44040847;

	/**
	 * The reference instance of <code>PUBLIC</code>
	 */
	public static final Public PUBLIC = new Public();

	/**
	 * No further instances allowed
	 */
	private Public() {
		super("PUBLIC");
	}

	@Override
	public final List<Table<?>> getTables() {
		List result = new ArrayList();
		result.addAll(getTables0());
		return result;
	}

	private final List<Table<?>> getTables0() {
		return Arrays.<Table<?>>asList(
			Chargen.CHARGEN,
			Chargenverbindungen.CHARGENVERBINDUNGEN,
			Extrafields.EXTRAFIELDS,
			Importmetadata.IMPORTMETADATA,
			Lieferungen.LIEFERUNGEN,
			Lookups.LOOKUPS,
			Produktkatalog.PRODUKTKATALOG,
			Station.STATION);
	}
}
