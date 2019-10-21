/*******************************************************************************
 * Copyright (c) 2019 German Federal Institute for Risk Assessment (BfR)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Department Biological Safety - BfR
 *******************************************************************************/
package de.bund.bfr.knime.openkrise.views.canvas;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.Icon; 

/**
 * This class has been automatically generated using svg2java
 * 
 */
public class CgiLogo implements Icon {
	
	private float origAlpha = 1.0f;

	/**
	 * Paints the transcoded SVG image on the specified graphics context. You
	 * can install a custom transformation on the graphics context to scale the
	 * image.
	 * 
	 * @param g
	 *            Graphics context.
	 */
	public void paint(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        origAlpha = 1.0f;
        Composite origComposite = g.getComposite();
        if (origComposite instanceof AlphaComposite) {
            AlphaComposite origAlphaComposite = 
                (AlphaComposite)origComposite;
            if (origAlphaComposite.getRule() == AlphaComposite.SRC_OVER) {
                origAlpha = origAlphaComposite.getAlpha();
            }
        }
        
		// _0
		AffineTransform trans_0 = g.getTransform();
		paintRootGraphicsNode_0(g);
		g.setTransform(trans_0);

	}

	private void paintShapeNode_0_0_0_0_0(Graphics2D g) {
		GeneralPath shape0 = new GeneralPath();
		shape0.moveTo(138.245, 91.201);
		shape0.curveTo(110.84599, 91.201, 94.04599, 112.6, 94.04599, 134.2);
		shape0.curveTo(94.04599, 160.2, 115.24599, 177.199, 138.44499, 177.199);
		shape0.curveTo(153.844, 177.199, 168.44499, 170.40001, 180.844, 159.40001);
		shape0.lineTo(180.844, 191.6);
		shape0.curveTo(167.844, 199.40001, 150.04399, 204.20001, 136.24399, 204.20001);
		shape0.curveTo(96.64499, 204.20001, 63.444984, 172.001, 63.444984, 134.20102);
		shape0.curveTo(63.444984, 94.20202, 96.84398, 64.20202, 136.44398, 64.20202);
		shape0.curveTo(151.64398, 64.20202, 169.44398, 68.80202, 181.04298, 74.60202);
		shape0.lineTo(181.04298, 106.20102);
		shape0.curveTo(166.444, 96.601, 151.645, 91.201, 138.245, 91.201);
		shape0.closePath();
		g.setPaint(new Color(225, 25, 55, 255));
		g.fill(shape0);
	}

	private void paintShapeNode_0_0_0_0_1(Graphics2D g) {
		GeneralPath shape1 = new GeneralPath();
		shape1.moveTo(265.025, 204.199);
		shape1.curveTo(225.226, 204.199, 191.62698, 173.199, 191.62698, 134.20001);
		shape1.curveTo(191.62698, 94.80101, 225.02698, 64.20101, 266.826, 64.20101);
		shape1.curveTo(282.025, 64.20101, 300.826, 68.20101, 312.426, 73.60101);
		shape1.lineTo(312.426, 105.000015);
		shape1.curveTo(299.227, 97.40002, 282.227, 91.20001, 267.426, 91.20001);
		shape1.curveTo(240.028, 91.20001, 222.22699, 112.599014, 222.22699, 134.199);
		shape1.curveTo(222.22699, 159.598, 243.22699, 177.99901, 267.827, 177.99901);
		shape1.curveTo(273.026, 177.99901, 278.026, 177.59901, 284.427, 175.19801);
		shape1.lineTo(284.427, 150.0);
		shape1.lineTo(262.029, 150.0);
		shape1.lineTo(262.029, 123.4);
		shape1.lineTo(313.228, 123.4);
		shape1.lineTo(313.228, 194.199);
		shape1.curveTo(298.625, 200.8, 282.227, 204.199, 265.025, 204.199);
		shape1.closePath();
		g.fill(shape1);
	}

	private void paintShapeNode_0_0_0_0_2(Graphics2D g) {
		GeneralPath shape2 = new GeneralPath();
		shape2.moveTo(335.59, 201.399);
		shape2.lineTo(335.59, 67.001);
		shape2.lineTo(364.389, 67.001);
		shape2.lineTo(364.389, 201.39899);
		shape2.lineTo(335.59, 201.39899);
		shape2.closePath();
		g.fill(shape2);
	}

