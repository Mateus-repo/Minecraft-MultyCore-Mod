package com.strefiz.multicore.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Debug-only thread-safety checker.
 *
 * <p>Call sites pass a short description of what they are about to touch; if the current thread is
 * not allowed to touch it, a warning is logged (so server admins and mod authors can spot unsafe
 * access in debug mode). This is intentionally lightweight: no stack-walking, no exceptions, just
 * a log line.
 *
 * <p>In production (debug = off) the checks are no-ops.
 */
public final class ThreadChecker {

	private static final Logger LOGGER = LoggerFactory.getLogger("multicore/thread-check");

	private final boolean enabled;

	private ThreadChecker(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Creates a checker that is active when {@code debug} is true.
	 *
	 * @param debug whether thread-safety instrumentation is enabled
	 */
	public static ThreadChecker debug(boolean debug) {
		return new ThreadChecker(debug);
	}

	/**
	 * Asserts that the calling thread is the server main thread.
	 *
	 * @param what a short description of what is being accessed (for the log message)
	 */
	public void assertMainThread(String what) {
		if (!enabled) {
			return;
		}
		if (!MainThread.isCurrent()) {
			LOGGER.warn("Thread-safety: '{}' accessed off the main thread (on {}).",
					what, Thread.currentThread().getName());
		}
	}

	/**
	 * Asserts that the calling thread is the owner thread of a region.
	 *
	 * @param regionId a short region identifier (for the log message)
	 * @param ownerThread the thread that owns the region
	 */
	public void assertRegionOwner(String regionId, Thread ownerThread) {
		if (!enabled) {
			return;
		}
		Thread current = Thread.currentThread();
		if (current != ownerThread) {
			LOGGER.warn("Thread-safety: region '{}' accessed off its owner thread (on {}).",
					regionId, current.getName());
		}
	}

	/** @return whether checks are currently enabled. */
	public boolean enabled() {
		return enabled;
	}
}
