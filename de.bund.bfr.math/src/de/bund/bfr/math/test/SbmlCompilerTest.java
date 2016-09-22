package de.bund.bfr.math.test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.util.CombinatoricsUtils;
import org.apache.commons.math3.util.FastMath;
import org.nfunk.jep.Node;
import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.CallableSBase;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.FunctionDefinition;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.text.parser.ParseException;
import org.sbml.jsbml.util.compilers.ASTNodeCompiler;
import org.sbml.jsbml.util.compilers.ASTNodeValue;

import com.google.common.collect.Sets;

import de.bund.bfr.math.Parser;

public class SbmlCompilerTest {
	public static void main(String[] args) throws ParseException, org.nfunk.jep.ParseException {
		DoubleCompiler compiler = new DoubleCompiler();
		compiler.setVarValue("a", 5);
		ASTNode math = ASTNode.parseFormula("sin((1<=3)*2*3+5)^2+4*234.34324*sin((1<=3)*2*3+5)^2+4*234.34324");
		long t1 = System.nanoTime();
		for (int i = 0; i < 100000; i++) {
			math.compile(compiler);
		}
		long t2 = System.nanoTime();
		System.out.println("Time:\t" + (t2 - t1));
		System.out.println("Result:\t" + math.compile(compiler));

		Parser parser = new Parser(Sets.newHashSet("a"));
		parser.setVarValue("a", 5);
		Node node = parser.parse("sin((1<=3)*2*3+5)^2+4*234.34324*sin((1<=3)*2*3+5)^2+4*234.34324");
		long tt1 = System.nanoTime();
		for (int i = 0; i < 100000; i++) {
			parser.evaluate(node);
		}
		long tt2 = System.nanoTime();
		System.out.println("Time:\t" + (tt2 - tt1));
		System.out.println("Result:\t" + parser.evaluate(node));
	}

	private static class DoubleCompiler implements ASTNodeCompiler {

		private Map<String, Double> variableValues;

		public DoubleCompiler() {
			variableValues = new LinkedHashMap<>();
		}

		public void setVarValue(String var, double value) {
			variableValues.put(var, value);
		}

		@Override
		public ASTNodeValue abs(ASTNode value) throws SBMLException {
			return new ASTNodeValue(FastMath.abs(toDouble(value)), this);
		}

		@Override
		public ASTNodeValue and(List<ASTNode> values) throws SBMLException {
			return new ASTNodeValue(toDouble(values.get(0)) != 0.0 && toDouble(values.get(1)) != 0.0 ? 1.0 : 0.0, this);
		}

		@Override
		public ASTNodeValue arccos(ASTNode value) throws SBMLException {
			return new ASTNodeValue(FastMath.acos(toDouble(value)), this);
		}

		@Override
		public ASTNodeValue arccosh(ASTNode value) throws SBMLException {
			return new ASTNodeValue(FastMath.acosh(toDouble(value)), this);
		}

		@Override
		public ASTNodeValue arccot(ASTNode value) throws SBMLException {
			return new ASTNodeValue(FastMath.PI / 2.0 - FastMath.atan(toDouble(value)), this);
		}

		@Override
		public ASTNodeValue arccoth(ASTNode value) throws SBMLException {
			return new ASTNodeValue(FastMath.atanh(1.0 / toDouble(value)), this);
		}

		@Override
		public ASTNodeValue arccsc(ASTNode value) throws SBMLException {
			return new ASTNodeValue(FastMath.asin(1.0 / toDouble(value)), this);
		}

		@Override
		public ASTNodeValue arccsch(ASTNode value) throws SBMLException {
			return new ASTNodeValue(FastMath.asinh(1.0 / toDouble(value)), this);
		}

		@Override
		public ASTNodeValue arcsec(ASTNode value) throws SBMLException {
			return new ASTNodeValue(FastMath.acos(1.0 / toDouble(value)), this);
		}

		@Override
		public ASTNodeValue arcsech(ASTNode value) throws SBMLException {
			return new ASTNodeValue(FastMath.acosh(1.0 / toDouble(value)), this);
		}

