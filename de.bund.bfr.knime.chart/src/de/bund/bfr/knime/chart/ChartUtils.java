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
package de.bund.bfr.knime.chart;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
import org.w3c.dom.svg.SVGDocument;

import com.google.common.collect.ImmutableList;

public class ChartUtils {

	private ChartUtils() {
	}

	public static final int SHAPE_SIZE = 6;
	public static final int SHAPE_DELTA = 3;

	public static final ImmutableList<Color> COLORS = ImmutableList.of(new Color(255, 85, 85), new Color(85, 85, 255),
			new Color(85, 255, 85), new Color(255, 85, 255), new Color(85, 255, 255), new Color(255, 175, 175),
			new Color(128, 128, 128), new Color(192, 0, 0), new Color(0, 0, 192), new Color(0, 192, 0),
			new Color(192, 192, 0), new Color(192, 0, 192), new Color(0, 192, 192), new Color(64, 64, 64),
			new Color(255, 64, 64), new Color(64, 64, 255), new Color(64, 255, 64), new Color(255, 64, 255),
			new Color(64, 255, 255), new Color(192, 192, 192), new Color(128, 0, 0), new Color(0, 0, 128),
			new Color(0, 128, 0), new Color(128, 128, 0), new Color(128, 0, 128), new Color(0, 128, 128),
			new Color(255, 128, 128), new Color(128, 128, 255), new Color(128, 255, 128), new Color(255, 128, 255),
			new Color(128, 255, 255));

	public static List<Color> createColorList(int n) {
		return IntStream.range(0, n).mapToObj(i -> COLORS.get(i % COLORS.size())).collect(Collectors.toList());
	}

	public static List<NamedShape> createShapeList(int n) {
		NamedShape[] shapes = NamedShape.values();

		return IntStream.range(0, n).mapToObj(i -> shapes[i % shapes.length]).collect(Collectors.toList());
	}

	public static ImagePortObjectSpec getImageSpec(boolean asSvg) {
		return new ImagePortObjectSpec(asSvg ? SvgCell.TYPE : PNGImageContent.TYPE);
	}

	public static ImagePortObject getImage(JFreeChart chart, boolean asSvg, int width, int height) {
		if (asSvg) {
			SVGDocument document = (SVGDocument) new SVGDOMImplementation().createDocument(null, "svg", null);
			SVGGraphics2D g = new SVGGraphics2D(document);

			g.setSVGCanvasSize(new Dimension(width, height));

			if (chart != null) {
				chart.draw(g, new Rectangle2D.Double(0, 0, width, height));
			}

			g.dispose();
			document.replaceChild(g.getRoot(), document.getDocumentElement());
			return new ImagePortObject(new SvgImageContent(document), new ImagePortObjectSpec(SvgCell.TYPE));
		} else {
			try {
				BufferedImage img = chart != null ? chart.createBufferedImage(width, height)
						: new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

				return new ImagePortObject(new PNGImageContent(ChartUtilities.encodeAsPNG(img)),
						new ImagePortObjectSpec(PNGImageContent.TYPE));
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	public static void addDataSetToPlot(XYPlot plot, XYDataset dataSet, XYItemRenderer renderer) {
		int i = plot.getDataset(0) == null ? 0 : plot.getDatasetCount();

		plot.setDataset(i, dataSet);
		plot.setRenderer(i, renderer);
	}
}
