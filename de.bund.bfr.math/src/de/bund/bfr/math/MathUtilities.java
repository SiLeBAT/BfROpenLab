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
package de.bund.bfr.math;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.distribution.TDistribution;
import org.lsmp.djep.djep.DJep;
import org.lsmp.djep.djep.DiffRulesI;
import org.lsmp.djep.djep.diffRules.MacroDiffRules;
import org.lsmp.djep.xjep.MacroFunction;
import org.nfunk.jep.ASTFunNode;
import org.nfunk.jep.Node;
import org.nfunk.jep.ParseException;
import org.nfunk.jep.TokenMgrError;

public class MathUtilities {

	private MathUtilities() {
	}

	public static double computeSum(List<Double> values) {
		double sum = 0.0;

		for (double v : values) {
			sum += v;
		}

		return sum;
	}

	public static String replaceVariable(String formula, String var,
			String newVar) {
		if (var.equals(newVar)) {
			return formula;
		}

		String newFormular = " " + formula + " ";
		boolean foundReplacement = true;

		while (foundReplacement) {
			foundReplacement = false;

			for (int i = 1; i < newFormular.length() - var.length(); i++) {
				boolean matches = newFormular.substring(i, i + var.length())
						.equals(var);
				boolean start = !isVariableCharacter(newFormular.charAt(i - 1));
				boolean end = !isVariableCharacter(newFormular.charAt(i
						+ var.length()));

				if (matches && start && end) {
					String orginal = newFormular.substring(i - 1,
							i + var.length() + 1);
					String replacement = newFormular.charAt(i - 1) + newVar
							+ newFormular.charAt(i + var.length());

					newFormular = newFormular.replace(orginal, replacement);
					foundReplacement = true;
					break;
				}
			}
		}

		return newFormular.replace(" ", "");
	}

	public static boolean isVariableCharacter(char ch) {
		return Character.isLetterOrDigit(ch) || ch == '_' || ch == '$';
	}

	public static List<String> getSymbols(String formula) {
		List<String> symbols = new ArrayList<String>();
		DJep parser = MathUtilities.createParser();

		try {
			parser.parse(formula);
		} catch (ParseException e) {
			return symbols;
		} catch (NullPointerException e) {
			return symbols;
		} catch (TokenMgrError e) {
			return symbols;
		}

		for (Object symbol : parser.getSymbolTable().keySet()) {
			symbols.add(symbol.toString());
		}

		return symbols;
	}

	public static Double getMSE(int numParam, int numSample, double sse) {
		if (numSample <= numParam) {
			return null;
		}

		return sse / (numSample - numParam);
	}

	public static Double getRMSE(int numParam, int numSample, double sse) {
		if (numSample <= numParam) {
			return null;
		}

		return Math.sqrt(sse / (numSample - numParam));
	}

	public static Double getR2(double sse, List<Double> targetValues) {
		if (targetValues.size() < 2) {
			return null;
		}

		double targetSum = 0.0;

		for (double v : targetValues) {
			targetSum += v;
		}

		double targetMean = targetSum / targetValues.size();
		double targetTotalSumOfSquares = 0.0;

		for (int i = 0; i < targetValues.size(); i++) {
			targetTotalSumOfSquares += Math.pow(targetValues.get(i)
					- targetMean, 2.0);
		}

		double rSquared = 1 - sse / targetTotalSumOfSquares;

		return Math.max(rSquared, 0.0);
	}

	public static Double getAic(int numParam, int numSample, double sse) {
		if (numSample <= numParam + 2) {
			return null;
		}

		return numSample * Math.log(sse / numSample) + 2 * (numParam + 1) + 2
				* (numParam + 1) * (numParam + 2) / (numSample - numParam - 2);
	}

	public static double getPValue(double tValue, int degreesOfFreedom) {
		TDistribution dist = new TDistribution(degreesOfFreedom);

		return 1.0 - dist.probability(-Math.abs(tValue), Math.abs(tValue));
	}

	public static DJep createParser() {
		DJep parser = new DJep();

		parser.setAllowAssignment(true);
		parser.setAllowUndeclared(true);
		parser.setImplicitMul(true);
		parser.addStandardFunctions();
		parser.addStandardDiffRules();
		parser.removeVariable("x");

		try {
			parser.removeFunction("log");
			parser.addFunction("log", new MacroFunction("log", 1, "ln(x)",
					parser));
			parser.addDiffRule(new MacroDiffRules(parser, "log", "1/x"));
			parser.addFunction("log10", new MacroFunction("log10", 1,
					"ln(x)/ln(10)", parser));
			parser.addDiffRule(new MacroDiffRules(parser, "log10",
					"1/(x*ln(10))"));

			parser.addDiffRule(new ZeroDiffRule("<"));
			parser.addDiffRule(new ZeroDiffRule(">"));
			parser.addDiffRule(new ZeroDiffRule("<="));
			parser.addDiffRule(new ZeroDiffRule(">="));
			parser.addDiffRule(new ZeroDiffRule("&&"));
			parser.addDiffRule(new ZeroDiffRule("||"));
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return parser;
	}

	private static class ZeroDiffRule implements DiffRulesI {

		private String name;

		public ZeroDiffRule(String name) {
			this.name = name;
		}

		@Override
		public Node differentiate(ASTFunNode node, String var, Node[] children,
				Node[] dchildren, DJep djep) throws ParseException {
			return djep.getNodeFactory().buildConstantNode(0.0);
		}

		@Override
		public String getName() {
			return name;
		}

	}

}
