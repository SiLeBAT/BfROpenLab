/*******************************************************************************
 * Copyright (c) 2021 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.pmmlite.views.tertiarymodelselection;

import java.util.Arrays;
import java.util.List;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeModel;
import org.sbml.jsbml.text.parser.ParseException;

import de.bund.bfr.knime.pmmlite.core.UnitException;
import de.bund.bfr.knime.pmmlite.core.port.PmmPortObject;
import de.bund.bfr.knime.pmmlite.core.port.PmmPortObjectSpec;
import de.bund.bfr.knime.pmmlite.views.MultiSelectionViewDialog;
import de.bund.bfr.knime.pmmlite.views.MultiSelectionViewModel;
import de.bund.bfr.knime.pmmlite.views.ViewFactory;
import de.bund.bfr.knime.pmmlite.views.ViewReader;

/**
 * <code>NodeFactory</code> for the "TertiaryModelSelection" Node.
 * 
 * 
 * @author Christian Thoens
 */
public class TertiaryModelSelectionNodeFactory extends ViewFactory {

	@Override
	public NodeModel createNodeModel() {
		return new MultiSelectionViewModel(true) {

			@Override
			protected List<PmmPortObjectSpec> getCompatibleSpecs() {
				return Arrays.asList(PmmPortObjectSpec.TERTIARY_MODEL_TYPE);
			}

			@Override
			protected ViewReader createReader(PmmPortObject input) throws UnitException, ParseException {
				return new TertiaryModelSelectionReader(input);
			}
		};
	}

	@Override
	public NodeDialogPane createNodeDialogPane() {
		return new MultiSelectionViewDialog() {

			@Override
			protected ViewReader createReader(PmmPortObject input) throws UnitException, ParseException {
				return new TertiaryModelSelectionReader(input);
			}

			@Override
			protected boolean showSamplePanel() {
				return false;
			}

			@Override
			protected boolean allowInterval() {
				return true;
			}
		};
	}
}
