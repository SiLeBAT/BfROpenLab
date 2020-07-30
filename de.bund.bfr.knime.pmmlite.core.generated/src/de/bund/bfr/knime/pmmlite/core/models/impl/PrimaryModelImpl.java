/*******************************************************************************
 * Copyright (c) 2019 German Federal Institute for Risk Assessment (BfR)
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

import de.bund.bfr.knime.pmmlite.core.data.TimeSeries;

import de.bund.bfr.knime.pmmlite.core.models.ModelsPackage;
import de.bund.bfr.knime.pmmlite.core.models.ParameterValue;
import de.bund.bfr.knime.pmmlite.core.models.PrimaryModel;
import de.bund.bfr.knime.pmmlite.core.models.PrimaryModelFormula;
import de.bund.bfr.knime.pmmlite.core.models.VariableRange;

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
 * An implementation of the model object '<em><b>Primary Model</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.impl.PrimaryModelImpl#getName <em>Name</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.impl.PrimaryModelImpl#getId <em>Id</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.impl.PrimaryModelImpl#getSse <em>Sse</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.impl.PrimaryModelImpl#getMse <em>Mse</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.impl.PrimaryModelImpl#getRmse <em>Rmse</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.impl.PrimaryModelImpl#getR2 <em>R2</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.impl.PrimaryModelImpl#getAic <em>Aic</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.impl.PrimaryModelImpl#getDegreesOfFreedom <em>Degrees Of Freedom</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.impl.PrimaryModelImpl#getVariableRanges <em>Variable Ranges</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.impl.PrimaryModelImpl#getParamValues <em>Param Values</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.impl.PrimaryModelImpl#getAssignments <em>Assignments</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.impl.PrimaryModelImpl#getFormula <em>Formula</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.impl.PrimaryModelImpl#getData <em>Data</em>}</li>
 * </ul>
 *
 * @generated
 */
public class PrimaryModelImpl extends EObjectImpl implements PrimaryModel {
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
	 * The default value of the '{@link #getSse() <em>Sse</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSse()
	 * @generated
	 * @ordered
	 */
	protected static final Double SSE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getSse() <em>Sse</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSse()
	 * @generated
	 * @ordered
	 */
	protected Double sse = SSE_EDEFAULT;

	/**
	 * The default value of the '{@link #getMse() <em>Mse</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMse()
	 * @generated
	 * @ordered
	 */
	protected static final Double MSE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getMse() <em>Mse</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMse()
	 * @generated
	 * @ordered
	 */
	protected Double mse = MSE_EDEFAULT;

	/**
	 * The default value of the '{@link #getRmse() <em>Rmse</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRmse()
	 * @generated
	 * @ordered
	 */
	protected static final Double RMSE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getRmse() <em>Rmse</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRmse()
	 * @generated
	 * @ordered
	 */
	protected Double rmse = RMSE_EDEFAULT;

	/**
	 * The default value of the '{@link #getR2() <em>R2</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getR2()
	 * @generated
	 * @ordered
	 */
	protected static final Double R2_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getR2() <em>R2</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getR2()
	 * @generated
	 * @ordered
	 */
	protected Double r2 = R2_EDEFAULT;

	/**
	 * The default value of the '{@link #getAic() <em>Aic</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAic()
	 * @generated
	 * @ordered
	 */
	protected static final Double AIC_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getAic() <em>Aic</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAic()
	 * @generated
	 * @ordered
	 */
	protected Double aic = AIC_EDEFAULT;

	/**
	 * The default value of the '{@link #getDegreesOfFreedom() <em>Degrees Of Freedom</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDegreesOfFreedom()
	 * @generated
	 * @ordered
	 */
	protected static final Integer DEGREES_OF_FREEDOM_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getDegreesOfFreedom() <em>Degrees Of Freedom</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDegreesOfFreedom()
	 * @generated
	 * @ordered
	 */
	protected Integer degreesOfFreedom = DEGREES_OF_FREEDOM_EDEFAULT;

	/**
	 * The cached value of the '{@link #getVariableRanges() <em>Variable Ranges</em>}' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getVariableRanges()
	 * @generated
	 * @ordered
	 */
	protected EMap<String, VariableRange> variableRanges;

