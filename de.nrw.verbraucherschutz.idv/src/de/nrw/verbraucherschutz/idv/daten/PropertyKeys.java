//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2016.11.30 um 03:35:33 PM CET 
//


package de.nrw.verbraucherschutz.idv.daten;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für PropertyKeys.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="PropertyKeys">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="CORRELATIONID"/>
 *     &lt;enumeration value="ORGID"/>
 *     &lt;enumeration value="LASTMODIFIED"/>
 *     &lt;enumeration value="MSGID"/>
 *     &lt;enumeration value="RECEIVER"/>
 *     &lt;enumeration value="SENDER"/>
 *     &lt;enumeration value="USERID"/>
 *     &lt;enumeration value="ROLEID"/>
 *     &lt;enumeration value="NOEXPORT"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "PropertyKeys", namespace = "http://verbraucherschutz.nrw.de/idv/daten/2010.1/kommunikation")
@XmlEnum
public enum PropertyKeys {


    /**
     * 
     * 						IDV-weit eindeutige Kennung.
     * 						Wenn sie einmal verwendet wurde,
     * 						darf sie nie wieder verwendet
     * 						werden. Über die CorrelationID
     * 						werden asynchrone
     * 						Request/Response oder
     * 						Notification/NotificationResponse
     * 						Nachrichten zueinander in
     * 						Verbindung gebracht.
     * 
     * 						Wenn ein System einen Request an
     * 						ein anderes System absetzt, muss
     * 						das sendende System eine
     * 						CorrelationID erzeugen und in
     * 						dieses Property stellen. Das
     * 						Empfängersystem muss in der
     * 						Response-Nachricht diese
     * 						CorrelationID verwenden, damit
     * 						das Sendersystem erkennen kann,
     * 						zu welchem offenen Request eine
     * 						Response gehört.
     * 
     * 						Dieses Property darf nur genau
     * 						einmal auftauchen.
     * 					
     * 
     */
    CORRELATIONID,

    /**
     * 
     * 						Dies ist die primäre
     * 						Organisationseinheit eines
     * 						IDV-Benutzers. Die
     * 						Organisationseinheit muss einen
     * 						Wert haben, der auch im
     * 						zentralen Active Directory
     * 						bekannt ist. Wenn ein System
     * 						einen Request oder eine
     * 						Notification abschickt, dann
     * 						muss es hier die
     * 						Organisationseinheit des
     * 						Benutzers, der diesen Request
     * 						erzeugt hat, angeben. Umgekehrt
     * 						muss das Empfängersystem beim
     * 						Versenden einer Response nur die
     * 						OrgID aus dem Request kopieren.
     * 						OrgID zusammen mit UserID und
     * 						RoleID ermöglicht es den
     * 						Systemen zu überpüfen, welche
     * 						Rechte ein Benutzer hinsichtlich
     * 						Datenzugriffen besitzt. Dies
     * 						setzt allerdings voraus, dass
     * 						jeglicher Benutzer nicht zur im
     * 						Active Directory bekannt ist
     * 						sondern auch in jeder
     * 						Applikation.
     * 
     * 						Dieses Property darf nur genau
     * 						einmal auftauchen.
     * 					
     * 
     */
    ORGID,

    /**
     * 
     * 						Zeitstempel einer Änderung im
     * 						Format YYYY-MM-DDTHH:mm:ss, also
     * 						2010-02-19T17:56:14, die Uhrzeit
     * 						ist in UTC anzugeben ! Besonders
     * 						bei der
     * 						Stammdatensynchronisation
     * 						enthält dieses Feld den
     * 						Zeitstempel der letzten Änderung
     * 						an einem Datensatz, egal an
     * 						welcher Stelle des
     * 						Betriebsobjektes die Änderung
     * 						aufgetreten ist.
     * 
     * 						Dieses Property darf nur genau
     * 						einmal auftauchen.
     * 					
     * 
     */
    LASTMODIFIED,

    /**
     * Identifiziert einen zusammengehörenden Nachrichtenstrom der Stammdatensynchronisation. 
     * Wenn ein Fachsystem Stammdatenänderungen meldet, so kann es eine MSGID vergeben. 
     * 
     */
    MSGID,

