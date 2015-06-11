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
package de.bund.bfr.knime.gis.views.locationtolocationvisualizer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObject;

import de.bund.bfr.knime.gis.views.VisualizerNodeDialog;
import de.bund.bfr.knime.gis.views.canvas.Canvas;
import de.bund.bfr.knime.gis.views.canvas.CanvasListener;
import de.bund.bfr.knime.gis.views.canvas.GraphCanvas;
import de.bund.bfr.knime.gis.views.canvas.LocationCanvas;
import de.bund.bfr.knime.gis.views.canvas.Naming;
import de.bund.bfr.knime.gis.views.canvas.element.LocationNode;

/**
 * <code>NodeDialog</code> for the "LocationToLocationVisualizer" Node.
 * 
 * @author Christian Thoens
 */
public class LocationToLocationVisualizerNodeDialog extends VisualizerNodeDialog implements
		CanvasListener {

	private JSplitPane splitPane;
	private GraphCanvas graphCanvas;
	private Canvas<LocationNode> gisCanvas;

	private BufferedDataTable shapeTable;
	private BufferedDataTable nodeTable;
	private BufferedDataTable edgeTable;

	private LocationToLocationVisualizerSettings set;

	/**
	 * New pane for configuring the LocationToLocationVisualizer node.
	 */
	protected LocationToLocationVisualizerNodeDialog() {
		set = new LocationToLocationVisualizerSettings();
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings, PortObject[] input)
			throws NotConfigurableException {
		shapeTable = (BufferedDataTable) input[0];
		nodeTable = (BufferedDataTable) input[1];
		edgeTable = (BufferedDataTable) input[2];
		set.loadSettings(settings);

		updateSplitPane(false);
		resized = set.getGraphSettings().getCanvasSize() == null
				|| set.getGisSettings().getCanvasSize() == null;
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		set.getGraphSettings().setFromCanvas(graphCanvas, resized);
		set.getGisSettings().setFromCanvas(gisCanvas, resized);
		set.saveSettings(settings);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		LocationToLocationVisualizerInputDialog dialog = new LocationToLocationVisualizerInputDialog(
				(JButton) e.getSource(), shapeTable.getSpec(), nodeTable.getSpec(),
				edgeTable.getSpec(), set);

		dialog.setVisible(true);

		if (dialog.isApproved()) {
			set.getGraphSettings().setFromCanvas(graphCanvas, resized);
			set.getGisSettings().setFromCanvas(gisCanvas, resized);
			updateSplitPane(true);
		}
	}

	@Override
	public void nodeSelectionChanged(Canvas<?> source) {
		if (source == graphCanvas) {
			gisCanvas.removeCanvasListener(this);
			gisCanvas.setSelectedNodeIds(graphCanvas.getSelectedNodeIds());
			gisCanvas.addCanvasListener(this);
		} else if (source == gisCanvas) {
			graphCanvas.removeCanvasListener(this);
			graphCanvas.setSelectedNodeIds(gisCanvas.getSelectedNodeIds());
			graphCanvas.addCanvasListener(this);
		}
	}

	@Override
	public void edgeSelectionChanged(Canvas<?> source) {
		if (source == graphCanvas) {
			gisCanvas.removeCanvasListener(this);
			gisCanvas.setSelectedEdgeIds(graphCanvas.getSelectedEdgeIds());
			gisCanvas.addCanvasListener(this);
		} else if (source == gisCanvas) {
			graphCanvas.removeCanvasListener(this);
			graphCanvas.setSelectedEdgeIds(gisCanvas.getSelectedEdgeIds());
			graphCanvas.addCanvasListener(this);
		}
	}

	@Override
	public void nodeHighlightingChanged(Canvas<?> source) {
		if (source == graphCanvas) {
			gisCanvas.removeCanvasListener(this);
			gisCanvas.setNodeHighlightConditions(graphCanvas.getNodeHighlightConditions());
			gisCanvas.addCanvasListener(this);
		} else if (source == gisCanvas) {
			graphCanvas.removeCanvasListener(this);
			graphCanvas.setNodeHighlightConditions(gisCanvas.getNodeHighlightConditions());
			graphCanvas.addCanvasListener(this);
		}
	}

	@Override
	public void edgeHighlightingChanged(Canvas<?> source) {
		if (source == graphCanvas) {
			gisCanvas.removeCanvasListener(this);
			gisCanvas.setEdgeHighlightConditions(graphCanvas.getEdgeHighlightConditions());
			gisCanvas.addCanvasListener(this);
		} else if (source == gisCanvas) {
			graphCanvas.removeCanvasListener(this);
			graphCanvas.setEdgeHighlightConditions(gisCanvas.getEdgeHighlightConditions());
			graphCanvas.addCanvasListener(this);
		}
	}

	@Override
	public void edgeJoinChanged(Canvas<?> source) {
		if (source == graphCanvas) {
			gisCanvas.removeCanvasListener(this);
			gisCanvas.setJoinEdges(graphCanvas.isJoinEdges());
			gisCanvas.addCanvasListener(this);
		} else if (source == gisCanvas) {
			graphCanvas.removeCanvasListener(this);
			graphCanvas.setJoinEdges(gisCanvas.isJoinEdges());
			graphCanvas.addCanvasListener(this);
		}
	}

	@Override
	public void skipEdgelessChanged(Canvas<?> source) {
		if (source == graphCanvas) {
			gisCanvas.removeCanvasListener(this);
			gisCanvas.setSkipEdgelessNodes(graphCanvas.isSkipEdgelessNodes());
			gisCanvas.addCanvasListener(this);
		} else if (source == gisCanvas) {
			graphCanvas.removeCanvasListener(this);
			graphCanvas.setSkipEdgelessNodes(gisCanvas.isSkipEdgelessNodes());
			graphCanvas.addCanvasListener(this);
		}
	}

	@Override
	public void showEdgesInMetaNodeChanged(Canvas<?> source) {
		if (source == graphCanvas) {
			gisCanvas.removeCanvasListener(this);
			gisCanvas.setShowEdgesInMetaNode(graphCanvas.isShowEdgesInMetaNode());
			gisCanvas.addCanvasListener(this);
		} else if (source == gisCanvas) {
			graphCanvas.removeCanvasListener(this);
			graphCanvas.setShowEdgesInMetaNode(gisCanvas.isShowEdgesInMetaNode());
			graphCanvas.addCanvasListener(this);
		}
	}

	@Override
	public void collapsedNodesChanged(Canvas<?> source) {
		if (source == graphCanvas) {
			gisCanvas.removeCanvasListener(this);
			gisCanvas.setCollapsedNodes(graphCanvas.getCollapsedNodes());
			gisCanvas.addCanvasListener(this);
		} else if (source == gisCanvas) {
			graphCanvas.removeCanvasListener(this);
			graphCanvas.setCollapsedNodes(gisCanvas.getCollapsedNodes());
			graphCanvas.addCanvasListener(this);
		}
	}

	private void updateSplitPane(boolean showWarning) {
		if (splitPane != null) {
			panel.remove(splitPane);
		}

		LocationToLocationVisualizerCanvasCreator creator = new LocationToLocationVisualizerCanvasCreator(
				shapeTable, nodeTable, edgeTable, set);

		try {
			graphCanvas = creator.createGraphCanvas();
			gisCanvas = creator.createLocationCanvas();
			graphCanvas.addCanvasListener(this);
			gisCanvas.addCanvasListener(this);
		} catch (InvalidSettingsException e) {
			graphCanvas = new GraphCanvas(false, Naming.DEFAULT_NAMING);
			graphCanvas.setCanvasSize(new Dimension(400, 600));
			gisCanvas = new LocationCanvas(true, Naming.DEFAULT_NAMING);
			gisCanvas.setCanvasSize(new Dimension(400, 600));

			if (showWarning) {
				JOptionPane.showMessageDialog(panel, e.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}

		graphCanvas.addComponentListener(this);
		gisCanvas.addComponentListener(this);
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, graphCanvas, gisCanvas);
		splitPane.setResizeWeight(0.5);
		panel.add(splitPane, BorderLayout.CENTER);
		panel.revalidate();
	}
}
