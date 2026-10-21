package com.strefiz.multicore.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Unit tests for {@link ConfigStore} file handling: round-trip, defaults and malformed input.
 */
class ConfigStoreTest {

	@TempDir
	Path tempDir;

	@Test
	void missingFileCreatesDefaults() throws IOException {
		ConfigStore store = new ConfigStore(tempDir.resolve("multicore.json"));

		MultiCoreConfig config = store.load();

		assertEquals(1, config.workerThreads());
		assertEquals(ConfigMode.SAFE, config.mode());
		assertTrue(Files.exists(store.file()), "defaults should be persisted");
	}

	@Test
	void roundTripsValues() throws IOException {
		Path file = tempDir.resolve("multicore.json");
		ConfigStore store = new ConfigStore(file);
		MultiCoreConfig original = new MultiCoreConfig();
		original.workerThreads = 4;
		original.mode = ConfigMode.AGGRESSIVE.name();
		original.debug = true;
		original.modWhitelist = new java.util.ArrayList<>(java.util.List.of("a", "b"));
		original.modBlacklist = new java.util.ArrayList<>(java.util.List.of("c"));
		store.save(original);

		MultiCoreConfig loaded = new ConfigStore(file).load();

		assertEquals(4, loaded.workerThreads());
		assertEquals(ConfigMode.AGGRESSIVE, loaded.mode());
		assertTrue(loaded.debug());
		assertEquals(java.util.List.of("a", "b"), loaded.modWhitelist());
		assertEquals(java.util.List.of("c"), loaded.modBlacklist());
	}

	@Test
	void malformedJsonFallsBackToDefaults() throws IOException {
		Path file = tempDir.resolve("multicore.json");
		Files.writeString(file, "{ this is not json", StandardCharsets.UTF_8);

		MultiCoreConfig loaded = new ConfigStore(file).load();

		assertEquals(1, loaded.workerThreads());
		assertEquals(ConfigMode.SAFE, loaded.mode());
	}

	@Test
	void invalidWorkerCountIsClamped() throws IOException {
		Path file = tempDir.resolve("multicore.json");
		Files.writeString(file, "{\"workerThreads\": -3}", StandardCharsets.UTF_8);

		MultiCoreConfig loaded = new ConfigStore(file).load();

		assertEquals(MultiCoreConfig.MIN_WORKER_THREADS, loaded.workerThreads());
		assertFalse(loaded.multithreaded());
	}

	@Test
	void unknownModeFallsBackToSafe() throws IOException {
		Path file = tempDir.resolve("multicore.json");
		Files.writeString(file, "{\"mode\": \"yolo\"}", StandardCharsets.UTF_8);

		MultiCoreConfig loaded = new ConfigStore(file).load();

		assertEquals(ConfigMode.SAFE, loaded.mode());
	}
}
