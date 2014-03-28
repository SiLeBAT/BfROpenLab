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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JEditorPane;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.undo.UndoManager;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.IntValue;
import org.knime.core.data.StringValue;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.util.DataColumnSpecListCellRenderer;
import org.knime.core.node.util.FlowVariableListCellRenderer;
import org.knime.core.node.workflow.FlowVariable;

/**
 * Panel to enter Octave expressions.
 * 
 * derived from KNIME R Node org.knime.ext.r.node.RDialogPanel
 * 
 * @author André-Patrick Bubel <code@andre-bubel.de>
 * 
 */
public class OctaveScriptNodeScriptEditorPanel extends JPanel {

	/** The default Octave command. */
	public static final String DEFAULT_OCTAVE_SCRIPT = "out = in\n";

	/** Key for the Octave Script. */
	private static final String CFG_SCRIPT = "EXPRESSION";

	private static final long serialVersionUID = -5002372438334380228L;

	/**
	 * derived from org.knime.ext.r.node.RNodeModel.ExpressionResolver
	 * 
	 * Replaces and returns the given flow variable.
	 * 
	 * @param var
	 *            flow variable to be extended
	 * @return the new variable as string with pre- and suffix for INTEGER,
	 *         DOUBLE and STRING types
	 */
	public static String extendVariable(final FlowVariable var) {
		// TODO: refactor
		switch (var.getType()) {
		case INTEGER:
			return "vars." + var.getName();
		case DOUBLE:
			return "vars." + var.getName();
		case STRING:
			return "vars." + var.getName();
		default:
			throw new RuntimeException("Unsupported flow variable type '"
					+ var.getType() + "'");
		}
	}

	/**
	 * Loads expression from given settings instance and returns it as string.
	 * 
	 * @param settings
	 *            settings instance to load expression from.
	 * @return The expression loaded from settings instance.
	 */
	public static final String getExpressionFrom(final NodeSettingsRO settings) {
		return settings.getString(CFG_SCRIPT, DEFAULT_OCTAVE_SCRIPT);
	}

	/**
	 * Loads expression from given settings instance and returns it as string.
	 * If no settings can be loaded, the given string is returned as default
	 * expression.
	 * 
	 * @param settings
	 *            settings instance to load expression from.
	 * @param defaultExpr
	 *            the default expression if no other can be loaded from
	 *            settings.
	 * @return The expression loaded from settings instance.
	 */
	public static final String getExpressionFrom(final NodeSettingsRO settings,
			final String defaultExpr) {
		return settings.getString(CFG_SCRIPT, defaultExpr);
	}

	/**
	 * Loads expression from given settings instance and returns it as string
	 * array.
	 * 
	 * @param settings
	 *            settings instance to load expression from.
	 * @return The expression loaded from settings instance.
	 */
	public static final String[] getExpressionsFrom(
			final NodeSettingsRO settings) {
		String expr = settings.getString(CFG_SCRIPT, DEFAULT_OCTAVE_SCRIPT);
		return expr.split("\n");
	}

	/**
	 * Saves given array of expressions to given settings instance.
	 * 
	 * @param settings
	 *            settings instance to save expression to.
	 * @param exprs
	 *            array of expressions to save.
	 */
	public static final void setExpressionsTo(final NodeSettingsWO settings,
			final String[] exprs) {
		StringBuilder expr = new StringBuilder();
		if (exprs != null) {
			for (int i = 0; i < exprs.length; i++) {
				if (i > 0) {
					expr.append("\n");
				}
				expr.append(exprs[i]);
			}
		} else {
			expr.append(DEFAULT_OCTAVE_SCRIPT);
		}
		settings.addString(CFG_SCRIPT, expr.toString());
	}

	/**
	 * Saves given expression to given settings instance.
	 * 
	 * @param settings
	 *            settings instance to save expression to.
	 * @param expr
	 *            expression to save.
	 */
	public static final void setExpressionTo(final NodeSettingsWO settings,
			final String expr) {
		settings.addString(CFG_SCRIPT, expr);
	}

	/**
	 * Formats the given in column name for access in the script
	 * 
	 * @param name
	 *            The name of the column to format.
	 * @return The formatted column name.
	 */
	private static String formatInColumnName(final String name) {
		return OctaveScriptNodeModel.IN_VARIABLE_NAME + "." + name;
	}

	private String m_defaultCommand = DEFAULT_OCTAVE_SCRIPT;

	private final JList<DataColumnSpec> m_inList;

	private final DefaultListModel<DataColumnSpec> m_inListModel;

	private final DefaultListModel<FlowVariable> m_listModelVars;

	private final JList<FlowVariable> m_listVars;

	private final JEditorPane m_textExpression;

	private UndoManager m_undoManager;

