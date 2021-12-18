//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2016.10.06 um 01:11:47 AM CEST 
//


package de.nrw.verbraucherschutz.idv.daten;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für Analyseergebnis complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="Analyseergebnis">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="meldung" type="{http://verbraucherschutz.nrw.de/idv/daten/2016.2/warenrueckverfolgung}Meldung"/>
 *         &lt;element name="dokument" type="{http://verbraucherschutz.nrw.de/idv/daten/2016.1/dokument}dokument" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="bewertung" type="{http://verbraucherschutz.nrw.de/idv/daten/2016.2/warenrueckverfolgung}Bewertung" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Analyseergebnis", namespace = "http://verbraucherschutz.nrw.de/idv/dienste/2016.2/warenrueckverfolgung/transport", propOrder = {
    "meldung",
    "dokument",
    "bewertung"
})
public class Analyseergebnis {

    @XmlElement(namespace = "http://verbraucherschutz.nrw.de/idv/dienste/2016.2/warenrueckverfolgung/transport", required = true)
    protected Meldung meldung;
    @XmlElement(namespace = "http://verbraucherschutz.nrw.de/idv/dienste/2016.2/warenrueckverfolgung/transport")
    protected List<Dokument> dokument;
    @XmlElement(namespace = "http://verbraucherschutz.nrw.de/idv/dienste/2016.2/warenrueckverfolgung/transport")
    protected Bewertung bewertung;

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
     * Gets the value of the dokument property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the dokument property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDokument().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Dokument }
     * 
     * 
     */
    public List<Dokument> getDokument() {
        if (dokument == null) {
            dokument = new ArrayList<Dokument>();
        }
        return this.dokument;
    }

    /**
     * Ruft den Wert der bewertung-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Bewertung }
     *     
     */
    public Bewertung getBewertung() {
        return bewertung;
    }

    /**
     * Legt den Wert der bewertung-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Bewertung }
     *     
     */
    public void setBewertung(Bewertung value) {
        this.bewertung = value;
    }

}
