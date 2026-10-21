package com.strefiz.multicore.thread;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runs CPU-heavy work on the {@link WorkerPool} and merges the results back on the main thread.
 *
 * <p>Two guarantees make this safe:
 *
 * <ol>
 *   <li><strong>Barrier.</strong> {@link #drainToMain()} waits for every offloaded task to finish
 *       before applying any merge, so the tick never ends with results still in flight.</li>
 *   <li><strong>Order.</strong> Merges are applied in <em>submission</em> order (not completion
 *       order), which keeps the tick deterministic and preserves RNG order.</li>
 * </ol>
 *
 * <p>When the pool is disabled ({@code workerThreads = 0}) the work runs inline and the merge is
 * applied immediately, so the behaviour is identical to vanilla.
 */
public final class TaskScheduler {

	private static final Logger LOGGER = LoggerFactory.getLogger("multicore/scheduler");

	/** How long {@link #drainToMain()} waits for all offloaded work before giving up. */
	public static final long DEFAULT_DRAIN_TIMEOUT_MS = 2_000L;

	private final WorkerPool pool;
	private final ConcurrentLinkedQueue<OffloadTask<?>> inFlight = new ConcurrentLinkedQueue<>();
	private final AtomicLong sequence = new AtomicLong();

	/**
	 * Creates a scheduler bound to a pool.
	 *
	 * @param pool the worker pool to run offloaded work on
	 */
	public TaskScheduler(WorkerPool pool) {
		this.pool = Objects.requireNonNull(pool, "pool");
	}

	/** @return whether there are tasks waiting for the main thread. */
	public boolean hasPending() {
		return !inFlight.isEmpty();
	}

	/** @return the number of tasks queued for the next drain. */
	public int pendingCount() {
		return inFlight.size();
	}

	/**
	 * Submits work that produces a value; the value is handed to {@code merge} on the main thread,
	 * in submission order.
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
		OffloadTask<T> task = new OffloadTask<>(sequence.incrementAndGet(), work, merge);
		inFlight.add(task);
		if (!pool.submit(task)) {
			inFlight.remove(task);
			runInline(work, merge);
		}
	}

	/**
	 * Submits fire-and-forget work. Exceptions are surfaced by the worker thread's
	 * uncaught-exception handler (this method uses {@code execute}, not {@code submit}).
	 *
	 * @param work the work to run on a worker thread
	 * @return whether the work was queued
	 */
	public boolean submit(Runnable work) {
		return pool.submit(Objects.requireNonNull(work, "work"));
	}

	/**
	 * Waits for all offloaded work and applies every merge, in submission order, on the calling
	 * thread.
	 *
	 * @return the number of merges applied
	 */
	public int drainToMain() {
		return drainToMain(DEFAULT_DRAIN_TIMEOUT_MS);
	}

	/**
	 * As {@link #drainToMain()}, with an explicit timeout.
	 *
	 * @param timeoutMillis how long to wait for the slowest task before skipping its merge
	 * @return the number of merges applied
	 */
	public int drainToMain(long timeoutMillis) {
		if (!MainThread.isCurrent()) {
			LOGGER.warn("drainToMain() was called off the main thread; merges may be unsafe.");
		}
		if (inFlight.isEmpty()) {
			return 0;
		}
		List<OffloadTask<?>> tasks = new ArrayList<>(inFlight);
		awaitAll(tasks, Math.max(1, timeoutMillis));
		int applied = 0;
		for (OffloadTask<?> task : tasks) {
			if (task.apply()) {
				applied++;
			}
		}
		inFlight.clear();
		return applied;
	}

	private void awaitAll(List<OffloadTask<?>> tasks, long timeoutMillis) {
		long perTask = Math.max(1, timeoutMillis / tasks.size());
		for (OffloadTask<?> task : tasks) {
			try {
				task.await(perTask);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				break;
			}
		}
	}

	private <T> void runInline(Callable<T> work, Consumer<T> merge) {
		try {
			merge.accept(work.call());
		} catch (Exception e) {
			LOGGER.error("Inline offload failed.", e);
		}
	}

	/** One offload: compute on a worker, merge on the main thread. */
	private final class OffloadTask<T> implements Runnable {

		private final long sequence;
		private final Callable<T> work;
		private final Consumer<T> merge;
		private final CountDownLatch done = new CountDownLatch(1);
		private volatile T result;
		private volatile Throwable error;

		private OffloadTask(long sequence, Callable<T> work, Consumer<T> merge) {
			this.sequence = sequence;
			this.work = work;
			this.merge = merge;
		}

		@Override
		public void run() {
			try {
				result = work.call();
			} catch (Throwable t) {
				error = t;
			} finally {
				done.countDown();
			}
		}

		private void await(long timeoutMillis) throws InterruptedException {
			done.await(timeoutMillis, TimeUnit.MILLISECONDS);
		}

		/**
		 * Applies the merge on the main thread.
		 *
		 * @return {@code true} when the merge was applied
		 */
		private boolean apply() {
			if (error != null) {
				LOGGER.error("Offloaded task #{} failed; its result will not be merged.", sequence, error);
				return false;
			}
			if (done.getCount() != 0) {
				LOGGER.warn("Offloaded task #{} did not finish in time; its merge was skipped.", sequence);
				return false;
			}
			try {
				merge.accept(result);
				return true;
			} catch (RuntimeException e) {
				LOGGER.error("Merge action #{} failed; continuing.", sequence, e);
				return false;
			}
		}
	}
}
