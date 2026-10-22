package com.strefiz.multicore.compat;

/**
 * Result of classifying a loaded mod for multithreading safety.
 *
 * <ul>
 *   <li>{@link #COMPATIBLE}: the mod declared {@code threadSafe: true} in its {@code fabric.mod.json}
 *       and may run on region threads (AGGRESSIVE mode only).</li>
 *   <li>{@link #LEGACY}: the mod did not declare thread safety, or declared {@code false}. It keeps
 *       running on the main thread, exactly as today.</li>
 * </ul>
 *
 * <p>The default is {@link #LEGACY}: a mod is thread-safe only if it says so. This mirrors Folia's
 * opt-in model and keeps unmodified mods working.
 */
public enum ModClassification {

	/** Not declared thread-safe; runs on the main thread. */
	LEGACY,

	/** Declared {@code custom.multicore.threadSafe: true}; may use region threads. */
	COMPATIBLE;
}
