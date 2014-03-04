package de.bund.bfr.knime.gis.shapeview;

import org.knime.core.data.DoubleValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter2;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;

/**
 * <code>NodeDialog</code> for the "ShapeView" Node.
 * 
 * 
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Christian Thoens
 */
public class ShapeViewNodeDialog extends DefaultNodeSettingsPane {

	/**
	 * New pane for configuring the ShapeView node.
	 */
	@SuppressWarnings("unchecked")
	protected ShapeViewNodeDialog() {
		createNewGroup("Coordinates of map markers");
		setHorizontalPlacement(true);
		addDialogComponent(new DialogComponentColumnNameSelection(
				ShapeViewNodeModel.createLatColModel(), "Latitude", 0, false,
				false, DoubleValue.class));
		addDialogComponent(new DialogComponentColumnNameSelection(
				ShapeViewNodeModel.createLonColModel(), "Longitude", 0, false,
				false, DoubleValue.class));
		setHorizontalPlacement(false);
		closeCurrentGroup();
		createNewGroup("Hover info");
		addDialogComponent(new DialogComponentColumnFilter2(
				ShapeViewNodeModel.createHoverInfoCols(), 0));
	}
}
