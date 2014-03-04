package de.bund.bfr.knime.gis.shapeproperties;

import java.util.Arrays;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;

import de.bund.bfr.knime.ColumnComboBox;
import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.gis.GisUtilities;

/**
 * <code>NodeDialog</code> for the "GetShapeCenter" Node.
 * 
 * 
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Christian Thoens
 */
public class ShapePropertiesNodeDialog extends NodeDialogPane {

	private ShapePropertiesSettings set;

	private ColumnComboBox shapeColumnBox;

	/**
	 * New pane for configuring the GetShapeCenter node.
	 */
	protected ShapePropertiesNodeDialog() {
		set = new ShapePropertiesSettings();
		shapeColumnBox = new ColumnComboBox(false);

		JPanel panel = UI.createOptionsPanel("Columns",
				Arrays.asList(new JLabel("Column with Shapes")),
				Arrays.asList(shapeColumnBox));

		addTab("Options", UI.createNorthPanel(panel));
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings,
			DataTableSpec[] specs) throws NotConfigurableException {
		shapeColumnBox.removeAllColumns();

		for (DataColumnSpec column : GisUtilities.getShapeColumns(specs[0])) {
			shapeColumnBox.addColumn(column);
		}

		set.loadSettings(settings);
		shapeColumnBox.setSelectedColumnName(set.getShapeColumn());
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings)
			throws InvalidSettingsException {
		set.setShapeColumn(shapeColumnBox.getSelectedColumnName());
		set.saveSettings(settings);
	}

}
