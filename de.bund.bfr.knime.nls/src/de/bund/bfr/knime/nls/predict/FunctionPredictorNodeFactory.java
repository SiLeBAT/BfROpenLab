/*******************************************************************************
 * Copyright (c) 2016 Federal Institute for Risk Assessment (BfR), Germany
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
package de.bund.bfr.knime.nls.predict;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortType;

import de.bund.bfr.knime.nls.ViewDialog;
import de.bund.bfr.knime.nls.ViewModel;
import de.bund.bfr.knime.nls.ViewReader;
import de.bund.bfr.knime.nls.chart.ChartConfigPanel;
import de.bund.bfr.knime.nls.functionport.FunctionPortObject;

/**
 * <code>NodeFactory</code> for the "FunctionPredictor" Node.
 * 
 *
 * @author Christian Thoens
 */
public class FunctionPredictorNodeFactory extends NodeFactory<ViewModel> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ViewModel createNodeModel() {
		return new ViewModel(
				new PortType[] { FunctionPortObject.TYPE, BufferedDataTable.TYPE, BufferedDataTable.TYPE_OPTIONAL }) {

			@Override
			protected ViewReader createReader(PortObject[] inObjects) {
				return new FunctionPredictorReader((FunctionPortObject) inObjects[0], (BufferedDataTable) inObjects[1],
						(BufferedDataTable) inObjects[2]);
			}
		};
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getNrNodeViews() {
		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NodeView<ViewModel> createNodeView(final int viewIndex, final ViewModel nodeModel) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasDialog() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NodeDialogPane createNodeDialogPane() {
		return new ViewDialog() {

			@Override
			protected ViewReader createReader() {
				return new FunctionPredictorReader((FunctionPortObject) input[0], (BufferedDataTable) input[1],
						(BufferedDataTable) input[2]);
			}

			@Override
			protected ChartConfigPanel createConfigPanel() {
				return new ChartConfigPanel(true, true, false, true, false);
			}
		};
	}
}
