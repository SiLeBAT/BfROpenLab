/*******************************************************************************
 * Copyright (c) 2014 Federal Institute for Risk Assessment (BfR), Germany 
 * 
 * Developers and contributors are
 * Armin A. Weiser (BfR) 
 * Christian Thoens (BfR)
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
package de.bund.bfr.knime.gis.util.clustering;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDouble;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * <code>NodeDialog</code> for the "DBSCAN" Node.
 * 
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author BfR
 */
public class DBSCANNodeDialog extends DefaultNodeSettingsPane {

    /**
     * New pane for configuring the DBSCAN node.
     */
    protected DBSCANNodeDialog() {
        addDialogComponent(new DialogComponentNumber(new SettingsModelInteger(DBSCANNodeModel.MINPTS, 2), "Enter minPts:", 1));
        addDialogComponent(new DialogComponentNumber(new SettingsModelDouble(DBSCANNodeModel.EPS, 2.0), "Enter epsilon (km):", 0.5));
        addDialogComponent(new DialogComponentBoolean(new SettingsModelBoolean(DBSCANNodeModel.DOUBLETTES, false), "Allow multiple unique points?"));
        addDialogComponent(new DialogComponentStringSelection(new SettingsModelString(DBSCANNodeModel.CHOSENMODEL, "DBSCAN"), "Choose model:", new String[]{"DBSCAN","KMeans"}));
    }
}

