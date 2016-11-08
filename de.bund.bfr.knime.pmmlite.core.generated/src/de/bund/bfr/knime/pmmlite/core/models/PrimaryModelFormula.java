/**
 */
package de.bund.bfr.knime.pmmlite.core.models;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Primary Model Formula</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.PrimaryModelFormula#getIndepVar <em>Indep Var</em>}</li>
 *   <li>{@link de.bund.bfr.knime.pmmlite.core.models.PrimaryModelFormula#getInitialParam <em>Initial Param</em>}</li>
 * </ul>
 *
 * @see de.bund.bfr.knime.pmmlite.core.models.ModelsPackage#getPrimaryModelFormula()
 * @model
 * @generated
 */
public interface PrimaryModelFormula extends ModelFormula {
	/**
	 * Returns the value of the '<em><b>Indep Var</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Indep Var</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Indep Var</em>' containment reference.
	 * @see #setIndepVar(Variable)
	 * @see de.bund.bfr.knime.pmmlite.core.models.ModelsPackage#getPrimaryModelFormula_IndepVar()
	 * @model containment="true"
	 * @generated
	 */
	Variable getIndepVar();

	/**
	 * Sets the value of the '{@link de.bund.bfr.knime.pmmlite.core.models.PrimaryModelFormula#getIndepVar <em>Indep Var</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Indep Var</em>' containment reference.
	 * @see #getIndepVar()
	 * @generated
	 */
	void setIndepVar(Variable value);

	/**
	 * Returns the value of the '<em><b>Initial Param</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Initial Param</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Initial Param</em>' attribute.
	 * @see #setInitialParam(String)
	 * @see de.bund.bfr.knime.pmmlite.core.models.ModelsPackage#getPrimaryModelFormula_InitialParam()
	 * @model
	 * @generated
	 */
	String getInitialParam();

	/**
	 * Sets the value of the '{@link de.bund.bfr.knime.pmmlite.core.models.PrimaryModelFormula#getInitialParam <em>Initial Param</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Initial Param</em>' attribute.
	 * @see #getInitialParam()
	 * @generated
	 */
	void setInitialParam(String value);

} // PrimaryModelFormula
