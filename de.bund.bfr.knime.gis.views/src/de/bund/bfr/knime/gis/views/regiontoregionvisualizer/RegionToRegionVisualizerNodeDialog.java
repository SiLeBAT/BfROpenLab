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
package de.bund.bfr.knime.gis.views.regiontoregionvisualizer;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JSplitPane;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObject;

import de.bund.bfr.knime.gis.views.VisualizerNodeDialog;
import de.bund.bfr.knime.gis.views.canvas.CanvasListener;
import de.bund.bfr.knime.gis.views.canvas.GisCanvas;
import de.bund.bfr.knime.gis.views.canvas.GraphCanvas;
import de.bund.bfr.knime.gis.views.canvas.ICanvas;
import de.bund.bfr.knime.gis.views.canvas.RegionCanvas;
import de.bund.bfr.knime.gis.views.canvas.element.RegionNode;
import de.bund.bfr.knime.gis.views.canvas.util.Naming;
import de.bund.bfr.knime.ui.Dialogs;

/**
 * <code>NodeDialog</code> for the "RegionToRegionVisualizer" Node.
 * 
 * @author Christian Thoens
 */
public class RegionToRegionVisualizerNodeDialog extends VisualizerNodeDialog implements CanvasListener {

	private JSplitPane splitPane;
	private GraphCanvas graphCanvas;
	private GisCanvas<RegionNode> gisCanvas;

	private BufferedDataTable shapeTable;
	private BufferedDataTable nodeTable;
	private BufferedDataTable edgeTable;

	private RegionToRegionVisualizerSettings set;

	/**
	 * New pane for configuring the RegionToRegionVisualizer node.
	 */
	protected RegionToRegionVisualizerNodeDialog() {
		set = new RegionToRegionVisualizerSettings();
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings, PortObject[] input) throws NotConfigurableException {
		shapeTable = (BufferedDataTable) input[0];
		nodeTable = (BufferedDataTable) input[1];
		edgeTable = (BufferedDataTable) input[2];
		set.loadSettings(settings);

		updateSplitPane(false);
		resized = false;
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		set.getGraphSettings().setFromCanvas(graphCanvas, resized);
		set.getGisSettings().setFromCanvas(gisCanvas, resized);
		set.saveSettings(settings);
	}

	@Override
	protected void inputButtonPressed() {
		RegionToRegionVisualizerInputDialog dialog = new RegionToRegionVisualizerInputDialog(inputButton,
				shapeTable.getSpec(), nodeTable.getSpec(), edgeTable.getSpec(), set);

		dialog.setVisible(true);

		if (dialog.isApproved()) {
			set.getGraphSettings().setFromCanvas(graphCanvas, resized);
			set.getGisSettings().setFromCanvas(gisCanvas, resized);
			updateSplitPane(true);
		}
	}

	@Override
	public void transformChanged(ICanvas<?> source) {
	}

	@Override
	public void selectionChanged(ICanvas<?> source) {
		if (source == graphCanvas) {
			gisCanvas.removeCanvasListener(this);
			gisCanvas.setSelectedNodeIds(
					RegionToRegionUtils.getSelectedGisNodeIds(gisCanvas.getNodes(), graphCanvas.getSelectedNodes()));
			gisCanvas.setSelectedEdgeIds(RegionToRegionUtils.getSelectedGisEdgeIds(gisCanvas.getEdges(),
					graphCanvas.getSelectedEdges(), graphCanvas.isJoinEdges()));
			gisCanvas.addCanvasListener(this);
			gisCanvas.repaint();
		} else if (source == gisCanvas) {
			graphCanvas.removeCanvasListener(this);
			graphCanvas.setSelectedNodeIds(
					RegionToRegionUtils.getSelectedGraphNodeIds(graphCanvas.getNodes(), gisCanvas.getSelectedNodes()));
			graphCanvas.setSelectedEdgeIds(RegionToRegionUtils.getSelectedGraphEdgeIds(graphCanvas.getEdges(),
					gisCanvas.getSelectedEdges(), graphCanvas.isJoinEdges()));
			graphCanvas.addCanvasListener(this);
		}
	}

