package de.bund.bfr.knime.gis.shapeview;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.knime.core.data.property.ColorAttr;
import org.knime.core.data.property.ShapeFactory;
import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.MapObjectImpl;
import org.openstreetmap.gui.jmapviewer.Style;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;

public class KnimeMapMarker2 extends MapObjectImpl implements MapMarker,
		Externalizable {

	private static final long serialVersionUID = 1L;

	private static final double DEFAULT_SHAPE_SIZE = 6;

	/*
	 * basic identifier for the externalization to maintain
	 * backwards-compatibility
	 */
	private static final long EXTERANLIZATION_KEY_V0 = -2354753067502000163L;

	// persistent properties
	private double m_lat;

	private double m_lon;

	private Color m_c;

	private double m_sizeFactor;

	private String m_info;

	private ShapeFactory.Shape m_shape;

	private String m_rowKey;

	// non persistent/transient properties
	private boolean m_hilite = false;

	/**
	 * @param lat
	 *            coordinate
	 * @param lon
	 *            coordinate
	 * @param c
	 *            the color
	 * @param shape
	 *            the shape
	 * @param sizeFactor
	 *            the size
	 * @param info
	 *            additional info (e.g. displayed if one hovers over the object)
	 * @param rowKey
	 *            the row key associated with this map marker (for hiliting)
	 */
	public KnimeMapMarker2(final double lat, final double lon, final Color c,
			final ShapeFactory.Shape shape, final double sizeFactor,
			final String info, final String rowKey) {
		super(null, null, new Style());
		m_lat = lat;
		m_lon = lon;
		m_c = c;
		m_shape = shape;
		m_sizeFactor = sizeFactor;
		m_info = info;
		m_rowKey = rowKey;

	}

	/**
	 * Do not use!
	 */
	public KnimeMapMarker2() {
		super(null, null, new Style());
		// externalization
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getLat() {
		return m_lat;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getLon() {
		return m_lon;
	}

	/**
	 * @return
	 */
	public String getInfo() {
		return m_info;
	}

	/**
	 * @return the row key associated with this map marker as string
	 */
	public String getRowKeyString() {
		return m_rowKey;
	}

	/**
	 * Sets the hilite state of the marker.
	 * 
	 * @param hilite
	 */
	public void setHilited(final boolean hilite) {
		m_hilite = hilite;

	}

	/**
	 * @return the hilite state
	 */
	public boolean isHilited() {
		return m_hilite;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void paint(final Graphics g, final Point position, final int radius) {
		if (m_hilite) {
			g.setColor(ColorAttr.HILITE);
		} else {
			g.setColor(m_c);
		}
		m_shape.paintShape(g, position.x, position.y,
				(int) Math.round(m_sizeFactor * DEFAULT_SHAPE_SIZE), m_hilite,
				false);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setLat(final double lat) {
		m_lat = lat;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setLon(final double lon) {
		m_lon = lon;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Coordinate getCoordinate() {
		return new Coordinate(m_lat, m_lon);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getRadius() {
		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public STYLE getMarkerStyle() {
		return STYLE.FIXED;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeExternal(final ObjectOutput out) throws IOException {
		out.writeLong(EXTERANLIZATION_KEY_V0);
		out.writeDouble(m_lat);
		out.writeDouble(m_lon);
		out.writeInt(m_c.getRGB());
		out.writeDouble(m_sizeFactor);
		out.writeUTF(m_info);
		out.writeUTF(m_shape.toString());
		out.writeUTF(m_rowKey);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void readExternal(final ObjectInput in) throws IOException,
			ClassNotFoundException {
		// backward compatibility
		long ext_key = in.readLong();
		if (ext_key == EXTERANLIZATION_KEY_V0) {
			m_lat = in.readDouble();
			m_lon = in.readDouble();
			m_c = new Color(in.readInt());
			m_sizeFactor = in.readDouble();
			m_info = in.readUTF();
			m_shape = ShapeFactory.getShape(in.readUTF());
			m_rowKey = in.readUTF();
		} else {
			throw new IOException("Can't read KnimeMapMarkerPoint!");
		}
	}
}
