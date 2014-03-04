package de.bund.bfr.knime.gis.shapeview;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.BingAerialTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.MapQuestOpenAerialTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.MapQuestOsmTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;

public class TileSources {

	private static TileSources m_instance;

	private Map<String, TileSource> m_tileSources = null;

	private TileSources() {
		// singleton
	}

	/**
	 * @return the id's of all available maps
	 */
	public static Collection<String> getAllAvailableTileSources() {
		getInstance().loadTileSources();
		return getInstance().m_tileSources.keySet();
	}

	/**
	 * @param id
	 * @return the requested map source. <code>null</code> if doesn't exist
	 *         anymore.
	 */
	public static TileSource getTileSource(final String id) {
		getInstance().loadTileSources();
		TileSource ts = getInstance().m_tileSources.get(id);
		return ts;
	}

	/**
	 * @return the default map
	 */
	public static String getDefaultTileSourceId() {
		return "Mapnik";
	}

	/**
	 * Clears all currently loaded tile sources. Subsequent calls of
	 * getTileSource.. will refresh the sources, including the changes from the
	 * preference page.
	 */
	public static void clearTileSourceCache() {
		getInstance().m_tileSources = null;
	}

	private static TileSources getInstance() {
		if (m_instance == null) {
			m_instance = new TileSources();
		}
		return m_instance;
	}

	// add/merge the selected tile sources from the preferences
	private void loadTileSources() {

		if (m_tileSources == null) {
			m_tileSources = new HashMap<String, TileSource>();
			// default tile sources
			m_tileSources.put("Mapnik", new OsmTileSource.Mapnik());
			m_tileSources.put("CycleMap", new OsmTileSource.CycleMap());
			m_tileSources.put("Bing", new BingAerialTileSource());
			m_tileSources.put("MapQuest", new MapQuestOsmTileSource());
			m_tileSources.put("MapQuest Aerial",
					new MapQuestOpenAerialTileSource());
		}
	}
}
