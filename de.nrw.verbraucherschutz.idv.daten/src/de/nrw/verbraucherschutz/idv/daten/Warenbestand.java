//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2016.09.20 um 02:24:37 PM CEST 
//


package de.nrw.verbraucherschutz.idv.daten;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für Warenbestand complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="Warenbestand">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="betrieb" type="{http://verbraucherschutz.nrw.de/idv/daten/2016.2/warenrueckverfolgung}Betrieb"/>
 *         &lt;element name="warenumfang" type="{http://verbraucherschutz.nrw.de/idv/daten/2016.2/warenrueckverfolgung}Warenumfang"/>
 *         &lt;element name="produkt" type="{http://verbraucherschutz.nrw.de/idv/daten/2016.2/warenrueckverfolgung}Produkt"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Warenbestand", propOrder = {
    "betrieb",
    "warenumfang",
    "produkt"
})
public class Warenbestand {

    @XmlElement(required = true)
    protected Betrieb betrieb;
    @XmlElement(required = true)
    protected Warenumfang warenumfang;
    @XmlElement(required = true)
    protected Produkt produkt;

    /**
     * Ruft den Wert der betrieb-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Betrieb }
     *     
     */
    public Betrieb getBetrieb() {
        return betrieb;
    }

    /**
     * Legt den Wert der betrieb-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Betrieb }
     *     
     */
    public void setBetrieb(Betrieb value) {
        this.betrieb = value;
    }

    /**
     * Ruft den Wert der warenumfang-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Warenumfang }
     *     
     */
    public Warenumfang getWarenumfang() {
        return warenumfang;
    }

    /**
     * Legt den Wert der warenumfang-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Warenumfang }
     *     
     */
    public void setWarenumfang(Warenumfang value) {
        this.warenumfang = value;
    }

    /**
     * Ruft den Wert der produkt-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Produkt }
     *     
     */
    public Produkt getProdukt() {
        return produkt;
    }

    /**
     * Legt den Wert der produkt-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Produkt }
     *     
     */
    public void setProdukt(Produkt value) {
        this.produkt = value;
    }

}
