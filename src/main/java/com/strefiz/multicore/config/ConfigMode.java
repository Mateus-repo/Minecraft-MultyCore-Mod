package com.strefiz.multicore.config;

import java.util.Locale;

/**
 * Execution mode of the mod.
 *
 * <ul>
 *   <li>{@link #SAFE} (default): Fabric events are dispatched on the main thread and worker
 *       threads never touch world state directly. Legacy mods keep working.</li>
 *   <li>{@link #AGGRESSIVE}: region threads run world ticks; only mods that declared
 *       {@code threadSafe} may join. Legacy mods are expected to break.</li>
 * </ul>
 */
public enum ConfigMode {

	/** Conservative default; behaviour is equivalent to vanilla when {@code workerThreads = 0}. */
	SAFE,

	/** Opt-in parallel tick; requires a verified modpack. */
	AGGRESSIVE;

	/**
	 * Parses a mode from its serialized name, tolerating case and surrounding whitespace.
	 *
	 * @param value the raw value (may be {@code null})
	 * @return the parsed mode, or {@link #SAFE} when the value is unknown
	 */
	public static ConfigMode fromString(String value) {
		if (value == null) {
			return SAFE;
		}
		return switch (value.trim().toUpperCase(Locale.ROOT)) {
			case "AGGRESSIVE" -> AGGRESSIVE;
			case "SAFE" -> SAFE;
			default -> SAFE;
		};
	}
}
