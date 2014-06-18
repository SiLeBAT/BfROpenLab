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
package de.bund.bfr.knime.gis.views.locationtolocationvisualizer;

import java.io.File;
import java.io.IOException;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.image.ImagePortObject;

import de.bund.bfr.knime.KnimeUtilities;
import de.bund.bfr.knime.gis.views.ViewUtilities;

/**
 * This is the model implementation of LocationToLocationVisualizer.
 * 
 * 
 * @author Christian Thoens
 */
public class LocationToLocationVisualizerNodeModel extends NodeModel {

	private LocationToLocationVisualizerSettings set;

	/**
	 * Constructor for the node model.
	 */
	protected LocationToLocationVisualizerNodeModel() {
		super(new PortType[] { BufferedDataTable.TYPE, BufferedDataTable.TYPE,
				BufferedDataTable.TYPE,
				new PortType(BufferedDataTable.class, true) }, new PortType[] {
				ImagePortObject.TYPE, ImagePortObject.TYPE,
				BufferedDataTable.TYPE });
		set = new LocationToLocationVisualizerSettings();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObject[] execute(PortObject[] inObjects, ExecutionContext exec)
			throws Exception {
		BufferedDataTable shapeTable = (BufferedDataTable) inObjects[0];
		BufferedDataTable nodeTable = (BufferedDataTable) inObjects[1];
		BufferedDataTable edgeTable = (BufferedDataTable) inObjects[2];
		LocationToLocationVisualizerCanvasCreator creator = new LocationToLocationVisualizerCanvasCreator(
				shapeTable, nodeTable, edgeTable, set);

		return new PortObject[] {
				ViewUtilities.getImage(creator.createGraphCanvas(),
						set.isExportAsSvg()),
				ViewUtilities.getImage(creator.createLocationCanvas(),
						set.isExportAsSvg()),
				KnimeUtilities.xmlToTable(set.toXml(), exec) };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {
		return new PortObjectSpec[] {
				ViewUtilities.getImageSpec(set.isExportAsSvg()),
				ViewUtilities.getImageSpec(set.isExportAsSvg()),
				KnimeUtilities.getXmlSpec() };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		set.saveSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		set.loadSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
	}
}
