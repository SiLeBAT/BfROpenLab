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
package de.bund.bfr.knime.openkrise.views.tracingview;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import de.bund.bfr.knime.NodeSettings;
import de.bund.bfr.knime.XmlConverter;
import de.bund.bfr.knime.gis.views.canvas.GraphCanvas;
import de.bund.bfr.knime.gis.views.canvas.util.Transform;
//import de.bund.bfr.knime.openkrise.util.json.JsonFormat;
import de.bund.bfr.knime.openkrise.util.json.JsonFormat.TracingViewSettings.View;
import de.bund.bfr.knime.openkrise.views.Activator;


public class GraphSettings extends NodeSettings {

	//private static Logger logger =  Logger.getLogger("de.bund.bfr");
	
	protected static final XmlConverter SERIALIZER = new XmlConverter(Activator.class.getClassLoader());

	private static final String CFG_SCALE_X = "GraphScaleX";
	private static final String CFG_SCALE_Y = "GraphScaleY";
	private static final String CFG_TRANSLATION_X = "GraphTranslationX";
	private static final String CFG_TRANSLATION_Y = "GraphTranslationY";
	private static final String CFG_NODE_POSITIONS = "GraphNodePositions";
	private static final String CFG_NODE_SIZE = "GraphNodeSize";
	private static final String CFG_NODE_MAX_SIZE = "GraphNodeMaxSize";
	private static final String CFG_EDGE_THICKNESS = "GraphEdgeThickness";
	private static final String CFG_EDGE_MAX_THICKNESS = "GraphEdgeMaxThickness";
	private static final String CFG_FONT_SIZE = "GraphTextSize";
	private static final String CFG_FONT_BOLD = "GraphTextBold";

	private Transform transform;
	private Map<String, Point2D> nodePositions;
	private int nodeSize;
	private Integer nodeMaxSize;
	private int edgeThickness;
	private Integer edgeMaxThickness;
	private int fontSize;
	private boolean fontBold;

	public GraphSettings() {
		transform = Transform.INVALID_TRANSFORM;
		nodePositions = null;
		nodeSize = 10;
		nodeMaxSize = null;
		edgeThickness = 1;
		edgeMaxThickness = null;
		fontSize = 12;
		fontBold = false;
	}

public GraphSettings(GraphSettings set) {
      this.transform = set.transform; //   transform = Transform.INVALID_TRANSFORM;
      this.nodePositions = null;
      this.nodeSize = set.nodeSize;
      this.nodeMaxSize = set.nodeMaxSize;
      this.edgeThickness = set.edgeThickness;
      this.edgeMaxThickness = set.edgeMaxThickness;
      this.fontSize = set.fontSize;
      this.fontBold = set.fontBold;
  }
  
	@Override
	public void loadSettings(NodeSettingsRO settings) {
		this.loadSettings(settings, "");
	}
	
