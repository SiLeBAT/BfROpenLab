//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2016.01.28 um 03:29:13 PM CET 
//


package de.bund.bfr.epcis;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für EPCISQueryBodyType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="EPCISQueryBodyType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;element ref="{urn:epcglobal:epcis-query:xsd:1}GetQueryNames"/>
 *         &lt;element ref="{urn:epcglobal:epcis-query:xsd:1}GetQueryNamesResult"/>
 *         &lt;element ref="{urn:epcglobal:epcis-query:xsd:1}Subscribe"/>
 *         &lt;element ref="{urn:epcglobal:epcis-query:xsd:1}SubscribeResult"/>
 *         &lt;element ref="{urn:epcglobal:epcis-query:xsd:1}Unsubscribe"/>
 *         &lt;element ref="{urn:epcglobal:epcis-query:xsd:1}UnsubscribeResult"/>
 *         &lt;element ref="{urn:epcglobal:epcis-query:xsd:1}GetSubscriptionIDs"/>
 *         &lt;element ref="{urn:epcglobal:epcis-query:xsd:1}GetSubscriptionIDsResult"/>
 *         &lt;element ref="{urn:epcglobal:epcis-query:xsd:1}Poll"/>
 *         &lt;element ref="{urn:epcglobal:epcis-query:xsd:1}GetStandardVersion"/>
 *         &lt;element ref="{urn:epcglobal:epcis-query:xsd:1}GetStandardVersionResult"/>
 *         &lt;element ref="{urn:epcglobal:epcis-query:xsd:1}GetVendorVersion"/>
 *         &lt;element ref="{urn:epcglobal:epcis-query:xsd:1}GetVendorVersionResult"/>
 *         &lt;element ref="{urn:epcglobal:epcis-query:xsd:1}DuplicateNameException"/>
 *         &lt;element ref="{urn:epcglobal:epcis-query:xsd:1}InvalidURIException"/>
 *         &lt;element ref="{urn:epcglobal:epcis-query:xsd:1}NoSuchNameException"/>
 *         &lt;element ref="{urn:epcglobal:epcis-query:xsd:1}NoSuchSubscriptionException"/>
 *         &lt;element ref="{urn:epcglobal:epcis-query:xsd:1}DuplicateSubscriptionException"/>
 *         &lt;element ref="{urn:epcglobal:epcis-query:xsd:1}QueryParameterException"/>
 *         &lt;element ref="{urn:epcglobal:epcis-query:xsd:1}QueryTooLargeException"/>
 *         &lt;element ref="{urn:epcglobal:epcis-query:xsd:1}QueryTooComplexException"/>
 *         &lt;element ref="{urn:epcglobal:epcis-query:xsd:1}SubscriptionControlsException"/>
 *         &lt;element ref="{urn:epcglobal:epcis-query:xsd:1}SubscribeNotPermittedException"/>
 *         &lt;element ref="{urn:epcglobal:epcis-query:xsd:1}SecurityException"/>
 *         &lt;element ref="{urn:epcglobal:epcis-query:xsd:1}ValidationException"/>
 *         &lt;element ref="{urn:epcglobal:epcis-query:xsd:1}ImplementationException"/>
 *         &lt;element ref="{urn:epcglobal:epcis-query:xsd:1}QueryResults"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EPCISQueryBodyType", namespace = "urn:epcglobal:epcis-query:xsd:1", propOrder = {
    "getQueryNames",
    "getQueryNamesResult",
    "subscribe",
    "subscribeResult",
    "unsubscribe",
    "unsubscribeResult",
    "getSubscriptionIDs",
    "getSubscriptionIDsResult",
    "poll",
    "getStandardVersion",
    "getStandardVersionResult",
    "getVendorVersion",
    "getVendorVersionResult",
    "duplicateNameException",
    "invalidURIException",
    "noSuchNameException",
    "noSuchSubscriptionException",
    "duplicateSubscriptionException",
    "queryParameterException",
    "queryTooLargeException",
    "queryTooComplexException",
    "subscriptionControlsException",
    "subscribeNotPermittedException",
    "securityException",
    "validationException",
    "implementationException",
    "queryResults"
})
public class EPCISQueryBodyType {

