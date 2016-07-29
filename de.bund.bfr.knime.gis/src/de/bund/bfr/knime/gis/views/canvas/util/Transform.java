/*******************************************************************************
 * Copyright (c) 2016 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.gis.views.canvas.util;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.Objects;

import org.geotools.geometry.jts.LiteShape;

import com.vividsolutions.jts.geom.Geometry;

public class Transform implements Serializable {

	private static final long serialVersionUID = 1L;

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

	public Transform inverse() {
		return new Transform(1 / scaleX, 1 / scaleY, -translationX / scaleX, -translationY / scaleY);
	}

	public Transform concatenate(Transform t) {
		return new Transform(scaleX * t.scaleX, scaleY * t.scaleY, translationX * t.scaleX + t.translationX,
				translationY * t.scaleY + t.translationY);
	}

	public boolean isValid() {
		return Double.isFinite(scaleX) && Double.isFinite(scaleY) && Double.isFinite(translationX)
				&& Double.isFinite(translationY);
	}

	public AffineTransform toAffineTransform() {
		return new AffineTransform(scaleX, 0, 0, scaleY, translationX, translationY);
	}

	public Point2D apply(double x, double y) {
		return new Point2D.Double(x * scaleX + translationX, y * scaleY + translationY);
	}

	public Point2D applyInverse(double x, double y) {
		return new Point2D.Double((x - translationX) / scaleX, (y - translationY) / scaleY);
	}

	public Shape apply(Geometry g) {
		return new LiteShape(g, toAffineTransform(), false);
	}

	@Override
	public int hashCode() {
		return Objects.hash(scaleX, scaleY, translationX, translationY);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}

		Transform other = (Transform) obj;

		return scaleX == other.scaleX && scaleY == other.scaleY && translationX == other.translationX
				&& translationY == other.translationY;
	}

	@Override
	public String toString() {
		return "Transform [scaleX=" + scaleX + ", scaleY=" + scaleY + ", translationX=" + translationX
				+ ", translationY=" + translationY + "]";
	}
}
