package com.strefiz.multicore.thread;

/**
 * Identifies the vanilla server main (tick) thread.
 *
 * <p>The reference is captured once, at server start, from the thread that runs the
 * {@code SERVER_STARTING} lifecycle callback. Everything else that needs to ask "am I on the main
 * thread?" uses {@link #isCurrent()}; worker threads must never touch world state when it returns
 * {@code true}.
 *
 * <p>This is deliberately not based on the thread name: it must keep working if the server is
 * renamed, run in an integrated server, or launched from a wrapper.
 */
public final class MainThread {

	private static volatile Thread mainThread;

	private MainThread() {
	}

	/**
	 * Captures the calling thread as the server main thread.
	 *
	 * <p>Must be called from the main thread (typically inside {@code SERVER_STARTING}).
	 */
	public static void capture() {
		mainThread = Thread.currentThread();
	}

	/**
	 * Returns whether the calling thread is the captured server main thread.
	 *
	 * @return {@code true} when the current thread is the main thread, or when no main thread has
	 *     been captured yet (safe default: treat the caller as main)
	 */
	public static boolean isCurrent() {
		Thread current = mainThread;
		return current == null || current == Thread.currentThread();
	}

	/**
	 * Returns the captured main thread.
	 *
	 * @return the main thread, or {@code null} before {@link #capture()} is called
	 */
	public static Thread get() {
		return mainThread;
	}
}