    @XmlElement(name = "GetQueryNames", namespace = "urn:epcglobal:epcis-query:xsd:1")
    protected EmptyParms getQueryNames;
    @XmlElement(name = "GetQueryNamesResult", namespace = "urn:epcglobal:epcis-query:xsd:1")
    protected ArrayOfString getQueryNamesResult;
    @XmlElement(name = "Subscribe", namespace = "urn:epcglobal:epcis-query:xsd:1")
    protected Subscribe subscribe;
    @XmlElement(name = "SubscribeResult", namespace = "urn:epcglobal:epcis-query:xsd:1")
    protected VoidHolder subscribeResult;
    @XmlElement(name = "Unsubscribe", namespace = "urn:epcglobal:epcis-query:xsd:1")
    protected Unsubscribe unsubscribe;
    @XmlElement(name = "UnsubscribeResult", namespace = "urn:epcglobal:epcis-query:xsd:1")
    protected VoidHolder unsubscribeResult;
    @XmlElement(name = "GetSubscriptionIDs", namespace = "urn:epcglobal:epcis-query:xsd:1")
    protected GetSubscriptionIDs getSubscriptionIDs;
    @XmlElement(name = "GetSubscriptionIDsResult", namespace = "urn:epcglobal:epcis-query:xsd:1")
    protected ArrayOfString getSubscriptionIDsResult;
    @XmlElement(name = "Poll", namespace = "urn:epcglobal:epcis-query:xsd:1")
    protected Poll poll;
    @XmlElement(name = "GetStandardVersion", namespace = "urn:epcglobal:epcis-query:xsd:1")
    protected EmptyParms getStandardVersion;
    @XmlElement(name = "GetStandardVersionResult", namespace = "urn:epcglobal:epcis-query:xsd:1")
    protected String getStandardVersionResult;
    @XmlElement(name = "GetVendorVersion", namespace = "urn:epcglobal:epcis-query:xsd:1")
    protected EmptyParms getVendorVersion;
    @XmlElement(name = "GetVendorVersionResult", namespace = "urn:epcglobal:epcis-query:xsd:1")
    protected String getVendorVersionResult;
    @XmlElement(name = "DuplicateNameException", namespace = "urn:epcglobal:epcis-query:xsd:1")
    protected DuplicateNameException duplicateNameException;
    @XmlElement(name = "InvalidURIException", namespace = "urn:epcglobal:epcis-query:xsd:1")
    protected InvalidURIException invalidURIException;
    @XmlElement(name = "NoSuchNameException", namespace = "urn:epcglobal:epcis-query:xsd:1")
    protected NoSuchNameException noSuchNameException;
    @XmlElement(name = "NoSuchSubscriptionException", namespace = "urn:epcglobal:epcis-query:xsd:1")
    protected NoSuchSubscriptionException noSuchSubscriptionException;
    @XmlElement(name = "DuplicateSubscriptionException", namespace = "urn:epcglobal:epcis-query:xsd:1")
    protected DuplicateSubscriptionException duplicateSubscriptionException;
    @XmlElement(name = "QueryParameterException", namespace = "urn:epcglobal:epcis-query:xsd:1")
    protected QueryParameterException queryParameterException;
    @XmlElement(name = "QueryTooLargeException", namespace = "urn:epcglobal:epcis-query:xsd:1")
    protected QueryTooLargeException queryTooLargeException;
    @XmlElement(name = "QueryTooComplexException", namespace = "urn:epcglobal:epcis-query:xsd:1")
    protected QueryTooComplexException queryTooComplexException;
    @XmlElement(name = "SubscriptionControlsException", namespace = "urn:epcglobal:epcis-query:xsd:1")
    protected SubscriptionControlsException subscriptionControlsException;
    @XmlElement(name = "SubscribeNotPermittedException", namespace = "urn:epcglobal:epcis-query:xsd:1")
    protected SubscribeNotPermittedException subscribeNotPermittedException;
    @XmlElement(name = "SecurityException", namespace = "urn:epcglobal:epcis-query:xsd:1")
    protected SecurityException securityException;
    @XmlElement(name = "ValidationException", namespace = "urn:epcglobal:epcis-query:xsd:1")
    protected ValidationException validationException;
    @XmlElement(name = "ImplementationException", namespace = "urn:epcglobal:epcis-query:xsd:1")
    protected ImplementationException implementationException;
    @XmlElement(name = "QueryResults", namespace = "urn:epcglobal:epcis-query:xsd:1")
    protected QueryResults queryResults;

    /**
     * Ruft den Wert der getQueryNames-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link EmptyParms }
     *     
     */
    public EmptyParms getGetQueryNames() {
        return getQueryNames;
    }

    /**
     * Legt den Wert der getQueryNames-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link EmptyParms }
     *     
     */
    public void setGetQueryNames(EmptyParms value) {
        this.getQueryNames = value;
    }

    /**
     * Ruft den Wert der getQueryNamesResult-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfString }
     *     
     */
    public ArrayOfString getGetQueryNamesResult() {
        return getQueryNamesResult;
    }

    /**
     * Legt den Wert der getQueryNamesResult-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfString }
     *     
     */
    public void setGetQueryNamesResult(ArrayOfString value) {
        this.getQueryNamesResult = value;
    }

    /**
     * Ruft den Wert der subscribe-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Subscribe }
     *     
     */
    public Subscribe getSubscribe() {
        return subscribe;
    }

