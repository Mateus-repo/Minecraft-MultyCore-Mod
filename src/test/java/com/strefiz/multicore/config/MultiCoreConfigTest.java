package com.strefiz.multicore.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link MultiCoreConfig} defaults, parsing and validation.
 */
class MultiCoreConfigTest {

	@Test
	void defaultsAreConservative() {
		MultiCoreConfig config = new MultiCoreConfig();

		assertEquals(MultiCoreConfig.DEFAULT_WORKER_THREADS, config.workerThreads());
		assertEquals(1, config.workerThreads());
		assertEquals(ConfigMode.SAFE, config.mode());
		assertFalse(config.debug());
		assertTrue(config.modWhitelist().isEmpty());
		assertTrue(config.modBlacklist().isEmpty());
		assertEquals(2, config.threadCount());
		assertTrue(config.multithreaded());
	}

	@Test
	void normalizedClampsWorkerThreads() {
		MultiCoreConfig low = configWithWorkers(-5).normalized();
		MultiCoreConfig high = configWithWorkers(10_000).normalized();
		MultiCoreConfig zero = configWithWorkers(0).normalized();

		assertEquals(MultiCoreConfig.MIN_WORKER_THREADS, low.workerThreads());
		assertEquals(MultiCoreConfig.MAX_WORKER_THREADS, high.workerThreads());
		assertEquals(0, zero.workerThreads());
		assertFalse(zero.multithreaded());
		assertEquals(1, zero.threadCount());
	}

	@Test
	void normalizedHandlesNulls() {
		MultiCoreConfig config = new MultiCoreConfig();
		config.modWhitelist = null;
		config.modBlacklist = null;
		config.mode = null;

		MultiCoreConfig normalized = config.normalized();

		assertEquals(ConfigMode.SAFE, normalized.mode());
		assertTrue(normalized.modWhitelist().isEmpty());
		assertTrue(normalized.modBlacklist().isEmpty());
	}

	@Test
	void modeParsingIsLenient() {
		assertEquals(ConfigMode.SAFE, ConfigMode.fromString(null));
		assertEquals(ConfigMode.SAFE, ConfigMode.fromString(""));
		assertEquals(ConfigMode.SAFE, ConfigMode.fromString("unknown"));
		assertEquals(ConfigMode.SAFE, ConfigMode.fromString("safe"));
		assertEquals(ConfigMode.AGGRESSIVE, ConfigMode.fromString("AGGRESSIVE"));
		assertEquals(ConfigMode.AGGRESSIVE, ConfigMode.fromString("  aggressive  "));
	}

	@Test
	void modeRoundTripsThroughItsName() {
		MultiCoreConfig config = new MultiCoreConfig();
		config.mode = ConfigMode.AGGRESSIVE.name();

		assertEquals(ConfigMode.AGGRESSIVE, config.mode());
		assertEquals(ConfigMode.AGGRESSIVE, config.normalized().mode());
	}

	private static MultiCoreConfig configWithWorkers(int workers) {
		MultiCoreConfig config = new MultiCoreConfig();
		config.workerThreads = workers;
		return config;
	}

	@Test
	void listsAreDefensiveCopies() {
		MultiCoreConfig config = new MultiCoreConfig();
		config.modWhitelist = new java.util.ArrayList<>(List.of("a"));
		config.modBlacklist = new java.util.ArrayList<>(List.of("b"));

		MultiCoreConfig normalized = config.normalized();
		normalized.modWhitelist().add("mutated");

		assertFalse(config.modWhitelist().contains("mutated"));
	}

	@Test
	void withWorkerThreadsClampsAndReturnsACopy() {
		MultiCoreConfig config = new MultiCoreConfig();

		MultiCoreConfig raised = config.withWorkerThreads(config.workerThreads() + 1);
		MultiCoreConfig tooMany = config.withWorkerThreads(9999);

		assertEquals(2, raised.workerThreads());
		assertEquals(MultiCoreConfig.MAX_WORKER_THREADS, tooMany.workerThreads());
		assertEquals(1, config.workerThreads(), "the original must not change");
	}

	@Test
	void withModeAndDebugReturnCopies() {
		MultiCoreConfig config = new MultiCoreConfig();

		MultiCoreConfig aggressive = config.withMode(ConfigMode.AGGRESSIVE);
		MultiCoreConfig debugging = config.withDebug(true);

		assertEquals(ConfigMode.AGGRESSIVE, aggressive.mode());
		assertTrue(debugging.debug());
		assertEquals(ConfigMode.SAFE, config.mode());
		assertFalse(config.debug());
	}

	@Test
	void withModeToleratesNull() {
		assertEquals(ConfigMode.SAFE, new MultiCoreConfig().withMode(null).mode());
	}
}
