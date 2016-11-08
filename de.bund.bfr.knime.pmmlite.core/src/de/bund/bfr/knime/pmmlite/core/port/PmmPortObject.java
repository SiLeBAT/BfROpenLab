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
package de.bund.bfr.knime.pmmlite.core.port;

import java.awt.BorderLayout;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;
import org.knime.core.node.port.pmml.XMLTreeCreator;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.pmmlite.core.EmfUtils;
import de.bund.bfr.knime.pmmlite.core.common.Identifiable;

public class PmmPortObject implements PortObject, Serializable {

	public static final PortType TYPE = PortTypeRegistry.getInstance().getPortType(PmmPortObject.class);
	public static final PortType TYPE_OPTIONAL = PortTypeRegistry.getInstance().getPortType(PmmPortObject.class, true);

	private static final long serialVersionUID = 1L;

	private String xml;
	private PmmPortObjectSpec spec;

	public static PmmPortObject createObject(Identifiable obj, PmmPortObjectSpec spec)
			throws IncompatibleObjectException {
		if (!spec.isCompatible(obj)) {
			throw createException(obj, spec);
		}

		return new PmmPortObject(EmfUtils.identifiableToXml(obj), spec);
	}

	public static PmmPortObject createListObject(List<? extends Identifiable> list, PmmPortObjectSpec spec)
			throws IncompatibleObjectException {
		for (Identifiable obj : list) {
			if (!spec.isCompatible(obj)) {
				throw createException(obj, spec);
			}
		}

		return new PmmPortObject(EmfUtils.identifiableListToXml(list), spec);
	}

	public static PmmPortObject createEmptyObject() {
		return new PmmPortObject(EmfUtils.identifiableListToXml(Arrays.asList()), PmmPortObjectSpec.EMPTY_TYPE);
	}

	private PmmPortObject(String xml, PmmPortObjectSpec spec) {
		this.xml = xml;
		this.spec = spec;
	}

	@Override
	public String getSummary() {
		return "Shapefile Port";
	}

	@Override
	public PmmPortObjectSpec getSpec() {
		return spec;
	}

	@Override
	public JComponent[] getViews() {
		JPanel panel = new JPanel();

		panel.setName("PMM Data");
		panel.setLayout(new BorderLayout());

		try {
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			XMLTreeCreator treeCreator = new XMLTreeCreator();

			parser.parse(new InputSource(new StringReader(xml)), treeCreator);
			panel.add(new JScrollPane(new JTree(new DefaultTreeModel(treeCreator.getTreeNode()))), BorderLayout.CENTER);

			if (spec != PmmPortObjectSpec.EMPTY_TYPE) {
				panel.add(UI.createEastPanel(UI.createBorderPanel(new JLabel("Number of " + spec.getEClass().getName()
						+ " elements: " + getData(Identifiable.class).size()))), BorderLayout.SOUTH);
			}
		} catch (SAXException | IOException | ParserConfigurationException e) {
			e.printStackTrace();
		}

		return new JComponent[] { panel };
	}

	public String getXml() {
		return xml;
	}

	public <T extends Identifiable> List<T> getData(Class<T> type) {
		return EmfUtils.identifiableListFromXml(xml, type);
	}

	private static IncompatibleObjectException createException(Identifiable obj, PmmPortObjectSpec spec) {
		return new IncompatibleObjectException(
				"Object of type \"" + obj.eClass().getName() + "\" is incompatible with spec \"" + spec + "\"");
	}
}
