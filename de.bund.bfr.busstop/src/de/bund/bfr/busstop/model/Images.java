package de.bund.bfr.busstop.model;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Images {

    @XmlElement(name = "List")
    private List<String> list;

    public Images() {/*JAXB requires it */

    }

    public Images(List<String> stringList) {
        list = stringList;
    }

    public List<String> getStringList() {
        return list;
    }

}