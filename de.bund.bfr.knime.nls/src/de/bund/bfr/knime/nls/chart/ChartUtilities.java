/*******************************************************************************
 * Copyright (c) 2014 Federal Institute for Risk Assessment (BfR), Germany 
 * 
 * Developers and contributors are 
 * Christian Thoens (BfR)
 * Armin A. Weiser (BfR)
 * Matthias Filter (BfR)
 * Alexander Falenski (BfR)
 * Annemarie Kaesbohrer (BfR)
 * Bernd Appel (BfR)
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
 ******************************************************************************/
package de.bund.bfr.knime.nls.chart;

import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.jfree.chart.JFreeChart;
import org.knime.base.data.xml.SvgCell;
import org.knime.base.data.xml.SvgImageContent;
import org.knime.core.data.image.png.PNGImageContent;
import org.knime.core.node.port.image.ImagePortObject;
import org.knime.core.node.port.image.ImagePortObjectSpec;
import org.w3c.dom.Document;
import org.w3c.dom.svg.SVGDocument;

public class ChartUtilities {

	public static final String STATUS = "Status";
	public static final String OK = "Ok";
	public static final String FAILED = "Failed";
	public static final String NO_COVARIANCE = "No Cov. Matrix";

	public static final String ID = "ID";
	public static final String SELECTED = "Selected";
	public static final String COLOR = "Color";
	public static final String SHAPE = "Shape";

	private ChartUtilities() {
	}

	public static void saveChartAs(JFreeChart chart, String fileName,
			int width, int height) {
		if (fileName.toLowerCase().endsWith(".png")) {
			try {
				org.jfree.chart.ChartUtilities.writeChartAsPNG(
						new FileOutputStream(fileName), chart, width, height);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (fileName.toLowerCase().endsWith(".svg")) {
			try {
				SVGDOMImplementation domImpl = new SVGDOMImplementation();
				Document document = domImpl.createDocument(null, "svg", null);
				SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
				Writer outsvg = new OutputStreamWriter(new FileOutputStream(
						fileName), "UTF-8");

				svgGenerator.setSVGCanvasSize(new Dimension(width, height));
				chart.draw(svgGenerator, new Rectangle2D.Double(0, 0, width,
						height));
				svgGenerator.stream(outsvg, true);
				outsvg.close();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (SVGGraphics2DIOException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static PNGImageContent convertToPNGImageContent(JFreeChart chart,
			int width, int height) {
		try {
			BufferedImage img;

			if (chart != null) {
				img = chart.createBufferedImage(width, height);
			} else {
				img = new BufferedImage(width, height,
						BufferedImage.TYPE_INT_RGB);
			}

			return new PNGImageContent(
					org.jfree.chart.ChartUtilities.encodeAsPNG(img));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static SvgImageContent convertToSVGImageContent(JFreeChart chart,
			int width, int height) {
		SVGDOMImplementation domImpl = new SVGDOMImplementation();
		Document document = domImpl.createDocument(null, "svg", null);
		SVGGraphics2D svgGenerator = new SVGGraphics2D(document);

		svgGenerator.setSVGCanvasSize(new Dimension(width, height));

		if (chart != null) {
			chart.draw(svgGenerator,
					new Rectangle2D.Double(0, 0, width, height));
		}

		svgGenerator.finalize();
		document.replaceChild(svgGenerator.getRoot(),
				document.getDocumentElement());

		return new SvgImageContent((SVGDocument) document, true);
	}

	public static ImagePortObjectSpec getImageSpec(boolean asSvg) {
		if (asSvg) {
			return new ImagePortObjectSpec(SvgCell.TYPE);
		} else {
			return new ImagePortObjectSpec(PNGImageContent.TYPE);
		}
	}

	public static ImagePortObject getImage(JFreeChart chart, boolean asSvg) {
		if (asSvg) {
			return new ImagePortObject(ChartUtilities.convertToSVGImageContent(
					chart, 640, 480), new ImagePortObjectSpec(SvgCell.TYPE));
		} else {
			return new ImagePortObject(ChartUtilities.convertToPNGImageContent(
					chart, 640, 480), new ImagePortObjectSpec(
					PNGImageContent.TYPE));
		}
	}

}
