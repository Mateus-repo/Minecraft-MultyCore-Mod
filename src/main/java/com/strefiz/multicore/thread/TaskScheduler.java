package com.strefiz.multicore.thread;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runs CPU-heavy work on the {@link WorkerPool} and merges the results back on the main thread.
 *
 * <p>This is the contract that keeps the mod safe: work is computed off-thread, but anything that
 * could touch shared state is only applied by {@link #drainToMain()} on the main thread, before the
 * tick ends. Merges are applied in submission order.
 *
 * <p>When the pool is disabled ({@code workerThreads = 0}) the work runs inline, so the behaviour
 * is identical to vanilla.
 */
public final class TaskScheduler {

	private static final Logger LOGGER = LoggerFactory.getLogger("multicore/scheduler");

	private final WorkerPool pool;
	private final ConcurrentLinkedQueue<Runnable> pendingMerges = new ConcurrentLinkedQueue<>();

	/**
	 * Creates a scheduler bound to a pool.
	 *
	 * @param pool the worker pool to run offloaded work on
	 */
	public TaskScheduler(WorkerPool pool) {
		this.pool = Objects.requireNonNull(pool, "pool");
	}

	/** @return whether there are merges waiting for the main thread. */
	public boolean hasPending() {
		return !pendingMerges.isEmpty();
	}

	/** @return the number of merges currently queued. */
	public int pendingCount() {
		return pendingMerges.size();
	}

	/**
	 * Submits work that produces a value; the value is handed to {@code merge} on the main thread.
	 *
	 * @param work the CPU work, run on a worker thread (or inline when the pool is disabled)
	 * @param merge the merge action, run on the main thread by {@link #drainToMain()}
	 * @param <T> the result type
	 */
	public <T> void submitOffload(Callable<T> work, Consumer<T> merge) {
		Objects.requireNonNull(work, "work");
		Objects.requireNonNull(merge, "merge");
		if (!pool.isRunning()) {
			runInline(work, merge);
			return;
		}
		pool.submit(() -> {
			try {
				T result = work.call();
				pendingMerges.add(() -> merge.accept(result));
			} catch (Exception e) {
				LOGGER.error("Offloaded task failed; its result will not be merged.", e);
			}
		});
	}

	/**
	 * Submits work with no result to merge.
	 *
	 * @param work the work to run on a worker thread
	 * @return whether the work was queued
	 */
	public boolean submit(Runnable work) {
		return pool.submit(Objects.requireNonNull(work, "work"));
	}

	/**
	 * Applies every queued merge on the calling thread, in order.
	 *
	 * <p>Must be called on the main thread (typically at {@code END_SERVER_TICK}); a warning is logged
	 * otherwise.
	 *
	 * @return the number of merges applied
	 */
	public int drainToMain() {
		if (!MainThread.isCurrent()) {
			LOGGER.warn("drainToMain() was called off the main thread; merges may be unsafe.");
		}
		int applied = 0;
		Runnable action;
		while ((action = pendingMerges.poll()) != null) {
			try {
				action.run();
				applied++;
			} catch (RuntimeException e) {
				LOGGER.error("A merge action failed; continuing with the next one.", e);
			}
		}
		return applied;
	}

	private <T> void runInline(Callable<T> work, Consumer<T> merge) {
		try {
			merge.accept(work.call());
		} catch (Exception e) {
			LOGGER.error("Inline offload failed.", e);
		}
	}
}
