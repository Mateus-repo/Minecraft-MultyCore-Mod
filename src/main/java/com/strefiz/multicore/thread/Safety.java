package com.strefiz.multicore.thread;

/**
 * How much the world the mod is allowed to touch during an offloaded task.
 *
 * <p>The {@link OffloadRegistry} only runs offloads whose safety is at or below the maximum allowed
 * by the current {@link com.strefiz.multicore.config.ConfigMode}; this is what makes SAFE mode safe
 * by construction instead of by convention.
 */
public enum Safety {

	/** The offload only touches data owned by the mod. Always allowed. */
	PURE,

	/** The offload reads world state but never modifies it. Requires AGGRESSIVE mode. */
	READ_ONLY,

	/** The offload modifies world state. Phase 3 only; requires AGGRESSIVE mode. */
	WORLD_STATE
}
