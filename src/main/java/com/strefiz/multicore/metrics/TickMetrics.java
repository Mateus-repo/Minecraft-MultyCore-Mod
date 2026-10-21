package com.strefiz.multicore.metrics;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.concurrent.atomic.LongAdder;

/**
 * Lock-free tick and per-thread timing metrics.
 *
 * <p>Everything here is called from the hot path, so no locks are taken: totals use
 * {@link LongAdder}/{@link LongAccumulator} and per-thread time uses a {@link ConcurrentHashMap}.
 * This is deliberately a measurement tool, not a profiler — it must cost almost nothing.
 */
public final class TickMetrics {

	private final LongAdder tickCount = new LongAdder();
	private final LongAdder totalNanos = new LongAdder();
	private final LongAccumulator maxNanos = new LongAccumulator(Math::max, 0L);

	private final ConcurrentHashMap<String, LongAdder> workNanos = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<String, LongAdder> workCount = new ConcurrentHashMap<>();
	private final ThreadLocal<Long> workStart = ThreadLocal.withInitial(() -> 0L);

	private volatile long tickStartNanos;
	private volatile long lastTickNanos;

	/** Marks the beginning of a server tick. */
	public void startTick() {
		tickStartNanos = System.nanoTime();
	}

	/** Marks the end of a server tick and records its duration. */
	public void endTick() {
		long elapsed = System.nanoTime() - tickStartNanos;
		lastTickNanos = elapsed;
		tickCount.increment();
		totalNanos.add(elapsed);
		maxNanos.accumulate(elapsed);
	}

	/**
	 * Marks the beginning of a labelled unit of work on the calling thread.
	 *
	 * @param label the work label (e.g. a thread name or a subsystem)
	 */
	public void beginWork(String label) {
		workStart.set(System.nanoTime());
	}

	/**
	 * Marks the end of a labelled unit of work and accumulates its duration.
	 *
	 * @param label the same label passed to {@link #beginWork(String)}
	 */
	public void endWork(String label) {
		long start = workStart.get();
		if (start == 0L) {
			return;
		}
		workStart.set(0L);
		long elapsed = System.nanoTime() - start;
		workNanos.computeIfAbsent(label, key -> new LongAdder()).add(elapsed);
		workCount.computeIfAbsent(label, key -> new LongAdder()).increment();
	}

	/** @return the duration of the last tick, in milliseconds. */
	public double mspt() {
		return lastTickNanos / 1_000_000.0;
	}

	/** @return the average tick duration since the last {@link #reset()}, in milliseconds. */
	public double averageMspt() {
		long ticks = tickCount.sum();
		return ticks == 0 ? 0.0 : totalNanos.sum() / (double) ticks / 1_000_000.0;
	}

	/** @return the worst tick duration since the last {@link #reset()}, in milliseconds. */
	public double maxMspt() {
		return maxNanos.get() / 1_000_000.0;
	}

	/** @return how many ticks have been recorded since the last {@link #reset()}. */
	public long tickCount() {
		return tickCount.sum();
	}

	/** @return a per-label average duration in milliseconds (empty when nothing was recorded). */
	public Map<String, Double> threadTimesMs() {
		Map<String, Double> result = new LinkedHashMap<>();
		for (Map.Entry<String, LongAdder> entry : workNanos.entrySet()) {
			LongAdder count = workCount.get(entry.getKey());
			long ticks = count == null ? 0 : count.sum();
			double average = ticks == 0 ? 0.0 : entry.getValue().sum() / (double) ticks / 1_000_000.0;
			result.put(entry.getKey(), average);
		}
		return result;
	}

	/** @return an immutable snapshot of the current metrics. */
	public Snapshot snapshot() {
		return new Snapshot(mspt(), averageMspt(), maxMspt(), tickCount(), Collections.unmodifiableMap(threadTimesMs()));
	}

	/** Clears every counter. */
	public void reset() {
		tickCount.reset();
		totalNanos.reset();
		maxNanos.reset();
		workNanos.clear();
		workCount.clear();
		lastTickNanos = 0L;
	}

	/** Immutable snapshot of the metrics, safe to hand to the command layer. */
	public record Snapshot(double lastMspt, double averageMspt, double maxMspt, long tickCount,
			Map<String, Double> threadTimesMs) {
	}
}
