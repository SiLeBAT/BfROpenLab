/*******************************************************************************
 * Copyright (c) 2014-2023 German Federal Institute for Risk Assessment (BfR)
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

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;

import javax.swing.Icon;

/**
 * This class has been automatically generated using svg2java
 * 
 */
public class BfrLogo implements Icon {

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
			AlphaComposite origAlphaComposite = (AlphaComposite) origComposite;
			if (origAlphaComposite.getRule() == AlphaComposite.SRC_OVER) {
				origAlpha = origAlphaComposite.getAlpha();
			}
		}

		// _0
		AffineTransform trans_0 = g.getTransform();
		paintRootGraphicsNode_0(g);
		g.setTransform(trans_0);

	}

	private static void paintShapeNode_0_0_0(Graphics2D g) {
		GeneralPath shape0 = new GeneralPath();
		shape0.moveTo(0.52796, 220.93124);
		shape0.lineTo(424.38037, 220.93124);
		shape0.lineTo(424.38037, 0.46932983);
		shape0.curveTo(315.90936, 1.05596, 148.8323, 25.69515, 0.52796, 220.93124);
		g.setPaint(new Color(211, 210, 210, 255));
		g.fill(shape0);
	}

	private static void paintShapeNode_0_0_1(Graphics2D g) {
		GeneralPath shape1 = new GeneralPath();
		shape1.moveTo(0.05867, 0.0);
		shape1.lineTo(0.05867, 220.93124);
		shape1.lineTo(0.52796, 220.93124);
		shape1.curveTo(148.8323, 25.69515, 315.90936, 1.05596, 424.3804, 0.46933);
		shape1.lineTo(424.3804, 0.0);
		shape1.lineTo(0.05867, 0.0);
		g.setPaint(new Color(36, 73, 164, 255));
		g.fill(shape1);
	}

	private static void paintShapeNode_0_0_2(Graphics2D g) {
		GeneralPath shape2 = new GeneralPath();
		shape2.moveTo(424.3804, 0.46933);
		shape2.curveTo(315.90936, 1.05596, 148.8323, 25.69515, 0.52796, 220.93124);
		g.setPaint(new Color(211, 210, 210, 255));
		g.setStroke(new BasicStroke(0.5866469f, 0, 0, 4.0f, null, 0.0f));
		g.draw(shape2);
	}

	private static void paintShapeNode_0_0_3(Graphics2D g) {
		GeneralPath shape3 = new GeneralPath();
		shape3.moveTo(0.0, 45.34782);
		shape3.lineTo(424.3804, 45.34782);
		shape3.lineTo(424.3804, 39.12935);
		shape3.lineTo(0.0, 39.12935);
		shape3.lineTo(0.0, 45.34782);
		shape3.closePath();
		g.setPaint(new Color(255, 255, 255, 255));
		g.fill(shape3);
	}

	private static void paintShapeNode_0_0_4(Graphics2D g) {
		GeneralPath shape4 = new GeneralPath();
		shape4.moveTo(0.0, 90.98894);
		shape4.lineTo(424.3804, 90.98894);
		shape4.lineTo(424.3804, 84.77051);
		shape4.lineTo(0.0, 84.77051);
		shape4.lineTo(0.0, 90.98894);
		shape4.closePath();
		g.fill(shape4);
	}

	private static void paintShapeNode_0_0_5(Graphics2D g) {
		GeneralPath shape5 = new GeneralPath();
		shape5.moveTo(0.0, 136.39545);
		shape5.lineTo(424.3804, 136.39545);
		shape5.lineTo(424.3804, 130.17697);
		shape5.lineTo(0.0, 130.17697);
		shape5.lineTo(0.0, 136.39545);
		shape5.closePath();
		g.fill(shape5);
	}

	private static void paintShapeNode_0_0_6(Graphics2D g) {
		GeneralPath shape6 = new GeneralPath();
		shape6.moveTo(0.0, 182.21255);
		shape6.lineTo(424.3804, 182.21255);
		shape6.lineTo(424.3804, 175.99408);
		shape6.lineTo(0.0, 175.99408);
		shape6.lineTo(0.0, 182.21255);
		shape6.closePath();
		g.fill(shape6);
	}

	private static void paintShapeNode_0_0_7(Graphics2D g) {
		GeneralPath shape7 = new GeneralPath();
		shape7.moveTo(312.5655, 50.45163);
		shape7.curveTo(312.5655, 58.13673, 306.28833, 64.35516, 298.6033, 64.35516);
		shape7.curveTo(290.91824, 64.35516, 284.69977, 58.136734, 284.69977, 50.451633);
		shape7.curveTo(284.69977, 42.766575, 290.91824, 36.548096, 298.6033, 36.548096);
		shape7.curveTo(306.28836, 36.548096, 312.5655, 42.766575, 312.5655, 50.451637);
		g.setPaint(new Color(36, 73, 164, 255));
		g.fill(shape7);
	}

	private static void paintShapeNode_0_0_8(Graphics2D g) {
		GeneralPath shape8 = new GeneralPath();
		shape8.moveTo(312.5655, 50.45163);
		shape8.curveTo(312.5655, 58.13673, 306.28833, 64.35516, 298.6033, 64.35516);
		shape8.curveTo(290.91824, 64.35516, 284.69977, 58.136734, 284.69977, 50.451633);
		shape8.curveTo(284.69977, 42.766575, 290.91824, 36.548096, 298.6033, 36.548096);
		shape8.curveTo(306.28836, 36.548096, 312.5655, 42.766575, 312.5655, 50.451637);
		shape8.closePath();
		g.draw(shape8);
	}

	private static void paintShapeNode_0_0_9(Graphics2D g) {
		GeneralPath shape9 = new GeneralPath();
		shape9.moveTo(265.57507, 213.6568);
		shape9.curveTo(265.63376, 213.5395, 265.6924, 213.48083, 265.75107, 213.36351);
		shape9.curveTo(301.8885, 148.891, 303.47247, 72.15756, 271.91083, 68.520386);
		shape9.curveTo(252.7275, 66.29112, 232.01884, 66.760414, 199.34259, 78.37602);
		shape9.curveTo(194.70808, 80.01866, 186.37773, 85.533134, 197.70001, 83.47987);
		shape9.curveTo(211.72089, 80.663956, 245.80504, 76.79206, 261.0579, 83.83183);
		shape9.curveTo(285.22775, 94.97815, 279.18527, 149.53629, 261.4685, 212.19022);
		shape9.curveTo(260.00192, 217.76335, 261.87918, 219.17131, 265.57507, 213.6568);
		g.fill(shape9);
	}

	private static void paintShapeNode_0_0_10(Graphics2D g) {
		GeneralPath shape10 = new GeneralPath();
		shape10.moveTo(265.57507, 213.6568);
		shape10.curveTo(265.63376, 213.5395, 265.6924, 213.48083, 265.75107, 213.36351);
		shape10.curveTo(301.8885, 148.891, 303.47247, 72.15756, 271.91083, 68.520386);
		shape10.curveTo(252.7275, 66.29112, 232.01884, 66.760414, 199.34259, 78.37602);
		shape10.curveTo(194.70808, 80.01866, 186.37773, 85.533134, 197.70001, 83.47987);
		shape10.curveTo(211.72089, 80.663956, 245.80504, 76.79206, 261.0579, 83.83183);
		shape10.curveTo(285.22775, 94.97815, 279.18527, 149.53629, 261.4685, 212.19022);
		shape10.curveTo(260.00192, 217.76335, 261.87918, 219.17131, 265.57507, 213.6568);
		shape10.closePath();
		g.draw(shape10);
	}

	private static void paintShapeNode_0_0_11(Graphics2D g) {
		GeneralPath shape11 = new GeneralPath();
		shape11.moveTo(334.15408, 212.19022);
		shape11.curveTo(316.43738, 149.5363, 310.3362, 94.97816, 334.56473, 83.83184);
		shape11.curveTo(349.81757, 76.79207, 383.90173, 80.66396, 397.9226, 83.47988);
		shape11.curveTo(409.24487, 85.53314, 400.97314, 80.01867, 396.27997, 78.37603);
		shape11.curveTo(363.60376, 66.76042, 342.8951, 66.29113, 323.71173, 68.520386);
		shape11.curveTo(292.15015, 72.157555, 293.73407, 148.891, 329.9302, 213.36351);
		shape11.curveTo(329.9302, 213.48083, 329.9889, 213.5395, 330.04755, 213.6568);
		shape11.curveTo(333.74344, 219.17133, 335.6207, 217.76335, 334.15405, 212.19022);
		g.fill(shape11);
	}

	private static void paintShapeNode_0_0_12(Graphics2D g) {
		GeneralPath shape12 = new GeneralPath();
		shape12.moveTo(334.15408, 212.19022);
		shape12.curveTo(316.43738, 149.5363, 310.3362, 94.97816, 334.56473, 83.83184);
		shape12.curveTo(349.81757, 76.79207, 383.90173, 80.66396, 397.9226, 83.47988);
		shape12.curveTo(409.24487, 85.53314, 400.97314, 80.01867, 396.27997, 78.37603);
		shape12.curveTo(363.60376, 66.76042, 342.8951, 66.29113, 323.71173, 68.520386);
		shape12.curveTo(292.15015, 72.157555, 293.73407, 148.891, 329.9302, 213.36351);
		shape12.curveTo(329.9302, 213.48083, 329.9889, 213.5395, 330.04755, 213.6568);
		shape12.curveTo(333.74344, 219.17133, 335.6207, 217.76335, 334.15405, 212.19022);
		shape12.closePath();
		g.draw(shape12);
	}

	private static void paintShapeNode_0_0_13(Graphics2D g) {
		GeneralPath shape13 = new GeneralPath();
		shape13.moveTo(547.4589, 41.59327);
		shape13.lineTo(564.4717, 41.59327);
		shape13.curveTo(584.0657, 41.59327, 602.9557, 43.52919, 602.9557, 63.6512);
		shape13.curveTo(602.9557, 82.30659, 583.06836, 87.87973, 563.82635, 87.87973);
		shape13.lineTo(547.4589, 87.87973);
		shape13.lineTo(547.4589, 41.59327);
		shape13.closePath();
		shape13.moveTo(488.79425, 220.10992);
		shape13.lineTo(581.48444, 220.10992);
		shape13.curveTo(620.9071, 220.10992, 667.3109, 209.08093, 667.3109, 156.40005);
		shape13.curveTo(667.3109, 129.59033, 652.1754, 113.80949, 622.843, 107.825676);
		shape13.lineTo(622.843, 107.18039);
		shape13.curveTo(647.4236, 99.026, 661.62036, 83.53855, 661.62036, 55.79014);
		shape13.curveTo(661.62036, 17.95142, 631.0561, 0.0, 590.0495, 0.0);
		shape13.lineTo(488.79425, 0.0);
		shape13.lineTo(488.79425, 220.10994);
		shape13.closePath();
		shape13.moveTo(547.4589, 126.77439);
		shape13.lineTo(565.4103, 126.77439);
		shape13.curveTo(585.59094, 126.77439, 608.6462, 128.94498, 608.6462, 152.93883);
		shape13.curveTo(608.6462, 175.2901, 586.23627, 178.45798, 564.82367, 178.45798);
		shape13.lineTo(547.4589, 178.45798);
		shape13.lineTo(547.4589, 126.77438);
		g.fill(shape13);
	}

	private static void paintShapeNode_0_0_14(Graphics2D g) {
		GeneralPath shape14 = new GeneralPath();
		shape14.moveTo(845.4756, 220.10994);
		shape14.lineTo(904.0816, 220.10994);
		shape14.lineTo(904.0816, 126.65708);
		shape14.lineTo(916.4012, 126.65708);
		shape14.curveTo(938.1658, 126.65708, 945.7335, 142.49654, 957.7011, 180.33525);
		shape14.lineTo(970.314, 220.10994);
		shape14.lineTo(1031.5013, 220.10994);
		shape14.lineTo(1010.03015, 160.15462);
		shape14.curveTo(999.0011, 133.4035, 997.7691, 112.57754, 978.82043, 107.5324);
		shape14.lineTo(978.82043, 106.94573);
		shape14.curveTo(1007.2141, 100.316635, 1021.11774, 84.82913, 1021.11774, 55.49681);
		shape14.curveTo(1021.1177, 17.65809, 989.2628, 0.0, 950.13336, 0.0);
		shape14.lineTo(845.4756, 0.0);
		shape14.lineTo(845.4756, 220.10994);
		shape14.closePath();
		shape14.moveTo(903.02563, 41.593277);
		shape14.lineTo(919.56903, 41.593277);
		shape14.curveTo(941.9203, 41.593277, 962.45294, 45.406498, 962.45294, 65.88046);
		shape14.curveTo(962.45294, 90.81294, 938.10706, 87.76239, 919.1584, 87.76239);
		shape14.lineTo(903.72955, 87.76239);
		shape14.lineTo(903.0256, 41.59327);
		g.fill(shape14);
	}

	private static void paintShapeNode_0_0_15(Graphics2D g) {
		GeneralPath shape15 = new GeneralPath();
		shape15.moveTo(817.60986, 0.0);
		shape15.lineTo(817.7859, 41.29994);
		shape15.lineTo(788.57086, 41.417267);
		shape15.curveTo(774.0807, 41.417267, 768.3902, 50.099667, 768.3902, 66.23245);
		shape15.lineTo(768.3902, 87.87972);
		shape15.lineTo(820.2498, 87.87972);
		shape15.lineTo(820.2498, 126.657074);
		shape15.lineTo(768.3902, 126.657074);
		shape15.lineTo(768.3902, 220.10992);
		shape15.lineTo(711.66144, 220.10992);
		shape15.lineTo(711.66144, 126.65707);
		shape15.lineTo(678.22253, 126.65707);
		shape15.lineTo(678.22253, 87.879715);
		shape15.lineTo(712.60004, 87.879715);
		shape15.lineTo(712.60004, 66.232445);
		shape15.curveTo(712.60004, 17.306065, 735.5966, 0.29331207, 781.0031, 0.29331207);
		shape15.lineTo(817.60986, 0.0);
		g.fill(shape15);
	}

	private static void paintCanvasGraphicsNode_0_0(Graphics2D g) {
		// _0_0_0
		AffineTransform trans_0_0_0 = g.getTransform();
		g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
		paintShapeNode_0_0_0(g);
		g.setTransform(trans_0_0_0);
		// _0_0_1
		AffineTransform trans_0_0_1 = g.getTransform();
		g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
		paintShapeNode_0_0_1(g);
		g.setTransform(trans_0_0_1);
		// _0_0_2
		AffineTransform trans_0_0_2 = g.getTransform();
		g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
		paintShapeNode_0_0_2(g);
		g.setTransform(trans_0_0_2);
		// _0_0_3
		AffineTransform trans_0_0_3 = g.getTransform();
		g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
		paintShapeNode_0_0_3(g);
		g.setTransform(trans_0_0_3);
		// _0_0_4
		AffineTransform trans_0_0_4 = g.getTransform();
		g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
		paintShapeNode_0_0_4(g);
		g.setTransform(trans_0_0_4);
		// _0_0_5
		AffineTransform trans_0_0_5 = g.getTransform();
		g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
		paintShapeNode_0_0_5(g);
		g.setTransform(trans_0_0_5);
		// _0_0_6
		AffineTransform trans_0_0_6 = g.getTransform();
		g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
		paintShapeNode_0_0_6(g);
		g.setTransform(trans_0_0_6);
		// _0_0_7
		AffineTransform trans_0_0_7 = g.getTransform();
		g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
		paintShapeNode_0_0_7(g);
		g.setTransform(trans_0_0_7);
		// _0_0_8
		AffineTransform trans_0_0_8 = g.getTransform();
		g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
		paintShapeNode_0_0_8(g);
		g.setTransform(trans_0_0_8);
		// _0_0_9
		AffineTransform trans_0_0_9 = g.getTransform();
		g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
		paintShapeNode_0_0_9(g);
		g.setTransform(trans_0_0_9);
		// _0_0_10
		AffineTransform trans_0_0_10 = g.getTransform();
		g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
		paintShapeNode_0_0_10(g);
		g.setTransform(trans_0_0_10);
		// _0_0_11
		AffineTransform trans_0_0_11 = g.getTransform();
		g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
		paintShapeNode_0_0_11(g);
		g.setTransform(trans_0_0_11);
		// _0_0_12
		AffineTransform trans_0_0_12 = g.getTransform();
		g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
		paintShapeNode_0_0_12(g);
		g.setTransform(trans_0_0_12);
		// _0_0_13
		AffineTransform trans_0_0_13 = g.getTransform();
		g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
		paintShapeNode_0_0_13(g);
		g.setTransform(trans_0_0_13);
		// _0_0_14
		AffineTransform trans_0_0_14 = g.getTransform();
		g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
		paintShapeNode_0_0_14(g);
		g.setTransform(trans_0_0_14);
		// _0_0_15
		AffineTransform trans_0_0_15 = g.getTransform();
		g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
		paintShapeNode_0_0_15(g);
		g.setTransform(trans_0_0_15);
	}

	private void paintRootGraphicsNode_0(Graphics2D g) {
		// _0_0
		g.setComposite(AlphaComposite.getInstance(3, 1.0f * origAlpha));
		AffineTransform trans_0_0 = g.getTransform();
		g.transform(new AffineTransform(1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f));
		paintCanvasGraphicsNode_0_0(g);
		g.setTransform(trans_0_0);
	}

	/**
	 * Returns the X of the bounding box of the original SVG image.
	 * 
	 * @return The X of the bounding box of the original SVG image.
	 */
	public int getOrigX() {
		return 0;
	}

	/**
	 * Returns the Y of the bounding box of the original SVG image.
	 * 
	 * @return The Y of the bounding box of the original SVG image.
	 */
	public int getOrigY() {
		return 0;
	}

	/**
	 * Returns the width of the bounding box of the original SVG image.
	 * 
	 * @return The width of the bounding box of the original SVG image.
	 */
	public int getOrigWidth() {
		return 1032;
	}

	/**
	 * Returns the height of the bounding box of the original SVG image.
	 * 
	 * @return The height of the bounding box of the original SVG image.
	 */
	public int getOrigHeight() {
		return 222;
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
	public BfrLogo() {
		this.width = getOrigWidth();
		this.height = getOrigHeight();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.Icon#getIconHeight()
	 */
	@Override
	public int getIconHeight() {
		return height;
	}

	/*
	 * (non-Javadoc)
	 * 
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
	 * 
	 * @see javax.swing.Icon#paintIcon(java.awt.Component, java.awt.Graphics,
	 * int, int)
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
