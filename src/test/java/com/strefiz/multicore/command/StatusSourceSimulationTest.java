package com.strefiz.multicore.command;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import com.strefiz.multicore.config.ConfigMode;
import com.strefiz.multicore.metrics.TickMetrics;

import org.junit.jupiter.api.Test;

/**
 * Simulates {@link MultiCoreMod} as a {@link StatusSource} to isolate whether the formatter works
 * outside the server (where stack traces are hidden).
 */
class StatusSourceSimulationTest {

	private static class FakeSource implements StatusSource {
		@Override
		public String mode() {
			return ConfigMode.SAFE.name();
		}

		@Override
		public int workerThreads() {
			return 1;
		}

		@Override
		public int threadCount() {
			return 2;
		}

		@Override
		public TickMetrics.Snapshot metrics() {
			return new TickMetrics().snapshot();
		}

		@Override
		public Map<String, Double> threadTimesMs() {
			return Map.of("multicore-worker-0", 3.25);
		}

		@Override
		public String compatSummary() {
			return "legacy=3 compatible=1 of 4";
		}

		@Override
		public String compatDump() {
			return "mod-a: LEGACY%nmod-b: COMPATIBLE".replace("%n", "\n");
		}

		@Override
		public String regionSummary() {
			return "";
		}

		@Override
		public String metricsRollup() {
			return "multicore-worker-0=3.250ms";
		}
	}

	@Test
	void statusFormatterWorksWithFakeSource() {
		String text = StatusFormatter.status(new FakeSource());

		assertTrue(text.contains("mode=SAFE"));
		assertTrue(text.contains("workers=1"));
		assertTrue(text.contains("threads=2"));
		assertTrue(text.contains("ticks=0"));
		assertTrue(text.contains("last=0.00"));
		assertTrue(text.contains("multicore-worker-0=3.250ms"));
	}

	@Test
	void compatFormatterWorks() {
		String text = StatusFormatter.compat("legacy=3 compatible=1 of 4");
		assertTrue(text.contains("legacy=3 compatible=1 of 4"));
	}

	@Test
	void compatDumpFormatterWorks() {
		String text = StatusFormatter.compatDump("mod-a: LEGACY\nmod-b: COMPATIBLE");
		assertTrue(text.contains("mod-a: LEGACY"));
		assertTrue(text.contains("mod-b: COMPATIBLE"));
	}
}
