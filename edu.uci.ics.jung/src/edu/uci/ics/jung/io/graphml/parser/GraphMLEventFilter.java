/*
 * Copyright (c) 2008, the JUNG Project and the Regents of the University
 * of California
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * http://jung.sourceforge.net/license.txt for a description.
 */

package edu.uci.ics.jung.io.graphml.parser;

import javax.xml.stream.EventFilter;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.events.XMLEvent;

/**
 * Filter to ignore unsupported XML events.
 *
 * @author Nathan Mittler - nathan.mittler@gmail.com
 */
public class GraphMLEventFilter implements EventFilter {    

    public boolean accept(XMLEvent event) {
        switch( event.getEventType() ) {
        case XMLStreamConstants.START_ELEMENT:                        
        case XMLStreamConstants.END_ELEMENT:
        case XMLStreamConstants.CHARACTERS:
        case XMLStreamConstants.ATTRIBUTE:
        case XMLStreamConstants.NAMESPACE:
        case XMLStreamConstants.START_DOCUMENT:
        case XMLStreamConstants.END_DOCUMENT: {
            return true;
        }
        default: {
            return false;
        }
        }
    }
    
}
