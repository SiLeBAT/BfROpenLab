//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2016.10.26 um 10:27:53 PM CEST 
//


package de.nrw.verbraucherschutz.idv.daten;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für dokument complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="dokument">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="metadaten" type="{http://verbraucherschutz.nrw.de/idv/daten/2016.1/dokument}metadaten"/>
 *         &lt;element name="content" type="{http://verbraucherschutz.nrw.de/idv/daten/2016.1/dokument}content"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "dokument", namespace = "http://verbraucherschutz.nrw.de/idv/daten/2016.1/dokument", propOrder = {
    "metadaten",
    "content"
})
public class Dokument {

    @XmlElement(required = true)
    protected Metadaten metadaten;
    @XmlElement(required = true)
    protected Content content;

    /**
     * Ruft den Wert der metadaten-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Metadaten }
     *     
     */
    public Metadaten getMetadaten() {
        return metadaten;
    }

    /**
     * Legt den Wert der metadaten-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Metadaten }
     *     
     */
    public void setMetadaten(Metadaten value) {
        this.metadaten = value;
    }

    /**
     * Ruft den Wert der content-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Content }
     *     
     */
    public Content getContent() {
        return content;
    }

    /**
     * Legt den Wert der content-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Content }
     *     
     */
    public void setContent(Content value) {
        this.content = value;
    }

}
