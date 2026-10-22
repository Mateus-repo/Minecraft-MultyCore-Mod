package com.strefiz.multicore.region;

/**
 * The four states a region can be in, as guaranteed by Folia's regioniser invariants.
 *
 * <ul>
 *   <li>{@link #TRANSIENT}: a buffer region around a ticking region; cannot tick, merges back after.</li>
 *   <li>{@link #READY}: eligible to transition to {@link #TICKING}.</li>
 *   <li>{@link #TICKING}: currently being ticked by its owner thread; cannot grow while ticking.</li>
 *   <li>{@link #DEAD}: marked for removal; its sections are absorbed by another region.</li>
 * </ul>
 */
public enum RegionState {

	/** Buffer region around a ticking region; may not tick. */
	TRANSIENT,

	/** Eligible to start ticking. */
	READY,

	/** Currently ticking on its owner thread. May not acquire new sections while ticking. */
	TICKING,

	/** Dead; sections are being absorbed by another region. */
	DEAD;
}