	/**
	 * The cached value of the '{@link #getParamValues() <em>Param Values</em>}' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getParamValues()
	 * @generated
	 * @ordered
	 */
	protected EMap<String, ParameterValue> paramValues;

	/**
	 * The cached value of the '{@link #getAssignments() <em>Assignments</em>}' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAssignments()
	 * @generated
	 * @ordered
	 */
	protected EMap<String, String> assignments;

	/**
	 * The cached value of the '{@link #getFormula() <em>Formula</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFormula()
	 * @generated
	 * @ordered
	 */
	protected PrimaryModelFormula formula;

	/**
	 * The cached value of the '{@link #getData() <em>Data</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getData()
	 * @generated
	 * @ordered
	 */
	protected TimeSeries data;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected PrimaryModelImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ModelsPackage.Literals.PRIMARY_MODEL;
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
			eNotify(new ENotificationImpl(this, Notification.SET, ModelsPackage.PRIMARY_MODEL__NAME, oldName, name));
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
			eNotify(new ENotificationImpl(this, Notification.SET, ModelsPackage.PRIMARY_MODEL__ID, oldId, id));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Double getSse() {
		return sse;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setSse(Double newSse) {
		Double oldSse = sse;
		sse = newSse;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ModelsPackage.PRIMARY_MODEL__SSE, oldSse, sse));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Double getMse() {
		return mse;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setMse(Double newMse) {
		Double oldMse = mse;
		mse = newMse;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ModelsPackage.PRIMARY_MODEL__MSE, oldMse, mse));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Double getRmse() {
		return rmse;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setRmse(Double newRmse) {
		Double oldRmse = rmse;
		rmse = newRmse;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ModelsPackage.PRIMARY_MODEL__RMSE, oldRmse, rmse));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Double getR2() {
		return r2;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setR2(Double newR2) {
		Double oldR2 = r2;
		r2 = newR2;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ModelsPackage.PRIMARY_MODEL__R2, oldR2, r2));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Double getAic() {
		return aic;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setAic(Double newAic) {
		Double oldAic = aic;
		aic = newAic;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ModelsPackage.PRIMARY_MODEL__AIC, oldAic, aic));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Integer getDegreesOfFreedom() {
		return degreesOfFreedom;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setDegreesOfFreedom(Integer newDegreesOfFreedom) {
		Integer oldDegreesOfFreedom = degreesOfFreedom;
		degreesOfFreedom = newDegreesOfFreedom;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ModelsPackage.PRIMARY_MODEL__DEGREES_OF_FREEDOM, oldDegreesOfFreedom, degreesOfFreedom));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EMap<String, VariableRange> getVariableRanges() {
		if (variableRanges == null) {
			variableRanges = new EcoreEMap<String,VariableRange>(ModelsPackage.Literals.STRING_TO_VARIABLE_RANGE_MAP_ENTRY, StringToVariableRangeMapEntryImpl.class, this, ModelsPackage.PRIMARY_MODEL__VARIABLE_RANGES);
		}
		return variableRanges;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EMap<String, ParameterValue> getParamValues() {
		if (paramValues == null) {
			paramValues = new EcoreEMap<String,ParameterValue>(ModelsPackage.Literals.STRING_TO_PARAMETER_VALUE_MAP_ENTRY, StringToParameterValueMapEntryImpl.class, this, ModelsPackage.PRIMARY_MODEL__PARAM_VALUES);
		}
		return paramValues;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EMap<String, String> getAssignments() {
		if (assignments == null) {
			assignments = new EcoreEMap<String,String>(ModelsPackage.Literals.STRING_TO_STRING_MAP_ENTRY, StringToStringMapEntryImpl.class, this, ModelsPackage.PRIMARY_MODEL__ASSIGNMENTS);
		}
		return assignments;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public PrimaryModelFormula getFormula() {
		if (formula != null && formula.eIsProxy()) {
			InternalEObject oldFormula = (InternalEObject)formula;
			formula = (PrimaryModelFormula)eResolveProxy(oldFormula);
			if (formula != oldFormula) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, ModelsPackage.PRIMARY_MODEL__FORMULA, oldFormula, formula));
			}
		}
		return formula;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public PrimaryModelFormula basicGetFormula() {
		return formula;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setFormula(PrimaryModelFormula newFormula) {
		PrimaryModelFormula oldFormula = formula;
		formula = newFormula;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ModelsPackage.PRIMARY_MODEL__FORMULA, oldFormula, formula));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public TimeSeries getData() {
		if (data != null && data.eIsProxy()) {
			InternalEObject oldData = (InternalEObject)data;
			data = (TimeSeries)eResolveProxy(oldData);
			if (data != oldData) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, ModelsPackage.PRIMARY_MODEL__DATA, oldData, data));
			}
		}
		return data;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public TimeSeries basicGetData() {
		return data;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setData(TimeSeries newData) {
		TimeSeries oldData = data;
		data = newData;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ModelsPackage.PRIMARY_MODEL__DATA, oldData, data));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case ModelsPackage.PRIMARY_MODEL__VARIABLE_RANGES:
				return ((InternalEList<?>)getVariableRanges()).basicRemove(otherEnd, msgs);
			case ModelsPackage.PRIMARY_MODEL__PARAM_VALUES:
				return ((InternalEList<?>)getParamValues()).basicRemove(otherEnd, msgs);
			case ModelsPackage.PRIMARY_MODEL__ASSIGNMENTS:
				return ((InternalEList<?>)getAssignments()).basicRemove(otherEnd, msgs);
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
			case ModelsPackage.PRIMARY_MODEL__NAME:
				return getName();
			case ModelsPackage.PRIMARY_MODEL__ID:
				return getId();
			case ModelsPackage.PRIMARY_MODEL__SSE:
				return getSse();
			case ModelsPackage.PRIMARY_MODEL__MSE:
				return getMse();
			case ModelsPackage.PRIMARY_MODEL__RMSE:
				return getRmse();
			case ModelsPackage.PRIMARY_MODEL__R2:
				return getR2();
			case ModelsPackage.PRIMARY_MODEL__AIC:
				return getAic();
			case ModelsPackage.PRIMARY_MODEL__DEGREES_OF_FREEDOM:
				return getDegreesOfFreedom();
			case ModelsPackage.PRIMARY_MODEL__VARIABLE_RANGES:
				if (coreType) return getVariableRanges();
				else return getVariableRanges().map();
			case ModelsPackage.PRIMARY_MODEL__PARAM_VALUES:
				if (coreType) return getParamValues();
				else return getParamValues().map();
			case ModelsPackage.PRIMARY_MODEL__ASSIGNMENTS:
				if (coreType) return getAssignments();
				else return getAssignments().map();
			case ModelsPackage.PRIMARY_MODEL__FORMULA:
				if (resolve) return getFormula();
				return basicGetFormula();
			case ModelsPackage.PRIMARY_MODEL__DATA:
				if (resolve) return getData();
				return basicGetData();
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
			case ModelsPackage.PRIMARY_MODEL__NAME:
				setName((String)newValue);
				return;
			case ModelsPackage.PRIMARY_MODEL__ID:
				setId((String)newValue);
				return;
			case ModelsPackage.PRIMARY_MODEL__SSE:
				setSse((Double)newValue);
				return;
			case ModelsPackage.PRIMARY_MODEL__MSE:
				setMse((Double)newValue);
				return;
			case ModelsPackage.PRIMARY_MODEL__RMSE:
				setRmse((Double)newValue);
				return;
			case ModelsPackage.PRIMARY_MODEL__R2:
				setR2((Double)newValue);
				return;
			case ModelsPackage.PRIMARY_MODEL__AIC:
				setAic((Double)newValue);
				return;
			case ModelsPackage.PRIMARY_MODEL__DEGREES_OF_FREEDOM:
				setDegreesOfFreedom((Integer)newValue);
				return;
			case ModelsPackage.PRIMARY_MODEL__VARIABLE_RANGES:
				((EStructuralFeature.Setting)getVariableRanges()).set(newValue);
				return;
			case ModelsPackage.PRIMARY_MODEL__PARAM_VALUES:
				((EStructuralFeature.Setting)getParamValues()).set(newValue);
				return;
			case ModelsPackage.PRIMARY_MODEL__ASSIGNMENTS:
				((EStructuralFeature.Setting)getAssignments()).set(newValue);
				return;
			case ModelsPackage.PRIMARY_MODEL__FORMULA:
				setFormula((PrimaryModelFormula)newValue);
				return;
			case ModelsPackage.PRIMARY_MODEL__DATA:
				setData((TimeSeries)newValue);
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
			case ModelsPackage.PRIMARY_MODEL__NAME:
				setName(NAME_EDEFAULT);
				return;
			case ModelsPackage.PRIMARY_MODEL__ID:
				setId(ID_EDEFAULT);
				return;
			case ModelsPackage.PRIMARY_MODEL__SSE:
				setSse(SSE_EDEFAULT);
				return;
			case ModelsPackage.PRIMARY_MODEL__MSE:
				setMse(MSE_EDEFAULT);
				return;
			case ModelsPackage.PRIMARY_MODEL__RMSE:
				setRmse(RMSE_EDEFAULT);
				return;
			case ModelsPackage.PRIMARY_MODEL__R2:
				setR2(R2_EDEFAULT);
				return;
			case ModelsPackage.PRIMARY_MODEL__AIC:
				setAic(AIC_EDEFAULT);
				return;
			case ModelsPackage.PRIMARY_MODEL__DEGREES_OF_FREEDOM:
				setDegreesOfFreedom(DEGREES_OF_FREEDOM_EDEFAULT);
				return;
			case ModelsPackage.PRIMARY_MODEL__VARIABLE_RANGES:
				getVariableRanges().clear();
				return;
			case ModelsPackage.PRIMARY_MODEL__PARAM_VALUES:
				getParamValues().clear();
				return;
			case ModelsPackage.PRIMARY_MODEL__ASSIGNMENTS:
				getAssignments().clear();
				return;
			case ModelsPackage.PRIMARY_MODEL__FORMULA:
				setFormula((PrimaryModelFormula)null);
				return;
			case ModelsPackage.PRIMARY_MODEL__DATA:
				setData((TimeSeries)null);
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
			case ModelsPackage.PRIMARY_MODEL__NAME:
				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
			case ModelsPackage.PRIMARY_MODEL__ID:
				return ID_EDEFAULT == null ? id != null : !ID_EDEFAULT.equals(id);
			case ModelsPackage.PRIMARY_MODEL__SSE:
				return SSE_EDEFAULT == null ? sse != null : !SSE_EDEFAULT.equals(sse);
			case ModelsPackage.PRIMARY_MODEL__MSE:
				return MSE_EDEFAULT == null ? mse != null : !MSE_EDEFAULT.equals(mse);
			case ModelsPackage.PRIMARY_MODEL__RMSE:
				return RMSE_EDEFAULT == null ? rmse != null : !RMSE_EDEFAULT.equals(rmse);
			case ModelsPackage.PRIMARY_MODEL__R2:
				return R2_EDEFAULT == null ? r2 != null : !R2_EDEFAULT.equals(r2);
			case ModelsPackage.PRIMARY_MODEL__AIC:
				return AIC_EDEFAULT == null ? aic != null : !AIC_EDEFAULT.equals(aic);
			case ModelsPackage.PRIMARY_MODEL__DEGREES_OF_FREEDOM:
				return DEGREES_OF_FREEDOM_EDEFAULT == null ? degreesOfFreedom != null : !DEGREES_OF_FREEDOM_EDEFAULT.equals(degreesOfFreedom);
			case ModelsPackage.PRIMARY_MODEL__VARIABLE_RANGES:
				return variableRanges != null && !variableRanges.isEmpty();
			case ModelsPackage.PRIMARY_MODEL__PARAM_VALUES:
				return paramValues != null && !paramValues.isEmpty();
			case ModelsPackage.PRIMARY_MODEL__ASSIGNMENTS:
				return assignments != null && !assignments.isEmpty();
			case ModelsPackage.PRIMARY_MODEL__FORMULA:
				return formula != null;
			case ModelsPackage.PRIMARY_MODEL__DATA:
				return data != null;
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
		result.append(", sse: ");
		result.append(sse);
		result.append(", mse: ");
		result.append(mse);
		result.append(", rmse: ");
		result.append(rmse);
		result.append(", r2: ");
		result.append(r2);
		result.append(", aic: ");
		result.append(aic);
		result.append(", degreesOfFreedom: ");
		result.append(degreesOfFreedom);
		result.append(')');
		return result.toString();
	}

} //PrimaryModelImpl
