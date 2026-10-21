package com.strefiz.multicore.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the pure {@link MetricsRollup}.
 */
class MetricsRollupTest {

	@Test
	void returnsEmptyWhenNothingWasRecorded() {
		assertEquals("", new MetricsRollup(new TickMetrics()).hottest(5));
	}

	@Test
	void ordersHottestFirst() {
		TickMetrics metrics = new TickMetrics();
		metrics.beginWork("slow");
		metrics.endWork("slow");
		metrics.beginWork("fast");
		metrics.endWork("fast");

		String rollup = new MetricsRollup(metrics).hottest(5);

		assertTrue(rollup.contains("slow="));
		assertTrue(rollup.contains("fast="));
		assertTrue(rollup.indexOf("slow=") < rollup.indexOf("fast="), "hottest label must come first");
	}

	@Test
	void respectsTheLimit() {
		TickMetrics metrics = new TickMetrics();
		for (int i = 0; i < 5; i++) {
			metrics.beginWork("w" + i);
			metrics.endWork("w" + i);
		}

		String rollup = new MetricsRollup(metrics).hottest(2);

		assertEquals(2, rollup.split(",").length);
	}

	@Test
	void zeroLimitReturnsEmpty() {
		TickMetrics metrics = new TickMetrics();
		metrics.beginWork("w");
		metrics.endWork("w");

		assertEquals("", new MetricsRollup(metrics).hottest(0));
	}
}