		@Override
		public ASTNodeValue arcsin(ASTNode value) throws SBMLException {
			return new ASTNodeValue(FastMath.asin(toDouble(value)), this);
		}

		@Override
		public ASTNodeValue arcsinh(ASTNode value) throws SBMLException {
			return new ASTNodeValue(FastMath.asinh(toDouble(value)), this);
		}

		@Override
		public ASTNodeValue arctan(ASTNode value) throws SBMLException {
			return new ASTNodeValue(FastMath.atan(toDouble(value)), this);
		}

		@Override
		public ASTNodeValue arctanh(ASTNode value) throws SBMLException {
			return new ASTNodeValue(FastMath.atanh(toDouble(value)), this);
		}

		@Override
		public ASTNodeValue ceiling(ASTNode value) throws SBMLException {
			return new ASTNodeValue(FastMath.ceil(toDouble(value)), this);
		}

		@Override
		public ASTNodeValue compile(Compartment c) {
			throw new UnsupportedOperationException("Method not supported by DoubleCompiler");
		}

		@Override
		public ASTNodeValue compile(double mantissa, int exponent, String units) {
			throw new UnsupportedOperationException("Method not supported by DoubleCompiler");
		}

		@Override
		public ASTNodeValue compile(double real, String units) {
			throw new UnsupportedOperationException("Method not supported by DoubleCompiler");
		}

		@Override
		public ASTNodeValue compile(int integer, String units) {
			throw new UnsupportedOperationException("Method not supported by DoubleCompiler");
		}

		@Override
		public ASTNodeValue compile(CallableSBase variable) throws SBMLException {
			throw new UnsupportedOperationException("Method not supported by DoubleCompiler");
		}

		@Override
		public ASTNodeValue compile(String name) {
			Double value = variableValues.get(name);

			if (value == null) {
				throw new IllegalArgumentException("Variable \"" + name + "\" is undefined");
			}

			return new ASTNodeValue(value, this);
		}

		@Override
		public ASTNodeValue cos(ASTNode value) throws SBMLException {
			return new ASTNodeValue(FastMath.cos(toDouble(value)), this);
		}

		@Override
		public ASTNodeValue cosh(ASTNode value) throws SBMLException {
			return new ASTNodeValue(FastMath.cosh(toDouble(value)), this);
		}

		@Override
		public ASTNodeValue cot(ASTNode value) throws SBMLException {
			return new ASTNodeValue(1.0 / FastMath.tan(toDouble(value)), this);
		}

		@Override
		public ASTNodeValue coth(ASTNode value) throws SBMLException {
			return new ASTNodeValue(1.0 / FastMath.tanh(toDouble(value)), this);
		}

		@Override
		public ASTNodeValue csc(ASTNode value) throws SBMLException {
			return new ASTNodeValue(1.0 / FastMath.sin(toDouble(value)), this);
		}

		@Override
		public ASTNodeValue csch(ASTNode value) throws SBMLException {
			return new ASTNodeValue(1.0 / FastMath.sinh(toDouble(value)), this);
		}

		@Override
		public ASTNodeValue delay(String delayName, ASTNode x, ASTNode delay, String timeUnits) throws SBMLException {
			throw new UnsupportedOperationException("Method not supported by DoubleCompiler");
		}

		@Override
		public ASTNodeValue eq(ASTNode left, ASTNode right) throws SBMLException {
			return new ASTNodeValue(toDouble(left) == toDouble(right) ? 1.0 : 0.0, this);
		}

		@Override
		public ASTNodeValue exp(ASTNode value) throws SBMLException {
			return new ASTNodeValue(FastMath.exp(toDouble(value)), this);
		}

		@Override
		public ASTNodeValue factorial(ASTNode value) throws SBMLException {
			double v = toDouble(value);
			double roundedV = FastMath.round(v);

			if (v != roundedV) {
				throw new IllegalArgumentException("factorial cannot be computed from floating point number: " + v);
			}

			return new ASTNodeValue(CombinatoricsUtils.factorialDouble((int) roundedV), this);
		}

