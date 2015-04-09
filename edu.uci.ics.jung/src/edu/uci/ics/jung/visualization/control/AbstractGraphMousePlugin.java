/*
 * Copyright (c) 2005, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 *
 * Created on Jul 6, 2005
 */

package edu.uci.ics.jung.visualization.control;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;

/**
 * a base class for GraphMousePlugin instances. Holds some members
 * common to all GraphMousePlugins 
 * @author thomasnelson
 *
 */
public abstract class AbstractGraphMousePlugin implements GraphMousePlugin {

	/**
	 * modifiers to compare against mouse event modifiers
	 */
    protected int modifiers;
    
    /**
     * the location in the View where the mouse was pressed
     */
    protected Point down;
    
    /**
     * the special cursor that plugins may display
     */
    protected Cursor cursor;
    
    /**
     * create an instance with passed values
     * @param modifiers
     */
    public AbstractGraphMousePlugin(int modifiers) {
        this.modifiers = modifiers;
    }
    
    /**
     * getter for mouse modifiers
     */
    public int getModifiers() {
        return modifiers;
    }

    /**
     * setter for mouse modifiers
     */
    public void setModifiers(int modifiers) {
        this.modifiers = modifiers;
    }
    
    /**
     * check the mouse event modifiers against the
     * instance member modifiers. Default implementation
     * checks equality. Can be overridden to test with a mask
     */
    public boolean checkModifiers(MouseEvent e) {
        return e.getModifiers() == modifiers;
    }

    /**
     * @return Returns the cursor.
     */
    public Cursor getCursor() {
        return cursor;
    }

    /**
     * @param cursor The cursor to set.
     */
    public void setCursor(Cursor cursor) {
        this.cursor = cursor;
    }
}