    /**
     * 
     * 						Name einer Applikations-Kennung
     * 						wie sie am ESB registriert wird.
     * 						Aktuell bekannte und erwartete
     * 						Applikations-Kennungen: BALVI,
     * 						IDV-SFDP, ESB, TSK. Falls
     * 						Applikationen innerhalb der
     * 						Produktionsumgebung in mehreren
     * 						Instanzen laufen, müssen hier
     * 						die IDs erweitert werden um
     * 						Instanzkennungen z.B.
     * 						IDV-SFDP-ID1.
     * 
     * 						Gibt es mehr als einen
     * 						Empfänger, dann ist der Wert =
     * 						systemA, systemB, systemX usw.
     * 						also eine Liste von Systemen,
     * 						Namen getrennt durch Komma.
     * 
     * 						Es wird verwendet bei
     * 						Notifications mit mehreren
     * 						Subscribern, die aber trotzdem
     * 						nicht immer alle Nachrichten
     * 						erhalten wollen. Bei der
     * 						Stammdatensynchronisation
     * 						könnten potentiell alle Systeme
     * 						den Stammdatendatz bekommen. Aus
     * 						Datensicherheitsgründen dürfen
     * 						aber nicht alle Systeme bestimme
     * 						Updates erhalten. Darum muss
     * 						IDV-SFDP pro Datensatz auch eine
     * 						Liste von RECEIVER-Systemen
     * 						angeben.
     * 					
     * 
     */
    RECEIVER,

    /**
     * 
     * 						Gleicher Inhalt wie bei
     * 						RECEIVER, nur dass sich hier ein
     * 						sendendes System mit seiner
     * 						ApplikationsID einträgt. Der
     * 						Wert enthält immer nur genau
     * 						EINE ApplikationsID, niemals
     * 						eine Liste.
     * 
     * 						Dieses Property taucht höchstens
     * 						einmal auf.
     * 
     * 						Verwendet wird es dort, wo
     * 						mehrere Systeme den gleichen
     * 						Request bei einem Zielsystem
     * 						aufrufen können und das
     * 						ZIelsystem bei der Response im
     * 						RECEIVER-Feld angeben muss,
     * 						welches System diese Response
     * 						bekommen soll. Dazu kopiert das
     * 						aufgerufene System bei der
     * 						Response den SENDER in den
     * 						RECEIVER. In diesem Fall darf es
     * 						nur genau einen RECEIVER geben.
     * 					
     * 
     */
    SENDER,

    /**
     * 
     * 						Dies ist die Benutzerkennung
     * 						eines IDV-Benutzers. Sie muss
     * 						einen Wert haben, der auch im
     * 						zentralen Active Directory
     * 						bekannt ist. Wenn ein System
     * 						einen Request oder eine
     * 						Notification abschickt, dann
     * 						muss es hier die UserID des
     * 						Benutzers, der diesen Request
     * 						erzeugt hat, angeben. Umgekehrt
     * 						muss das Empfängersystem beim
     * 						Versenden einer Response nur die
     * 						UserID aus dem Request kopieren.
     * 						OrgID, RoleID zusammen mit
     * 						UserID ermöglicht es den
     * 						Systemen zu überpüfen, welche
     * 						Rechte ein Benutzer hinsichtlich
     * 						Datenzugriffen besitzt. Dies
     * 						setzt allerdings voraus, dass
     * 						jeglicher Benutzer nicht zur im
     * 						Active Directory bekannt ist
     * 						sondern auch in jeder
     * 						Applikation.
     * 
     * 						Dieses Property darf nur genau
     * 						einmal auftauchen.
     * 					
     * 
     */
    USERID,

    /**
     * 
     * 						Es handelt sich um die fachliche
     * 						Rolle des Benutzers, unter der
     * 						er diese Änderung getroffen hat.
     * 						Die fachlichen Rollen sind
     * 						ebenfalls im Active Directory
     * 						hinterlegt und sagen aus, für
     * 						welchen Fachbereich ein User
     * 						Daten lesen und/oder
     * 						modifizieren darf. Hat ein
     * 						Benutzer mehrere Rollen, so wird
     * 						hier die Rolle angegeben, mit
     * 						der er die Änderung abgeschickt
     * 						hat.
     * 					
     * 
     */
    ROLEID,

    /**
     * 
     * 						Flag für den Export. Wird nur vom ADB Adapter in speziellen Fällen genutzt um einen
     *             Export von IDV-SFDB zu unterdrücken. Muss dann mit true belegt sein.
     * 					
     * 
     */
    NOEXPORT;

    public String value() {
        return name();
    }

    public static PropertyKeys fromValue(String v) {
        return valueOf(v);
    }

}
