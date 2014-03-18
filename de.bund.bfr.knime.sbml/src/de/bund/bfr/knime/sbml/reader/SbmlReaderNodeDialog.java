package de.bund.bfr.knime.sbml.reader;

import javax.swing.JFileChooser;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * <code>NodeDialog</code> for the "SbmlReader" Node.
 * 
 * 
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Christian Thoens
 */
public class SbmlReaderNodeDialog extends DefaultNodeSettingsPane {

	/**
	 * New pane for configuring the Sbml Reader node.
	 */
	protected SbmlReaderNodeDialog() {
		DialogComponentFileChooser outComp = new DialogComponentFileChooser(
				new SettingsModelString(SbmlReaderNodeModel.CFG_IN_PATH, null),
				"History", JFileChooser.OPEN_DIALOG, true);

		outComp.setBorderTitle("Input Path");

		addDialogComponent(outComp);
	}
}
