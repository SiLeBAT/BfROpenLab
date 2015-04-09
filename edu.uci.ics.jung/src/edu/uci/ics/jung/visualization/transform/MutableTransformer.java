/*
 * Copyright (c) 2005, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 *
 * Created on Jul 3, 2005
 */

package edu.uci.ics.jung.visualization.transform;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import edu.uci.ics.jung.visualization.transform.shape.ShapeTransformer;
import edu.uci.ics.jung.visualization.util.ChangeEventSupport;

/**
 * Provides an API for the mutation of a transformer
 * and for adding listeners for changes on the transformer
 * 
 * @author Tom Nelson 
 *
 *
 */
public interface MutableTransformer extends ShapeTransformer, ChangeEventSupport {
    
    void translate(double dx, double dy);
    
    void setTranslate(double dx, double dy);
    
    void scale(double sx, double sy, Point2D point);
    
    void setScale(double sx, double sy, Point2D point);
    
    void rotate(double radians, Point2D point);
    
    void rotate(double radians, double x, double y);
    
    void shear(double shx, double shy, Point2D from);
    
    void concatenate(AffineTransform transform);
    
    void preConcatenate(AffineTransform transform);
    
    double getScaleX();
    
    double getScaleY();
    
    double getScale();
    
    double getTranslateX();
    
    double getTranslateY();
    
    double getShearX();
    
    double getShearY();

    AffineTransform getTransform();
    
    void setToIdentity();
    
    double getRotation();
    
}
