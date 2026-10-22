package com.strefiz.multicore.compat;

/**
 * Public API for mod authors who want their mods to work with MultiCore.
 *
 * <p>Use this to declare thread-safety intent and to run code on the correct thread. This class is
 * intentionally small and has no Minecraft dependencies so it can be referenced by other mods
 * without pulling in the whole codebase.
 *
 * <h2>Declaring thread safety</h2>
 *
 * <p>Add this to your {@code fabric.mod.json}:
 *
 * <pre>
 *   "custom": {
 *     "multicore": { "threadSafe": true }
 *   }
 * </pre>
 *
 * <p>Mods that do not declare this are treated as legacy and keep running on the main thread.
 *
 * <h2>Running on the main thread</h2>
 *
 * <p>If your mod needs to touch Minecraft world state or other mods' state from an unknown thread,
 * use {@link #runOnMain(Runnable)} to enqueue the work for the main thread.
 */
public final class MultiCoreCompat {

	private MultiCoreCompat() {
	}

	/**
	 * Runs the given callback on the server main thread.
	 *
	 * <p>Safe to call from any thread. In SAFE mode this is the only way for off-thread code to
	 * safely touch world state; in AGGRESSIVE mode it is still the safe way to touch legacy-mod
	 * state.
	 *
	 * @param callback the work to run on the main thread, never {@code null}
	 */
	public static void runOnMain(Runnable callback) {
		Handler handler = HandlerHolder.HANDLER;
		if (handler != null) {
			handler.runOnMain(callback);
		}
	}

	/**
	 * Returns whether the current mod context is considered thread-safe.
	 *
	 * @return {@code true} when the mod is running in AGGRESSIVE mode and declared thread-safe
	 */
	public static boolean isThreadSafe() {
		Handler handler = HandlerHolder.HANDLER;
		return handler != null && handler.isThreadSafe();
	}

	/**
	 * Sets the handler that backs this public API. Called once by {@link MultiCoreMod} at server
	 * start. Not part of the public contract for mod authors.
	 */
	public static void setHandler(Handler handler) {
		HandlerHolder.HANDLER = handler;
	}

	/** Callback contract implemented by the mod's entrypoint. */
	public interface Handler {

		void runOnMain(Runnable callback);

		boolean isThreadSafe();
	}

	private static final class HandlerHolder {
		static Handler HANDLER;
	}
}
