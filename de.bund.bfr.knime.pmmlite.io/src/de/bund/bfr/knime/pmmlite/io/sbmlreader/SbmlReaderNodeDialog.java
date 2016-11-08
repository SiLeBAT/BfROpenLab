/*******************************************************************************
 * Copyright (c) 2016 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.pmmlite.io.sbmlreader;

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

	private static final String IN_HISTORY = "In History";

	/**
	 * New pane for configuring the SbmlReader node.
	 */
	protected SbmlReaderNodeDialog() {
		addDialogComponent(
				new DialogComponentFileChooser(new SettingsModelString(SbmlReaderNodeModel.CFG_IN_PATH, null),
						IN_HISTORY, JFileChooser.OPEN_DIALOG, true));
	}
}
