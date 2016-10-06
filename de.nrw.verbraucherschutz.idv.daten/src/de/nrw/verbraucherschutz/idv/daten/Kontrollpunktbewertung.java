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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für Kontrollpunktbewertung complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="Kontrollpunktbewertung">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="warenbewegungsbewertung" type="{http://verbraucherschutz.nrw.de/idv/daten/2016.2/warenrueckverfolgung}Warenbewegungsbewertung" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *       &lt;attribute name="nummer" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Kontrollpunktbewertung", propOrder = {
    "warenbewegungsbewertung"
})
public class Kontrollpunktbewertung {

    @XmlElement(required = true)
    protected List<Warenbewegungsbewertung> warenbewegungsbewertung;
    @XmlAttribute(name = "nummer", required = true)
    protected String nummer;

    /**
     * Gets the value of the warenbewegungsbewertung property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the warenbewegungsbewertung property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getWarenbewegungsbewertung().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Warenbewegungsbewertung }
     * 
     * 
     */
    public List<Warenbewegungsbewertung> getWarenbewegungsbewertung() {
        if (warenbewegungsbewertung == null) {
            warenbewegungsbewertung = new ArrayList<Warenbewegungsbewertung>();
        }
        return this.warenbewegungsbewertung;
    }

    /**
     * Ruft den Wert der nummer-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNummer() {
        return nummer;
    }

    /**
     * Legt den Wert der nummer-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNummer(String value) {
        this.nummer = value;
    }

}
