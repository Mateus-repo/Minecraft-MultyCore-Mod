package com.strefiz.multicore.metrics;

import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Pure aggregation of {@link TickMetrics}: the "hottest" per-thread work labels.
 *
 * <p>It only reads mod-owned data, so it is a safe Phase 1 offload (see {@code Safety.PURE}). The
 * result is merged on the main thread and shown by {@code /multicore status}.
 */
public final class MetricsRollup {

	private final TickMetrics metrics;

	/**
	 * Creates a rollup over the given metrics.
	 *
	 * @param metrics the source of the per-thread timings
	 */
	public MetricsRollup(TickMetrics metrics) {
		this.metrics = Objects.requireNonNull(metrics, "metrics");
	}

	/**
	 * Builds a comma-separated list of the labels with the highest average time, hottest first.
	 *
	 * @param limit the maximum number of labels to include
	 * @return the formatted list, or an empty string when nothing was recorded
	 */
	public String hottest(int limit) {
		return metrics.threadTimesMs().entrySet().stream()
				.filter(entry -> entry.getValue() > 0.0)
				.sorted(Map.Entry.<String, Double>comparingByValue(Comparator.reverseOrder()))
				.limit(Math.max(0, limit))
				.map(entry -> String.format(Locale.ROOT, "%s=%.3fms", entry.getKey(), entry.getValue()))
				.collect(Collectors.joining(", "));
	}
}
