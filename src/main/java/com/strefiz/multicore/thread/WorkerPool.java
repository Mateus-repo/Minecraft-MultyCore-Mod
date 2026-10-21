package com.strefiz.multicore.thread;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The mod's worker thread pool.
 *
 * <p>Threads are created here and nowhere else, always named {@code <poolName>-N} so they are
 * obvious in crash reports, thread dumps and {@code /multicore status}. The common pool is never
 * used: our work is CPU-bound and must not starve the rest of the JVM.
 *
 * <p>Lifecycle: {@link #start()} on {@code SERVER_STARTING}, {@link #shutdown()} on
 * {@code SERVER_STOPPED}. A pool of size {@code 0} never starts, which is how the mod becomes
 * behaviourally equivalent to vanilla.
 */
public final class WorkerPool {

	private static final Logger LOGGER = LoggerFactory.getLogger("multicore/thread");

	private final String poolName;
	private final int size;
	private final AtomicInteger counter = new AtomicInteger();

	private volatile ExecutorService executor;
	private volatile boolean running;

	/**
	 * Creates a pool description (it does not start any thread yet).
	 *
	 * @param poolName prefix used for the thread names
	 * @param size number of worker threads; {@code <= 0} means "disabled"
	 */
	public WorkerPool(String poolName, int size) {
		this.poolName = poolName;
		this.size = Math.max(0, size);
	}

	/** @return the configured number of worker threads. */
	public int size() {
		return size;
	}

	/** @return whether the pool is currently running. */
	public boolean isRunning() {
		return running;
	}

	/**
	 * Starts the pool. Does nothing when the size is {@code 0} or the pool is already running.
	 */
	public void start() {
		if (size == 0) {
			LOGGER.info("Worker pool '{}' disabled (0 workers) — running single-threaded like vanilla.", poolName);
			return;
		}
		if (running) {
			return;
		}
		executor = Executors.newFixedThreadPool(size, this::newThread);
		running = true;
		LOGGER.info("Worker pool '{}' started with {} thread(s).", poolName, size);
	}

	/**
	 * Submits a task, returning whether it was accepted.
	 *
	 * <p>Uses {@code execute} (not {@code submit}) so that an exception thrown by the task reaches
	 * the thread's uncaught-exception handler instead of being silently stored in an unread
	 * {@code Future}.
	 *
	 * @param task the work to run on a worker thread
	 * @return {@code true} if the task was queued, {@code false} if the pool is stopped
	 */
	public boolean submit(Runnable task) {
		ExecutorService current = executor;
		if (current == null || !running) {
			return false;
		}
		try {
			current.execute(task);
			return true;
		} catch (RejectedExecutionException e) {
			return false;
		}
	}

	/**
	 * Submits a batch of tasks and returns their futures, in the same order as the input.
	 *
	 * @param tasks the tasks to run
	 * @param <T> the result type
	 * @return the futures, or an empty list when the pool is stopped
	 */
	public <T> List<Future<T>> submitAll(Collection<? extends Callable<T>> tasks) {
		ExecutorService current = executor;
		if (current == null || !running) {
			return List.of();
		}
		return tasks.stream().map(task -> {
			try {
				return current.submit(task);
			} catch (RejectedExecutionException e) {
				return null;
			}
		}).filter(java.util.Objects::nonNull).toList();
	}

	/**
	 * Stops accepting new tasks and lets the queued work finish (a graceful drain), then waits up to
	 * {@code timeoutSeconds} before forcing the remaining threads to stop.
	 */
	public void shutdown() {
		ExecutorService current = executor;
		if (current == null) {
			running = false;
			return;
		}
		current.shutdown();
		try {
			if (!current.awaitTermination(5, TimeUnit.SECONDS)) {
				current.shutdownNow();
			}
		} catch (InterruptedException e) {
			current.shutdownNow();
			Thread.currentThread().interrupt();
		} finally {
			running = false;
		}
	}

	private Thread newThread(Runnable runnable) {
		Thread thread = new Thread(runnable, poolName + "-" + counter.getAndIncrement());
		thread.setDaemon(true);
		thread.setUncaughtExceptionHandler((t, error) ->
				LOGGER.error("Uncaught error on worker thread {}", t.getName(), error));
		return thread;
	}
}
