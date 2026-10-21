package com.strefiz.multicore.command;

import java.util.Map;

import com.strefiz.multicore.metrics.TickMetrics;

/**
 * Read-only view of the runtime state used by the {@code /multicore} command.
 *
 * <p>The command depends on this interface instead of concrete modules so it can be formatted and
 * tested without a running server.
 */
public interface StatusSource {

	/** @return the execution mode name ({@code SAFE} or {@code AGGRESSIVE}). */
	String mode();

	/** @return the configured number of worker threads. */
	int workerThreads();

	/** @return the total thread count, including the main thread. */
	int threadCount();

	/** @return a snapshot of the tick metrics. */
	TickMetrics.Snapshot metrics();

	/** @return average per-thread work times in milliseconds (may be empty). */
	default Map<String, Double> threadTimesMs() {
		return metrics().threadTimesMs();
	}

	/** @return a summary of the compatibility layer, or an empty string when not implemented. */
	default String compatSummary() {
		return "";
	}

	/** @return a summary of the region scheduler, or an empty string when not implemented. */
	default String regionSummary() {
		return "";
	}
}
