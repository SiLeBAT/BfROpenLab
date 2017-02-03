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
import static org.junit.Assert.assertThat;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.CoreMatchers.hasItem;

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

	private static final String TRADER_1 = "trader1";

	private static final String MARKET_1 = "market1";
	private static final String MARKET_2 = "market2";
	private static final String MARKET_3 = "market3";

	private static final String F1P1_1 = FARM_1 + PRODUCER_1 + 1;
	private static final String F1P1_2 = FARM_1 + PRODUCER_1 + 2;
	private static final String F2P1_1 = FARM_2 + PRODUCER_1 + 1;
	private static final String F2P2_1 = FARM_2 + PRODUCER_2 + 1;
	private static final String F3P2_1 = FARM_3 + PRODUCER_2 + 1;

	private static final String P1T1_1 = PRODUCER_1 + TRADER_1 + 1;
	private static final String P1T1_2 = PRODUCER_1 + TRADER_1 + 2;
	private static final String P2T1_1 = PRODUCER_2 + TRADER_1 + 1;
	private static final String P2T1_2 = PRODUCER_2 + TRADER_1 + 2;

	private static final String T1M1_1 = TRADER_1 + MARKET_1 + 1;
	private static final String T1M2_1 = TRADER_1 + MARKET_2 + 1;
	private static final String T1M2_2 = TRADER_1 + MARKET_2 + 2;
	private static final String T1M3_1 = TRADER_1 + MARKET_3 + 1;

	private List<Delivery> deliveries;

	@Before
	public void setUp() {
		deliveries = new ArrayList<>();

		deliveries.add(new Delivery.Builder(F1P1_1, FARM_1, PRODUCER_1)
				.connectedDeliveries(ImmutableSet.of(), ImmutableSet.of(P1T1_1)).departure(2016, 1, 1).build());
		deliveries.add(new Delivery.Builder(F1P1_2, FARM_1, PRODUCER_1)
				.connectedDeliveries(ImmutableSet.of(), ImmutableSet.of(P1T1_2)).departure(2016, 1, 1)
				.arrival(2016, 1, 3).build());
		deliveries.add(new Delivery.Builder(F2P1_1, FARM_2, PRODUCER_1)
				.connectedDeliveries(ImmutableSet.of(), ImmutableSet.of(P1T1_1)).departure(2016, 1, 1).build());
		deliveries.add(new Delivery.Builder(F2P2_1, FARM_2, PRODUCER_2)
				.connectedDeliveries(ImmutableSet.of(), ImmutableSet.of(P2T1_1)).departure(2016, 1, 5).build());
		deliveries.add(new Delivery.Builder(F3P2_1, FARM_3, PRODUCER_2)
				.connectedDeliveries(ImmutableSet.of(), ImmutableSet.of(P2T1_1, P2T1_2)).departure(2016, 1, 5).build());

		deliveries.add(new Delivery.Builder(P1T1_1, PRODUCER_1, TRADER_1)
				.connectedDeliveries(ImmutableSet.of(F1P1_1, F2P1_1), ImmutableSet.of(T1M1_1)).departure(2016, 1, 2)
				.build());
		deliveries.add(new Delivery.Builder(P1T1_2, PRODUCER_1, TRADER_1)
				.connectedDeliveries(ImmutableSet.of(F1P1_2), ImmutableSet.of(T1M2_1)).departure(2016, 1, 4).build());
		deliveries.add(new Delivery.Builder(P2T1_1, PRODUCER_2, TRADER_1)
				.connectedDeliveries(ImmutableSet.of(F2P2_1, F3P2_1), ImmutableSet.of(T1M2_2)).departure(2016, 1, 6)
				.build());
		deliveries.add(new Delivery.Builder(P2T1_2, PRODUCER_2, TRADER_1)
				.connectedDeliveries(ImmutableSet.of(F3P2_1), ImmutableSet.of(T1M3_1)).departure(2016, 1, 6).build());

		deliveries.add(new Delivery.Builder(T1M1_1, TRADER_1, MARKET_1)
				.connectedDeliveries(ImmutableSet.of(P1T1_1), ImmutableSet.of()).departure(2016, 1, 5).build());
		deliveries.add(new Delivery.Builder(T1M2_1, TRADER_1, MARKET_2)
				.connectedDeliveries(ImmutableSet.of(P1T1_2), ImmutableSet.of()).departure(2016, 1, null).build());
		deliveries.add(new Delivery.Builder(T1M2_2, TRADER_1, MARKET_2)
				.connectedDeliveries(ImmutableSet.of(P2T1_1), ImmutableSet.of()).departure(2016, 1, 7).build());
		deliveries.add(new Delivery.Builder(T1M3_1, TRADER_1, MARKET_3)
				.connectedDeliveries(ImmutableSet.of(P2T1_2), ImmutableSet.of()).departure(2016, 1, 7).build());
	}

	@Test
	public void testStationToStationTrace() {
		Tracing tracing = new Tracing(deliveries);
		Tracing.Result result = tracing.getResult(true);

		assertEquals(ImmutableSet.of(PRODUCER_1, TRADER_1, MARKET_1, MARKET_2),
				result.getForwardStationsByStation().get(FARM_1));
		assertEquals(ImmutableSet.of(PRODUCER_1, PRODUCER_2, TRADER_1, MARKET_1, MARKET_2),
				result.getForwardStationsByStation().get(FARM_2));
		assertEquals(ImmutableSet.of(PRODUCER_2, TRADER_1, MARKET_2, MARKET_3),
				result.getForwardStationsByStation().get(FARM_3));
		assertEquals(ImmutableSet.of(), result.getForwardStationsByStation().get(MARKET_1));
		assertEquals(ImmutableSet.of(), result.getForwardStationsByStation().get(MARKET_2));
		assertEquals(ImmutableSet.of(), result.getForwardStationsByStation().get(MARKET_3));

		assertEquals(ImmutableSet.of(), result.getBackwardStationsByStation().get(FARM_1));
		assertEquals(ImmutableSet.of(), result.getBackwardStationsByStation().get(FARM_2));
		assertEquals(ImmutableSet.of(), result.getBackwardStationsByStation().get(FARM_3));
		assertEquals(ImmutableSet.of(TRADER_1, PRODUCER_1, FARM_1, FARM_2),
				result.getBackwardStationsByStation().get(MARKET_1));
		assertEquals(ImmutableSet.of(TRADER_1, PRODUCER_1, PRODUCER_2, FARM_1, FARM_2, FARM_3),
				result.getBackwardStationsByStation().get(MARKET_2));
		assertEquals(ImmutableSet.of(TRADER_1, PRODUCER_2, FARM_3),
				result.getBackwardStationsByStation().get(MARKET_3));
	}

	@Test
	public void testDeliveryToDeliveryTrace() {
		Tracing tracing = new Tracing(deliveries);
		Tracing.Result result = tracing.getResult(true);

		assertEquals(ImmutableSet.of(P1T1_1, T1M1_1), result.getForwardDeliveriesByDelivery().get(F1P1_1));
		assertEquals(ImmutableSet.of(P1T1_2, T1M2_1), result.getForwardDeliveriesByDelivery().get(F1P1_2));
		assertEquals(ImmutableSet.of(P1T1_1, T1M1_1), result.getForwardDeliveriesByDelivery().get(F2P1_1));
		assertEquals(ImmutableSet.of(P2T1_1, T1M2_2), result.getForwardDeliveriesByDelivery().get(F2P2_1));
		assertEquals(ImmutableSet.of(P2T1_1, P2T1_2, T1M2_2, T1M3_1),
				result.getForwardDeliveriesByDelivery().get(F3P2_1));
		assertEquals(ImmutableSet.of(), result.getForwardDeliveriesByDelivery().get(T1M1_1));
		assertEquals(ImmutableSet.of(), result.getForwardDeliveriesByDelivery().get(T1M2_1));
		assertEquals(ImmutableSet.of(), result.getForwardDeliveriesByDelivery().get(T1M2_2));
		assertEquals(ImmutableSet.of(), result.getForwardDeliveriesByDelivery().get(T1M3_1));

		assertEquals(ImmutableSet.of(), result.getBackwardDeliveriesByDelivery().get(F1P1_1));
		assertEquals(ImmutableSet.of(), result.getBackwardDeliveriesByDelivery().get(F1P1_2));
		assertEquals(ImmutableSet.of(), result.getBackwardDeliveriesByDelivery().get(F2P1_1));
		assertEquals(ImmutableSet.of(), result.getBackwardDeliveriesByDelivery().get(F2P2_1));
		assertEquals(ImmutableSet.of(), result.getBackwardDeliveriesByDelivery().get(F3P2_1));
		assertEquals(ImmutableSet.of(P1T1_1, F1P1_1, F2P1_1), result.getBackwardDeliveriesByDelivery().get(T1M1_1));
		assertEquals(ImmutableSet.of(P1T1_2, F1P1_2), result.getBackwardDeliveriesByDelivery().get(T1M2_1));
		assertEquals(ImmutableSet.of(P2T1_1, F2P2_1, F3P2_1), result.getBackwardDeliveriesByDelivery().get(T1M2_2));
		assertEquals(ImmutableSet.of(P2T1_2, F3P2_1), result.getBackwardDeliveriesByDelivery().get(T1M3_1));
	}

	@Test
	public void testStationCrossContamination() {
		Tracing tracing = new Tracing(deliveries);

		tracing.setCrossContaminationOfStation(PRODUCER_1, true);

		Tracing.Result result = tracing.getResult(true);

		assertThat(result.getForwardDeliveriesByDelivery().get(F1P1_1), hasItem(P1T1_2));
		assertThat(result.getForwardDeliveriesByDelivery().get(F1P1_2), not(hasItem(P1T1_1)));
		assertThat(result.getBackwardDeliveriesByDelivery().get(P1T1_2), hasItem(F1P1_1));
		assertThat(result.getBackwardDeliveriesByDelivery().get(P1T1_1), not(hasItem(F1P1_2)));

		assertThat(tracing.getResult(false).getForwardDeliveriesByDelivery().get(F1P1_2), hasItem(P1T1_1));
	}
}
