/*******************************************************************************
 * Copyright (c) 2018 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.openkrise.db.imports.custom.bfrnewformat;

import java.util.Arrays;
import java.util.HashMap;

public class XlsDelivery {

	private HashMap<Integer, String> extraVals = new HashMap<>();	
	public HashMap<Integer, String> getExtraVals() {
		return extraVals;
	}

	public static String BLOCK(String lang) {
		if (lang == null) return null;
		if (lang.equals("de")) return "Lieferung";
		else if (lang.equals("en")) return "Delivery";
		else if (lang.equals("es")) return "Envío";
		else return null;
	}
	
	private static String[] DELIVERY_DATE_HEADERS(String lang) {
		if (lang == null) return new String[0];
		if (lang.equals("de")) return new String[] { "Lieferdatum" };
		else if (lang.equals("en")) return new String[] { "DeliveryDate" };
		else if (lang.equals("es")) return new String[]{ "Fechaderecepcióndeproducto", "Fechaderecepción", "Fechadeenvío" };
		else return new String[0];
	}
	
	private static String DAY(String lang) {
		if (lang == null) return null;
		if (lang.equals("de")) return "Tag";
		else if (lang.equals("en")) return "Day";
		else if (lang.equals("es")) return "Día";
		else return null;
	}
	private static String MONTH(String lang) {
		if (lang == null) return null;
		if (lang.equals("de")) return "Monat";
		else if (lang.equals("en")) return "Month";
		else if (lang.equals("es")) return "Mes";
		else return null;
	}
	private static String YEAR(String lang) {
		if (lang == null) return null;
		if (lang.equals("de")) return "Jahr";
		else if (lang.equals("en")) return "Year";
		else if (lang.equals("es")) return "Año";
		else return null;
	}
	private static String AMOUNT(String lang) {
		if (lang == null) return null;
		if (lang.equals("de")) return "abgegebeneMenge";
		else if (lang.equals("en")) return "Amount";
		else if (lang.equals("es")) return "Cantidad";
		else return null;
	}
	public static String COMMENT(String lang) {
		if (lang == null) return null;
		if (lang.equals("de")) return "Kommentar";
		else if (lang.equals("en")) return "Comments";
		else if (lang.equals("es")) return "Comentarios";
		else return null;
	}
	
	public void addField(String fieldname, int index, String lang) {
		if (fieldname != null) {
			String s = fieldname.replaceAll("\\s+","");
			if (Arrays.asList(DELIVERY_DATE_HEADERS(lang)).stream().anyMatch((header) -> s.equalsIgnoreCase(header))) {
				deliveryDateCol = index;
				dayCol = index;
				monthCol = index+1;
				yearCol = index+2;
			}
			else if (s.equalsIgnoreCase(DAY(lang))) dayCol = index;
			else if (s.equalsIgnoreCase(MONTH(lang))) monthCol = index;
			else if (s.equalsIgnoreCase(YEAR(lang))) yearCol = index;
			else if (isAmount(s, lang)) amountCol = index;
			else if (s.equalsIgnoreCase(COMMENT(lang))) commentCol = index;
			else extraVals.put(index, s);
		}
	}
	private boolean isAmount(String parameter, String lang) {
		boolean result = parameter.toLowerCase().startsWith(AMOUNT(lang).toLowerCase());
		if (!result && lang.equals("de")) result = parameter.toLowerCase().startsWith("menge");
		return result;
	}

	private int startCol = -1;
	private int endCol = -1;
	private int dayCol = -1, monthCol = -1, yearCol = -1, amountCol = -1, commentCol = -1, chargenLinkCol = -1;
	private int deliveryDateCol = -1;

	public int getChargenLinkCol() {
		return chargenLinkCol;
	}
	public void setChargenLinkCol(int chargenLinkCol) {
		this.chargenLinkCol = chargenLinkCol;
	}
	public int getStartCol() {
		return startCol;
	}
	public void setStartCol(int startCol) {
		this.startCol = startCol;
	}
	public int getEndCol() {
		return endCol;
	}
	public void setEndCol(int endCol) {
		this.endCol = endCol;
	}
	public int getDayCol() {
		return dayCol;
	}
	public void setDayCol(int dayCol) {
		this.dayCol = dayCol;
	}
	public int getMonthCol() {
		return monthCol;
	}
	public void setMonthCol(int monthCol) {
		this.monthCol = monthCol;
	}
	public int getYearCol() {
		return yearCol;
	}
	public void setYearCol(int yearCol) {
		this.yearCol = yearCol;
	}
	public int getAmountCol() {
		return amountCol;
	}
	public void setAmountCol(int amountCol) {
		this.amountCol = amountCol;
	}
	public int getCommentCol() {
		return commentCol;
	}
	public void setCommentCol(int commentCol) {
		this.commentCol = commentCol;
	}
	
	public int getDeliveryDateCol() {
		return this.deliveryDateCol;
	}
}
