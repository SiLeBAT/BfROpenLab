package de.bund.bfr.knime.gis.shapeview;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;

import org.knime.core.data.RowKey;
import org.knime.core.node.NodeView;
import org.knime.core.node.property.hilite.HiLiteHandler;
import org.knime.core.node.property.hilite.HiLiteListener;
import org.knime.core.node.property.hilite.KeyEvent;
import org.knime.core.util.Pair;
import org.openstreetmap.gui.jmapviewer.OsmFileCacheTileLoader;
import org.openstreetmap.gui.jmapviewer.OsmTileLoader;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoader;

import de.bund.bfr.knime.gis.shapeview.JMapViewerExtended.MapOverlayRectangle;

/**
 * <code>NodeView</code> for the "ShapeView" Node.
 * 
 * 
 * @author Christian Thoens
 */
public class ShapeViewNodeView extends NodeView<ShapeViewNodeModel> implements
		HiLiteListener {

	/*
	 * tolerance for selecting a point or showing additional information as tool
	 * tip
	 */
	private static final double POINT_TOLERANCE = 5;

	private static final int FILTER_NONE = 0;

	private static final int FILTER_HILITE_ONLY = 1;

	private static final int FILTER_UNHILITE_ONLY = 2;

	private final JMapViewerExtended m_map;

	private final JPanel m_panel;

	private final MapOverlayRectangle m_selectionRectangle;

	private final Set<KnimeMapMarker2> m_hiliteSet = new HashSet<KnimeMapMarker2>();

	private final Set<KnimeMapMarker2> m_unhiliteSet = new HashSet<KnimeMapMarker2>();

	private HiLiteHandler m_hiliteHandler;

	private int m_hiliteFilter = FILTER_NONE;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected ShapeViewNodeView(final ShapeViewNodeModel nodeModel) {
		super(nodeModel);

		m_panel = new JPanel();
		m_map = new JMapViewerExtended(
				m_selectionRectangle = new MapOverlayRectangle());
		m_map.setToolTipText("");

		m_panel.setLayout(new BorderLayout());
		JPanel panel = new JPanel();

		m_panel.add(panel, BorderLayout.NORTH);
		JButton fitButton = new JButton("Fit display to markers");
		fitButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				m_map.setDisplayToFitMapMarkers();
			}
		});

		Collection<String> tsIds = TileSources.getAllAvailableTileSources();
		JComboBox tileSourceSelector = new JComboBox(
				tsIds.toArray(new String[tsIds.size()]));
		tileSourceSelector.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(final ItemEvent e) {
				m_map.setTileSource(TileSources.getTileSource((String) e
						.getItem()));
			}
		});
		tileSourceSelector
				.setSelectedItem(TileSources.getDefaultTileSourceId());

		JMenu tiles = new JMenu("Tiles");
		JRadioButtonMenuItem osmCachedTileLoader = new JRadioButtonMenuItem(
				"OsmFileCacheTileLoader");
		JRadioButtonMenuItem osmTileLoader = new JRadioButtonMenuItem(
				"OsmTileLoader");
		ButtonGroup tileLoaderGroup = new ButtonGroup();
		tileLoaderGroup.add(osmTileLoader);
		tileLoaderGroup.add(osmCachedTileLoader);
		tiles.add(osmCachedTileLoader);
		tiles.add(osmTileLoader);
		final TileLoader[] tloader = new TileLoader[2];
		try {
			tloader[0] = new OsmFileCacheTileLoader(m_map);
		} catch (IOException e2) {
			tloader[0] = null;
			osmCachedTileLoader.setEnabled(false);
			tileLoaderGroup.setSelected(osmTileLoader.getModel(), true);
		}
		if (tloader[0] != null) {
			osmCachedTileLoader.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					m_map.setTileLoader(tloader[0]);
				}
			});
		}
		tloader[1] = new OsmTileLoader(m_map);
		osmTileLoader.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				m_map.setTileLoader(tloader[1]);
			}
		});
		// select the cached tile loader as default, if available
		if (tloader[0] != null) {
			osmCachedTileLoader.doClick();
		} else {
			osmTileLoader.doClick();
		}

		panel.add(tileSourceSelector);
		panel.add(fitButton);

		/* Menu */
		JMenu hilite = new JMenu("Hilite");
		JMenuItem clearHilite = new JMenuItem("Clear Hilite");
		clearHilite.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				m_hiliteHandler.fireClearHiLiteEvent();
			}
		});
		hilite.add(clearHilite);

		JMenu filter = new JMenu("Filter");
		hilite.add(filter);
		JRadioButtonMenuItem all = new JRadioButtonMenuItem("Show All");
		JRadioButtonMenuItem hiliteonly = new JRadioButtonMenuItem(
				"Show Hilited Only");
		JRadioButtonMenuItem unhiliteonly = new JRadioButtonMenuItem(
				"Show UnHilited Only");
		filter.add(all);
		filter.add(hiliteonly);
		filter.add(unhiliteonly);
		ButtonGroup filterGroup = new ButtonGroup();
		filterGroup.add(all);
		filterGroup.add(hiliteonly);
		filterGroup.add(unhiliteonly);
		filterGroup.setSelected(all.getModel(), true);
		all.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				m_hiliteFilter = FILTER_NONE;
				updateMapMarkers();
				m_map.repaint();
			}
		});
		hiliteonly.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				m_hiliteFilter = FILTER_HILITE_ONLY;
				updateMapMarkers();
				m_map.repaint();
			}
		});
		unhiliteonly.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				m_hiliteFilter = FILTER_UNHILITE_ONLY;
				updateMapMarkers();
				m_map.repaint();
			}
		});

		JMenu view = new JMenu("View");
		final JCheckBoxMenuItem showMapMarker = new JCheckBoxMenuItem(
				"Map markers visible");
		showMapMarker.setSelected(m_map.getMapMarkersVisible());
		showMapMarker.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				m_map.setMapMarkerVisible(showMapMarker.isSelected());
			}
		});
		view.add(showMapMarker);

		final JCheckBoxMenuItem showTileGrid = new JCheckBoxMenuItem(
				"Tile grid visible");
		showTileGrid.setSelected(m_map.isTileGridVisible());
		showTileGrid.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				m_map.setTileGridVisible(showTileGrid.isSelected());
			}
		});
		view.add(showTileGrid);

		final JCheckBoxMenuItem showZoomControls = new JCheckBoxMenuItem(
				"Show zoom controls");
		showZoomControls.setSelected(m_map.getZoomContolsVisible());
		showZoomControls.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				m_map.setZoomContolsVisible(showZoomControls.isSelected());
			}
		});
		view.add(showZoomControls);

		final JCheckBoxMenuItem scrollWrapEnabled = new JCheckBoxMenuItem(
				"Scrollwrap enabled");
		scrollWrapEnabled.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				m_map.setScrollWrapEnabled(scrollWrapEnabled.isSelected());
			}
		});
		view.add(scrollWrapEnabled);

		JMenu help = new JMenu("Help");
		JMenuItem howto = new JMenuItem("How to ...");
		final String helpMessage = "Use right mouse button to move, left double click or mouse wheel to zoom.\n"
				+ "To hilite map markers, span a selection rectangle (left mouse) or left click on a marker.";
		howto.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				JOptionPane.showMessageDialog(getComponent(), helpMessage);
			}
		});
		help.add(howto);

		getJMenuBar().add(hilite);
		getJMenuBar().add(view);
		getJMenuBar().add(tiles);
		getJMenuBar().add(help);

		m_panel.add(m_map, BorderLayout.CENTER);

		// add map markers to map
		updateMapMarkers();

		// hiliting
		m_hiliteHandler = getNodeModel().getInHiLiteHandler(0);
		m_hiliteHandler.addHiLiteListener(this);

		/* mouse listeners */
		m_map.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					m_map.getAttribution()
							.handleAttribution(e.getPoint(), true);

					for (Pair<KnimeMapMarker2, Point> marker : m_map
							.getPaintedKnimeMapMarkers()) {
						Point p = marker.getSecond();
						if (p.distance(e.getPoint()) < POINT_TOLERANCE) {
							if (marker.getFirst().isHilited()) {
								m_unhiliteSet.add(marker.getFirst());
							} else {
								m_hiliteSet.add(marker.getFirst());
							}
							break;
						}
					}
					processHiliteMarkerEvents();
				}
			}

			@Override
			public void mousePressed(final MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					m_selectionRectangle.setEnabled(true);
					m_selectionRectangle.setPoint1(e.getPoint().x,
							e.getPoint().y);
				}
			}

			@Override
			public void mouseReleased(final MouseEvent e) {
				if (m_selectionRectangle.isEnabled()) {
					m_selectionRectangle.setEnabled(false);
					m_selectionRectangle.setPoint2(e.getPoint().x,
							e.getPoint().y);
					for (Pair<KnimeMapMarker2, Point> marker : m_map
							.getPaintedKnimeMapMarkers()) {
						Point p = marker.getSecond();
						if (m_selectionRectangle.contains(p)) {
							if (marker.getFirst().isHilited()) {
								m_unhiliteSet.add(marker.getFirst());
							} else {
								m_hiliteSet.add(marker.getFirst());
							}
						}
					}
					processHiliteMarkerEvents();
					m_map.repaint();
				}
			}
		});

		m_map.addMouseMotionListener(new MouseAdapter() {

			@Override
			public void mouseMoved(final MouseEvent e) {
				boolean cursorHand = m_map.getAttribution()
						.handleAttributionCursor(e.getPoint());
				if (cursorHand) {
					m_map.setCursor(new Cursor(Cursor.HAND_CURSOR));
				} else {
					m_map.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				}
				m_map.setToolTipText("");
				for (Pair<KnimeMapMarker2, Point> marker : m_map
						.getPaintedKnimeMapMarkers()) {
					if (marker.getSecond().distance(e.getPoint()) < POINT_TOLERANCE) {
						m_map.setCursor(new Cursor(Cursor.HAND_CURSOR));
						m_map.setToolTipText(marker.getFirst().getInfo());
						break;
					}
				}
			}

			@Override
			public void mouseDragged(final MouseEvent e) {
				if (m_selectionRectangle.isEnabled()) {
					m_selectionRectangle.setPoint2(e.getPoint().x,
							e.getPoint().y);
					m_map.repaint();
				}

			}
		});

		m_map.repaint();
		setComponent(m_panel);

	}

	private void processHiliteMarkerEvents() {
		for (KnimeMapMarker2 marker : m_hiliteSet) {
			m_hiliteHandler
					.fireHiLiteEvent(new RowKey(marker.getRowKeyString()));
		}
		for (KnimeMapMarker2 marker : m_unhiliteSet) {
			m_hiliteHandler.fireUnHiLiteEvent(new RowKey(marker
					.getRowKeyString()));
		}
		m_hiliteSet.clear();
		m_unhiliteSet.clear();

	}

	/* Gets the map markers from the node model and filters them, if necessary */
	private void updateMapMarkers() {
		if (getNodeModel().getMapMarkers() != null) {
			Collection<KnimeMapMarker2> markers = getNodeModel()
					.getMapMarkers().values();
			if (m_hiliteFilter == FILTER_NONE) {
				m_map.setKnimeMapMarkers(markers);
			} else {
				List<KnimeMapMarker2> filtered = new ArrayList<KnimeMapMarker2>(
						markers.size());
				for (KnimeMapMarker2 marker : markers) {
					if (m_hiliteFilter == FILTER_HILITE_ONLY) {
						if (marker.isHilited()) {
							filtered.add(marker);
						}
					} else {
						if (!marker.isHilited()) {
							filtered.add(marker);
						}
					}
				}
				m_map.setKnimeMapMarkers(filtered);

			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onClose() {
		// nothing to do
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onOpen() {
		// nothing to do

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void modelChanged() {

		// add map markers to map
		updateMapMarkers();

		// replace hilite listener
		m_hiliteHandler.removeHiLiteListener(this);
		if (getNodeModel() != null) {
			m_hiliteHandler = getNodeModel().getInHiLiteHandler(0);
			m_hiliteHandler.addHiLiteListener(this);
		} else {
			m_hiliteHandler = new HiLiteHandler();
		}

	}

	/*---- HiLite Listener ----*/

	@Override
	public void unHiLiteAll(final KeyEvent event) {
		for (KnimeMapMarker2 marker : getNodeModel().getMapMarkers().values()) {
			marker.setHilited(false);
		}
		if (m_hiliteFilter == FILTER_HILITE_ONLY
				|| m_hiliteFilter == FILTER_UNHILITE_ONLY) {
			updateMapMarkers();
		}
		m_map.repaint();
	}

	@Override
	public void unHiLite(final KeyEvent event) {
		for (RowKey rk : event.keys()) {
			getNodeModel().getMapMarkers().get(rk).setHilited(false);
		}
		if (m_hiliteFilter == FILTER_HILITE_ONLY
				|| m_hiliteFilter == FILTER_UNHILITE_ONLY) {
			updateMapMarkers();
		}
		m_map.repaint();

	}

	@Override
	public void hiLite(final KeyEvent event) {
		for (RowKey rk : event.keys()) {
			getNodeModel().getMapMarkers().get(rk).setHilited(true);
		}
		if (m_hiliteFilter == FILTER_HILITE_ONLY
				|| m_hiliteFilter == FILTER_UNHILITE_ONLY) {
			updateMapMarkers();
		}
		m_map.repaint();

	}

}
