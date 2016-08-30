//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2016.08.30 um 04:08:29 PM CEST 
//


package de.nrw.verbraucherschutz.idv.daten;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für kontrollpunktmeldung complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="kontrollpunktmeldung">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="meldung" type="{http://verbraucherschutz.nrw.de/idv/daten/2016.2/warenrueckverfolgung}meldung"/>
 *         &lt;element name="betrieb" type="{http://verbraucherschutz.nrw.de/idv/daten/2016.2/warenrueckverfolgung}betrieb"/>
 *         &lt;element name="wareneingaenge" type="{http://verbraucherschutz.nrw.de/idv/daten/2016.2/warenrueckverfolgung}wareneingaenge"/>
 *         &lt;element name="produktionen" type="{http://verbraucherschutz.nrw.de/idv/daten/2016.2/warenrueckverfolgung}produktionen"/>
 *         &lt;element name="warenausgaenge" type="{http://verbraucherschutz.nrw.de/idv/daten/2016.2/warenrueckverfolgung}warenausgaenge"/>
 *         &lt;element name="warenbestaende" type="{http://verbraucherschutz.nrw.de/idv/daten/2016.2/warenrueckverfolgung}warenbestaende"/>
 *         &lt;element name="ausloeser" type="{http://verbraucherschutz.nrw.de/idv/daten/2016.2/warenrueckverfolgung}ausloeser"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "kontrollpunktmeldung", namespace = "http://verbraucherschutz.nrw.de/idv/daten/2016.2/warenrueckverfolgung/transport", propOrder = {
    "meldung",
    "betrieb",
    "wareneingaenge",
    "produktionen",
    "warenausgaenge",
    "warenbestaende",
    "ausloeser"
})
public class Kontrollpunktmeldung {

    @XmlElement(namespace = "http://verbraucherschutz.nrw.de/idv/daten/2016.2/warenrueckverfolgung/transport", required = true)
    protected Meldung meldung;
    @XmlElement(namespace = "http://verbraucherschutz.nrw.de/idv/daten/2016.2/warenrueckverfolgung/transport", required = true)
    protected Betrieb betrieb;
    @XmlElement(namespace = "http://verbraucherschutz.nrw.de/idv/daten/2016.2/warenrueckverfolgung/transport", required = true, nillable = true)
    protected Wareneingaenge wareneingaenge;
    @XmlElement(namespace = "http://verbraucherschutz.nrw.de/idv/daten/2016.2/warenrueckverfolgung/transport", required = true, nillable = true)
    protected Produktionen produktionen;
    @XmlElement(namespace = "http://verbraucherschutz.nrw.de/idv/daten/2016.2/warenrueckverfolgung/transport", required = true, nillable = true)
    protected Warenausgaenge warenausgaenge;
    @XmlElement(namespace = "http://verbraucherschutz.nrw.de/idv/daten/2016.2/warenrueckverfolgung/transport", required = true, nillable = true)
    protected Warenbestaende warenbestaende;
    @XmlElement(namespace = "http://verbraucherschutz.nrw.de/idv/daten/2016.2/warenrueckverfolgung/transport", required = true, nillable = true)
    protected Ausloeser ausloeser;

    /**
     * Ruft den Wert der meldung-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Meldung }
     *     
     */
    public Meldung getMeldung() {
        return meldung;
    }

    /**
     * Legt den Wert der meldung-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Meldung }
     *     
     */
    public void setMeldung(Meldung value) {
        this.meldung = value;
    }

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
     * Ruft den Wert der produktionen-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Produktionen }
     *     
     */
    public Produktionen getProduktionen() {
        return produktionen;
    }

    /**
     * Legt den Wert der produktionen-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Produktionen }
     *     
     */
    public void setProduktionen(Produktionen value) {
        this.produktionen = value;
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

    /**
     * Ruft den Wert der warenbestaende-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Warenbestaende }
     *     
     */
    public Warenbestaende getWarenbestaende() {
        return warenbestaende;
    }

    /**
     * Legt den Wert der warenbestaende-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Warenbestaende }
     *     
     */
    public void setWarenbestaende(Warenbestaende value) {
        this.warenbestaende = value;
    }

    /**
     * Ruft den Wert der ausloeser-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Ausloeser }
     *     
     */
    public Ausloeser getAusloeser() {
        return ausloeser;
    }

    /**
     * Legt den Wert der ausloeser-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Ausloeser }
     *     
     */
    public void setAusloeser(Ausloeser value) {
        this.ausloeser = value;
    }

}
