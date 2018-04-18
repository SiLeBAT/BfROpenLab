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
package de.bund.bfr.knime.pmmlite.core.models.impl;

import de.bund.bfr.knime.pmmlite.core.models.ModelsPackage;
import de.bund.bfr.knime.pmmlite.core.models.ParameterValue;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EMap;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import org.eclipse.emf.ecore.util.EcoreEMap;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Parameter Value</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.impl.ParameterValueImpl#getValue <em>Value</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.impl.ParameterValueImpl#getError <em>Error</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.impl.ParameterValueImpl#getT <em>T</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.impl.ParameterValueImpl#getP <em>P</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.impl.ParameterValueImpl#getCorrelations <em>Correlations</em>}</li>
 * </ul>
 *
 * @generated
 */
public class ParameterValueImpl extends EObjectImpl implements ParameterValue {
	/**
	 * The default value of the '{@link #getValue() <em>Value</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getValue()
	 * @generated
	 * @ordered
	 */
	protected static final Double VALUE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getValue() <em>Value</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getValue()
	 * @generated
	 * @ordered
	 */
	protected Double value = VALUE_EDEFAULT;

	/**
	 * The default value of the '{@link #getError() <em>Error</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getError()
	 * @generated
	 * @ordered
	 */
	protected static final Double ERROR_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getError() <em>Error</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getError()
	 * @generated
	 * @ordered
	 */
	protected Double error = ERROR_EDEFAULT;

	/**
	 * The default value of the '{@link #getT() <em>T</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getT()
	 * @generated
	 * @ordered
	 */
	protected static final Double T_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getT() <em>T</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getT()
	 * @generated
	 * @ordered
	 */
	protected Double t = T_EDEFAULT;

	/**
	 * The default value of the '{@link #getP() <em>P</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getP()
	 * @generated
	 * @ordered
	 */
	protected static final Double P_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getP() <em>P</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getP()
	 * @generated
	 * @ordered
	 */
	protected Double p = P_EDEFAULT;

	/**
	 * The cached value of the '{@link #getCorrelations() <em>Correlations</em>}' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCorrelations()
	 * @generated
	 * @ordered
	 */
	protected EMap<String, Double> correlations;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ParameterValueImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ModelsPackage.Literals.PARAMETER_VALUE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Double getValue() {
		return value;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setValue(Double newValue) {
		Double oldValue = value;
		value = newValue;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ModelsPackage.PARAMETER_VALUE__VALUE, oldValue, value));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Double getError() {
		return error;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setError(Double newError) {
		Double oldError = error;
		error = newError;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ModelsPackage.PARAMETER_VALUE__ERROR, oldError, error));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Double getT() {
		return t;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setT(Double newT) {
		Double oldT = t;
		t = newT;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ModelsPackage.PARAMETER_VALUE__T, oldT, t));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Double getP() {
		return p;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setP(Double newP) {
		Double oldP = p;
		p = newP;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ModelsPackage.PARAMETER_VALUE__P, oldP, p));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EMap<String, Double> getCorrelations() {
		if (correlations == null) {
			correlations = new EcoreEMap<String,Double>(ModelsPackage.Literals.STRING_TO_DOUBLE_MAP_ENTRY, StringToDoubleMapEntryImpl.class, this, ModelsPackage.PARAMETER_VALUE__CORRELATIONS);
		}
		return correlations;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case ModelsPackage.PARAMETER_VALUE__CORRELATIONS:
				return ((InternalEList<?>)getCorrelations()).basicRemove(otherEnd, msgs);
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
			case ModelsPackage.PARAMETER_VALUE__VALUE:
				return getValue();
			case ModelsPackage.PARAMETER_VALUE__ERROR:
				return getError();
			case ModelsPackage.PARAMETER_VALUE__T:
				return getT();
			case ModelsPackage.PARAMETER_VALUE__P:
				return getP();
			case ModelsPackage.PARAMETER_VALUE__CORRELATIONS:
				if (coreType) return getCorrelations();
				else return getCorrelations().map();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case ModelsPackage.PARAMETER_VALUE__VALUE:
				setValue((Double)newValue);
				return;
			case ModelsPackage.PARAMETER_VALUE__ERROR:
				setError((Double)newValue);
				return;
			case ModelsPackage.PARAMETER_VALUE__T:
				setT((Double)newValue);
				return;
			case ModelsPackage.PARAMETER_VALUE__P:
				setP((Double)newValue);
				return;
			case ModelsPackage.PARAMETER_VALUE__CORRELATIONS:
				((EStructuralFeature.Setting)getCorrelations()).set(newValue);
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
			case ModelsPackage.PARAMETER_VALUE__VALUE:
				setValue(VALUE_EDEFAULT);
				return;
			case ModelsPackage.PARAMETER_VALUE__ERROR:
				setError(ERROR_EDEFAULT);
				return;
			case ModelsPackage.PARAMETER_VALUE__T:
				setT(T_EDEFAULT);
				return;
			case ModelsPackage.PARAMETER_VALUE__P:
				setP(P_EDEFAULT);
				return;
			case ModelsPackage.PARAMETER_VALUE__CORRELATIONS:
				getCorrelations().clear();
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
			case ModelsPackage.PARAMETER_VALUE__VALUE:
				return VALUE_EDEFAULT == null ? value != null : !VALUE_EDEFAULT.equals(value);
			case ModelsPackage.PARAMETER_VALUE__ERROR:
				return ERROR_EDEFAULT == null ? error != null : !ERROR_EDEFAULT.equals(error);
			case ModelsPackage.PARAMETER_VALUE__T:
				return T_EDEFAULT == null ? t != null : !T_EDEFAULT.equals(t);
			case ModelsPackage.PARAMETER_VALUE__P:
				return P_EDEFAULT == null ? p != null : !P_EDEFAULT.equals(p);
			case ModelsPackage.PARAMETER_VALUE__CORRELATIONS:
				return correlations != null && !correlations.isEmpty();
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
		result.append(" (value: ");
		result.append(value);
		result.append(", error: ");
		result.append(error);
		result.append(", t: ");
		result.append(t);
		result.append(", p: ");
		result.append(p);
		result.append(')');
		return result.toString();
	}

} //ParameterValueImpl
