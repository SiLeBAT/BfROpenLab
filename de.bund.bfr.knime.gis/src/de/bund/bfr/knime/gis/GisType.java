/*******************************************************************************
 * Copyright (c) 2016 Federal Institute for Risk Assessment (BfR), Germany
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
import org.openstreetmap.gui.jmapviewer.tilesources.BingAerialTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.MapQuestOpenAerialTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.MapQuestOsmTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;

public enum GisType {
	SHAPEFILE("Shapefile", null),

	MAPNIK("Mapnik", new OsmTileSource.Mapnik()),

	CYCLE_MAP("Cycle Map", new OsmTileSource.CycleMap()),

	BING_AERIAL("Bing Aerial", new BingAerialTileSource()),

	MAPQUEST("MapQuest", new MapQuestOsmTileSource()),

	MAPQUEST_AERIAL("MapQuest Aerial", new MapQuestOpenAerialTileSource());

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
