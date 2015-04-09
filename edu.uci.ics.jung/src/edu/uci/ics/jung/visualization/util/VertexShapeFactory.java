/*
 * Copyright (c) 2003, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 * 
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 * 
 * Created on Jul 20, 2004
 */
package edu.uci.ics.jung.visualization.util;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ConstantTransformer;

/**
 * A utility class for generating <code>Shape</code>s for drawing vertices.  
 * The available shapes include rectangles, rounded rectangles, ellipses,
 * regular polygons, and regular stars.  The dimensions of the requested 
 * shapes are defined by the specified vertex size function (specified by
 * a <code>Transformer<V,Integer></code>) and vertex aspect ratio function 
 * (specified by a <code>Transformer<V,Float></code>) implementations: the width
 * of the bounding box of the shape is given by the vertex size, and the
 * height is given by the size multiplied by the vertex's aspect ratio.
 *  
 * @author Joshua O'Madadhain
 */
public class VertexShapeFactory<V>
{
    protected Transformer<V,Integer> vsf;
    protected Transformer<V,Float> varf;
    
    /**
     * Creates a <code>VertexShapeFactory</code> with the specified 
     * vertex size and aspect ratio functions.
     */
    public VertexShapeFactory(Transformer<V,Integer> vsf, Transformer<V,Float> varf)
    {
        this.vsf = vsf;
        this.varf = varf;
    }
    
    /**
     * Creates a <code>VertexShapeFactory</code> with a constant size of
     * 10 and a constant aspect ratio of 1.
     */
    @SuppressWarnings("unchecked")
	public VertexShapeFactory()
    {
        this(new ConstantTransformer(10), 
            new ConstantTransformer(1.0f));
    }
    
    private static final Rectangle2D theRectangle = new Rectangle2D.Float();
    /**
     * Returns a <code>Rectangle2D</code> whose width and 
     * height are defined by this instance's size and
     * aspect ratio functions for this vertex.
     */
    public Rectangle2D getRectangle(V v)
    {
        float width = vsf.transform(v);
        float height = width * varf.transform(v);
        float h_offset = -(width / 2);
        float v_offset = -(height / 2);
        theRectangle.setFrame(h_offset, v_offset, width, height);
        return theRectangle;
    }

    private static final Ellipse2D theEllipse = new Ellipse2D.Float();
    /**
     * Returns a <code>Ellipse2D</code> whose width and 
     * height are defined by this instance's size and
     * aspect ratio functions for this vertex.
     */
    public Ellipse2D getEllipse(V v)
    {
        theEllipse.setFrame(getRectangle(v));
        return theEllipse;
    }
    
    private static final RoundRectangle2D theRoundRectangle =
        new RoundRectangle2D.Float();
    /**
     * Returns a <code>RoundRectangle2D</code> whose width and 
     * height are defined by this instance's size and
     * aspect ratio functions for this vertex.  The arc size is
     * set to be half the minimum of the height and width of the frame.
     */
    public RoundRectangle2D getRoundRectangle(V v)
    {
        Rectangle2D frame = getRectangle(v);
        float arc_size = (float)Math.min(frame.getHeight(), frame.getWidth()) / 2;
        theRoundRectangle.setRoundRect(frame.getX(), frame.getY(),
                frame.getWidth(), frame.getHeight(), arc_size, arc_size);
        return theRoundRectangle;
    }
    
    private static final GeneralPath thePolygon = new GeneralPath();
    /**
     * Returns a regular <code>num_sides</code>-sided 
     * <code>Polygon</code> whose bounding 
     * box's width and height are defined by this instance's size and
     * aspect ratio functions for this vertex.
     * @param num_sides the number of sides of the polygon; must be >= 3.
     */
    public Shape getRegularPolygon(V v, int num_sides)
    {
        if (num_sides < 3)
            throw new IllegalArgumentException("Number of sides must be >= 3");
        Rectangle2D frame = getRectangle(v);
        float width = (float)frame.getWidth();
        float height = (float)frame.getHeight();
        
        // generate coordinates
        double angle = 0;
        thePolygon.reset();
        thePolygon.moveTo(0,0);
        thePolygon.lineTo(width, 0);
        double theta = (2 * Math.PI) / num_sides; 
        for (int i = 2; i < num_sides; i++)
        {
            angle -= theta; 
            float delta_x = (float) (width * Math.cos(angle));
            float delta_y = (float) (width * Math.sin(angle));
            Point2D prev = thePolygon.getCurrentPoint();
            thePolygon.lineTo((float)prev.getX() + delta_x, (float)prev.getY() + delta_y);
        }
        thePolygon.closePath();
        
        // scale polygon to be right size, translate to center at (0,0)
        Rectangle2D r = thePolygon.getBounds2D();
        double scale_x = width / r.getWidth();
        double scale_y = height / r.getHeight();
        float translationX = (float) (r.getMinX() + r.getWidth()/2);
        float translationY = (float) (r.getMinY() + r.getHeight()/2);

        AffineTransform at = AffineTransform.getScaleInstance(scale_x, scale_y);
        at.translate(-translationX, -translationY);

        Shape shape = at.createTransformedShape(thePolygon);
        return shape;
    }
    
    /**
     * Returns a regular <code>Polygon</code> of <code>num_points</code>
     * points whose bounding 
     * box's width and height are defined by this instance's size and
     * aspect ratio functions for this vertex.
     * @param num_points the number of points of the polygon; must be >= 5.
     */
    public Shape getRegularStar(V v, int num_points)
    {
        if (num_points < 5)
            throw new IllegalArgumentException("Number of sides must be >= 5");
        Rectangle2D frame = getRectangle(v);
        float width = (float) frame.getWidth();
        float height = (float) frame.getHeight();
        
        // generate coordinates
        double theta = (2 * Math.PI) / num_points;
        double angle = -theta/2;
        thePolygon.reset();
        thePolygon.moveTo(0,0);
        float delta_x = width * (float)Math.cos(angle);
        float delta_y = width * (float)Math.sin(angle);
        Point2D prev = thePolygon.getCurrentPoint();
        thePolygon.lineTo((float)prev.getX() + delta_x, (float)prev.getY() + delta_y);
        for (int i = 1; i < num_points; i++)
        {
            angle += theta; 
            delta_x = width * (float)Math.cos(angle);
            delta_y = width * (float)Math.sin(angle);
            prev = thePolygon.getCurrentPoint();
            thePolygon.lineTo((float)prev.getX() + delta_x, (float)prev.getY() + delta_y);
            angle -= theta*2; 
            delta_x = width * (float)Math.cos(angle);
            delta_y = width * (float)Math.sin(angle);
            prev = thePolygon.getCurrentPoint();
            thePolygon.lineTo((float)prev.getX() + delta_x, (float)prev.getY() + delta_y);
        }
        thePolygon.closePath();
        
        // scale polygon to be right size, translate to center at (0,0)
        Rectangle2D r = thePolygon.getBounds2D();
        double scale_x = width / r.getWidth();
        double scale_y = height / r.getHeight();

        float translationX = (float) (r.getMinX() + r.getWidth()/2);
        float translationY = (float) (r.getMinY() + r.getHeight()/2);
        
        AffineTransform at = AffineTransform.getScaleInstance(scale_x, scale_y);
        at.translate(-translationX, -translationY);

        Shape shape = at.createTransformedShape(thePolygon);
        return shape;
    }
}
