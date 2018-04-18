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
/**
 */
package de.bund.bfr.knime.pmmlite.core.data.impl;

import de.bund.bfr.knime.pmmlite.core.PmmUnit;

import de.bund.bfr.knime.pmmlite.core.common.CommonFactory;
import de.bund.bfr.knime.pmmlite.core.common.CommonPackage;

import de.bund.bfr.knime.pmmlite.core.data.Condition;
import de.bund.bfr.knime.pmmlite.core.data.DataPackage;
import de.bund.bfr.knime.pmmlite.core.data.TimeSeries;
import de.bund.bfr.knime.pmmlite.core.data.TimeSeriesPoint;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Time Series</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.data.impl.TimeSeriesImpl#getName <em>Name</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.data.impl.TimeSeriesImpl#getId <em>Id</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.data.impl.TimeSeriesImpl#getPoints <em>Points</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.data.impl.TimeSeriesImpl#getConditions <em>Conditions</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.data.impl.TimeSeriesImpl#getOrganism <em>Organism</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.data.impl.TimeSeriesImpl#getMatrix <em>Matrix</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.data.impl.TimeSeriesImpl#getTimeUnit <em>Time Unit</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.data.impl.TimeSeriesImpl#getConcentrationUnit <em>Concentration Unit</em>}</li>
 * </ul>
 *
 * @generated
 */
public class TimeSeriesImpl extends EObjectImpl implements TimeSeries {
	/**
	 * The default value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected static final String NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected String name = NAME_EDEFAULT;

	/**
	 * The default value of the '{@link #getId() <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getId()
	 * @generated
	 * @ordered
	 */
	protected static final String ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getId() <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getId()
	 * @generated
	 * @ordered
	 */
	protected String id = ID_EDEFAULT;

	/**
	 * The cached value of the '{@link #getPoints() <em>Points</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPoints()
	 * @generated
	 * @ordered
	 */
	protected EList<TimeSeriesPoint> points;

	/**
	 * The cached value of the '{@link #getConditions() <em>Conditions</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getConditions()
	 * @generated
	 * @ordered
	 */
	protected EList<Condition> conditions;

	/**
	 * The default value of the '{@link #getOrganism() <em>Organism</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getOrganism()
	 * @generated
	 * @ordered
	 */
	protected static final String ORGANISM_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getOrganism() <em>Organism</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getOrganism()
	 * @generated
	 * @ordered
	 */
	protected String organism = ORGANISM_EDEFAULT;

	/**
	 * The default value of the '{@link #getMatrix() <em>Matrix</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMatrix()
	 * @generated
	 * @ordered
	 */
	protected static final String MATRIX_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getMatrix() <em>Matrix</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMatrix()
	 * @generated
	 * @ordered
	 */
	protected String matrix = MATRIX_EDEFAULT;

	/**
	 * The default value of the '{@link #getTimeUnit() <em>Time Unit</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTimeUnit()
	 * @generated
	 * @ordered
	 */
	protected static final PmmUnit TIME_UNIT_EDEFAULT = (PmmUnit)CommonFactory.eINSTANCE.createFromString(CommonPackage.eINSTANCE.getUnit(), "NO_TRANSFORM(NO_UNIT)");

	/**
	 * The cached value of the '{@link #getTimeUnit() <em>Time Unit</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTimeUnit()
	 * @generated
	 * @ordered
	 */
	protected PmmUnit timeUnit = TIME_UNIT_EDEFAULT;

	/**
	 * The default value of the '{@link #getConcentrationUnit() <em>Concentration Unit</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getConcentrationUnit()
	 * @generated
	 * @ordered
	 */
	protected static final PmmUnit CONCENTRATION_UNIT_EDEFAULT = (PmmUnit)CommonFactory.eINSTANCE.createFromString(CommonPackage.eINSTANCE.getUnit(), "NO_TRANSFORM(NO_UNIT)");

