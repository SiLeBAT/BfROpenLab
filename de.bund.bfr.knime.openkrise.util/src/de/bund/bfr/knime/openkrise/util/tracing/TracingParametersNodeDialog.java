/*******************************************************************************
 * Copyright (c) 2014 Federal Institute for Risk Assessment (BfR), Germany 
 * 
 * Developers and contributors are 
 * Christian Thoens (BfR)
 * Armin A. Weiser (BfR)
 * Matthias Filter (BfR)
 * Alexander Falenski (BfR)
 * Annemarie Kaesbohrer (BfR)
 * Bernd Appel (BfR)
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
 ******************************************************************************/
package de.bund.bfr.knime.openkrise.util.tracing;

import java.util.List;
import java.util.Map;

import javax.swing.JCheckBox;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.DataAwareNodeDialogPane;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;

import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.GraphNode;
import de.bund.bfr.knime.openkrise.TracingUtils;

/**
 * <code>NodeDialog</code> for the "TracingVisualizer" Node.
 * 
 * @author Christian Thoens
 */
public class TracingParametersNodeDialog extends DataAwareNodeDialogPane {

	private TracingParametersSettings set;

	private TableInputPanel<Double> nodeWeightPanel;
	private TableInputPanel<Double> edgeWeightPanel;
	private TableInputPanel<Boolean> nodeContaminationPanel;
	private TableInputPanel<Boolean> edgeContaminationPanel;
	private TableInputPanel<Boolean> nodeFilterPanel;
	private TableInputPanel<Boolean> edgeFilterPanel;
	private JCheckBox enforceTempBox;

	/**
	 * New pane for configuring the TracingVisualizer node.
	 */
	protected TracingParametersNodeDialog() {
		set = new TracingParametersSettings();
		nodeWeightPanel = new TableInputPanel<>(Double.class,
				TableInputPanel.Type.NODE);
		edgeWeightPanel = new TableInputPanel<>(Double.class,
				TableInputPanel.Type.EDGE);
		nodeContaminationPanel = new TableInputPanel<>(Boolean.class,
				TableInputPanel.Type.NODE);
		edgeContaminationPanel = new TableInputPanel<>(Boolean.class,
				TableInputPanel.Type.EDGE);
		nodeFilterPanel = new TableInputPanel<>(Boolean.class,
				TableInputPanel.Type.NODE);
		edgeFilterPanel = new TableInputPanel<>(Boolean.class,
				TableInputPanel.Type.EDGE);
		enforceTempBox = new JCheckBox("Enforce Temporal Order");

		addTab("Options",
				UI.createNorthPanel(UI.createHorizontalPanel(enforceTempBox)));
		addTab(TracingUtils.NAMING.Node() + " Weights", nodeWeightPanel);
		addTab(TracingUtils.NAMING.Edge() + " Weights", edgeWeightPanel);
		addTab(TracingUtils.NAMING.Node() + " Cross Contaminations",
				nodeContaminationPanel);
		addTab(TracingUtils.NAMING.Edge() + " Cross Contaminations",
				edgeContaminationPanel);
		addTab("Observed " + TracingUtils.NAMING.Nodes(), nodeFilterPanel);
		addTab("Observed " + TracingUtils.NAMING.Edges(), edgeFilterPanel);
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings,
			BufferedDataTable[] input) throws NotConfigurableException {
		BufferedDataTable nodeTable = input[0];
		BufferedDataTable edgeTable = input[1];

		set.loadSettings(settings);

		Map<String, Class<?>> nodeProperties = TracingUtils
				.getTableColumns(nodeTable.getSpec());
		Map<String, Class<?>> edgeProperties = TracingUtils
				.getTableColumns(edgeTable.getSpec());
		Map<String, GraphNode> nodes;
		List<Edge<GraphNode>> edges;

		try {
			nodes = TracingUtils.readGraphNodes(nodeTable, nodeProperties);
			edges = TracingUtils.readEdges(edgeTable, edgeProperties, nodes);
		} catch (InvalidSettingsException e) {
			throw new NotConfigurableException(e.getMessage());
		}

		nodeWeightPanel.update(nodes.values(), nodeProperties,
				set.getNodeWeights(), set.getNodeWeightCondition(),
				set.getNodeWeightConditionValue());
		edgeWeightPanel
				.update(edges, edgeProperties, set.getEdgeWeights(),
						set.getEdgeWeightCondition(),
						set.getEdgeWeightConditionValue());
		nodeContaminationPanel.update(nodes.values(), nodeProperties,
				set.getNodeCrossContaminations(),
				set.getNodeContaminationCondition(),
				set.getNodeContaminationConditionValue());
		edgeContaminationPanel.update(edges, edgeProperties,
				set.getEdgeCrossContaminations(),
				set.getEdgeContaminationCondition(),
				set.getEdgeContaminationConditionValue());
		nodeFilterPanel.update(nodes.values(), nodeProperties,
				set.getObservedNodes(), set.getObservedNodesCondition(),
				set.getObservedNodesConditionValue());
		edgeFilterPanel.update(edges, edgeProperties, set.getObservedEdges(),
				set.getObservedEdgesCondition(),
				set.getObservedEdgesConditionValue());
		enforceTempBox.setSelected(set.isEnforeTemporalOrder());
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings)
			throws InvalidSettingsException {
		set.setNodeWeights(nodeWeightPanel.getValues());
		set.setNodeWeightCondition(nodeWeightPanel.getCondition());
		set.setNodeWeightConditionValue(nodeWeightPanel.getValueForAll());

		set.setEdgeWeights(edgeWeightPanel.getValues());
		set.setEdgeWeightCondition(edgeWeightPanel.getCondition());
		set.setEdgeWeightConditionValue(edgeWeightPanel.getValueForAll());

		set.setNodeCrossContaminations(nodeContaminationPanel.getValues());
		set.setNodeContaminationCondition(nodeContaminationPanel.getCondition());
		set.setNodeContaminationConditionValue(nodeContaminationPanel
				.getValueForAll());

		set.setEdgeCrossContaminations(edgeContaminationPanel.getValues());
		set.setEdgeContaminationCondition(edgeContaminationPanel.getCondition());
		set.setEdgeContaminationConditionValue(edgeContaminationPanel
				.getValueForAll());

		set.setObservedNodes(nodeFilterPanel.getValues());
		set.setObservedNodesCondition(nodeFilterPanel.getCondition());
		set.setObservedNodesConditionValue(nodeFilterPanel.getValueForAll());

		set.setObservedEdges(edgeFilterPanel.getValues());
		set.setObservedEdgesCondition(edgeFilterPanel.getCondition());
		set.setObservedEdgesConditionValue(edgeFilterPanel.getValueForAll());

		set.setEnforeTemporalOrder(enforceTempBox.isSelected());

		set.saveSettings(settings);
	}
}
