package com.strefiz.multicore.thread;

/**
 * What the scheduler does when an offloaded task exceeds the tick deadline.
 *
 * <p>The choice matters: dropping a metrics result is harmless, but dropping work that would have
 * modified the world is corruption. The policy is therefore derived from the offload's
 * {@link Safety} level, not left to the caller's memory.
 */
public enum DeadlinePolicy {

	/** Drop the merge and log a warning. For pure/metrics work only. */
	SKIP,

	/** Never drop: block until the task finishes (capped by a hard limit). For world work. */
	WAIT;

	/**
	 * Returns the policy implied by a safety level.
	 *
	 * @param safety the offload safety level
	 * @return {@link #SKIP} for {@link Safety#PURE}, {@link #WAIT} otherwise
	 */
	public static DeadlinePolicy forSafety(Safety safety) {
		return safety == Safety.PURE ? SKIP : WAIT;
	}
}