	/**
	 * The cached value of the '{@link #getConcentrationUnit() <em>Concentration Unit</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getConcentrationUnit()
	 * @generated
	 * @ordered
	 */
	protected PmmUnit concentrationUnit = CONCENTRATION_UNIT_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected TimeSeriesImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return DataPackage.Literals.TIME_SERIES;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getName() {
		return name;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setName(String newName) {
		String oldName = name;
		name = newName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DataPackage.TIME_SERIES__NAME, oldName, name));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getId() {
		return id;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setId(String newId) {
		String oldId = id;
		id = newId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DataPackage.TIME_SERIES__ID, oldId, id));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<TimeSeriesPoint> getPoints() {
		if (points == null) {
			points = new EObjectContainmentEList<TimeSeriesPoint>(TimeSeriesPoint.class, this, DataPackage.TIME_SERIES__POINTS);
		}
		return points;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<Condition> getConditions() {
		if (conditions == null) {
			conditions = new EObjectContainmentEList<Condition>(Condition.class, this, DataPackage.TIME_SERIES__CONDITIONS);
		}
		return conditions;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getOrganism() {
		return organism;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setOrganism(String newOrganism) {
		String oldOrganism = organism;
		organism = newOrganism;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DataPackage.TIME_SERIES__ORGANISM, oldOrganism, organism));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getMatrix() {
		return matrix;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setMatrix(String newMatrix) {
		String oldMatrix = matrix;
		matrix = newMatrix;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DataPackage.TIME_SERIES__MATRIX, oldMatrix, matrix));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public PmmUnit getTimeUnit() {
		return timeUnit;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setTimeUnit(PmmUnit newTimeUnit) {
		PmmUnit oldTimeUnit = timeUnit;
		timeUnit = newTimeUnit;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DataPackage.TIME_SERIES__TIME_UNIT, oldTimeUnit, timeUnit));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public PmmUnit getConcentrationUnit() {
		return concentrationUnit;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setConcentrationUnit(PmmUnit newConcentrationUnit) {
		PmmUnit oldConcentrationUnit = concentrationUnit;
		concentrationUnit = newConcentrationUnit;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DataPackage.TIME_SERIES__CONCENTRATION_UNIT, oldConcentrationUnit, concentrationUnit));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case DataPackage.TIME_SERIES__POINTS:
				return ((InternalEList<?>)getPoints()).basicRemove(otherEnd, msgs);
			case DataPackage.TIME_SERIES__CONDITIONS:
				return ((InternalEList<?>)getConditions()).basicRemove(otherEnd, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case DataPackage.TIME_SERIES__NAME:
				return getName();
			case DataPackage.TIME_SERIES__ID:
				return getId();
			case DataPackage.TIME_SERIES__POINTS:
				return getPoints();
			case DataPackage.TIME_SERIES__CONDITIONS:
				return getConditions();
			case DataPackage.TIME_SERIES__ORGANISM:
				return getOrganism();
			case DataPackage.TIME_SERIES__MATRIX:
				return getMatrix();
			case DataPackage.TIME_SERIES__TIME_UNIT:
				return getTimeUnit();
			case DataPackage.TIME_SERIES__CONCENTRATION_UNIT:
				return getConcentrationUnit();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case DataPackage.TIME_SERIES__NAME:
				setName((String)newValue);
				return;
			case DataPackage.TIME_SERIES__ID:
				setId((String)newValue);
				return;
			case DataPackage.TIME_SERIES__POINTS:
				getPoints().clear();
				getPoints().addAll((Collection<? extends TimeSeriesPoint>)newValue);
				return;
			case DataPackage.TIME_SERIES__CONDITIONS:
				getConditions().clear();
				getConditions().addAll((Collection<? extends Condition>)newValue);
				return;
			case DataPackage.TIME_SERIES__ORGANISM:
				setOrganism((String)newValue);
				return;
			case DataPackage.TIME_SERIES__MATRIX:
				setMatrix((String)newValue);
				return;
			case DataPackage.TIME_SERIES__TIME_UNIT:
				setTimeUnit((PmmUnit)newValue);
				return;
			case DataPackage.TIME_SERIES__CONCENTRATION_UNIT:
				setConcentrationUnit((PmmUnit)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case DataPackage.TIME_SERIES__NAME:
				setName(NAME_EDEFAULT);
				return;
			case DataPackage.TIME_SERIES__ID:
				setId(ID_EDEFAULT);
				return;
			case DataPackage.TIME_SERIES__POINTS:
				getPoints().clear();
				return;
			case DataPackage.TIME_SERIES__CONDITIONS:
				getConditions().clear();
				return;
			case DataPackage.TIME_SERIES__ORGANISM:
				setOrganism(ORGANISM_EDEFAULT);
				return;
			case DataPackage.TIME_SERIES__MATRIX:
				setMatrix(MATRIX_EDEFAULT);
				return;
			case DataPackage.TIME_SERIES__TIME_UNIT:
				setTimeUnit(TIME_UNIT_EDEFAULT);
				return;
			case DataPackage.TIME_SERIES__CONCENTRATION_UNIT:
				setConcentrationUnit(CONCENTRATION_UNIT_EDEFAULT);
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case DataPackage.TIME_SERIES__NAME:
				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
			case DataPackage.TIME_SERIES__ID:
				return ID_EDEFAULT == null ? id != null : !ID_EDEFAULT.equals(id);
			case DataPackage.TIME_SERIES__POINTS:
				return points != null && !points.isEmpty();
			case DataPackage.TIME_SERIES__CONDITIONS:
				return conditions != null && !conditions.isEmpty();
			case DataPackage.TIME_SERIES__ORGANISM:
				return ORGANISM_EDEFAULT == null ? organism != null : !ORGANISM_EDEFAULT.equals(organism);
			case DataPackage.TIME_SERIES__MATRIX:
				return MATRIX_EDEFAULT == null ? matrix != null : !MATRIX_EDEFAULT.equals(matrix);
			case DataPackage.TIME_SERIES__TIME_UNIT:
				return TIME_UNIT_EDEFAULT == null ? timeUnit != null : !TIME_UNIT_EDEFAULT.equals(timeUnit);
			case DataPackage.TIME_SERIES__CONCENTRATION_UNIT:
				return CONCENTRATION_UNIT_EDEFAULT == null ? concentrationUnit != null : !CONCENTRATION_UNIT_EDEFAULT.equals(concentrationUnit);
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (name: ");
		result.append(name);
		result.append(", id: ");
		result.append(id);
		result.append(", organism: ");
		result.append(organism);
		result.append(", matrix: ");
		result.append(matrix);
		result.append(", timeUnit: ");
		result.append(timeUnit);
		result.append(", concentrationUnit: ");
		result.append(concentrationUnit);
		result.append(')');
		return result.toString();
	}

} //TimeSeriesImpl