	/*
	 * loads graph settings 
	 * (prefix is non empty for explosion graph settings)
	 */
	@SuppressWarnings("unchecked")
	protected void loadSettings(NodeSettingsRO settings, String prefix) {
		try {
			transform = new Transform(settings.getDouble(prefix + CFG_SCALE_X), settings.getDouble(prefix + CFG_SCALE_Y),
					settings.getDouble(prefix + CFG_TRANSLATION_X), settings.getDouble(prefix + CFG_TRANSLATION_Y));
		} catch (InvalidSettingsException e) {
		}

		try {
			nodePositions = (Map<String, Point2D>) SERIALIZER.fromXml(settings.getString(prefix + CFG_NODE_POSITIONS));
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
	}

	
	
	@Override
	public void saveSettings(NodeSettingsWO settings) {
		this.saveSettings(settings, "");
	}
	
	public void saveSettings(JsonConverter.JsonBuilder jsonBuilder) {
      jsonBuilder.setGraphSettings(transform.getScaleX(), transform.getScaleY(), transform.getTranslationX(), transform.getTranslationY(),
          this.edgeThickness, this.edgeMaxThickness, this.fontSize, this.fontBold, this.nodeSize, this.nodeMaxSize, this.nodePositions);
    }
	
	public void saveSettings(JsonConverter.JsonBuilder jsonBuilder, int index) {
      jsonBuilder.setExplosionGraphSettings(index, transform.getScaleX(), transform.getScaleY(), transform.getTranslationX(), transform.getTranslationY(),
          this.edgeThickness, this.edgeMaxThickness, this.fontSize, this.fontBold, this.nodeSize, this.nodeMaxSize, this.nodePositions);
    }

	public void setFromCanvas(GraphCanvas canvas) {
		transform = canvas.getTransform();
		nodeSize = canvas.getOptionsPanel().getNodeSize();
		nodeMaxSize = canvas.getOptionsPanel().getNodeMaxSize();
		edgeThickness = canvas.getOptionsPanel().getEdgeThickness();
		edgeMaxThickness = canvas.getOptionsPanel().getEdgeMaxThickness();
		fontSize = canvas.getOptionsPanel().getFontSize();
		fontBold = canvas.getOptionsPanel().isFontBold();
		nodePositions = canvas.getNodePositions();
	}

	public void setToCanvas(GraphCanvas canvas) {
		canvas.getOptionsPanel().setNodeSize(nodeSize);
		canvas.getOptionsPanel().setNodeMaxSize(nodeMaxSize);
		canvas.getOptionsPanel().setEdgeThickness(edgeThickness);
		canvas.getOptionsPanel().setEdgeMaxThickness(edgeMaxThickness);
		canvas.getOptionsPanel().setFontSize(fontSize);
		canvas.getOptionsPanel().setFontBold(fontBold);

		if (transform.isValid()) {
			canvas.setTransform(transform);
		}

		if (nodePositions != null) {
			canvas.setNodePositions(nodePositions);
		} else {
			canvas.initLayout();
		}
	}
	
	/*
	 * saves the graph settings
	 * (prefix is non empty for explosion graph settings)
	 */
	public void saveSettings(NodeSettingsWO settings, String prefix) {
		settings.addDouble(prefix + CFG_SCALE_X, transform.getScaleX());
		settings.addDouble(prefix + CFG_SCALE_Y, transform.getScaleY());
		settings.addDouble(prefix + CFG_TRANSLATION_X, transform.getTranslationX());
		settings.addDouble(prefix + CFG_TRANSLATION_Y, transform.getTranslationY());
		settings.addString(prefix + CFG_NODE_POSITIONS, SERIALIZER.toXml(nodePositions));
		settings.addInt(prefix + CFG_NODE_SIZE, nodeSize);
		settings.addInt(prefix + CFG_NODE_MAX_SIZE, nullToMinusOne(nodeMaxSize));
		settings.addInt(prefix + CFG_EDGE_THICKNESS, edgeThickness);
		settings.addInt(prefix + CFG_EDGE_MAX_THICKNESS, nullToMinusOne(edgeMaxThickness));
		settings.addInt(prefix + CFG_FONT_SIZE, fontSize);
		settings.addBoolean(prefix + CFG_FONT_BOLD, fontBold);
	}
	
	public void loadSettings(View.GraphSettings graphView) {
	  this.nodePositions = new HashMap<>();
	  for(View.NodePosition nodePosition: graphView.node.positions) 
	    this.nodePositions.put(nodePosition.id, new Point2D.Double(nodePosition.position.x,nodePosition.position.y));
	  
	  this.transform = new Transform(
	      graphView.transformation.scale.x, graphView.transformation.scale.y,
	      graphView.transformation.translation.x, graphView.transformation.translation.y);
	  
	  this.edgeThickness = graphView.edge.minWidth;
	  this.edgeMaxThickness = graphView.edge.maxWidth;
	  this.fontSize = graphView.text.fontSize;
	  this.fontBold = graphView.text.fontBold;
	  this.nodeSize = graphView.node.minSize;
	  this.nodeMaxSize = graphView.node.maxSize;
	}
}
