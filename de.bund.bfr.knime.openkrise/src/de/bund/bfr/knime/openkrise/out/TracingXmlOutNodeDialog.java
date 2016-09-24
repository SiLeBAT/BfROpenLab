package de.bund.bfr.knime.openkrise.out;

import javax.swing.JFileChooser;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * <code>NodeDialog</code> for the "TracingXmlOut" Node.
 * 
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author BfR
 */
public class TracingXmlOutNodeDialog extends DefaultNodeSettingsPane {

    /**
     * New pane for configuring TracingXmlOut node dialog.
     * This is just a suggestion to demonstrate possible default dialog
     * components.
     */
    protected TracingXmlOutNodeDialog() {
        super();
        
        addDialogComponent(new DialogComponentFileChooser(
        		new SettingsModelString (
        				TracingXmlOutNodeModel.CFGKEY_SAVE,
        				TracingXmlOutNodeModel.DEFAULT_SAVE ),
        				"Save file", JFileChooser.SAVE_DIALOG, "xml"));
    }
}

