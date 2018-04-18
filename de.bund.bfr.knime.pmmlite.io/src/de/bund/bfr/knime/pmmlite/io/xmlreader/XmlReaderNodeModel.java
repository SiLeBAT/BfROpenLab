/*******************************************************************************
 * Copyright (c) 2018 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.pmmlite.io.xmlreader;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;

import de.bund.bfr.knime.KnimeUtils;
import de.bund.bfr.knime.NoInternalsNodeModel;
import de.bund.bfr.knime.pmmlite.core.EmfUtils;
import de.bund.bfr.knime.pmmlite.core.common.Identifiable;
import de.bund.bfr.knime.pmmlite.core.port.PmmPortObject;
import de.bund.bfr.knime.pmmlite.core.port.PmmPortObjectSpec;

/**
 * This is the model implementation of XmlReader.
 * 
 * @author Christian Thoens
 */
public class XmlReaderNodeModel extends NoInternalsNodeModel {

	protected static final String CFG_IN_PATH = "InPath";

	private SettingsModelString inPath;

	/**
	 * Constructor for the node model.
	 */
	protected XmlReaderNodeModel() {
		super(new PortType[] {}, new PortType[] { PmmPortObject.TYPE });
		inPath = new SettingsModelString(CFG_IN_PATH, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObject[] execute(PortObject[] inObjects, ExecutionContext exec) throws Exception {
		List<File> files = Files.list(KnimeUtils.getFile(inPath.getStringValue()).toPath())
				.filter(f -> f.toString().endsWith(".xml")).map(f -> f.toFile()).collect(Collectors.toList());
		List<Identifiable> objects = new ArrayList<>();
		PmmPortObjectSpec spec = null;

		for (File f : files) {
			Identifiable obj = EmfUtils.identifiableFromXml(
					Files.lines(f.toPath(), StandardCharsets.UTF_8).collect(Collectors.joining()), Identifiable.class);
			PmmPortObjectSpec objSpec = PmmPortObjectSpec.getCompatibleSpec(obj);

			if (spec == null) {
				spec = objSpec;
			} else if (spec != objSpec) {
				throw new Exception(
						"XML files contain different data type: \"" + spec + "\", \"" + objSpec + "\", ...");
			}

			objects.add(obj);
		}

		return new PortObject[] { PmmPortObject.createListObject(objects, spec) };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) throws InvalidSettingsException {
		return new PortObjectSpec[] { PmmPortObjectSpec.PRIMARY_MODEL_TYPE };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		inPath.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		inPath.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
		inPath.validateSettings(settings);
	}
}