    /**
     * Legt den Wert der subscribe-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Subscribe }
     *     
     */
    public void setSubscribe(Subscribe value) {
        this.subscribe = value;
    }

    /**
     * Ruft den Wert der subscribeResult-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link VoidHolder }
     *     
     */
    public VoidHolder getSubscribeResult() {
        return subscribeResult;
    }

    /**
     * Legt den Wert der subscribeResult-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link VoidHolder }
     *     
     */
    public void setSubscribeResult(VoidHolder value) {
        this.subscribeResult = value;
    }

    /**
     * Ruft den Wert der unsubscribe-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Unsubscribe }
     *     
     */
    public Unsubscribe getUnsubscribe() {
        return unsubscribe;
    }

    /**
     * Legt den Wert der unsubscribe-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Unsubscribe }
     *     
     */
    public void setUnsubscribe(Unsubscribe value) {
        this.unsubscribe = value;
    }

    /**
     * Ruft den Wert der unsubscribeResult-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link VoidHolder }
     *     
     */
    public VoidHolder getUnsubscribeResult() {
        return unsubscribeResult;
    }

    /**
     * Legt den Wert der unsubscribeResult-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link VoidHolder }
     *     
     */
    public void setUnsubscribeResult(VoidHolder value) {
        this.unsubscribeResult = value;
    }

    /**
     * Ruft den Wert der getSubscriptionIDs-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link GetSubscriptionIDs }
     *     
     */
    public GetSubscriptionIDs getGetSubscriptionIDs() {
        return getSubscriptionIDs;
    }

    /**
     * Legt den Wert der getSubscriptionIDs-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link GetSubscriptionIDs }
     *     
     */
    public void setGetSubscriptionIDs(GetSubscriptionIDs value) {
        this.getSubscriptionIDs = value;
    }

    /**
     * Ruft den Wert der getSubscriptionIDsResult-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfString }
     *     
     */
    public ArrayOfString getGetSubscriptionIDsResult() {
        return getSubscriptionIDsResult;
    }

    /**
     * Legt den Wert der getSubscriptionIDsResult-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfString }
     *     
     */
    public void setGetSubscriptionIDsResult(ArrayOfString value) {
        this.getSubscriptionIDsResult = value;
    }

    /**
     * Ruft den Wert der poll-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Poll }
     *     
     */
    public Poll getPoll() {
        return poll;
    }

    /**
     * Legt den Wert der poll-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Poll }
     *     
     */
    public void setPoll(Poll value) {
        this.poll = value;
    }

    /**
     * Ruft den Wert der getStandardVersion-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link EmptyParms }
     *     
     */
    public EmptyParms getGetStandardVersion() {
        return getStandardVersion;
    }

    /**
     * Legt den Wert der getStandardVersion-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link EmptyParms }
     *     
     */
    public void setGetStandardVersion(EmptyParms value) {
        this.getStandardVersion = value;
    }

    /**
     * Ruft den Wert der getStandardVersionResult-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGetStandardVersionResult() {
        return getStandardVersionResult;
    }

    /**
     * Legt den Wert der getStandardVersionResult-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGetStandardVersionResult(String value) {
        this.getStandardVersionResult = value;
    }

    /**
     * Ruft den Wert der getVendorVersion-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link EmptyParms }
     *     
     */
    public EmptyParms getGetVendorVersion() {
        return getVendorVersion;
    }

    /**
     * Legt den Wert der getVendorVersion-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link EmptyParms }
     *     
     */
    public void setGetVendorVersion(EmptyParms value) {
        this.getVendorVersion = value;
    }

    /**
     * Ruft den Wert der getVendorVersionResult-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGetVendorVersionResult() {
        return getVendorVersionResult;
    }

    /**
     * Legt den Wert der getVendorVersionResult-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGetVendorVersionResult(String value) {
        this.getVendorVersionResult = value;
    }

    /**
     * Ruft den Wert der duplicateNameException-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link DuplicateNameException }
     *     
     */
    public DuplicateNameException getDuplicateNameException() {
        return duplicateNameException;
    }

    /**
     * Legt den Wert der duplicateNameException-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link DuplicateNameException }
     *     
     */
    public void setDuplicateNameException(DuplicateNameException value) {
        this.duplicateNameException = value;
    }

    /**
     * Ruft den Wert der invalidURIException-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link InvalidURIException }
     *     
     */
    public InvalidURIException getInvalidURIException() {
        return invalidURIException;
    }

    /**
     * Legt den Wert der invalidURIException-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link InvalidURIException }
     *     
     */
    public void setInvalidURIException(InvalidURIException value) {
        this.invalidURIException = value;
    }

    /**
     * Ruft den Wert der noSuchNameException-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link NoSuchNameException }
     *     
     */
    public NoSuchNameException getNoSuchNameException() {
        return noSuchNameException;
    }

