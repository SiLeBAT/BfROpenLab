/**
 */
package de.bund.bfr.knime.pmmlite.core.models;

import de.bund.bfr.knime.pmmlite.core.data.TimeSeries;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Primary Model</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.PrimaryModel#getFormula <em>Formula</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.PrimaryModel#getData <em>Data</em>}</li>
 * </ul>
 *
 * @see de.bund.bfr.knime.pmmlite.core.models.ModelsPackage#getPrimaryModel()
 * @model
 * @generated
 */
public interface PrimaryModel extends Model {
	/**
	 * Returns the value of the '<em><b>Formula</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Formula</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Formula</em>' reference.
	 * @see #setFormula(PrimaryModelFormula)
	 * @see de.bund.bfr.knime.pmmlite.core.models.ModelsPackage#getPrimaryModel_Formula()
	 * @model
	 * @generated
	 */
	PrimaryModelFormula getFormula();

	/**
	 * Sets the value of the '{@link de.bund.bfr.knime.pmmlite.core.models.PrimaryModel#getFormula <em>Formula</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Formula</em>' reference.
	 * @see #getFormula()
	 * @generated
	 */
	void setFormula(PrimaryModelFormula value);

	/**
	 * Returns the value of the '<em><b>Data</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Data</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Data</em>' reference.
	 * @see #setData(TimeSeries)
	 * @see de.bund.bfr.knime.pmmlite.core.models.ModelsPackage#getPrimaryModel_Data()
	 * @model
	 * @generated
	 */
	TimeSeries getData();

	/**
	 * Sets the value of the '{@link de.bund.bfr.knime.pmmlite.core.models.PrimaryModel#getData <em>Data</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Data</em>' reference.
	 * @see #getData()
	 * @generated
	 */
	void setData(TimeSeries value);

} // PrimaryModel
