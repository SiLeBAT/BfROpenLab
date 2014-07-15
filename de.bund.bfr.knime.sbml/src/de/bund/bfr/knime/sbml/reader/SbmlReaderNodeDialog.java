package de.bund.bfr.knime.sbml.reader;

import javax.swing.JFileChooser;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * <code>NodeDialog</code> for the "SbmlReader" Node.
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
