/*******************************************************************************
 * Copyright (c) 2018 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.openkrise.views.tracingview;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import de.bund.bfr.knime.NodeSettings;
import de.bund.bfr.knime.XmlConverter;
import de.bund.bfr.knime.gis.views.canvas.IGisCanvas;
import de.bund.bfr.knime.gis.views.canvas.util.Transform;
import de.bund.bfr.knime.openkrise.views.Activator;

public class GisSettings extends NodeSettings {

	protected static final XmlConverter SERIALIZER = new XmlConverter(Activator.class.getClassLoader());

	private static final String CFG_SCALE_X = "OsmScaleX";
	private static final String CFG_SCALE_Y = "OsmScaleY";
	private static final String CFG_TRANSLATION_X = "OsmTranslationX";
	private static final String CFG_TRANSLATION_Y = "OsmTranslationY";
	private static final String CFG_NODE_SIZE = "GisLocationSize";
	private static final String CFG_NODE_MAX_SIZE = "GisNodeMaxSize";
	private static final String CFG_EDGE_THICKNESS = "GisEdgeThickness";
	private static final String CFG_EDGE_MAX_THICKNESS = "GisEdgeMaxThickness";
	private static final String CFG_FONT_SIZE = "GisTextSize";
	private static final String CFG_FONT_BOLD = "GisTextBold";
	private static final String CFG_BORDER_ALPHA = "GisBorderAlpha";
	private static final String CFG_AVOID_OVERLAY = "GisAvoidOverlay";

	private Transform transform;
	private int nodeSize;
	private Integer nodeMaxSize;
	private int edgeThickness;
	private Integer edgeMaxThickness;
	private int fontSize;
	private boolean fontBold;
	private int borderAlpha;
	private boolean avoidOverlay;

	public GisSettings() {
		transform = Transform.INVALID_TRANSFORM;
		nodeSize = 10;
		nodeMaxSize = null;
		edgeThickness = 1;
		edgeMaxThickness = null;
		fontSize = 12;
		fontBold = false;
		borderAlpha = 255;
		avoidOverlay = false;
	}

	@Override
	public void loadSettings(NodeSettingsRO settings) {
		this.loadSettings(settings, "");
	}

	@Override
	public void saveSettings(NodeSettingsWO settings) {
		this.saveSettings(settings, "");
	}

	public void setFromCanvas(IGisCanvas<?> canvas) {
		transform = canvas.getTransform();
		nodeSize = canvas.getOptionsPanel().getNodeSize();
		nodeMaxSize = canvas.getOptionsPanel().getNodeMaxSize();
		edgeThickness = canvas.getOptionsPanel().getEdgeThickness();
		edgeMaxThickness = canvas.getOptionsPanel().getEdgeMaxThickness();
		fontSize = canvas.getOptionsPanel().getFontSize();
		fontBold = canvas.getOptionsPanel().isFontBold();
		borderAlpha = canvas.getOptionsPanel().getBorderAlpha();
		avoidOverlay = canvas.getOptionsPanel().isAvoidOverlay();
	}

	public void setToCanvas(IGisCanvas<?> canvas) {
		canvas.getOptionsPanel().setNodeSize(nodeSize);
		canvas.getOptionsPanel().setNodeMaxSize(nodeMaxSize);
		canvas.getOptionsPanel().setEdgeThickness(edgeThickness);
		canvas.getOptionsPanel().setEdgeMaxThickness(edgeMaxThickness);
		canvas.getOptionsPanel().setFontSize(fontSize);
		canvas.getOptionsPanel().setFontBold(fontBold);
		canvas.getOptionsPanel().setBorderAlpha(borderAlpha);
		canvas.getOptionsPanel().setAvoidOverlay(avoidOverlay);

		if (transform.isValid()) {
			canvas.setTransform(transform);
		}
	}

	public void loadSettings(NodeSettingsRO settings, String prefix) {
		// TODO Auto-generated method stub
		try {
			transform = new Transform(settings.getDouble(prefix + CFG_SCALE_X), settings.getDouble(prefix + CFG_SCALE_Y),
					settings.getDouble(prefix + CFG_TRANSLATION_X), settings.getDouble(prefix + CFG_TRANSLATION_Y));
		} catch (InvalidSettingsException e) {
		}

		try {
			nodeSize = settings.getInt(prefix + CFG_NODE_SIZE);
		} catch (InvalidSettingsException e) {
		}

		try {
			nodeMaxSize = minusOneToNull(settings.getInt(prefix + CFG_NODE_MAX_SIZE));
		} catch (InvalidSettingsException e) {
		}

		try {
			edgeThickness = settings.getInt(prefix + CFG_EDGE_THICKNESS);
		} catch (InvalidSettingsException e) {
		}

		try {
			edgeMaxThickness = minusOneToNull(settings.getInt(prefix + CFG_EDGE_MAX_THICKNESS));
		} catch (InvalidSettingsException e) {
		}

		try {
			fontSize = settings.getInt(prefix + CFG_FONT_SIZE);
		} catch (InvalidSettingsException e) {
		}

		try {
			fontBold = settings.getBoolean(prefix + CFG_FONT_BOLD);
		} catch (InvalidSettingsException e) {
		}

		try {
			borderAlpha = settings.getInt(prefix + CFG_BORDER_ALPHA);
		} catch (InvalidSettingsException e) {
		}

		try {
			avoidOverlay = settings.getBoolean(prefix + CFG_AVOID_OVERLAY);
		} catch (InvalidSettingsException e) {
		}
	}

	public void saveSettings(NodeSettingsWO settings, String prefix) {
		// TODO Auto-generated method stub
		settings.addDouble(prefix + CFG_SCALE_X, transform.getScaleX());
		settings.addDouble(prefix + CFG_SCALE_Y, transform.getScaleY());
		settings.addDouble(prefix + CFG_TRANSLATION_X, transform.getTranslationX());
		settings.addDouble(prefix + CFG_TRANSLATION_Y, transform.getTranslationY());
		settings.addInt(prefix + CFG_NODE_SIZE, nodeSize);
		settings.addInt(prefix + CFG_NODE_MAX_SIZE, nullToMinusOne(nodeMaxSize));
		settings.addInt(prefix + CFG_EDGE_THICKNESS, edgeThickness);
		settings.addInt(prefix + CFG_EDGE_MAX_THICKNESS, nullToMinusOne(edgeMaxThickness));
		settings.addInt(prefix + CFG_FONT_SIZE, fontSize);
		settings.addBoolean(prefix + CFG_FONT_BOLD, fontBold);
		settings.addInt(prefix + CFG_BORDER_ALPHA, borderAlpha);
		settings.addBoolean(prefix + CFG_AVOID_OVERLAY, avoidOverlay);
	}
}
