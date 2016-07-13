/*******************************************************************************
 * Copyright (c) 2016 Federal Institute for Risk Assessment (BfR), Germany
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
package de.bund.bfr.math;

import java.util.LinkedHashSet;
import java.util.Set;

import org.lsmp.djep.djep.DJep;
import org.lsmp.djep.djep.DiffRulesI;
import org.lsmp.djep.djep.diffRules.MacroDiffRules;
import org.lsmp.djep.xjep.MacroFunction;
import org.nfunk.jep.ASTFunNode;
import org.nfunk.jep.Node;
import org.nfunk.jep.ParseException;

public class Parser {

	private DJep parser;

	public Parser() {
		this(new LinkedHashSet<>());
	}

	public Parser(Set<String> variables) {
		parser = new DJep();

		parser.setAllowAssignment(true);
		parser.setAllowUndeclared(true);
		parser.setImplicitMul(true);
		parser.addStandardFunctions();
		parser.addStandardDiffRules();
		parser.removeVariable("x");

		try {
			parser.removeFunction("log");
			parser.addFunction("log", new MacroFunction("log", 1, "ln(x)", parser));
			parser.addDiffRule(new MacroDiffRules(parser, "log", "1/x"));
			parser.addFunction("log10", new MacroFunction("log10", 1, "ln(x)/ln(10)", parser));
			parser.addDiffRule(new MacroDiffRules(parser, "log10", "1/(x*ln(10))"));

			parser.addDiffRule(new ZeroDiffRule("<"));
			parser.addDiffRule(new ZeroDiffRule(">"));
			parser.addDiffRule(new ZeroDiffRule("<="));
			parser.addDiffRule(new ZeroDiffRule(">="));
			parser.addDiffRule(new ZeroDiffRule("&&"));
			parser.addDiffRule(new ZeroDiffRule("||"));
			parser.addDiffRule(new ZeroDiffRule("=="));
			parser.addDiffRule(new ZeroDiffRule("!="));
		} catch (ParseException e) {
			e.printStackTrace();
		}

		for (String var : variables) {
			parser.addVariable(var, 0.0);
		}
	}

	public void addVariable(String var) {
		parser.addVariable(var, 0.0);
	}

	public void setVarValue(String var, double value) {
		parser.setVarValue(var, value);
	}

	public void addConstant(String constant, double value) {
		parser.addConstant(constant, value);
	}

	public Node parse(String term) throws ParseException {
		return parser.parse(term);
	}

	public Set<String> getSymbols() {
		Set<String> symbols = new LinkedHashSet<>();

		for (Object s : parser.getSymbolTable().keySet()) {
			symbols.add((String) s);
		}

		return symbols;
	}

	public Node differentiate(Node function, String var) throws ParseException {
		return parser.differentiate(function, var);
	}

	public double evaluate(Node function) throws ParseException {
		Object value = parser.evaluate(function);

		return value instanceof Double ? (Double) value : Double.NaN;
	}

	private static class ZeroDiffRule implements DiffRulesI {

		private String name;

		public ZeroDiffRule(String name) {
			this.name = name;
		}

		@Override
		public Node differentiate(ASTFunNode node, String var, Node[] children, Node[] dchildren, DJep djep)
				throws ParseException {
			return djep.getNodeFactory().buildConstantNode(0.0);
		}

		@Override
		public String getName() {
			return name;
		}
	}
}
