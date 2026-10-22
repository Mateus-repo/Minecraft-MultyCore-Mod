package com.strefiz.multicore.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import com.strefiz.multicore.metrics.TickMetrics;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the pure {@link StatusFormatter} text (no game needed).
 */
class StatusFormatterTest {

	private static class FakeSource implements StatusSource {
		@Override
		public String mode() {
			return "SAFE";
		}

		@Override
		public int workerThreads() {
			return 2;
		}

		@Override
		public int threadCount() {
			return 3;
		}

		@Override
		public TickMetrics.Snapshot metrics() {
			return new TickMetrics.Snapshot(12.5, 10.0, 20.0, 100, Map.of("multicore-worker-0", 3.25));
		}
	}

	@Test
	void statusContainsTheImportantFields() {
		String text = StatusFormatter.status(new FakeSource());

		assertTrue(text.contains("mode=SAFE"));
		assertTrue(text.contains("workers=2"));
		assertTrue(text.contains("threads=3"));
		assertTrue(text.contains("ticks=100"));
		assertTrue(text.contains("last=12.50"));
		assertTrue(text.contains("avg=10.00"));
		assertTrue(text.contains("max=20.00"));
		assertTrue(text.contains("multicore-worker-0: 3.25 ms"));
	}

	@Test
	void statusHandlesNoThreadWork() {
		StatusSource source = new FakeSource() {
			@Override
			public TickMetrics.Snapshot metrics() {
				return new TickMetrics.Snapshot(5.0, 5.0, 5.0, 1, Map.of());
			}
		};

		assertTrue(StatusFormatter.status(source).contains("(no work recorded yet)"));
	}

	@Test
	void compatAndRegionsExplainTheyAreNotImplementedYet() {
		String compat = StatusFormatter.compat("");
		String regions = StatusFormatter.regions(null);

		assertTrue(compat.contains("Phase 2"));
		assertTrue(regions.contains("Phase 3"));
	}

	@Test
	void compatUsesTheProvidedSummary() {
		String text = StatusFormatter.compat("legacy=4 compatible=2");

		assertTrue(text.contains("legacy=4 compatible=2"));
	}

	@Test
	void compatDumpReturnsTheDump() {
		String text = StatusFormatter.compatDump("mod-a: COMPATIBLE%nmod-b: LEGACY".replace("%n", "\n"));

		assertTrue(text.contains("mod-a: COMPATIBLE"));
		assertTrue(text.contains("mod-b: LEGACY"));
	}

	@Test
	void compatDumpExplainsWhenNotImplemented() {
		assertTrue(StatusFormatter.compatDump("").contains("Phase 2"));
		assertTrue(StatusFormatter.compatDump(null).contains("Phase 2"));
	}

	@Test
	void reloadWarningMentionsRestart() {
		assertTrue(StatusFormatter.reloadWarning().contains("restart"));
	}

	@Test
	void singleThreadedMessageMentionsVanilla() {
		String text = StatusFormatter.singleThreaded();

		assertTrue(text.contains("vanilla"));
		assertFalse(text.isBlank());
	}
}