	/**
	 * Creates a new dialog to enter R expressions with a default mouse
	 * listener.
	 * 
	 * @param UndoManager
	 */
	public OctaveScriptNodeScriptEditorPanel() {
		super(new BorderLayout());

		// init editor pane
		m_textExpression = new JEditorPane();
		m_textExpression.setPreferredSize(new Dimension(350, 200));
		Font font = m_textExpression.getFont();
		Font newFont = new Font(Font.MONOSPACED, Font.PLAIN, (font == null ? 12
				: font.getSize()));
		m_textExpression.setFont(newFont);
		m_textExpression.setBorder(BorderFactory
				.createTitledBorder(" Octave Script "));
		JScrollPane scroll = new JScrollPane(m_textExpression,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		JPanel textPanel = new JPanel(new BorderLayout());
		textPanel.add(scroll, BorderLayout.CENTER);
		textPanel.setMinimumSize(new Dimension(0, 0));
		textPanel.setMaximumSize(new Dimension(0, 0));

		m_undoManager = new UndoManager();
		m_textExpression.getDocument().addUndoableEditListener(m_undoManager);
		m_textExpression.addKeyListener(new UndoRedoKeyListener(m_undoManager));

		// init in column list
		m_inListModel = new DefaultListModel<DataColumnSpec>();
		m_inList = new JList<DataColumnSpec>(m_inListModel);
		m_inList.setBorder(BorderFactory.createTitledBorder(" Column List"));
		m_inList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		m_inList.setCellRenderer(new DataColumnSpecListCellRenderer());
		m_inList.addMouseListener(new MouseAdapter() {
			/** {@inheritDoc} */
			@Override
			public final void mouseClicked(final MouseEvent e) {
				if (e.getClickCount() == 2) {
					Object o = m_inList.getSelectedValue();
					if (o != null) {
						DataColumnSpec cspec = (DataColumnSpec) o;
						m_textExpression
								.replaceSelection(formatInColumnName(cspec
										.getName()));
						m_inList.clearSelection();
						m_textExpression.requestFocus();
					}
				}
			}
		});
		JScrollPane leftComp = new JScrollPane(m_inList,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		// init variable list
		m_listModelVars = new DefaultListModel<FlowVariable>();
		m_listVars = new JList<FlowVariable>(m_listModelVars);

		final JSplitPane allSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		allSplit.setResizeWeight(0.25);
		allSplit.setRightComponent(textPanel);

		m_listVars.setBorder(BorderFactory
				.createTitledBorder(" Flow Variable List "));
		m_listVars.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		m_listVars.setCellRenderer(new FlowVariableListCellRenderer());
		m_listVars.addMouseListener(new MouseAdapter() {
			/** {@inheritDoc} */
			@Override
			public final void mouseClicked(final MouseEvent e) {
				if (e.getClickCount() == 2) {
					Object o = m_listVars.getSelectedValue();
					if (o != null) {
						FlowVariable var = (FlowVariable) o;
						m_textExpression.replaceSelection(extendVariable(var));
						m_listVars.clearSelection();
						m_textExpression.requestFocus();
					}
				}
			}
		});
		JScrollPane scrollVars = new JScrollPane(m_listVars,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		JSplitPane leftPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				leftComp, scrollVars);
		leftPane.setResizeWeight(0.5);

		allSplit.setLeftComponent(leftPane);

		super.add(allSplit, BorderLayout.CENTER);

	}

	/**
	 * @return the defaultCommand
	 */
	public String getDefaultCommand() {
		return m_defaultCommand;
	}

	/**
	 * @return complete text as string.
	 */
	public final String getText() {
		return m_textExpression.getText();
	}

	/**
	 * Loads Octave command string out of given settings instance.
	 * 
	 * @param settings
	 *            settings instance to load R command string from.
	 * @param specs
	 *            input DataTable spec
	 * @param map
	 *            holding flow variables together with its identifier
	 * @throws NotConfigurableException
	 *             if no columns are available.
	 */
	public void loadSettingsFrom(final NodeSettingsRO settings,
			final PortObjectSpec[] specs, final Map<String, FlowVariable> map)
			throws NotConfigurableException {
		if (!(specs[0] instanceof DataTableSpec))
			throw new NotConfigurableException("Expected DataTableSpec at"
					+ " port 0!");
		update((DataTableSpec) specs[0], map);
		setText(getExpressionFrom(settings));
	}

	/**
	 * Saves internal Octave command string to given settings instance.
	 * 
	 * @param settings
	 *            settings instance to write R command string to.
	 */
	public void saveSettingsTo(final NodeSettingsWO settings) {
		setExpressionTo(settings, getText());
	}

	/**
	 * @param defaultCommand
	 *            the defaultCommand to set
	 */
	public void setDefaultCommand(final String defaultCommand) {
		m_defaultCommand = defaultCommand;
	}

	/**
	 * @param str
	 *            sets the given string as text.
	 */
	public final void setText(final String str) {
		m_textExpression.setText(str);
		m_textExpression.setCaretPosition(str.length());
	}

	/**
	 * Updates the list of columns based on the given table spec.
	 * 
	 * @param spec
	 *            The spec to get columns from. compatible with the Rserv
	 *            implementation.
	 */
	private final void update(final DataTableSpec spec,
			final Map<String, FlowVariable> map)
			throws NotConfigurableException {
		// update column list
		m_inListModel.removeAllElements();
		DataTableSpec newSpec = spec;
		// RConnectionRemote.createRenamedDataTableSpec(spec);
		for (int i = 0; i < newSpec.getNumColumns(); i++) {
			DataColumnSpec cspec = newSpec.getColumnSpec(i);
			DataType type = cspec.getType();
			if (type.isCompatible(IntValue.class)) {
				m_inListModel.addElement(cspec);
			} else if (type.isCompatible(DoubleValue.class)) {
				m_inListModel.addElement(cspec);
			} else if (type.isCompatible(StringValue.class)) {
				m_inListModel.addElement(cspec);
			}
		}
		if (m_inListModel.size() <= 0)
			throw new NotConfigurableException("No valid columns "
					+ "(Integer, Double, String) are available!");

		// update list of flow/workflow variables
		m_listModelVars.removeAllElements();
		for (Map.Entry<String, FlowVariable> e : map.entrySet()) {
			m_listModelVars.addElement(e.getValue());
		}
		repaint();
	}

}
