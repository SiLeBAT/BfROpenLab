/*******************************************************************************
 * Copyright (c) 2014-2022 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.pmmlite.util.totable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcorePackage;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.collection.CollectionCellFactory;
import org.knime.core.data.collection.ListCell;
import org.knime.core.data.def.BooleanCell;
import org.knime.core.data.def.BooleanCell.BooleanCellFactory;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
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
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;

import com.google.common.collect.ImmutableMap;

import de.bund.bfr.knime.pmmlite.core.PmmUnit;
import de.bund.bfr.knime.pmmlite.core.common.CommonPackage;
import de.bund.bfr.knime.pmmlite.core.common.Identifiable;
import de.bund.bfr.knime.pmmlite.core.port.PmmPortObject;
import de.bund.bfr.knime.pmmlite.core.port.PmmPortObjectSpec;

/**
 * This is the model implementation of ToTable.
 * 
 * 
 * @author Christian Thoens
 */
public class ToTableNodeModel extends NodeModel {

	private static final ImmutableMap<EClassifier, DataType> EMF_TO_KNIME = new ImmutableMap.Builder<EClassifier, DataType>()
			.put(EcorePackage.Literals.EINT, IntCell.TYPE).put(EcorePackage.Literals.EINTEGER_OBJECT, IntCell.TYPE)
			.put(EcorePackage.Literals.EDOUBLE, DoubleCell.TYPE)
			.put(EcorePackage.Literals.EDOUBLE_OBJECT, DoubleCell.TYPE)
			.put(EcorePackage.Literals.EBOOLEAN, BooleanCell.TYPE)
			.put(EcorePackage.Literals.EBOOLEAN_OBJECT, BooleanCell.TYPE)
			.put(EcorePackage.Literals.ESTRING, StringCell.TYPE).put(CommonPackage.Literals.UNIT, StringCell.TYPE)
			.build();

