//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2016.11.30 um 03:35:33 PM CET 
//


package de.nrw.verbraucherschutz.idv.daten;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für Bewertung complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="Bewertung">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="kontrollpunktbewertung" type="{http://verbraucherschutz.nrw.de/idv/daten/2016.2/warenrueckverfolgung}Kontrollpunktbewertung"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Bewertung", propOrder = {
    "kontrollpunktbewertung"
})
public class Bewertung {

    @XmlElement(required = true)
    protected Kontrollpunktbewertung kontrollpunktbewertung;

    /**
     * Ruft den Wert der kontrollpunktbewertung-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Kontrollpunktbewertung }
     *     
     */
    public Kontrollpunktbewertung getKontrollpunktbewertung() {
        return kontrollpunktbewertung;
    }

    /**
     * Legt den Wert der kontrollpunktbewertung-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Kontrollpunktbewertung }
     *     
     */
    public void setKontrollpunktbewertung(Kontrollpunktbewertung value) {
        this.kontrollpunktbewertung = value;
    }

}
