package de.bund.bfr.knime.openkrise.db.imports.custom.bfrnewformat;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DeliveriesSheet {
	// zero based indices
	public static final int COL_DELIVERY_ID = SheetColumns.A;
	public static final int COL_SENDER_ID = SheetColumns.B;
	public static final int COL_PRODUCT_NAME = SheetColumns.C;
	public static final int COL_LOT_NO = SheetColumns.D;
	public static final int COL_LOT_SIZE_Q = SheetColumns.E;
	public static final int COL_LOT_SIZE_U = SheetColumns.F;
	public static final int COL_DELIVERY_DATE_D = SheetColumns.G;
	public static final int COL_DELIVERY_DATE_M = SheetColumns.H;
	public static final int COL_DELIVERY_DATE_Y = SheetColumns.I;
	public static final int COL_ARRIVAL_DATE_D = SheetColumns.J;
	public static final int COL_ARRIVAL_DATE_M = SheetColumns.K;
	public static final int COL_ARRIVAL_DATE_Y = SheetColumns.L;
	public static final int COL_UNIT_Q = SheetColumns.M;
	public static final int COL_UNIT_U = SheetColumns.N;
	public static final int COL_RECIPIENT = SheetColumns.O;
	
	public static final int COL_FIRST_ADDITIONAL = SheetColumns.Q;
	public static final int COL_LAST_ADDITIONAL = SheetColumns.Y;
	
	public static final String COL_LABEL_DELIVERY_ID = "Delivery ID";
	
	public static final Set<String> KNOWN_LOT_FIELDS_LC = Stream.of(new String[] {
			"Production date",
			"Best before date",
			"Treatment of product during production",
			"Sampling"
	}).map(String::toLowerCase).collect(Collectors.toSet());
}