	/**
	 * Constructor for the node model.
	 */
	protected ToTableNodeModel() {
		super(new PortType[] { PmmPortObject.TYPE }, new PortType[] { BufferedDataTable.TYPE });
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObject[] execute(PortObject[] inObjects, ExecutionContext exec) throws Exception {
		PmmPortObject input = (PmmPortObject) inObjects[0];
		List<Identifiable> data = input.getData(Identifiable.class);
		DataTableSpec spec = createSpec(input.getSpec());
		BufferedDataContainer container = exec.createDataContainer(spec);
		int i = 0;

		for (Identifiable obj : data) {
			container.addRowToTable(createRow(obj, spec));
			exec.checkCanceled();
			exec.setProgress((double) i / (double) data.size());
			i++;
		}

		container.close();

		return new PortObject[] { container.getTable() };
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
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) throws InvalidSettingsException {
		PmmPortObjectSpec spec = (PmmPortObjectSpec) inSpecs[0];

		if (spec == PmmPortObjectSpec.EMPTY_TYPE) {
			throw new InvalidSettingsException("Input type \"" + spec + "\" is not supported");
		}

		return new PortObjectSpec[] { createSpec(spec) };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
	}

	private static DataTableSpec createSpec(PmmPortObjectSpec spec) {
		EClass eClass = spec.getEClass();
		List<DataColumnSpec> columns = new ArrayList<>();

		columns.addAll(createColumns(eClass, ""));

		EStructuralFeature dataRef = eClass.getEStructuralFeature("data");

		if (dataRef instanceof EReference && !dataRef.isMany()) {
			columns.addAll(createColumns(((EReference) dataRef).getEReferenceType(), dataRef.getName() + "_"));
		}

		return new DataTableSpec(columns.toArray(new DataColumnSpec[0]));
	}

	private static List<DataColumnSpec> createColumns(EClass eClass, String prefix) {
		List<DataColumnSpec> columns = new ArrayList<>();

		for (EAttribute attr : eClass.getEAllAttributes()) {
			if (attr.isMany()) {
				continue;
			}

			if (EMF_TO_KNIME.containsKey(attr.getEType())) {
				columns.add(new DataColumnSpecCreator(prefix + attr.getName(), EMF_TO_KNIME.get(attr.getEType()))
						.createSpec());
			}
		}

		for (EReference ref : eClass.getEAllContainments()) {
			if (!ref.isMany() || ignoreReference(ref)) {
				continue;
			}

			for (EAttribute attr : ref.getEReferenceType().getEAllAttributes()) {
				if (attr.isMany()) {
					continue;
				}

				if (EMF_TO_KNIME.containsKey(attr.getEType())) {
					columns.add(new DataColumnSpecCreator(prefix + ref.getName() + "_" + attr.getName(),
							ListCell.getCollectionType(EMF_TO_KNIME.get(attr.getEType()))).createSpec());
				}
			}

			for (EReference ref2 : ref.getEReferenceType().getEAllContainments()) {
				if (ref2.isMany()) {
					continue;
				}

				for (EAttribute attr : ref2.getEReferenceType().getEAllAttributes()) {
					if (attr.isMany()) {
						continue;
					}

					if (EMF_TO_KNIME.containsKey(attr.getEType())) {
						columns.add(new DataColumnSpecCreator(
								prefix + ref.getName() + "_" + ref2.getName() + "_" + attr.getName(),
								ListCell.getCollectionType(EMF_TO_KNIME.get(attr.getEType()))).createSpec());
					}
				}
			}
		}

		return columns;
	}

	private static DataRow createRow(Identifiable obj, DataTableSpec spec) {
		DataCell[] cells = Collections.nCopies(spec.getNumColumns(), DataType.getMissingCell())
				.toArray(new DataCell[0]);

		fillCells(obj, cells, spec, "");

		EStructuralFeature dataRef = obj.eClass().getEStructuralFeature("data");

		if (dataRef instanceof EReference && !dataRef.isMany()) {
			EObject dataRefObj = (EObject) obj.eGet(dataRef);

			fillCells(dataRefObj, cells, spec, dataRef.getName() + "_");
		}

		return new DefaultRow(obj.getId(), cells);
	}

	@SuppressWarnings("unchecked")
	private static void fillCells(EObject obj, DataCell[] cells, DataTableSpec spec, String prefix) {
		for (EAttribute attr : obj.eClass().getEAllAttributes()) {
			if (attr.isMany()) {
				continue;
			}

			int index = spec.findColumnIndex(prefix + attr.getName());

			if (index != -1) {
				cells[index] = createCell(obj.eGet(attr));
			}
		}

		for (EReference ref : obj.eClass().getEAllContainments()) {
			if (!ref.isMany() || ignoreReference(ref)) {
				continue;
			}

			List<EObject> list = (List<EObject>) obj.eGet(ref);

			for (EAttribute attr : ref.getEReferenceType().getEAllAttributes()) {
				if (attr.isMany()) {
					continue;
				}

				int index = spec.findColumnIndex(prefix + ref.getName() + "_" + attr.getName());

				if (index == -1) {
					continue;
				}

				List<DataCell> cellList = new ArrayList<>();

				for (EObject refObj : list) {
					cellList.add(createCell(refObj.eGet(attr)));
				}

				cells[index] = CollectionCellFactory.createListCell(cellList);
			}

			for (EReference ref2 : ref.getEReferenceType().getEAllContainments()) {
				if (ref2.isMany()) {
					continue;
				}

				for (EAttribute attr : ref2.getEReferenceType().getEAllAttributes()) {
					if (attr.isMany()) {
						continue;
					}

					int index = spec
							.findColumnIndex(prefix + ref.getName() + "_" + ref2.getName() + "_" + attr.getName());

					if (index == -1) {
						continue;
					}

					List<DataCell> cellList = new ArrayList<>();

					for (EObject refObj : list) {
						cellList.add(createCell(((EObject) refObj.eGet(ref2)).eGet(attr)));
					}

					cells[index] = CollectionCellFactory.createListCell(cellList);
				}
			}
		}
	}

	private static DataCell createCell(Object value) {
		if (value == null) {
			return DataType.getMissingCell();
		} else if (value instanceof Integer) {
			return new IntCell((Integer) value);
		} else if (value instanceof Double) {
			return new DoubleCell((Double) value);
		} else if (value instanceof Boolean) {
			return BooleanCellFactory.create((Boolean) value);
		} else if (value instanceof PmmUnit) {
			return new StringCell(((PmmUnit) value).toString());
		} else {
			return new StringCell(value.toString());
		}
	}

	private static boolean ignoreReference(EReference ref) {
		if (ref.getEReferenceType() instanceof Identifiable) {
			return true;
		}

		if (ref.getName().toLowerCase().endsWith("assignments")) {
			return true;
		}

		if (ref.getName().toLowerCase().endsWith("renamings")) {
			return true;
		}

		return false;
	}
}
