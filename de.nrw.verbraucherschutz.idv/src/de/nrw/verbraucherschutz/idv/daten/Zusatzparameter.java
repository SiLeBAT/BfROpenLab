//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2016.12.01 um 11:24:07 AM CET 
//


package de.nrw.verbraucherschutz.idv.daten;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für Zusatzparameter complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="Zusatzparameter">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded">
 *         &lt;element name="datum" type="{http://verbraucherschutz.nrw.de/idv/daten/2016.2/warenrueckverfolgung}ZusatzparameterDatum" minOccurs="0"/>
 *         &lt;element name="text" type="{http://verbraucherschutz.nrw.de/idv/daten/2016.2/warenrueckverfolgung}ZusatzparameterText" minOccurs="0"/>
 *         &lt;element name="zahl" type="{http://verbraucherschutz.nrw.de/idv/daten/2016.2/warenrueckverfolgung}ZusatzparameterZahl" minOccurs="0"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Zusatzparameter", propOrder = {
    "datumOrTextOrZahl"
})
public class Zusatzparameter {

    @XmlElements({
        @XmlElement(name = "datum", type = ZusatzparameterDatum.class),
        @XmlElement(name = "text", type = ZusatzparameterText.class),
        @XmlElement(name = "zahl", type = ZusatzparameterZahl.class)
    })
    protected List<Object> datumOrTextOrZahl;

    /**
     * Gets the value of the datumOrTextOrZahl property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the datumOrTextOrZahl property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDatumOrTextOrZahl().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ZusatzparameterDatum }
     * {@link ZusatzparameterText }
     * {@link ZusatzparameterZahl }
     * 
     * 
     */
    public List<Object> getDatumOrTextOrZahl() {
        if (datumOrTextOrZahl == null) {
            datumOrTextOrZahl = new ArrayList<Object>();
        }
        return this.datumOrTextOrZahl;
    }

}
