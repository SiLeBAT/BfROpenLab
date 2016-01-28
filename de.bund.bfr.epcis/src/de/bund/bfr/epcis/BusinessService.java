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
 * <p>Java-Klasse für BusinessService complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="BusinessService">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="BusinessServiceName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ServiceTransaction" type="{http://www.unece.org/cefact/namespaces/StandardBusinessDocumentHeader}ServiceTransaction" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BusinessService", namespace = "http://www.unece.org/cefact/namespaces/StandardBusinessDocumentHeader", propOrder = {
    "businessServiceName",
    "serviceTransaction"
})
public class BusinessService {

    @XmlElement(name = "BusinessServiceName", namespace = "http://www.unece.org/cefact/namespaces/StandardBusinessDocumentHeader")
    protected String businessServiceName;
    @XmlElement(name = "ServiceTransaction", namespace = "http://www.unece.org/cefact/namespaces/StandardBusinessDocumentHeader")
    protected ServiceTransaction serviceTransaction;

    /**
     * Ruft den Wert der businessServiceName-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBusinessServiceName() {
        return businessServiceName;
    }

    /**
     * Legt den Wert der businessServiceName-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBusinessServiceName(String value) {
        this.businessServiceName = value;
    }

    /**
     * Ruft den Wert der serviceTransaction-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ServiceTransaction }
     *     
     */
    public ServiceTransaction getServiceTransaction() {
        return serviceTransaction;
    }

    /**
     * Legt den Wert der serviceTransaction-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ServiceTransaction }
     *     
     */
    public void setServiceTransaction(ServiceTransaction value) {
        this.serviceTransaction = value;
    }

}