		@Override
		public ASTNodeValue floor(ASTNode value) throws SBMLException {
			return new ASTNodeValue(FastMath.floor(toDouble(value)), this);
		}

		@Override
		public ASTNodeValue frac(ASTNode numerator, ASTNode denominator) throws SBMLException {
			return new ASTNodeValue(toDouble(numerator) / toDouble(denominator), this);
		}

		@Override
		public ASTNodeValue frac(int numerator, int denominator) throws SBMLException {
			return new ASTNodeValue(numerator / denominator, this);
		}

		@Override
		public ASTNodeValue function(FunctionDefinition functionDefinition, List<ASTNode> args) throws SBMLException {
			throw new UnsupportedOperationException("Method not supported by DoubleCompiler");
		}

		@Override
		public ASTNodeValue function(String functionDefinitionName, List<ASTNode> args) throws SBMLException {
			throw new UnsupportedOperationException("Method not supported by DoubleCompiler");
		}

		@Override
		public ASTNodeValue geq(ASTNode left, ASTNode right) throws SBMLException {
			return new ASTNodeValue(toDouble(left) >= toDouble(right) ? 1.0 : 0.0, this);
		}

		@Override
		public ASTNodeValue getConstantAvogadro(String name) {
			throw new UnsupportedOperationException("Method not supported by DoubleCompiler");
		}

		@Override
		public ASTNodeValue getConstantE() {
			return new ASTNodeValue(FastMath.E, this);
		}

		@Override
		public ASTNodeValue getConstantFalse() {
			return new ASTNodeValue(0.0, this);
		}

		@Override
		public ASTNodeValue getConstantPi() {
			return new ASTNodeValue(FastMath.PI, this);
		}

		@Override
		public ASTNodeValue getConstantTrue() {
			return new ASTNodeValue(1.0, this);
		}

		@Override
		public ASTNodeValue getNegativeInfinity() throws SBMLException {
			return new ASTNodeValue(Double.NEGATIVE_INFINITY, this);
		}

		@Override
		public ASTNodeValue getPositiveInfinity() {
			return new ASTNodeValue(Double.POSITIVE_INFINITY, this);
		}

		@Override
		public ASTNodeValue gt(ASTNode left, ASTNode right) throws SBMLException {
			return new ASTNodeValue(toDouble(left) > toDouble(right) ? 1.0 : 0.0, this);
		}

		@Override
		public ASTNodeValue lambda(List<ASTNode> values) throws SBMLException {
			throw new UnsupportedOperationException("Method not supported by DoubleCompiler");
		}

		@Override
		public ASTNodeValue leq(ASTNode left, ASTNode right) throws SBMLException {
			return new ASTNodeValue(toDouble(left) <= toDouble(right) ? 1.0 : 0.0, this);
		}

		@Override
		public ASTNodeValue ln(ASTNode value) throws SBMLException {
			return new ASTNodeValue(FastMath.log(toDouble(value)), this);
		}

		@Override
		public ASTNodeValue log(ASTNode value) throws SBMLException {
			return new ASTNodeValue(FastMath.log10(toDouble(value)), this);
		}

		@Override
		public ASTNodeValue log(ASTNode base, ASTNode value) throws SBMLException {
			return new ASTNodeValue(FastMath.log(toDouble(base), toDouble(value)), this);
		}

		@Override
		public ASTNodeValue lt(ASTNode left, ASTNode right) throws SBMLException {
			return new ASTNodeValue(toDouble(left) < toDouble(right) ? 1.0 : 0.0, this);
		}

		@Override
		public ASTNodeValue minus(List<ASTNode> values) throws SBMLException {
			return new ASTNodeValue(toDouble(values.get(0)) - toDouble(values.get(1)), this);
		}

		@Override
		public ASTNodeValue neq(ASTNode left, ASTNode right) throws SBMLException {
			return new ASTNodeValue(toDouble(left) != toDouble(right) ? 1.0 : 0.0, this);
		}

		@Override
		public ASTNodeValue not(ASTNode value) throws SBMLException {
			return new ASTNodeValue(toDouble(value) != 0.0 ? 0.0 : 1.0, this);
		}

