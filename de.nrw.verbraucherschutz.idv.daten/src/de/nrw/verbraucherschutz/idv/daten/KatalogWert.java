//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2016.08.31 um 01:45:20 PM CEST 
//


package de.nrw.verbraucherschutz.idv.daten;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;


/**
 * 
 * 				Ein Katalogwert beschreibt einen einzelnen Katalogeintrag. Er
 * 				gehoert dabei immer zu einer bestimmten Version eines Katalog
 * 				innerhalb eines Katalogverzeichnisses in einem bestimmten Scope.
 * 				Zulaessige Scopes sind: BUND, NRW, LABOR, KOMMUNE. Beispiel:
 * 				
 * 				Ein Katalogwert kann, wenn der dazu gehörige Katalog es vorsieht
 * 				auch als freier Eintrag ohne Code übertragen werden. Beispiel:
 * 				
 * 			
 * 
 * <p>Java-Klasse für KatalogWert complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="KatalogWert">
 *   &lt;simpleContent>
 *     &lt;extension base="&lt;http://verbraucherschutz.nrw.de/idv/daten/2016.1/katalogsystem>langtextEmpty">
 *       &lt;attribute name="scope" use="required" type="{http://verbraucherschutz.nrw.de/idv/daten/2016.1/katalogsystem}kuerzel" />
 *       &lt;attribute name="verz" use="required" type="{http://verbraucherschutz.nrw.de/idv/daten/2016.1/katalogsystem}kuerzel" />
 *       &lt;attribute name="katalog" use="required" type="{http://verbraucherschutz.nrw.de/idv/daten/2016.1/katalogsystem}kuerzel" />
 *       &lt;attribute name="version" use="required" type="{http://verbraucherschutz.nrw.de/idv/daten/2016.1/katalogsystem}langtextEmpty" />
 *       &lt;attribute name="code" type="{http://verbraucherschutz.nrw.de/idv/daten/2016.1/katalogsystem}kuerzel" />
 *     &lt;/extension>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "KatalogWert", namespace = "http://verbraucherschutz.nrw.de/idv/daten/2016.1/katalogsystem", propOrder = {
    "value"
})
public class KatalogWert {

    @XmlValue
    protected String value;
    @XmlAttribute(name = "scope", required = true)
    protected String scope;
    @XmlAttribute(name = "verz", required = true)
    protected String verz;
    @XmlAttribute(name = "katalog", required = true)
    protected String katalog;
    @XmlAttribute(name = "version", required = true)
    protected String version;
    @XmlAttribute(name = "code")
    protected String code;

    /**
     * Ruft den Wert der value-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValue() {
        return value;
    }

    /**
     * Legt den Wert der value-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Ruft den Wert der scope-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getScope() {
        return scope;
    }

    /**
     * Legt den Wert der scope-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setScope(String value) {
        this.scope = value;
    }

    /**
     * Ruft den Wert der verz-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVerz() {
        return verz;
    }

    /**
     * Legt den Wert der verz-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVerz(String value) {
        this.verz = value;
    }

    /**
     * Ruft den Wert der katalog-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getKatalog() {
        return katalog;
    }

    /**
     * Legt den Wert der katalog-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setKatalog(String value) {
        this.katalog = value;
    }

    /**
     * Ruft den Wert der version-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVersion() {
        return version;
    }

    /**
     * Legt den Wert der version-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVersion(String value) {
        this.version = value;
    }

    /**
     * Ruft den Wert der code-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCode() {
        return code;
    }

    /**
     * Legt den Wert der code-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCode(String value) {
        this.code = value;
    }

}
