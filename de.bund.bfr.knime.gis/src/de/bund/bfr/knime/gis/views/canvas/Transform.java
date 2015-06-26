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

import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import org.geotools.geometry.jts.LiteShape;

import com.vividsolutions.jts.geom.Geometry;

public class Transform {

	public static final Transform IDENTITY_TRANSFORM = new Transform(1, 1, 0, 0);
	public static final Transform INVALID_TRANSFORM = new Transform(Double.NaN, Double.NaN, Double.NaN, Double.NaN);

	private double scaleX;
	private double scaleY;
	private double translationX;
	private double translationY;

	public Transform(double scaleX, double scaleY, double translationX, double translationY) {
		this.scaleX = scaleX;
		this.scaleY = scaleY;
		this.translationX = translationX;
		this.translationY = translationY;
	}

	public Transform(AffineTransform transform) {
		this(transform.getScaleX(), transform.getScaleY(), transform.getTranslateX(), transform.getTranslateY());
	}

	public double getScaleX() {
		return scaleX;
	}

	public double getScaleY() {
		return scaleY;
	}

	public double getTranslationX() {
		return translationX;
	}

	public double getTranslationY() {
		return translationY;
	}

	public boolean isValid() {
		return !Double.isNaN(scaleX) && !Double.isNaN(scaleY) && !Double.isNaN(translationX)
				&& !Double.isNaN(translationY) && !Double.isInfinite(scaleX) && !Double.isInfinite(scaleY)
				&& !Double.isInfinite(translationX) && !Double.isInfinite(translationY);
	}

	public AffineTransform toAffineTransform() {
		return new AffineTransform(scaleX, 0, 0, scaleY, translationX, translationY);
	}

	public Point apply(double x, double y) {
		return new Point((int) (x * scaleX + translationX), (int) (y * scaleY + translationY));
	}

	public Point2D applyInverse(int x, int y) {
		return new Point2D.Double((x - translationX) / scaleX, (y - translationY) / scaleY);
	}

	public Shape apply(Geometry g) {
		return new LiteShape(g, toAffineTransform(), false);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;

		temp = Double.doubleToLongBits(scaleX);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(scaleY);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(translationX);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(translationY);
		result = prime * result + (int) (temp ^ (temp >>> 32));

		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Transform) {
			Transform t = (Transform) obj;

			return scaleX == t.scaleX && scaleY == t.scaleY && translationX == t.translationX
					&& translationY == t.translationY;
		}

		return false;
	}
}
