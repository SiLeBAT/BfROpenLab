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
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;

import de.bund.bfr.knime.gis.views.canvas.jung.BetterGraphMouse;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationServer.Paintable;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.transform.MutableTransformer;

public class ZoomingPaintable implements Paintable, MouseMotionListener, MouseListener {

	private ICanvas<?> canvas;
	private double zoomFactor;

	private Rectangle plusRect;
	private Rectangle minusRect;

	private boolean plusFocused;
	private boolean minusFocused;

	public ZoomingPaintable(ICanvas<?> canvas, double zoomFactor) {
		this.canvas = canvas;
		this.zoomFactor = zoomFactor;
		plusRect = null;
		minusRect = null;
		plusFocused = false;
		minusFocused = false;

		canvas.getViewer().addMouseMotionListener(this);
		canvas.getViewer().addMouseListener(this);
	}

	@Override
	public void paint(Graphics g) {
		int w = canvas.getCanvasSize().width;
		int h = canvas.getCanvasSize().height;
		int size = 30;
		int d = 10;
		int lineD = 8;
		int lineWidth = 4;

		int xPlus = w - d - size;
		int yPlus = h - size - d - 2 * size;
		int xMinus = xPlus;
		int yMinus = h - size - d - size;

		g.setColor(plusFocused ? Color.BLUE : CanvasUtils.LEGEND_BACKGROUND);
		g.fillRect(xPlus, yPlus, size, size);
		g.setColor(minusFocused ? Color.BLUE : CanvasUtils.LEGEND_BACKGROUND);
		g.fillRect(xMinus, yMinus, size, size);
		g.setColor(Color.BLACK);
		g.drawRect(xPlus, yPlus, size, size);
		g.drawRect(xMinus, yMinus, size, size);
		plusRect = new Rectangle(xPlus, yPlus, size, size);
		minusRect = new Rectangle(xMinus, yMinus, size, size);

		Stroke currentStroke = ((Graphics2D) g).getStroke();

		((Graphics2D) g).setStroke(new BasicStroke(lineWidth));
		g.drawLine(xPlus + lineD, yPlus + size / 2, xPlus + size - lineD, yPlus + size / 2);
		g.drawLine(xPlus + size / 2, yPlus + lineD, xPlus + size / 2, yPlus + size - lineD);
		g.drawLine(xMinus + lineD, yMinus + size / 2, xMinus + size - lineD, yMinus + size / 2);
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
		boolean newPlusFocused = plusRect != null && plusRect.contains(e.getPoint());
		boolean newMinusFocused = minusRect != null && minusRect.contains(e.getPoint());
		boolean changed = newPlusFocused != plusFocused || newMinusFocused != minusFocused;

		plusFocused = newPlusFocused;
		minusFocused = newMinusFocused;

		if (changed) {
			BetterGraphMouse<?, ?> graphMouse = (BetterGraphMouse<?, ?>) canvas.getViewer().getGraphMouse();

			graphMouse.setPickingDeactivated(plusFocused || minusFocused);
			paint(canvas.getViewer().getGraphics());
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1 && (plusFocused || minusFocused)) {
			VisualizationViewer<?, ?> viewer = canvas.getViewer();
			Point2D center = viewer.getRenderContext().getMultiLayerTransformer().inverseTransform(Layer.VIEW,
					viewer.getCenter());
			MutableTransformer transformer = viewer.getRenderContext().getMultiLayerTransformer()
					.getTransformer(Layer.LAYOUT);

			if (plusFocused) {
				transformer.scale(zoomFactor, zoomFactor, center);
				viewer.repaint();
			} else if (minusFocused) {
				transformer.scale(1 / zoomFactor, 1 / zoomFactor, center);
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
