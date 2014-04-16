/*******************************************************************************
 * Copyright (c) 2014 Federal Institute for Risk Assessment (BfR), Germany 
 * 
 * Developers and contributors are 
 * Christian Thoens (BfR)
 * Armin A. Weiser (BfR)
 * Matthias Filter (BfR)
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
package de.bund.bfr.knime.openkrise.views.tracingparameters;

import java.awt.BorderLayout;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.DataAwareNodeDialogPane;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;

import de.bund.bfr.knime.KnimeUtilities;
import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.GraphNode;
import de.bund.bfr.knime.openkrise.views.TracingUtilities;

/**
 * <code>NodeDialog</code> for the "TracingVisualizer" Node.
 * 
 * 
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Christian Thoens
 */
public class TracingParametersNodeDialog extends DataAwareNodeDialogPane {

	private TracingParametersSettings set;

	private TableInputPanel<Double> weightPanel;
	private TableInputPanel<Boolean> contaminationPanel;
	private TableInputPanel<Boolean> filterPanel;
	private TableInputPanel<Boolean> edgeFilterPanel;
	private JCheckBox enforceTempBox;

	/**
	 * New pane for configuring the TracingVisualizer node.
	 */
	protected TracingParametersNodeDialog() {
		set = new TracingParametersSettings();
		weightPanel = new TableInputPanel<Double>(Double.class);
		contaminationPanel = new TableInputPanel<Boolean>(Boolean.class);
		filterPanel = new TableInputPanel<Boolean>(Boolean.class);
		edgeFilterPanel = new TableInputPanel<Boolean>(Boolean.class);
		enforceTempBox = new JCheckBox("Enforce Temporal Order");

		JPanel contPanel = new JPanel();

		contPanel.setLayout(new BorderLayout());
		contPanel.add(UI.createEmptyBorderPanel(enforceTempBox),
				BorderLayout.NORTH);
		contPanel.add(contaminationPanel, BorderLayout.CENTER);

		addTab("Case Weights", weightPanel);
		addTab("Cross Contaminations", contPanel);
		addTab("Filter", filterPanel);
		addTab("Edge Filter", edgeFilterPanel);
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings,
			BufferedDataTable[] input) throws NotConfigurableException {
		BufferedDataTable nodeTable = input[0];
		BufferedDataTable edgeTable = input[1];

		set.loadSettings(settings);

		Map<String, Class<?>> nodeProperties = KnimeUtilities
				.getTableColumns(nodeTable.getSpec());
		Map<String, Class<?>> edgeProperties = KnimeUtilities
				.getTableColumns(edgeTable.getSpec());
		Map<Integer, GraphNode> nodes = TracingUtilities.readGraphNodes(
				nodeTable, nodeProperties);
		List<Edge<GraphNode>> edges = TracingUtilities.readEdges(edgeTable,
				edgeProperties, nodes);

		weightPanel.update(nodes.values(), nodeProperties,
				set.getCaseWeights(), set.getWeightCondition());
		contaminationPanel.update(nodes.values(), nodeProperties,
				set.getCrossContaminations(), set.getContaminationCondition());
		filterPanel.update(nodes.values(), nodeProperties, set.getFilter(),
				set.getFilterCondition());
		edgeFilterPanel.update(edges, edgeProperties, set.getEdgeFilter(),
				set.getEdgeFilterCondition());
		enforceTempBox.setSelected(set.isEnforeTemporalOrder());
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings)
			throws InvalidSettingsException {
		set.setCaseWeights(weightPanel.getValues());
		set.setWeightCondition(weightPanel.getCondition());
		set.setCrossContaminations(contaminationPanel.getValues());
		set.setContaminationCondition(contaminationPanel.getCondition());
		set.setFilter(filterPanel.getValues());
		set.setFilterCondition(filterPanel.getCondition());
		set.setEdgeFilter(edgeFilterPanel.getValues());
		set.setEdgeFilterCondition(edgeFilterPanel.getCondition());
		set.setEnforeTemporalOrder(enforceTempBox.isSelected());
		set.saveSettings(settings);
	}
}
