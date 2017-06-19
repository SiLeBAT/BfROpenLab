/*******************************************************************************
 * Copyright (c) 2016 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.gis;

import java.util.stream.Stream;

import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.AbstractOsmTileSource;

public enum GisType {
	SHAPEFILE("Shapefile", null),

	MAPNIK("Mapnik", new AbstractOsmTileSource("Mapnik", "http://a.tile.openstreetmap.org", "MAPNIK") {
	}),

	WIKIMEDIA("Wikimedia", new AbstractOsmTileSource("Wikimedia", "https://maps.wikimedia.org/osm-intl", "WIKIMEDIA") {
	}),

	CARTO_LIGHT("Carto Light",
			new AbstractOsmTileSource("Carto Light", "http://a.basemaps.cartocdn.com/light_all", "CARTO_LIGHT") {
			}),

	BLACK_AND_WHITE("Black & White",
			new AbstractOsmTileSource("Black & White", "https://tiles.wmflabs.org/bw-mapnik", "BLACK_AND_WHITE") {
			}),

	HOT("HOT", new AbstractOsmTileSource("HOT", "http://a.tile.openstreetmap.fr/hot", "HOT") {
	});

	private String name;
	private TileSource tileSource;

	private GisType(String name, TileSource tileSource) {
		this.name = name;
		this.tileSource = tileSource;
	}

	public String getName() {
		return name;
	}

	public TileSource getTileSource() {
		return tileSource;
	}

	@Override
	public String toString() {
		return name;
	}

	public static GisType[] valuesWithoutShapefile() {
		return Stream.of(values()).filter(t -> t != SHAPEFILE).toArray(GisType[]::new);
	}
}
