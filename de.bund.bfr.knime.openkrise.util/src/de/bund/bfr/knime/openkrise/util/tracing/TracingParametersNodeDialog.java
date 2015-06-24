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
package de.bund.bfr.knime.openkrise.util.tracing;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JCheckBox;

import org.knime.core.data.RowKey;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.DataAwareNodeDialogPane;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;

import de.bund.bfr.knime.NodeDialogWarningThread;
import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.gis.views.canvas.CanvasUtils;
import de.bund.bfr.knime.gis.views.canvas.EdgePropertySchema;
import de.bund.bfr.knime.gis.views.canvas.NodePropertySchema;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.GraphNode;
import de.bund.bfr.knime.openkrise.TracingColumns;
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
	private TableInputPanel<Boolean> nodeKillPanel;
	private TableInputPanel<Boolean> edgeKillPanel;
	private TableInputPanel<Boolean> nodeFilterPanel;
	private TableInputPanel<Boolean> edgeFilterPanel;
	private JCheckBox enforceTempBox;

	/**
	 * New pane for configuring the TracingVisualizer node.
	 */
	protected TracingParametersNodeDialog() {
		set = new TracingParametersSettings();
		nodeWeightPanel = new TableInputPanel<>(Double.class, TableInputPanel.Type.NODE);
		edgeWeightPanel = new TableInputPanel<>(Double.class, TableInputPanel.Type.EDGE);
		nodeContaminationPanel = new TableInputPanel<>(Boolean.class, TableInputPanel.Type.NODE);
		edgeContaminationPanel = new TableInputPanel<>(Boolean.class, TableInputPanel.Type.EDGE);
		nodeKillPanel = new TableInputPanel<>(Boolean.class, TableInputPanel.Type.NODE);
		edgeKillPanel = new TableInputPanel<>(Boolean.class, TableInputPanel.Type.EDGE);
		nodeFilterPanel = new TableInputPanel<>(Boolean.class, TableInputPanel.Type.NODE);
		edgeFilterPanel = new TableInputPanel<>(Boolean.class, TableInputPanel.Type.EDGE);
		enforceTempBox = new JCheckBox("Enforce Temporal Order");

		addTab("Options", UI.createNorthPanel(UI.createHorizontalPanel(enforceTempBox)));
		addTab(TracingUtils.NAMING.Node() + " " + TracingColumns.WEIGHT, nodeWeightPanel);
		addTab(TracingUtils.NAMING.Edge() + " " + TracingColumns.WEIGHT, edgeWeightPanel);
		addTab(TracingUtils.NAMING.Node() + " " + TracingColumns.CROSS_CONTAMINATION,
				nodeContaminationPanel);
		addTab(TracingUtils.NAMING.Edge() + " " + TracingColumns.CROSS_CONTAMINATION,
				edgeContaminationPanel);
		addTab(TracingUtils.NAMING.Node() + " " + TracingColumns.KILL_CONTAMINATION, nodeKillPanel);
		addTab(TracingUtils.NAMING.Edge() + " " + TracingColumns.KILL_CONTAMINATION, edgeKillPanel);
		addTab(TracingColumns.OBSERVED + " " + TracingUtils.NAMING.Nodes(), nodeFilterPanel);
		addTab(TracingColumns.OBSERVED + " " + TracingUtils.NAMING.Edges(), edgeFilterPanel);
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings, BufferedDataTable[] input)
			throws NotConfigurableException {
		BufferedDataTable nodeTable = input[0];
		BufferedDataTable edgeTable = input[1];

		set.loadSettings(settings);

		NodePropertySchema nodeSchema = new NodePropertySchema(
				TracingUtils.getTableColumns(nodeTable.getSpec()), TracingColumns.ID);
		EdgePropertySchema edgeSchema = new EdgePropertySchema(
				TracingUtils.getTableColumns(edgeTable.getSpec()), TracingColumns.ID,
				TracingColumns.FROM, TracingColumns.TO);
		Map<String, GraphNode> nodes = TracingUtils.readGraphNodes(nodeTable, nodeSchema);
		Set<RowKey> skippedEdgeRows = new LinkedHashSet<>();
		List<Edge<GraphNode>> edges = TracingUtils.readEdges(edgeTable, edgeSchema, nodes,
				skippedEdgeRows);

		nodeSchema.getPossibleValues().putAll(CanvasUtils.getPossibleValues(nodes.values()));
		edgeSchema.getPossibleValues().putAll(CanvasUtils.getPossibleValues(edges));

		nodeWeightPanel.update(nodes.values(), nodeSchema, set.getNodeWeights(),
				set.getNodeWeightCondition(), set.getNodeWeightConditionValue());
		edgeWeightPanel.update(edges, edgeSchema, set.getEdgeWeights(),
				set.getEdgeWeightCondition(), set.getEdgeWeightConditionValue());
		nodeContaminationPanel.update(nodes.values(), nodeSchema, set.getNodeCrossContaminations(),
				set.getNodeContaminationCondition(), set.getNodeContaminationConditionValue());
		edgeContaminationPanel.update(edges, edgeSchema, set.getEdgeCrossContaminations(),
				set.getEdgeContaminationCondition(), set.getEdgeContaminationConditionValue());
		nodeKillPanel.update(nodes.values(), nodeSchema, set.getNodeKillContaminations(),
				set.getNodeKillCondition(), set.getNodeKillConditionValue());
		edgeKillPanel.update(edges, edgeSchema, set.getEdgeKillContaminations(),
				set.getEdgeKillCondition(), set.getEdgeKillConditionValue());
		nodeFilterPanel.update(nodes.values(), nodeSchema, set.getObservedNodes(),
				set.getObservedNodesCondition(), set.getObservedNodesConditionValue());
		edgeFilterPanel.update(edges, edgeSchema, set.getObservedEdges(),
				set.getObservedEdgesCondition(), set.getObservedEdgesConditionValue());
		enforceTempBox.setSelected(set.isEnforeTemporalOrder());

		if (!skippedEdgeRows.isEmpty()) {
			String warning = "Some rows from the delivery table could not be imported."
					+ " Execute the Tracing View for more information.";

			new Thread(new NodeDialogWarningThread(enforceTempBox, warning)).start();
		}
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		set.setNodeWeights(nodeWeightPanel.getValues());
		set.setNodeWeightCondition(nodeWeightPanel.getCondition());
		set.setNodeWeightConditionValue(nodeWeightPanel.getValueForAll());

		set.setEdgeWeights(edgeWeightPanel.getValues());
		set.setEdgeWeightCondition(edgeWeightPanel.getCondition());
		set.setEdgeWeightConditionValue(edgeWeightPanel.getValueForAll());

		set.setNodeCrossContaminations(nodeContaminationPanel.getValues());
		set.setNodeContaminationCondition(nodeContaminationPanel.getCondition());
		set.setNodeContaminationConditionValue(nodeContaminationPanel.getValueForAll());

		set.setEdgeCrossContaminations(edgeContaminationPanel.getValues());
		set.setEdgeContaminationCondition(edgeContaminationPanel.getCondition());
		set.setEdgeContaminationConditionValue(edgeContaminationPanel.getValueForAll());

		set.setNodeKillContaminations(nodeKillPanel.getValues());
		set.setNodeKillCondition(nodeKillPanel.getCondition());
		set.setNodeKillConditionValue(nodeKillPanel.getValueForAll());

		set.setEdgeKillContaminations(edgeKillPanel.getValues());
		set.setEdgeKillCondition(edgeKillPanel.getCondition());
		set.setEdgeKillConditionValue(edgeKillPanel.getValueForAll());

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
