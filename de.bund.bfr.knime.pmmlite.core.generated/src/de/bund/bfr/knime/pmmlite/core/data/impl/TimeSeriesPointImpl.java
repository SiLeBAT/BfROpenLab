/**
 */
package de.bund.bfr.knime.pmmlite.core.data.impl;

import de.bund.bfr.knime.pmmlite.core.data.DataPackage;
import de.bund.bfr.knime.pmmlite.core.data.TimeSeriesPoint;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Time Series Point</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.data.impl.TimeSeriesPointImpl#getTime <em>Time</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.data.impl.TimeSeriesPointImpl#getConcentration <em>Concentration</em>}</li>
 * </ul>
 *
 * @generated
 */
public class TimeSeriesPointImpl extends EObjectImpl implements TimeSeriesPoint {
	/**
	 * The default value of the '{@link #getTime() <em>Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTime()
	 * @generated
	 * @ordered
	 */
	protected static final double TIME_EDEFAULT = 0.0;

	/**
	 * The cached value of the '{@link #getTime() <em>Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTime()
	 * @generated
	 * @ordered
	 */
	protected double time = TIME_EDEFAULT;

	/**
	 * The default value of the '{@link #getConcentration() <em>Concentration</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getConcentration()
	 * @generated
	 * @ordered
	 */
	protected static final double CONCENTRATION_EDEFAULT = 0.0;

	/**
	 * The cached value of the '{@link #getConcentration() <em>Concentration</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getConcentration()
	 * @generated
	 * @ordered
	 */
	protected double concentration = CONCENTRATION_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected TimeSeriesPointImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return DataPackage.Literals.TIME_SERIES_POINT;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public double getTime() {
		return time;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setTime(double newTime) {
		double oldTime = time;
		time = newTime;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DataPackage.TIME_SERIES_POINT__TIME, oldTime, time));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public double getConcentration() {
		return concentration;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setConcentration(double newConcentration) {
		double oldConcentration = concentration;
		concentration = newConcentration;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DataPackage.TIME_SERIES_POINT__CONCENTRATION, oldConcentration, concentration));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case DataPackage.TIME_SERIES_POINT__TIME:
				return getTime();
			case DataPackage.TIME_SERIES_POINT__CONCENTRATION:
				return getConcentration();
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
			case DataPackage.TIME_SERIES_POINT__TIME:
				setTime((Double)newValue);
				return;
			case DataPackage.TIME_SERIES_POINT__CONCENTRATION:
				setConcentration((Double)newValue);
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
			case DataPackage.TIME_SERIES_POINT__TIME:
				setTime(TIME_EDEFAULT);
				return;
			case DataPackage.TIME_SERIES_POINT__CONCENTRATION:
				setConcentration(CONCENTRATION_EDEFAULT);
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
			case DataPackage.TIME_SERIES_POINT__TIME:
				return time != TIME_EDEFAULT;
			case DataPackage.TIME_SERIES_POINT__CONCENTRATION:
				return concentration != CONCENTRATION_EDEFAULT;
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
		result.append(" (time: ");
		result.append(time);
		result.append(", concentration: ");
		result.append(concentration);
		result.append(')');
		return result.toString();
	}

} //TimeSeriesPointImpl
