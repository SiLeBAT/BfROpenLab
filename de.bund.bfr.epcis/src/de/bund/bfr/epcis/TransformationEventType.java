//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2016.01.28 um 03:29:13 PM CET 
//


package de.bund.bfr.epcis;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import org.w3c.dom.Element;


/**
 * 
 * 			Transformation Event captures an event in which inputs are consumed
 * 			and outputs are produced
 *            	
 * 
 * <p>Java-Klasse für TransformationEventType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="TransformationEventType">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:epcglobal:epcis:xsd:1}EPCISEventType">
 *       &lt;sequence>
 *         &lt;element name="inputEPCList" type="{urn:epcglobal:epcis:xsd:1}EPCListType" minOccurs="0"/>
 *         &lt;element name="inputQuantityList" type="{urn:epcglobal:epcis:xsd:1}QuantityListType" minOccurs="0"/>
 *         &lt;element name="outputEPCList" type="{urn:epcglobal:epcis:xsd:1}EPCListType" minOccurs="0"/>
 *         &lt;element name="outputQuantityList" type="{urn:epcglobal:epcis:xsd:1}QuantityListType" minOccurs="0"/>
 *         &lt;element name="transformationID" type="{urn:epcglobal:epcis:xsd:1}TransformationIDType" minOccurs="0"/>
 *         &lt;element name="bizStep" type="{urn:epcglobal:epcis:xsd:1}BusinessStepIDType" minOccurs="0"/>
 *         &lt;element name="disposition" type="{urn:epcglobal:epcis:xsd:1}DispositionIDType" minOccurs="0"/>
 *         &lt;element name="readPoint" type="{urn:epcglobal:epcis:xsd:1}ReadPointType" minOccurs="0"/>
 *         &lt;element name="bizLocation" type="{urn:epcglobal:epcis:xsd:1}BusinessLocationType" minOccurs="0"/>
 *         &lt;element name="bizTransactionList" type="{urn:epcglobal:epcis:xsd:1}BusinessTransactionListType" minOccurs="0"/>
 *         &lt;element name="sourceList" type="{urn:epcglobal:epcis:xsd:1}SourceListType" minOccurs="0"/>
 *         &lt;element name="destinationList" type="{urn:epcglobal:epcis:xsd:1}DestinationListType" minOccurs="0"/>
 *         &lt;element name="ilmd" type="{urn:epcglobal:epcis:xsd:1}ILMDType" minOccurs="0"/>
 *         &lt;element name="extension" type="{urn:epcglobal:epcis:xsd:1}TransformationEventExtensionType" minOccurs="0"/>
 *         &lt;any processContents='lax' namespace='##other' maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;anyAttribute processContents='lax'/>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TransformationEventType", propOrder = {
    "inputEPCList",
    "inputQuantityList",
    "outputEPCList",
    "outputQuantityList",
    "transformationID",
    "bizStep",
    "disposition",
    "readPoint",
    "bizLocation",
    "bizTransactionList",
    "sourceList",
    "destinationList",
    "ilmd",
    "extension",
    "any"
})
public class TransformationEventType
    extends EPCISEventType
{

    protected EPCListType inputEPCList;
    protected QuantityListType inputQuantityList;
    protected EPCListType outputEPCList;
    protected QuantityListType outputQuantityList;
    @XmlSchemaType(name = "anyURI")
    protected String transformationID;
    @XmlSchemaType(name = "anyURI")
    protected String bizStep;
    @XmlSchemaType(name = "anyURI")
    protected String disposition;
    protected ReadPointType readPoint;
    protected BusinessLocationType bizLocation;
    protected BusinessTransactionListType bizTransactionList;
    protected SourceListType sourceList;
    protected DestinationListType destinationList;
    protected ILMDType ilmd;
    protected TransformationEventExtensionType extension;
    @XmlAnyElement(lax = true)
    protected List<Object> any;

    /**
     * Ruft den Wert der inputEPCList-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link EPCListType }
     *     
     */
    public EPCListType getInputEPCList() {
        return inputEPCList;
    }

    /**
     * Legt den Wert der inputEPCList-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link EPCListType }
     *     
     */
    public void setInputEPCList(EPCListType value) {
        this.inputEPCList = value;
    }

    /**
     * Ruft den Wert der inputQuantityList-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link QuantityListType }
     *     
     */
    public QuantityListType getInputQuantityList() {
        return inputQuantityList;
    }

    /**
     * Legt den Wert der inputQuantityList-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link QuantityListType }
     *     
     */
    public void setInputQuantityList(QuantityListType value) {
        this.inputQuantityList = value;
    }

    /**
     * Ruft den Wert der outputEPCList-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link EPCListType }
     *     
     */
    public EPCListType getOutputEPCList() {
        return outputEPCList;
    }

    /**
     * Legt den Wert der outputEPCList-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link EPCListType }
     *     
     */
    public void setOutputEPCList(EPCListType value) {
        this.outputEPCList = value;
    }

    /**
     * Ruft den Wert der outputQuantityList-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link QuantityListType }
     *     
     */
    public QuantityListType getOutputQuantityList() {
        return outputQuantityList;
    }

    /**
     * Legt den Wert der outputQuantityList-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link QuantityListType }
     *     
     */
    public void setOutputQuantityList(QuantityListType value) {
        this.outputQuantityList = value;
    }

    /**
     * Ruft den Wert der transformationID-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTransformationID() {
        return transformationID;
    }

    /**
     * Legt den Wert der transformationID-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTransformationID(String value) {
        this.transformationID = value;
    }

    /**
     * Ruft den Wert der bizStep-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBizStep() {
        return bizStep;
    }

    /**
     * Legt den Wert der bizStep-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBizStep(String value) {
        this.bizStep = value;
    }

    /**
     * Ruft den Wert der disposition-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDisposition() {
        return disposition;
    }

    /**
     * Legt den Wert der disposition-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDisposition(String value) {
        this.disposition = value;
    }

    /**
     * Ruft den Wert der readPoint-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ReadPointType }
     *     
     */
    public ReadPointType getReadPoint() {
        return readPoint;
    }

    /**
     * Legt den Wert der readPoint-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ReadPointType }
     *     
     */
    public void setReadPoint(ReadPointType value) {
        this.readPoint = value;
    }

    /**
     * Ruft den Wert der bizLocation-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BusinessLocationType }
     *     
     */
    public BusinessLocationType getBizLocation() {
        return bizLocation;
    }

    /**
     * Legt den Wert der bizLocation-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BusinessLocationType }
     *     
     */
    public void setBizLocation(BusinessLocationType value) {
        this.bizLocation = value;
    }

    /**
     * Ruft den Wert der bizTransactionList-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BusinessTransactionListType }
     *     
     */
    public BusinessTransactionListType getBizTransactionList() {
        return bizTransactionList;
    }

    /**
     * Legt den Wert der bizTransactionList-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BusinessTransactionListType }
     *     
     */
    public void setBizTransactionList(BusinessTransactionListType value) {
        this.bizTransactionList = value;
    }

    /**
     * Ruft den Wert der sourceList-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link SourceListType }
     *     
     */
    public SourceListType getSourceList() {
        return sourceList;
    }

    /**
     * Legt den Wert der sourceList-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link SourceListType }
     *     
     */
    public void setSourceList(SourceListType value) {
        this.sourceList = value;
    }

    /**
     * Ruft den Wert der destinationList-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link DestinationListType }
     *     
     */
    public DestinationListType getDestinationList() {
        return destinationList;
    }

    /**
     * Legt den Wert der destinationList-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link DestinationListType }
     *     
     */
    public void setDestinationList(DestinationListType value) {
        this.destinationList = value;
    }

    /**
     * Ruft den Wert der ilmd-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ILMDType }
     *     
     */
    public ILMDType getIlmd() {
        return ilmd;
    }

    /**
     * Legt den Wert der ilmd-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ILMDType }
     *     
     */
    public void setIlmd(ILMDType value) {
        this.ilmd = value;
    }

    /**
     * Ruft den Wert der extension-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TransformationEventExtensionType }
     *     
     */
    public TransformationEventExtensionType getExtension() {
        return extension;
    }

    /**
     * Legt den Wert der extension-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TransformationEventExtensionType }
     *     
     */
    public void setExtension(TransformationEventExtensionType value) {
        this.extension = value;
    }

    /**
     * Gets the value of the any property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the any property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAny().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * {@link Element }
     * 
     * 
     */
    public List<Object> getAny() {
        if (any == null) {
            any = new ArrayList<Object>();
        }
        return this.any;
    }

}
