/*******************************************************************************
 * Copyright (c) 2014 Federal Institute for Risk Assessment (BfR), Germany 
 * 
 * Developers and contributors are 
 * Christian Thoens (BfR)
 * Armin A. Weiser (BfR)
 * Matthias Filter (BfR)
 * Alexander Falenski (BfR)
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
package de.bund.bfr.knime.sbml.reader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.AlgebraicRule;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Rule;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.UnitDefinition;

import de.bund.bfr.knime.IO;
import de.bund.bfr.knime.KnimeUtilities;

/**
 * This is the model implementation of SbmlReader.
 * 
 * 
 * @author Christian Thoens
 */
public class SbmlReaderNodeModel extends NodeModel {

	protected static final String CFG_IN_PATH = "inPath";

	private SettingsModelString inPath = new SettingsModelString(CFG_IN_PATH,
			null);

	private static final String MODEL_ID = "ModelID";
	private static final String ORGANISM = "Organism";
	private static final String MATRIX = "Matrix";
	private static final String FORMULA_LEFT = "FormulaLeft";
	private static final String FORMULA_RIGHT = "FormulaRight";
	private static final String DEPENDENT_VARIABLE = "DependentVariable";
	private static final String INDEPENDENT_VARIABLES = "IndependentVariables";
	private static final String PARAMETERS = "Parameters";
	private static final String START_PARAMETER = "StartParameter";

	private static final String UNIT = " Unit";
	private static final String MIN_MAX = " MinMax";

