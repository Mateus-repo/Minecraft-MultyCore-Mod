package com.strefiz.multicore.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Immutable-by-convention configuration of the mod, persisted as JSON.
 *
 * <p>The object is loaded once at server start and then treated as a read-only snapshot (see
 * D-008): changing threading settings cannot be applied to a running server. Use
 * {@link #normalized()} to obtain a defensive copy with validated values.
 *
 * <p>Instances are created by Gson (reflection) and by the tests; no Fabric/Minecraft class is
 * referenced so this class is unit-testable without the game.
 */
public class MultiCoreConfig {

	/** Default number of worker threads (1 main + 1 worker = 2 threads total). */
	public static final int DEFAULT_WORKER_THREADS = 1;

	/** Lower bound: {@code 0} workers means "behave exactly like vanilla". */
	public static final int MIN_WORKER_THREADS = 0;

	/** Upper bound, generous but safe. */
	public static final int MAX_WORKER_THREADS = 64;

	/** Number of worker threads (the main thread is added on top). */
	int workerThreads = DEFAULT_WORKER_THREADS;

	/** Serialized mode name; use {@link #mode()} to read it as an enum. */
	String mode = ConfigMode.SAFE.name();

	/** Enables thread-safety instrumentation and verbose logging. */
	boolean debug = false;

	/** Only mods in this list may use region threads (empty = all compatible mods). */
	List<String> modWhitelist = new ArrayList<>();

	/** Mods that must always be treated as legacy, even if they declare thread safety. */
	List<String> modBlacklist = new ArrayList<>();

	/** @return the configured worker-thread count (not clamped). */
	public int workerThreads() {
		return workerThreads;
	}

	/** @return the execution mode, never {@code null}. */
	public ConfigMode mode() {
		return ConfigMode.fromString(mode);
	}

	/** @return the raw serialized mode name (used by the codec). */
	public String modeName() {
		return mode;
	}

	/** @return whether thread-safety instrumentation is enabled. */
	public boolean debug() {
		return debug;
	}

	/** @return the mod whitelist (never {@code null}). */
	public List<String> modWhitelist() {
		return modWhitelist;
	}

	/** @return the mod blacklist (never {@code null}). */
	public List<String> modBlacklist() {
		return modBlacklist;
	}

	/** @return the total thread count, including the main thread. */
	public int threadCount() {
		return normalized().workerThreads() + 1;
	}

	/** @return whether the mod should run worker threads at all. */
	public boolean multithreaded() {
		return normalized().workerThreads() > 0;
	}

	/**
	 * Returns a copy with a validated worker-thread count.
	 *
	 * @param value the desired count (clamped to the allowed range)
	 * @return a new configuration
	 */
	public MultiCoreConfig withWorkerThreads(int value) {
		MultiCoreConfig copy = normalized();
		copy.workerThreads = clamp(value, MIN_WORKER_THREADS, MAX_WORKER_THREADS);
		return copy;
	}

	/**
	 * Returns a copy with the given execution mode.
	 *
	 * @param value the new mode (never {@code null})
	 * @return a new configuration
	 */
	public MultiCoreConfig withMode(ConfigMode value) {
		MultiCoreConfig copy = normalized();
		copy.mode = (value == null ? ConfigMode.SAFE : value).name();
		return copy;
	}

	/**
	 * Returns a copy with the given debug flag.
	 *
	 * @param value whether instrumentation is enabled
	 * @return a new configuration
	 */
	public MultiCoreConfig withDebug(boolean value) {
		MultiCoreConfig copy = normalized();
		copy.debug = value;
		return copy;
	}

	/**
	 * Returns a defensive copy with validated values: the worker count is clamped to
	 * {@value #MIN_WORKER_THREADS}..{@value #MAX_WORKER_THREADS}, the mode is never {@code null} and
	 * the lists are never {@code null}.
	 *
	 * @return a normalized copy of this configuration
	 */
	public MultiCoreConfig normalized() {
		MultiCoreConfig copy = new MultiCoreConfig();
		copy.workerThreads = clamp(workerThreads, MIN_WORKER_THREADS, MAX_WORKER_THREADS);
		copy.mode = mode == null ? ConfigMode.SAFE.name() : mode;
		copy.debug = debug;
		copy.modWhitelist = modWhitelist == null ? new ArrayList<>() : new ArrayList<>(modWhitelist);
		copy.modBlacklist = modBlacklist == null ? new ArrayList<>() : new ArrayList<>(modBlacklist);
		return copy;
	}

	private static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}
}
