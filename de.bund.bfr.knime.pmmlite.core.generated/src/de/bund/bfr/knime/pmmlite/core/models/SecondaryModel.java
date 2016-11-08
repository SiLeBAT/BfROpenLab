/**
 */
package de.bund.bfr.knime.pmmlite.core.models;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Secondary Model</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.SecondaryModel#getFormula <em>Formula</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.SecondaryModel#getData <em>Data</em>}</li>
 * </ul>
 *
 * @see de.bund.bfr.knime.pmmlite.core.models.ModelsPackage#getSecondaryModel()
 * @model
 * @generated
 */
public interface SecondaryModel extends Model {
	/**
	 * Returns the value of the '<em><b>Formula</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Formula</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Formula</em>' reference.
	 * @see #setFormula(SecondaryModelFormula)
	 * @see de.bund.bfr.knime.pmmlite.core.models.ModelsPackage#getSecondaryModel_Formula()
	 * @model
	 * @generated
	 */
	SecondaryModelFormula getFormula();

	/**
	 * Sets the value of the '{@link de.bund.bfr.knime.pmmlite.core.models.SecondaryModel#getFormula <em>Formula</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Formula</em>' reference.
	 * @see #getFormula()
	 * @generated
	 */
	void setFormula(SecondaryModelFormula value);

	/**
	 * Returns the value of the '<em><b>Data</b></em>' reference list.
	 * The list contents are of type {@link de.bund.bfr.knime.pmmlite.core.models.PrimaryModel}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Data</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Data</em>' reference list.
	 * @see de.bund.bfr.knime.pmmlite.core.models.ModelsPackage#getSecondaryModel_Data()
	 * @model
	 * @generated
	 */
	EList<PrimaryModel> getData();

} // SecondaryModel
