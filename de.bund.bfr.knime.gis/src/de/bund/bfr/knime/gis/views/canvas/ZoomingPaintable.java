/*******************************************************************************
 * Copyright (c) 2015 Federal Institute for Risk Assessment (BfR), Germany
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
package de.bund.bfr.knime.gis.views.canvas;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;

import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationServer.Paintable;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.transform.MutableTransformer;

public class ZoomingPaintable implements Paintable, MouseMotionListener,
		MouseListener {

	private static final int SIZE = 30;

	private ICanvas<?> canvas;
	private int dy;

	private int xPlus;
	private int yPlus;
	private int xMinus;
	private int yMinus;

	private boolean plusFocused;
	private boolean minusFocused;

	public ZoomingPaintable(ICanvas<?> canvas, int dy) {
		this.canvas = canvas;
		this.dy = dy;
		xPlus = -1;
		yPlus = -1;
		xMinus = -1;
		yMinus = -1;
		plusFocused = false;
		minusFocused = false;

		canvas.getViewer().addMouseMotionListener(this);
		canvas.getViewer().addMouseListener(this);
	}

	@Override
	public void paint(Graphics g) {
		int w = canvas.getCanvasSize().width;
		int h = canvas.getCanvasSize().height;
		int d = 10;
		int lineD = 8;
		int lineWidth = 4;

		xPlus = w - d - SIZE;
		yPlus = h - dy - d - 2 * SIZE;
		xMinus = xPlus;
		yMinus = h - dy - d - SIZE;

		g.setColor(plusFocused ? Color.BLUE : CanvasUtils.LEGEND_BACKGROUND);
		g.fillRect(xPlus, yPlus, SIZE, SIZE);
		g.setColor(minusFocused ? Color.BLUE : CanvasUtils.LEGEND_BACKGROUND);
		g.fillRect(xMinus, yMinus, SIZE, SIZE);
		g.setColor(Color.BLACK);
		g.drawRect(xPlus, yPlus, SIZE, SIZE);
		g.drawRect(xMinus, yMinus, SIZE, SIZE);

		Stroke currentStroke = ((Graphics2D) g).getStroke();

		((Graphics2D) g).setStroke(new BasicStroke(lineWidth));
		g.drawLine(xPlus + lineD, yPlus + SIZE / 2, xPlus + SIZE - lineD, yPlus
				+ SIZE / 2);
		g.drawLine(xPlus + SIZE / 2, yPlus + lineD, xPlus + SIZE / 2, yPlus
				+ SIZE - lineD);
		g.drawLine(xMinus + lineD, yMinus + SIZE / 2, xMinus + SIZE - lineD,
				yMinus + SIZE / 2);
		((Graphics2D) g).setStroke(currentStroke);
	}

	@Override
	public boolean useTransform() {
		return false;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		boolean newPlusFocused = x > xPlus && x < xPlus + SIZE && y > yPlus
				&& y < yPlus + SIZE;
		boolean newMinusFocused = x > xMinus && x < xMinus + SIZE && y > yMinus
				&& y < yMinus + SIZE;
		boolean changed = newPlusFocused != plusFocused
				|| newMinusFocused != minusFocused;

		plusFocused = newPlusFocused;
		minusFocused = newMinusFocused;

		if (changed) {
			GraphMouse<?, ?> graphMouse = (GraphMouse<?, ?>) canvas.getViewer()
					.getGraphMouse();

			graphMouse.setPickingDeactivated(plusFocused || minusFocused);
			canvas.getViewer().repaint();
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1
				&& (plusFocused || minusFocused)) {
			VisualizationViewer<?, ?> viewer = canvas.getViewer();
			Point2D center = viewer.getRenderContext()
					.getMultiLayerTransformer()
					.inverseTransform(Layer.VIEW, viewer.getCenter());
			MutableTransformer transformer = viewer.getRenderContext()
					.getMultiLayerTransformer().getTransformer(Layer.LAYOUT);

			if (plusFocused) {
				transformer.scale(1.2, 1.2, center);
				viewer.repaint();
			} else if (minusFocused) {
				transformer.scale(1 / 1.2, 1 / 1.2, center);
				viewer.repaint();
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}
}