	private void paintCompositeGraphicsNode_0_0_0_0(Graphics2D g) {
		// _0_0_0_0_0
		AffineTransform trans_0_0_0_0_0 = g.getTransform();
		g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
		paintShapeNode_0_0_0_0_0(g);
		g.setTransform(trans_0_0_0_0_0);
		// _0_0_0_0_1
		AffineTransform trans_0_0_0_0_1 = g.getTransform();
		g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
		paintShapeNode_0_0_0_0_1(g);
		g.setTransform(trans_0_0_0_0_1);
		// _0_0_0_0_2
		AffineTransform trans_0_0_0_0_2 = g.getTransform();
		g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
		paintShapeNode_0_0_0_0_2(g);
		g.setTransform(trans_0_0_0_0_2);
	}

	private void paintCompositeGraphicsNode_0_0_0(Graphics2D g) {
		// _0_0_0_0
		AffineTransform trans_0_0_0_0 = g.getTransform();
		g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
		paintCompositeGraphicsNode_0_0_0_0(g);
		g.setTransform(trans_0_0_0_0);
	}

	private void paintCompositeGraphicsNode_0_0_1(Graphics2D g) {
	}

	private void paintCompositeGraphicsNode_0_0_2(Graphics2D g) {
	}

	private void paintCanvasGraphicsNode_0_0(Graphics2D g) {
		// _0_0_0
		AffineTransform trans_0_0_0 = g.getTransform();
		g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
		paintCompositeGraphicsNode_0_0_0(g);
		g.setTransform(trans_0_0_0);
		// _0_0_1
		AffineTransform trans_0_0_1 = g.getTransform();
		g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
		paintCompositeGraphicsNode_0_0_1(g);
		g.setTransform(trans_0_0_1);
		// _0_0_2
		AffineTransform trans_0_0_2 = g.getTransform();
		g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
		paintCompositeGraphicsNode_0_0_2(g);
		g.setTransform(trans_0_0_2);
	}

	private void paintRootGraphicsNode_0(Graphics2D g) {
		// _0_0
		g.setComposite(AlphaComposite.getInstance(3, 1.0f * origAlpha));
		AffineTransform trans_0_0 = g.getTransform();
		g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, -0.0f, -0.0f));
		paintCanvasGraphicsNode_0_0(g);
		g.setTransform(trans_0_0);
	}



    /**
     * Returns the X of the bounding box of the original SVG image.
     * @return The X of the bounding box of the original SVG image.
     */
    public int getOrigX() {
        return 64;
    }

    /**
     * Returns the Y of the bounding box of the original SVG image.
     * @return The Y of the bounding box of the original SVG image.
     */
    public int getOrigY() {
        return 65;
    }

    /**
     * Returns the width of the bounding box of the original SVG image.
     * @return The width of the bounding box of the original SVG image.
     */
    public int getOrigWidth() {
        return 301;
    }

    /**
     * Returns the height of the bounding box of the original SVG image.
     * @return The height of the bounding box of the original SVG image.
     */
    public int getOrigHeight() {
        return 140;
    }
    
    
	/**
	 * The current width of this resizable icon.
	 */
	int width;

	/**
	 * The current height of this resizable icon.
	 */
	int height;

	/**
	 * Creates a new transcoded SVG image.
	 */
	public CgiLogo() {
        this.width = getOrigWidth();
        this.height = getOrigHeight();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.Icon#getIconHeight()
	 */
    @Override
	public int getIconHeight() {
		return height;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.Icon#getIconWidth()
	 */
    @Override
	public int getIconWidth() {
		return width;
	}

	/*
	 * Set the dimension of the icon.
	 */

	public void setDimension(Dimension newDimension) {
		this.width = newDimension.width;
		this.height = newDimension.height;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.Icon#paintIcon(java.awt.Component, java.awt.Graphics, int, int)
	 */
    @Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		Graphics2D g2d = (Graphics2D) g.create();
		g2d.translate(x, y);

		double coef1 = (double) this.width / (double) getOrigWidth();
		double coef2 = (double) this.height / (double) getOrigHeight();
		double coef = Math.min(coef1, coef2);
		g2d.scale(coef, coef);
		paint(g2d);
		g2d.dispose();
	}
}

