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
package de.bund.bfr.knime.chart;

import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.knime.base.data.xml.SvgCell;
import org.knime.base.data.xml.SvgImageContent;
import org.knime.core.data.image.png.PNGImageContent;
import org.knime.core.node.port.image.ImagePortObject;
import org.knime.core.node.port.image.ImagePortObjectSpec;
import org.w3c.dom.Document;
import org.w3c.dom.svg.SVGDocument;

public class ChartUtils {

	private ChartUtils() {
	}

	public static ImagePortObjectSpec getImageSpec(boolean asSvg) {
		if (asSvg) {
			return new ImagePortObjectSpec(SvgCell.TYPE);
		} else {
			return new ImagePortObjectSpec(PNGImageContent.TYPE);
		}
	}

	public static ImagePortObject getImage(JFreeChart chart, boolean asSvg, int width, int height) {
		if (asSvg) {
			return new ImagePortObject(ChartUtils.convertToSVGImageContent(chart, width, height),
					new ImagePortObjectSpec(SvgCell.TYPE));
		} else {
			return new ImagePortObject(ChartUtils.convertToPNGImageContent(chart, width, height),
					new ImagePortObjectSpec(PNGImageContent.TYPE));
		}
	}

	public static void addDataSetToPlot(XYPlot plot, XYDataset dataSet, XYItemRenderer renderer) {
		int i;

		if (plot.getDataset(0) == null) {
			i = 0;
		} else {
			i = plot.getDatasetCount();
		}

		plot.setDataset(i, dataSet);
		plot.setRenderer(i, renderer);
	}

	private static PNGImageContent convertToPNGImageContent(JFreeChart chart, int width, int height) {
		BufferedImage img;

		if (chart != null) {
			img = chart.createBufferedImage(width, height);
		} else {
			img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		}

		try {
			return new PNGImageContent(ChartUtilities.encodeAsPNG(img));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static SvgImageContent convertToSVGImageContent(JFreeChart chart, int width, int height) {
		SVGDOMImplementation domImpl = new SVGDOMImplementation();
		Document document = domImpl.createDocument(null, "svg", null);
		SVGGraphics2D g = new SVGGraphics2D(document);

		g.setSVGCanvasSize(new Dimension(width, height));

		if (chart != null) {
			chart.draw(g, new Rectangle2D.Double(0, 0, width, height));
		}

		g.dispose();
		document.replaceChild(g.getRoot(), document.getDocumentElement());

		return new SvgImageContent((SVGDocument) document, true);
	}
}
