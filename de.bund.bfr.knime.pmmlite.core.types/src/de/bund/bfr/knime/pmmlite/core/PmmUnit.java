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
package de.bund.bfr.knime.pmmlite.core;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.Unit;
import org.sbml.jsbml.UnitDefinition;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import de.bund.bfr.knime.Pair;
import de.bund.bfr.math.Transform;

public class PmmUnit implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final String NO_UNIT = "NO_UNIT";
	private static final int LEVEL = 3;
	private static final int VERSION = 1;

	private static Cache<Pair<String, String>, Double> factors = CacheBuilder.newBuilder().maximumSize(1000000).build();
	private static Cache<String, UnitDefinition> definitions = CacheBuilder.newBuilder().maximumSize(1000).build();

	private Transform transform;
	private String definitionXml;

	public static class Builder {

		private Transform transform;
		private UnitDefinition definition;

		public Builder() {
			transform = Transform.NO_TRANSFORM;
			definition = new UnitDefinition(LEVEL, VERSION);
		}

		public Builder transform(Transform transform) {
			this.transform = transform;
			return this;
		}

		public Builder addUnit(Unit.Kind unit) {
			definition.addUnit(unit);
			return this;
		}

		public Builder addUnit(Unit.Kind unit, double multiplier, int scale, double exponent) {
			definition.addUnit(new Unit(multiplier, scale, unit, exponent, LEVEL, VERSION));
			return this;
		}

		public PmmUnit build() {
			if (definition.isSetId() || definition.isSetAnnotation()) {
				definition.unsetId();
				definition.unsetAnnotation();
			}

			return new PmmUnit(transform, definitionToXml(definition));
		}
	}

	public static class ConversionDelegate implements org.eclipse.emf.ecore.EDataType.Internal.ConversionDelegate {

		@Override
		public String convertToString(Object value) {
			if (value == null) {
				return null;
			}

			PmmUnit unit = (PmmUnit) value;

			return unit.getTransform().name() + "(" + unit.getDefinitionXml() + ")";
		}

		@Override
		public Object createFromString(String literal) {
			if (literal == null) {
				return null;
			}

			Transform transform;
			String definitionXml;

			if (literal.endsWith(")")) {
				transform = Transform.valueOf(literal.substring(0, literal.indexOf('(')));
				definitionXml = literal.substring(literal.indexOf('(') + 1, literal.indexOf(')'));
			} else {
				transform = Transform.NO_TRANSFORM;
				definitionXml = literal;
			}

			return new PmmUnit(transform, definitionXml);
		}
	}

	private PmmUnit(Transform transform, String definitionXml) {
		this.transform = transform;
		this.definitionXml = definitionXml;
	}

	public Transform getTransform() {
		return transform;
	}

	public String getDefinitionXml() {
		return definitionXml;
	}

	public List<Unit> getUnits() {
		return definitionFromXml(definitionXml).getListOfUnits();
	}

	public double convertTo(double value, PmmUnit unit) throws UnitException {
		double factor = getConversionFactor(definitionXml, unit.definitionXml);

		if (Double.isNaN(factor)) {
			throw new UnitException("\"" + toString() + "\" cannot be converted to \"" + unit.toString() + "\"");
		}

		return unit.transform.to(factor * transform.from(value));
	}

	public boolean isEmpty() {
		return definitionXml.equals(NO_UNIT) && transform == Transform.NO_TRANSFORM;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;

		result = prime * result + transform.hashCode();
		result = prime * result + definitionXml.hashCode();

		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PmmUnit) {
			return transform == ((PmmUnit) obj).transform && definitionXml.equals(((PmmUnit) obj).definitionXml);
		}

		return false;
	}

	@Override
	public String toString() {
		if (definitionXml.equals(NO_UNIT)) {
			return transform != Transform.NO_TRANSFORM ? transform.toString() : "No Unit";
		}

		return transform.getName(definitionFromXml(definitionXml).toString());
	}

	private static double getConversionFactor(String fromXml, String toXml) {
		Double result = factors.getIfPresent(new Pair<>(fromXml, toXml));

		if (result != null) {
			return result;
		}

		double factor = Double.NaN;

		if (fromXml.equals(toXml)) {
			factor = 1.0;
		} else {
			UnitDefinition from = definitionFromXml(fromXml);
			UnitDefinition to = definitionFromXml(toXml);

			if (UnitDefinition.areIdentical(from, to)) {
				factor = 1.0;
			} else {
				UnitDefinition div = from.divideBy(to).simplify();

				if (div.getNumUnits() == 1 && div.getUnit(0).isDimensionless() && div.getUnit(0).getExponent() == 1.0) {
					factor = div.getUnit(0).getMultiplier() * Math.pow(10, div.getUnit(0).getScale());
				}
			}
		}

		factors.put(new Pair<>(fromXml, toXml), factor);

		return factor;
	}

	private static String definitionToXml(UnitDefinition definition) {
		if (definition.getUnitCount() == 0) {
			return NO_UNIT;
		}

		SBMLDocument doc = new SBMLDocument(LEVEL, VERSION);
		Model model = doc.createModel("ID");

		model.addUnitDefinition(definition.clone());

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		try {
			SBMLWriter.write(doc, out, "test", "1.0", ' ', (short) 0);

			String xml = out.toString(StandardCharsets.UTF_8.name());
			String from = "<listOfUnitDefinitions>";
			String to = "</listOfUnitDefinitions>";

			return xml.substring(xml.indexOf(from) + from.length(), xml.indexOf(to)).replace("\n", "");
		} catch (SBMLException | XMLStreamException | UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static UnitDefinition definitionFromXml(String xml) {
		UnitDefinition result = definitions.getIfPresent(xml);

		if (result != null) {
			return result.clone();
		}

		UnitDefinition def;

		if (xml.equals(NO_UNIT)) {
			def = new UnitDefinition(LEVEL, VERSION);
		} else {
			String preXml = "<?xml version='1.0' encoding='UTF-8' standalone='no'?>"
					+ "<sbml xmlns=\"http://www.sbml.org/sbml/level" + LEVEL + "/version" + VERSION + "/core\" level=\""
					+ LEVEL + "\" version=\"" + VERSION + "\">" + "<model id=\"ID\">" + "<listOfUnitDefinitions>";
			String postXml = "</listOfUnitDefinitions>" + "</model>" + "</sbml>";

			try {
				def = SBMLReader.read(preXml + xml + postXml).getModel().getUnitDefinition(0);
			} catch (XMLStreamException e) {
				e.printStackTrace();
				return null;
			}
		}

		definitions.put(xml, def);

		return def;
	}
}
