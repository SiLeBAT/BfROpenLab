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
 * <p>Java-Klasse für QueryResultsBody complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="QueryResultsBody">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;element name="EventList" type="{urn:epcglobal:epcis:xsd:1}EventListType"/>
 *         &lt;element name="VocabularyList" type="{urn:epcglobal:epcis-masterdata:xsd:1}VocabularyListType"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QueryResultsBody", namespace = "urn:epcglobal:epcis-query:xsd:1", propOrder = {
    "eventList",
    "vocabularyList"
})
public class QueryResultsBody {

    @XmlElement(name = "EventList")
    protected EventListType eventList;
    @XmlElement(name = "VocabularyList")
    protected VocabularyListType vocabularyList;

    /**
     * Ruft den Wert der eventList-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link EventListType }
     *     
     */
    public EventListType getEventList() {
        return eventList;
    }

    /**
     * Legt den Wert der eventList-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link EventListType }
     *     
     */
    public void setEventList(EventListType value) {
        this.eventList = value;
    }

    /**
     * Ruft den Wert der vocabularyList-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link VocabularyListType }
     *     
     */
    public VocabularyListType getVocabularyList() {
        return vocabularyList;
    }

    /**
     * Legt den Wert der vocabularyList-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link VocabularyListType }
     *     
     */
    public void setVocabularyList(VocabularyListType value) {
        this.vocabularyList = value;
    }

}