    /**
     * Legt den Wert der noSuchNameException-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link NoSuchNameException }
     *     
     */
    public void setNoSuchNameException(NoSuchNameException value) {
        this.noSuchNameException = value;
    }

    /**
     * Ruft den Wert der noSuchSubscriptionException-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link NoSuchSubscriptionException }
     *     
     */
    public NoSuchSubscriptionException getNoSuchSubscriptionException() {
        return noSuchSubscriptionException;
    }

    /**
     * Legt den Wert der noSuchSubscriptionException-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link NoSuchSubscriptionException }
     *     
     */
    public void setNoSuchSubscriptionException(NoSuchSubscriptionException value) {
        this.noSuchSubscriptionException = value;
    }

    /**
     * Ruft den Wert der duplicateSubscriptionException-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link DuplicateSubscriptionException }
     *     
     */
    public DuplicateSubscriptionException getDuplicateSubscriptionException() {
        return duplicateSubscriptionException;
    }

    /**
     * Legt den Wert der duplicateSubscriptionException-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link DuplicateSubscriptionException }
     *     
     */
    public void setDuplicateSubscriptionException(DuplicateSubscriptionException value) {
        this.duplicateSubscriptionException = value;
    }

    /**
     * Ruft den Wert der queryParameterException-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link QueryParameterException }
     *     
     */
    public QueryParameterException getQueryParameterException() {
        return queryParameterException;
    }

    /**
     * Legt den Wert der queryParameterException-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link QueryParameterException }
     *     
     */
    public void setQueryParameterException(QueryParameterException value) {
        this.queryParameterException = value;
    }

    /**
     * Ruft den Wert der queryTooLargeException-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link QueryTooLargeException }
     *     
     */
    public QueryTooLargeException getQueryTooLargeException() {
        return queryTooLargeException;
    }

    /**
     * Legt den Wert der queryTooLargeException-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link QueryTooLargeException }
     *     
     */
    public void setQueryTooLargeException(QueryTooLargeException value) {
        this.queryTooLargeException = value;
    }

    /**
     * Ruft den Wert der queryTooComplexException-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link QueryTooComplexException }
     *     
     */
    public QueryTooComplexException getQueryTooComplexException() {
        return queryTooComplexException;
    }

    /**
     * Legt den Wert der queryTooComplexException-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link QueryTooComplexException }
     *     
     */
    public void setQueryTooComplexException(QueryTooComplexException value) {
        this.queryTooComplexException = value;
    }

    /**
     * Ruft den Wert der subscriptionControlsException-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link SubscriptionControlsException }
     *     
     */
    public SubscriptionControlsException getSubscriptionControlsException() {
        return subscriptionControlsException;
    }

    /**
     * Legt den Wert der subscriptionControlsException-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link SubscriptionControlsException }
     *     
     */
    public void setSubscriptionControlsException(SubscriptionControlsException value) {
        this.subscriptionControlsException = value;
    }

    /**
     * Ruft den Wert der subscribeNotPermittedException-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link SubscribeNotPermittedException }
     *     
     */
    public SubscribeNotPermittedException getSubscribeNotPermittedException() {
        return subscribeNotPermittedException;
    }

    /**
     * Legt den Wert der subscribeNotPermittedException-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link SubscribeNotPermittedException }
     *     
     */
    public void setSubscribeNotPermittedException(SubscribeNotPermittedException value) {
        this.subscribeNotPermittedException = value;
    }

    /**
     * Ruft den Wert der securityException-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link SecurityException }
     *     
     */
    public SecurityException getSecurityException() {
        return securityException;
    }

    /**
     * Legt den Wert der securityException-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link SecurityException }
     *     
     */
    public void setSecurityException(SecurityException value) {
        this.securityException = value;
    }

    /**
     * Ruft den Wert der validationException-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ValidationException }
     *     
     */
    public ValidationException getValidationException() {
        return validationException;
    }

    /**
     * Legt den Wert der validationException-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ValidationException }
     *     
     */
    public void setValidationException(ValidationException value) {
        this.validationException = value;
    }

    /**
     * Ruft den Wert der implementationException-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ImplementationException }
     *     
     */
    public ImplementationException getImplementationException() {
        return implementationException;
    }

    /**
     * Legt den Wert der implementationException-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ImplementationException }
     *     
     */
    public void setImplementationException(ImplementationException value) {
        this.implementationException = value;
    }

    /**
     * Ruft den Wert der queryResults-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link QueryResults }
     *     
     */
    public QueryResults getQueryResults() {
        return queryResults;
    }

    /**
     * Legt den Wert der queryResults-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link QueryResults }
     *     
     */
    public void setQueryResults(QueryResults value) {
        this.queryResults = value;
    }

}
