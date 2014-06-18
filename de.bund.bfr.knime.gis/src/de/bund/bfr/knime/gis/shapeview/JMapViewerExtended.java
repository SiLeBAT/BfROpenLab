package de.bund.bfr.knime.gis.shapeview;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.swing.border.LineBorder;

import org.knime.core.util.Pair;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.events.JMVCommandEvent;
import org.openstreetmap.gui.jmapviewer.interfaces.JMapViewerEventListener;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;
import org.openstreetmap.gui.jmapviewer.interfaces.MapRectangle;

public class JMapViewerExtended extends JMapViewer {

	private static final long serialVersionUID = 1L;

	private final MapOverlay[] m_mapOverlays;

	private final Set<Pair<KnimeMapMarker2, Point>> m_paintedMarkers = new HashSet<>();

	/**
	 * Initializes the map viewer.
	 * 
	 * @param mapOverlays
	 *            optional overlays
	 */
	public JMapViewerExtended(final MapOverlay... mapOverlays) {
		m_mapOverlays = mapOverlays;
		// Listen to the map viewer for user operations so components
		// will
		// recieve events and update
		this.addJMVListener(new JMapViewerEventListener() {
			@Override
			public void processCommand(final JMVCommandEvent command) {
				// clear considered markers
				m_paintedMarkers.clear();

			}
		});
		setBorder(new LineBorder(Color.black));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void paint(final Graphics g) {
		super.paint(g);
		for (MapOverlay overlay : m_mapOverlays) {
			if (overlay.isEnabled()) {
				overlay.paint(g);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void paintMarker(final Graphics g, final MapMarker marker) {
		super.paintMarker(g, marker);
		if (marker instanceof KnimeMapMarker2) {
			// collect all KnimeMapMarkers that are painted on the map and their
			// position
			Point p = getMapPosition(marker.getLat(), marker.getLon(),
					!isScrollWrapEnabled());
			if (p != null) {
				if (isScrollWrapEnabled()) {
					int tilesize = getMinimumSize().width;
					int mapSize = tilesize << zoom;
					p.x = p.x % mapSize;
				}
				m_paintedMarkers.add(new Pair<>((KnimeMapMarker2) marker, p));
			}
		}
	}

	/**
	 * @param markers
	 *            all {@link KnimeMapMarker} to be shown on the map. The new
	 *            markers will replace the old ones.
	 */
	public void setKnimeMapMarkers(final Collection<KnimeMapMarker2> markers) {
		mapMarkerList.clear();
		mapMarkerList.addAll(markers);
		m_paintedMarkers.clear();
	}

	/**
	 * @return all {@link KnimeMapMarker} that where painted onto the map with
	 *         there position on the map
	 */
	public Collection<Pair<KnimeMapMarker2, Point>> getPaintedKnimeMapMarkers() {
		return Collections.unmodifiableCollection(m_paintedMarkers);
	}

	/**
	 * Things to be overlayed on top of the map (e.g. the rectangle for the
	 * selection of the nodes to be hilited). A {@link MapOverlay} is different
	 * to a {@link MapMarker} or {@link MapRectangle} as its only specified by
	 * the "gui"-coordinates and not the geo-coordinates (lon, lat).
	 * Additionally it can be enabled or disabled.
	 */
	public interface MapOverlay {

		public void paint(final Graphics g);

		public boolean isEnabled();
	}

	/**
	 * Overlaying rectangle.
	 * 
	 */
	public static class MapOverlayRectangle implements MapOverlay {

		private int m_x1;

		private int m_y1;

		private int m_x2;

		private int m_y2;

		private boolean m_enabled = false;

		// buffered rectangle
		private Rectangle m_rectangle = null;

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void paint(final Graphics g) {
			Rectangle r = getRectangle();
			g.setColor(new Color(50, 50, 50, 50));
			g.fillRect(r.x, r.y, r.width, r.height);
			g.setColor(new Color(255, 255, 255));
			g.drawRect(r.x, r.y, r.width, r.height);
		}

		/**
		 * Arbitrary first point.
		 * 
		 * @param x
		 * @param y
		 */
		public void setPoint1(final int x, final int y) {
			m_x1 = x;
			m_y1 = y;
			m_rectangle = null;

		}

		/**
		 * Arbitrary second point.
		 * 
		 * @param x
		 * @param y
		 */
		public void setPoint2(final int x, final int y) {
			m_x2 = x;
			m_y2 = y;
			m_rectangle = null;

		}

		@Override
		public boolean isEnabled() {
			return m_enabled;
		}

		/**
		 * Enables or disables the rectangle
		 * 
		 * @param enable
		 *            if false, it will not be painted onto the map.
		 */
		public void setEnabled(final boolean enable) {
			m_enabled = enable;
		}

		/**
		 * 
		 * @param p
		 *            checks if the point is within the rectangle
		 *            (gui-coordinates!)
		 * @return
		 */
		public boolean contains(final Point p) {
			return getRectangle().contains(p);

		}

		private Rectangle getRectangle() {
			if (m_rectangle == null) {
				int x1 = Math.min(m_x1, m_x2);
				int y1 = Math.min(m_y1, m_y2);
				int x2 = Math.max(m_x1, m_x2);
				int y2 = Math.max(m_y1, m_y2);

				m_rectangle = new Rectangle(x1, y1, x2 - x1, y2 - y1);
			}
			return m_rectangle;
		}

	}

	/**
	 * Overlay polygon.
	 */
	public static class MapOverlayPolygon implements MapOverlay {

		// awt polygon
		private Polygon m_poly = new Polygon();

		private boolean m_enabled;

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void paint(final Graphics g) {
			if (m_poly.npoints > 0) {
				g.setColor(Color.red);
				g.fillOval(m_poly.xpoints[0] - 3, m_poly.ypoints[0] - 3, 7, 7);
			}
			if (m_poly.npoints > 1) {
				g.setColor(Color.green);
				g.fillOval(m_poly.xpoints[m_poly.npoints - 1] - 3,
						m_poly.ypoints[m_poly.npoints - 1] - 3, 7, 7);
				g.setColor(new Color(50, 50, 50, 50));
				g.fillPolygon(m_poly);
				g.setColor(new Color(255, 255, 255));
				g.drawPolygon(m_poly);
			}

		}

		/**
		 * Adds a new point to the polygon.
		 * 
		 * @param x
		 *            map coordinate (not the geographic coordinate!)
		 * @param y
		 *            map coordinate (not the geographic coordinate!)
		 */
		public void addPoint(final int x, final int y) {
			m_poly.addPoint(x, y);
		}

		/**
		 * Resets all points of the polygon to an empty polygon.
		 */
		public void reset() {
			m_poly.reset();
		}

		/**
		 * @return the x coordinates of the polygon
		 */
		public int[] getXPoints() {
			return m_poly.xpoints;
		}

		/**
		 * @return the y coordinates of the polygone
		 */
		public int[] getYPoints() {
			return m_poly.ypoints;
		}

		/**
		 * Translates the polygon by the given amount.
		 * 
		 * @param dx
		 * @param dy
		 */
		public void move(final int dx, final int dy) {
			for (int i = 0; i < m_poly.npoints; i++) {
				m_poly.xpoints[i] -= dx;
				m_poly.ypoints[i] -= dy;
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean isEnabled() {
			return m_enabled;
		}

		/**
		 * Enables or disables the polygon.
		 * 
		 * @param enabled
		 */
		public void setEnabled(final boolean enabled) {
			m_enabled = enabled;
		}

	}
}
