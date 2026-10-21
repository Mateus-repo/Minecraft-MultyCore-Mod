package com.strefiz.multicore.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link TickMetrics}.
 */
class TickMetricsTest {

	@Test
	void recordsTickDuration() throws InterruptedException {
		TickMetrics metrics = new TickMetrics();

		metrics.startTick();
		Thread.sleep(2);
		metrics.endTick();

		assertTrue(metrics.mspt() > 0.0, "a recorded tick must have a positive duration");
		assertEquals(1, metrics.tickCount());
		assertTrue(metrics.maxMspt() >= metrics.mspt());
	}

	@Test
	void accumulatesAverageAndMax() {
		TickMetrics metrics = new TickMetrics();
		metrics.startTick();
		metrics.endTick();
		double firstMax = metrics.maxMspt();

		metrics.startTick();
		metrics.endTick();

		assertEquals(2, metrics.tickCount());
		assertTrue(metrics.maxMspt() >= firstMax);
		assertTrue(metrics.averageMspt() > 0.0);
		assertTrue(metrics.averageMspt() <= metrics.maxMspt());
	}

	@Test
	void recordsPerThreadWork() {
		TickMetrics metrics = new TickMetrics();

		metrics.beginWork("multicore-worker-0");
		metrics.endWork("multicore-worker-0");
		metrics.beginWork("multicore-worker-0");
		metrics.endWork("multicore-worker-0");

		Map<String, Double> times = metrics.threadTimesMs();
		assertTrue(times.containsKey("multicore-worker-0"));
		assertTrue(times.get("multicore-worker-0") >= 0.0);
	}

	@Test
	void endWorkWithoutBeginIsIgnored() {
		TickMetrics metrics = new TickMetrics();

		metrics.endWork("never-started");

		assertTrue(metrics.threadTimesMs().isEmpty());
	}

	@Test
	void snapshotIsConsistent() {
		TickMetrics metrics = new TickMetrics();
		metrics.startTick();
		metrics.endTick();
		metrics.beginWork("w");
		metrics.endWork("w");

		TickMetrics.Snapshot snapshot = metrics.snapshot();

		assertEquals(metrics.tickCount(), snapshot.tickCount());
		assertEquals(metrics.mspt(), snapshot.lastMspt());
		assertEquals(metrics.averageMspt(), snapshot.averageMspt());
		assertEquals(metrics.maxMspt(), snapshot.maxMspt());
		assertTrue(snapshot.threadTimesMs().containsKey("w"));
	}

	@Test
	void resetClearsEverything() {
		TickMetrics metrics = new TickMetrics();
		metrics.startTick();
		metrics.endTick();
		metrics.beginWork("w");
		metrics.endWork("w");

		metrics.reset();

		assertEquals(0, metrics.tickCount());
		assertEquals(0.0, metrics.mspt());
		assertTrue(metrics.threadTimesMs().isEmpty());
	}
}
