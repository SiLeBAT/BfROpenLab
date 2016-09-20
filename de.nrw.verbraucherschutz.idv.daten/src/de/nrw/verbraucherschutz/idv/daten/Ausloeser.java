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
 * <p>Java-Klasse für Ausloeser complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="Ausloeser">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="vorgaengerMeldung" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="wareneingaenge" type="{http://verbraucherschutz.nrw.de/idv/daten/2016.2/warenrueckverfolgung}Wareneingaenge"/>
 *         &lt;element name="warenausgaenge" type="{http://verbraucherschutz.nrw.de/idv/daten/2016.2/warenrueckverfolgung}Warenausgaenge"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Ausloeser", propOrder = {
    "vorgaengerMeldung",
    "wareneingaenge",
    "warenausgaenge"
})
public class Ausloeser {

    @XmlElement(required = true)
    protected String vorgaengerMeldung;
    @XmlElement(required = true)
    protected Wareneingaenge wareneingaenge;
    @XmlElement(required = true)
    protected Warenausgaenge warenausgaenge;

    /**
     * Ruft den Wert der vorgaengerMeldung-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVorgaengerMeldung() {
        return vorgaengerMeldung;
    }

    /**
     * Legt den Wert der vorgaengerMeldung-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVorgaengerMeldung(String value) {
        this.vorgaengerMeldung = value;
    }

    /**
     * Ruft den Wert der wareneingaenge-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Wareneingaenge }
     *     
     */
    public Wareneingaenge getWareneingaenge() {
        return wareneingaenge;
    }

    /**
     * Legt den Wert der wareneingaenge-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Wareneingaenge }
     *     
     */
    public void setWareneingaenge(Wareneingaenge value) {
        this.wareneingaenge = value;
    }

    /**
     * Ruft den Wert der warenausgaenge-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Warenausgaenge }
     *     
     */
    public Warenausgaenge getWarenausgaenge() {
        return warenausgaenge;
    }

    /**
     * Legt den Wert der warenausgaenge-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Warenausgaenge }
     *     
     */
    public void setWarenausgaenge(Warenausgaenge value) {
        this.warenausgaenge = value;
    }

}
