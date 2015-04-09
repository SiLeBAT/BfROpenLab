/*
 * Copyright (c) 2003, the JUNG Project and the Regents of the University 
 * of California
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * http://jung.sourceforge.net/license.txt for a description.
 */
/*
 * Created on Feb 17, 2004
 */
package edu.uci.ics.jung.visualization.control;

import java.awt.event.MouseEvent;

/**
 * This interface allows users to register listeners to register to receive
 * vertex clicks.
 * 
 * @author danyelf
 */
public interface GraphMouseListener<V> {

	void graphClicked(V v, MouseEvent me);
	void graphPressed(V v, MouseEvent me);
	void graphReleased(V v, MouseEvent me);

}
