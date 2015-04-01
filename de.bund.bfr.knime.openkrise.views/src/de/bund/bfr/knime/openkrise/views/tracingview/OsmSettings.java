/*******************************************************************************
 * Copyright (c) 2015 Federal Institute for Risk Assessment (BfR), Germany
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
import de.bund.bfr.knime.gis.views.canvas.LocationOsmCanvas;
import de.bund.bfr.knime.gis.views.canvas.Transform;
import de.bund.bfr.knime.openkrise.views.Activator;

public class OsmSettings extends NodeSettings {

	protected static final XmlConverter SERIALIZER = new XmlConverter(
			Activator.class.getClassLoader());

	private static final String CFG_SCALE_X = "OsmScaleX";
	private static final String CFG_SCALE_Y = "OsmScaleY";
	private static final String CFG_TRANSLATION_X = "OsmTranslationX";
	private static final String CFG_TRANSLATION_Y = "OsmTranslationY";
	private static final String CFG_NODE_SIZE = "OsmLocationSize";
	private static final String CFG_FONT_SIZE = "OsmTextSize";
	private static final String CFG_FONT_BOLD = "OsmTextBold";

	private Transform transform;
	private int nodeSize;
	private int fontSize;
	private boolean fontBold;

	public OsmSettings() {
		transform = Transform.INVALID_TRANSFORM;
		nodeSize = 10;
		fontSize = 12;
		fontBold = false;
	}

	@Override
	public void loadSettings(NodeSettingsRO settings) {
		try {
			transform = new Transform(settings.getDouble(CFG_SCALE_X),
					settings.getDouble(CFG_SCALE_Y),
					settings.getDouble(CFG_TRANSLATION_X),
					settings.getDouble(CFG_TRANSLATION_Y));
		} catch (InvalidSettingsException e) {
		}

		try {
			nodeSize = settings.getInt(CFG_NODE_SIZE);
		} catch (InvalidSettingsException e) {
		}

		try {
			fontSize = settings.getInt(CFG_FONT_SIZE);
		} catch (InvalidSettingsException e) {
		}

		try {
			fontBold = settings.getBoolean(CFG_FONT_BOLD);
		} catch (InvalidSettingsException e) {
		}
	}

	@Override
	public void saveSettings(NodeSettingsWO settings) {
		settings.addDouble(CFG_SCALE_X, transform.getScaleX());
		settings.addDouble(CFG_SCALE_Y, transform.getScaleY());
		settings.addDouble(CFG_TRANSLATION_X, transform.getTranslationX());
		settings.addDouble(CFG_TRANSLATION_Y, transform.getTranslationY());
		settings.addInt(CFG_NODE_SIZE, nodeSize);
		settings.addInt(CFG_FONT_SIZE, fontSize);
		settings.addBoolean(CFG_FONT_BOLD, fontBold);
	}

	public void setFromCanvas(LocationOsmCanvas canvas) {
		transform = canvas.getTransform();
		nodeSize = canvas.getNodeSize();
		fontSize = canvas.getFontSize();
		fontBold = canvas.isFontBold();
	}

	public void setToCanvas(LocationOsmCanvas canvas) {
		canvas.setNodeSize(nodeSize);
		canvas.setFontSize(fontSize);
		canvas.setFontBold(fontBold);

		if (transform.isValid()) {
			canvas.setTransform(transform);
		}
	}

	public Transform getTransform() {
		return transform;
	}

	public void setTransform(Transform transform) {
		this.transform = transform;
	}

	public int getNodeSize() {
		return nodeSize;
	}

	public void setNodeSize(int nodeSize) {
		this.nodeSize = nodeSize;
	}

	public int getFontSize() {
		return fontSize;
	}

	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
	}

	public boolean isFontBold() {
		return fontBold;
	}

	public void setFontBold(boolean fontBold) {
		this.fontBold = fontBold;
	}
}