	/**
	 * Constructor for the node model.
	 */
	protected SbmlReaderNodeModel() {
		super(0, 1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
			final ExecutionContext exec) throws Exception {
		File path = KnimeUtilities.getFile(inPath.getStringValue());

		if (!path.isDirectory()) {
			throw new Exception(path + " is not a directory");
		}

		Map<String, DataType> columns = new LinkedHashMap<>();
		List<Map<String, DataCell>> rows = new ArrayList<>();
		File[] files = path.listFiles();
		int index1 = 0;

		for (File file : files) {
			SBMLDocument doc = null;

			try {
				doc = SBMLReader.read(file);
			} catch (Exception e) {
			}

			if (doc != null) {
				readSBML(doc, columns, rows);
				exec.checkCanceled();
				exec.setProgress((double) index1 / (double) files.length);
				index1++;
			}
		}

		DataTableSpec spec = createSpec(columns);
		BufferedDataContainer container = exec.createDataContainer(spec);
		int index2 = 0;

		for (Map<String, DataCell> row : rows) {
			DataCell[] cells = new DataCell[spec.getNumColumns()];

			for (int i = 0; i < spec.getNumColumns(); i++) {
				cells[i] = row.get(spec.getColumnNames()[i]);

				if (cells[i] == null) {
					cells[i] = DataType.getMissingCell();
				}
			}

			container.addRowToTable(new DefaultRow(index2 + "", cells));
			exec.checkCanceled();
			index2++;
		}

		container.close();

		return new BufferedDataTable[] { container.getTable() };
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
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
			throws InvalidSettingsException {
		return null;
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
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		inPath.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		inPath.validateSettings(settings);
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

	private static void readSBML(SBMLDocument doc,
			Map<String, DataType> columns, List<Map<String, DataCell>> rows) {
		Model model = doc.getModel();
		Map<String, DataCell> row = new LinkedHashMap<>();

		if (!columns.containsKey(MODEL_ID)) {
			columns.put(MODEL_ID, StringCell.TYPE);
		}

		row.put(MODEL_ID, IO.createCell(model.getId()));

		Species organism = model.getSpecies(0);
		Compartment matrix = model.getCompartment(0);

		if (organism != null) {
			if (!columns.containsKey(ORGANISM)) {
				columns.put(ORGANISM, StringCell.TYPE);
			}

			row.put(ORGANISM, IO.createCell(organism.getName()));
		}

		if (matrix != null) {
			if (!columns.containsKey(MATRIX)) {
				columns.put(MATRIX, StringCell.TYPE);
			}

			row.put(MATRIX, IO.createCell(matrix.getName()));
		}

		AlgebraicRule rule = getAssignmentRule(model.getListOfRules());
		Map<String, AlgebraicRule> minMaxRules = new LinkedHashMap<>();

		for (AlgebraicRule r : getMinMaxRules(model.getListOfRules())) {
			minMaxRules.put(getParam(r.getMath()), r);
		}

		if (!columns.containsKey(FORMULA_LEFT)) {
			columns.put(FORMULA_LEFT, StringCell.TYPE);
		}

		if (!columns.containsKey(FORMULA_RIGHT)) {
			columns.put(FORMULA_RIGHT, StringCell.TYPE);
		}

		if (!columns.containsKey(START_PARAMETER)) {
			columns.put(START_PARAMETER, StringCell.TYPE);
		}

		row.put(FORMULA_LEFT,
				IO.createCell(rule.getMath().getChild(0).toFormula()));
		row.put(FORMULA_RIGHT,
				IO.createCell(rule.getMath().getChild(1).toFormula()));

		String dependentVariable = null;
		List<String> independentVariables = new ArrayList<>();
		List<String> paramters = new ArrayList<>();

		for (Parameter param : model.getListOfParameters()) {
			if (!param.isConstant()) {
				String name = param.getId();
				UnitDefinition unit = param.getUnitsInstance();
				AlgebraicRule minMax = minMaxRules.get(name);
				String annotation = param.getAnnotation().getNonRDFannotation();

				if (annotation != null && annotation.trim().equals("<start>")) {
					row.put(START_PARAMETER, IO.createCell(name));
				}

				if (!columns.containsKey(name + UNIT)) {
					columns.put(name + UNIT, StringCell.TYPE);
				}

				if (unit != null) {
					row.put(name + UNIT, IO.createCell(unit.toString()));
				} else {
					row.put(name + UNIT, DataType.getMissingCell());
				}

				if (!columns.containsKey(name + MIN_MAX)) {
					columns.put(name + MIN_MAX, StringCell.TYPE);
				}

				if (minMax != null) {
					row.put(name + MIN_MAX,
							IO.createCell(minMax.getMath().toFormula()));
				} else {
					row.put(name + MIN_MAX, DataType.getMissingCell());
				}

				if (dependentVariable == null) {
					dependentVariable = name;
				} else {
					independentVariables.add(name);
				}
			}
		}

		for (Parameter param : model.getListOfParameters()) {
			if (param.isConstant()) {
				String name = param.getId();

				if (!columns.containsKey(name)) {
					columns.put(name, DoubleCell.TYPE);
				}

				row.put(name, IO.createCell(param.getValue()));
				paramters.add(name);
			}
		}

		if (!columns.containsKey(DEPENDENT_VARIABLE)) {
			columns.put(DEPENDENT_VARIABLE, StringCell.TYPE);
		}

		row.put(DEPENDENT_VARIABLE, IO.createCell(dependentVariable));

		if (!columns.containsKey(INDEPENDENT_VARIABLES)) {
			columns.put(INDEPENDENT_VARIABLES, StringCell.TYPE);
		}

		row.put(INDEPENDENT_VARIABLES, IO.createCell(KnimeUtilities
				.listToString(independentVariables)));

		if (!columns.containsKey(PARAMETERS)) {
			columns.put(PARAMETERS, StringCell.TYPE);
		}

		row.put(PARAMETERS,
				IO.createCell(KnimeUtilities.listToString(paramters)));

		rows.add(row);
	}

	private static DataTableSpec createSpec(Map<String, DataType> columns) {
		List<DataColumnSpec> specs = new ArrayList<>();

		for (String name : columns.keySet()) {
			specs.add(new DataColumnSpecCreator(name, columns.get(name))
					.createSpec());
		}

		return new DataTableSpec(specs.toArray(new DataColumnSpec[0]));
	}

	private static AlgebraicRule getAssignmentRule(ListOf<Rule> rules) {
		return (AlgebraicRule) rules.get(0);
	}

	private static List<AlgebraicRule> getMinMaxRules(ListOf<Rule> rules) {
		List<AlgebraicRule> minMaxRules = new ArrayList<>();

		for (int i = 1; i < rules.size(); i++) {
			minMaxRules.add((AlgebraicRule) rules.get(i));
		}

		return minMaxRules;
	}

	private static String getParam(ASTNode node) {
		if (node.getType() == ASTNode.Type.NAME) {
			return node.getName();
		}

		for (ASTNode n : node.getListOfNodes()) {
			String param = getParam(n);

			if (param != null) {
				return param;
			}
		}

		return null;
	}

}