	@Override
	public void nodeSelectionChanged(ICanvas<?> source) {
		if (source == graphCanvas) {
			gisCanvas.removeCanvasListener(this);
			gisCanvas.setSelectedNodeIds(
					RegionToRegionUtils.getSelectedGisNodeIds(gisCanvas.getNodes(), graphCanvas.getSelectedNodes()));
			gisCanvas.addCanvasListener(this);
			gisCanvas.repaint();
		} else if (source == gisCanvas) {
			graphCanvas.removeCanvasListener(this);
			graphCanvas.setSelectedNodeIds(
					RegionToRegionUtils.getSelectedGraphNodeIds(graphCanvas.getNodes(), gisCanvas.getSelectedNodes()));
			graphCanvas.addCanvasListener(this);
		}
	}

	@Override
	public void edgeSelectionChanged(ICanvas<?> source) {
		if (source == graphCanvas) {
			gisCanvas.removeCanvasListener(this);
			gisCanvas.setSelectedEdgeIds(RegionToRegionUtils.getSelectedGisEdgeIds(gisCanvas.getEdges(),
					graphCanvas.getSelectedEdges(), graphCanvas.isJoinEdges()));
			gisCanvas.addCanvasListener(this);
			gisCanvas.repaint();
		} else if (source == gisCanvas) {
			graphCanvas.removeCanvasListener(this);
			graphCanvas.setSelectedEdgeIds(RegionToRegionUtils.getSelectedGraphEdgeIds(graphCanvas.getEdges(),
					gisCanvas.getSelectedEdges(), graphCanvas.isJoinEdges()));
			graphCanvas.addCanvasListener(this);
		}
	}

	@Override
	public void nodeHighlightingChanged(ICanvas<?> source) {
	}

	@Override
	public void edgeHighlightingChanged(ICanvas<?> source) {
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
	public void highlightingChanged(ICanvas<?> source) {
		nodeHighlightingChanged(source);
		edgeHighlightingChanged(source);
	}

	@Override
	public void layoutProcessFinished(ICanvas<?> source) {
	}

	@Override
	public void nodePositionsChanged(ICanvas<?> source) {
	}

	@Override
	public void edgeJoinChanged(ICanvas<?> source) {
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
	public void skipEdgelessChanged(ICanvas<?> source) {
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
	public void showEdgesInMetaNodeChanged(ICanvas<?> source) {
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
	public void arrowInMiddleChanged(ICanvas<?> source) {
	}

	@Override
	public void showLegendChanged(ICanvas<?> source) {
	}

	@Override
	public void collapsedNodesChanged(ICanvas<?> source) {
	}

	@Override
	public void collapsedNodesAndPickingChanged(ICanvas<?> source) {
	}

	@Override
	public void nodeSizeChanged(ICanvas<?> source) {
	}

	@Override
	public void edgeThicknessChanged(ICanvas<?> source) {
	}

	@Override
	public void fontChanged(ICanvas<?> source) {
	}

	@Override
	public void labelChanged(ICanvas<?> source) {
	}

	@Override
	public void borderAlphaChanged(ICanvas<?> source) {
	}

	@Override
	public void avoidOverlayChanged(ICanvas<?> source) {
	}

	private void updateSplitPane(boolean showWarning) {
		if (splitPane != null) {
			panel.remove(splitPane);
		}

		RegionToRegionVisualizerCanvasCreator creator = new RegionToRegionVisualizerCanvasCreator(shapeTable, nodeTable,
				edgeTable, set);

		try {
			graphCanvas = creator.createGraphCanvas();
			gisCanvas = creator.createGisCanvas(graphCanvas);
			graphCanvas.addCanvasListener(this);
			gisCanvas.addCanvasListener(this);

			if (showWarning && !creator.getNonExistingRegions().isEmpty()) {
				Dialogs.showWarningMessage(panel, "Some regions from the table are not contained in the shapefile",
						"Warning");
			}
		} catch (NotConfigurableException e) {
			graphCanvas = new GraphCanvas(false, Naming.DEFAULT_NAMING);
			graphCanvas.setCanvasSize(new Dimension(400, 600));
			gisCanvas = new RegionCanvas(true, Naming.DEFAULT_NAMING);
			gisCanvas.setCanvasSize(new Dimension(400, 600));

			if (showWarning) {
				Dialogs.showErrorMessage(panel, e.getMessage(), "Error");
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