		@Override
		public ASTNodeValue or(List<ASTNode> values) throws SBMLException {
			return new ASTNodeValue(toDouble(values.get(0)) != 0.0 || toDouble(values.get(1)) != 0.0 ? 1.0 : 0.0, this);
		}

		@Override
		public ASTNodeValue piecewise(List<ASTNode> values) throws SBMLException {
			throw new UnsupportedOperationException("Method not supported by DoubleCompiler");
		}

		@Override
		public ASTNodeValue plus(List<ASTNode> values) throws SBMLException {
			return new ASTNodeValue(toDouble(values.get(0)) + toDouble(values.get(1)), this);
		}

		@Override
		public ASTNodeValue pow(ASTNode base, ASTNode exponent) throws SBMLException {
			return new ASTNodeValue(FastMath.pow(toDouble(base), toDouble(exponent)), this);
		}

		@Override
		public ASTNodeValue root(ASTNode rootExponent, ASTNode radiant) throws SBMLException {
			return new ASTNodeValue(FastMath.pow(toDouble(radiant), 1.0 / toDouble(rootExponent)), this);
		}

		@Override
		public ASTNodeValue root(double rootExponent, ASTNode radiant) throws SBMLException {
			return new ASTNodeValue(FastMath.pow(toDouble(radiant), 1.0 / rootExponent), this);
		}

		@Override
		public ASTNodeValue sec(ASTNode value) throws SBMLException {
			return new ASTNodeValue(1.0 / FastMath.cos(toDouble(value)), this);
		}

		@Override
		public ASTNodeValue sech(ASTNode value) throws SBMLException {
			return new ASTNodeValue(1.0 / FastMath.cosh(toDouble(value)), this);
		}

		@Override
		public ASTNodeValue selector(List<ASTNode> nodes) throws SBMLException {
			throw new UnsupportedOperationException("Method not supported by DoubleCompiler");
		}

		@Override
		public ASTNodeValue sin(ASTNode value) throws SBMLException {
			return new ASTNodeValue(FastMath.sin(toDouble(value)), this);
		}

		@Override
		public ASTNodeValue sinh(ASTNode value) throws SBMLException {
			return new ASTNodeValue(FastMath.sinh(toDouble(value)), this);
		}

		@Override
		public ASTNodeValue sqrt(ASTNode radiant) throws SBMLException {
			return new ASTNodeValue(FastMath.sqrt(toDouble(radiant)), this);
		}

		@Override
		public ASTNodeValue symbolTime(String time) {
			Double value = variableValues.get("time");

			if (value == null) {
				throw new IllegalArgumentException("Variable \"time\" is undefined");
			}

			return new ASTNodeValue(value, this);
		}

		@Override
		public ASTNodeValue tan(ASTNode value) throws SBMLException {
			return new ASTNodeValue(FastMath.tan(toDouble(value)), this);
		}

		@Override
		public ASTNodeValue tanh(ASTNode value) throws SBMLException {
			return new ASTNodeValue(FastMath.tanh(toDouble(value)), this);
		}

		@Override
		public ASTNodeValue times(List<ASTNode> values) throws SBMLException {
			return new ASTNodeValue(toDouble(values.get(0)) * toDouble(values.get(1)), this);
		}

		@Override
		public ASTNodeValue uMinus(ASTNode value) throws SBMLException {
			return new ASTNodeValue(-(toDouble(value)), this);
		}

		@Override
		public ASTNodeValue unknownValue() throws SBMLException {
			return new ASTNodeValue(Double.NaN, this);
		}

		@Override
		public ASTNodeValue vector(List<ASTNode> nodes) throws SBMLException {
			throw new UnsupportedOperationException("Method not supported by DoubleCompiler");
		}

		@Override
		public ASTNodeValue xor(List<ASTNode> values) throws SBMLException {
			return new ASTNodeValue(toDouble(values.get(0)) != 0.0 ^ toDouble(values.get(1)) != 0.0 ? 1.0 : 0.0, this);
		}

		private double toDouble(ASTNode value) {
			return value.isNumber() ? value.getReal() : value.compile(this).toDouble();
		}
	}
}
