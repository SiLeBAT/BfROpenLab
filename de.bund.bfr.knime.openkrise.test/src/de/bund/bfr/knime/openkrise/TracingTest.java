/*******************************************************************************
 * Copyright (c) 2017 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.openkrise;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;

import de.bund.bfr.knime.openkrise.common.Delivery;

public class TracingTest {

	private static final String FARM_1 = "farm1";
	private static final String FARM_2 = "farm2";
	private static final String FARM_3 = "farm3";

	private static final String PRODUCER_1 = "producer1";
	private static final String PRODUCER_2 = "producer2";

	private static final String MARKET_1 = "market1";
	private static final String MARKET_2 = "market2";
	private static final String MARKET_3 = "market3";

	private static final String F1P1_1 = FARM_1 + PRODUCER_1 + 1;
	private static final String F1P1_2 = FARM_1 + PRODUCER_1 + 2;
	private static final String F2P1_1 = FARM_2 + PRODUCER_1 + 1;
	private static final String F2P2_1 = FARM_2 + PRODUCER_2 + 1;
	private static final String F3P2_1 = FARM_3 + PRODUCER_2 + 1;

	private static final String P1M1_1 = PRODUCER_1 + MARKET_1 + 1;
	private static final String P1M2_1 = PRODUCER_1 + MARKET_2 + 1;
	private static final String P2M2_1 = PRODUCER_2 + MARKET_2 + 1;
	private static final String P2M3_1 = PRODUCER_2 + MARKET_3 + 1;

	private List<Delivery> deliveries;

	@Before
	public void setUp() {
		deliveries = new ArrayList<>();

		deliveries.add(new Delivery.Builder(F1P1_1, FARM_1, PRODUCER_1)
				.connectedDeliveries(ImmutableSet.of(), ImmutableSet.of(P1M1_1)).build());
		deliveries.add(new Delivery.Builder(F1P1_2, FARM_1, PRODUCER_1)
				.connectedDeliveries(ImmutableSet.of(), ImmutableSet.of(P1M2_1)).build());
		deliveries.add(new Delivery.Builder(F2P1_1, FARM_2, PRODUCER_1)
				.connectedDeliveries(ImmutableSet.of(), ImmutableSet.of(P1M1_1)).build());
		deliveries.add(new Delivery.Builder(F2P2_1, FARM_2, PRODUCER_2)
				.connectedDeliveries(ImmutableSet.of(), ImmutableSet.of(P2M2_1)).build());
		deliveries.add(new Delivery.Builder(F3P2_1, FARM_3, PRODUCER_2)
				.connectedDeliveries(ImmutableSet.of(), ImmutableSet.of(P2M3_1)).build());

		deliveries.add(new Delivery.Builder(P1M1_1, PRODUCER_1, MARKET_1)
				.connectedDeliveries(ImmutableSet.of(F1P1_1, F2P1_1), ImmutableSet.of()).build());
		deliveries.add(new Delivery.Builder(P1M2_1, PRODUCER_1, MARKET_2)
				.connectedDeliveries(ImmutableSet.of(F1P1_2), ImmutableSet.of()).build());
		deliveries.add(new Delivery.Builder(P2M2_1, PRODUCER_2, MARKET_2)
				.connectedDeliveries(ImmutableSet.of(F2P2_1), ImmutableSet.of()).build());
		deliveries.add(new Delivery.Builder(P2M3_1, PRODUCER_2, MARKET_3)
				.connectedDeliveries(ImmutableSet.of(F3P2_1), ImmutableSet.of()).build());
	}

	@Test
	public void testStationToStationTrace() {
		Tracing tracing = new Tracing(deliveries);
		Tracing.Result result = tracing.getResult(true);

		assertEquals(ImmutableSet.of(PRODUCER_1, MARKET_1, MARKET_2), result.getForwardStationsByStation().get(FARM_1));
		assertEquals(ImmutableSet.of(PRODUCER_1, PRODUCER_2, MARKET_1, MARKET_2),
				result.getForwardStationsByStation().get(FARM_2));
		assertEquals(ImmutableSet.of(PRODUCER_2, MARKET_3), result.getForwardStationsByStation().get(FARM_3));
		assertEquals(ImmutableSet.of(), result.getBackwardStationsByStation().get(FARM_1));
		assertEquals(ImmutableSet.of(), result.getBackwardStationsByStation().get(FARM_2));
		assertEquals(ImmutableSet.of(), result.getBackwardStationsByStation().get(FARM_3));

		assertEquals(ImmutableSet.of(), result.getForwardStationsByStation().get(MARKET_1));
		assertEquals(ImmutableSet.of(), result.getForwardStationsByStation().get(MARKET_2));
		assertEquals(ImmutableSet.of(), result.getForwardStationsByStation().get(MARKET_3));
		assertEquals(ImmutableSet.of(PRODUCER_1, FARM_1, FARM_2), result.getBackwardStationsByStation().get(MARKET_1));
		assertEquals(ImmutableSet.of(PRODUCER_1, PRODUCER_2, FARM_1, FARM_2),
				result.getBackwardStationsByStation().get(MARKET_2));
		assertEquals(ImmutableSet.of(PRODUCER_2, FARM_3), result.getBackwardStationsByStation().get(MARKET_3));
	}
}
