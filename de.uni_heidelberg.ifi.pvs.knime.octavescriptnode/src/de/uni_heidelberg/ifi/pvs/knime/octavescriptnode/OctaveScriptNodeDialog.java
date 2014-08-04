/*
 * OctaveScriptNode - A KNIME node that runs Octave scripts
 * Copyright (C) 2011 Andre-Patrick Bubel (pvs@andre-bubel.de) and
 *                    Parallel and Distributed Systems Group (PVS),
 *                    University of Heidelberg, Germany
 * Website: http://pvs.ifi.uni-heidelberg.de/
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
 */
package de.uni_heidelberg.ifi.pvs.knime.octavescriptnode;

import java.util.Map;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.workflow.FlowVariable;

/**
 * <code>NodeDialog</code> for the "OctaveScriptNode" Node.
 * 
 * 
 * derived from KNIME R Node
 * org.knime.ext.r.node.local.RLocalScriptingNodeDialogPane
 * 
 * @author André-Patrick Bubel, Universität Heidelberg
 */
public class OctaveScriptNodeDialog extends DefaultNodeSettingsPane {
	@SuppressWarnings("unused")
	private static NodeLogger logger = NodeLogger
			.getLogger(OctaveScriptNodeDialog.class);

	private ColumnEditorPanel m_outputPanel;
	private OctaveScriptNodeScriptEditorPanel m_scriptingPanel;

	/**
	 * New pane for configuring OctaveScriptNode node dialog.
	 */
	protected OctaveScriptNodeDialog() {
		super();

		m_outputPanel = new ColumnEditorPanel();
		m_scriptingPanel = new OctaveScriptNodeScriptEditorPanel();
		addTabAt(1, "Octave Script", m_scriptingPanel);
		addTabAt(0, "Script Output", m_outputPanel);

		removeTab("Options");

		setSelected("Script Output");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void loadAdditionalSettingsFrom(final NodeSettingsRO settings,
			final DataTableSpec[] specs) throws NotConfigurableException {
		super.loadAdditionalSettingsFrom(settings, specs);
		Map<String, FlowVariable> flowMap = getAvailableFlowVariables();
		m_scriptingPanel.loadSettingsFrom(settings, specs, flowMap);
		m_outputPanel.loadSettingsFrom(settings, specs);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void saveAdditionalSettingsTo(final NodeSettingsWO settings)
			throws InvalidSettingsException {
		super.saveAdditionalSettingsTo(settings);
		m_scriptingPanel.saveSettingsTo(settings);
		m_outputPanel.saveSettingsTo(settings);
	}

}
