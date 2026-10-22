package com.strefiz.multicore.region;

import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A region: a set of owned region sections plus a per-region data object, ticked by exactly one
 * thread (the owner thread).
 *
 * <p>A region maintains the invariant that, while it is {@link RegionState#TICKING}, only its owner
 * thread may write to the data it owns. Other threads must communicate through messages.
 *
 * <p>This class is deliberately free of Minecraft classes so it is unit-testable without the game.
 * The mixin layer hooks region ticks into {@code ServerLevel.tick}.
 */
public final class Region {

	private static final AtomicLong ID_COUNTER = new AtomicLong();

	private final long id;
	private final Set<RegionSection> sections = new HashSet<>();
	private final Map<Object, Object> regionLocalData = new IdentityHashMap<>();
	private final RegionMessageQueue messageQueue = RegionMessageQueue.create();
	private volatile boolean problematic = false;

	private volatile RegionState state = RegionState.READY;
	private volatile Thread ownerThread;

	/**
	 * Creates a new region in the {@link RegionState#READY} state.
	 */
	public Region() {
		this.id = ID_COUNTER.incrementAndGet();
	}

	/** @return the region's stable id. */
	public long id() {
		return id;
	}

	/** @return the region's current state. */
	public RegionState state() {
		return state;
	}

	/** @return the owner thread, or {@code null} when not ticking. */
	public Thread ownerThread() {
		return ownerThread;
	}

	/** @return an unmodifiable copy of the owned sections. */
	public Set<RegionSection> sections() {
		return Collections.unmodifiableSet(new HashSet<>(sections));
	}

	/** @return the number of owned sections. */
	public int sectionCount() {
		return sections.size();
	}

	/**
	 * Adds a section to this region. Only allowed when the region is not ticking.
	 *
	 * @param section the section to add
	 * @throws IllegalStateException if the region is ticking
	 */
	public void addSection(RegionSection section) {
		if (state == RegionState.TICKING) {
			throw new IllegalStateException("Region " + id + " is ticking; cannot add section " + section);
		}
		sections.add(section);
	}

	/**
	 * Removes a section from this region. Only allowed when the region is not ticking.
	 *
	 * @param section the section to remove
	 * @throws IllegalStateException if the region is ticking
	 */
	public void removeSection(RegionSection section) {
		if (state == RegionState.TICKING) {
			throw new IllegalStateException("Region " + id + " is ticking; cannot remove section " + section);
		}
		sections.remove(section);
	}

	/**
	 * Attempts to transition this region from {@link RegionState#READY} to
	 * {@link RegionState#TICKING}. The owner thread is set when the tick actually begins (see
	 * {@link #markTickingOwner(Thread)}).
	 *
	 * @return {@code true} if the transition succeeded, {@code false} if the region was not READY
	 */
	public boolean tryMarkTicking() {
		if (state != RegionState.READY) {
			return false;
		}
		state = RegionState.TICKING;
		return true;
	}

	/**
	 * Called by the worker thread that begins ticking this region, to record the owner thread.
	 *
	 * @param owner the thread that owns this region while ticking
	 */
	public void markTickingOwner(Thread owner) {
		if (state != RegionState.TICKING) {
			throw new IllegalStateException("Region " + id + " is not ticking");
		}
		this.ownerThread = owner;
	}

	/**
	 * Transitions this region from {@link RegionState#TICKING} back to {@link RegionState#READY},
	 * clearing the owner thread. Must be called by the owner thread.
	 *
	 * @return {@code true} if the transition succeeded
	 */
	public boolean markNotTicking() {
		if (state != RegionState.TICKING || ownerThread != Thread.currentThread()) {
			return false;
		}
		state = RegionState.READY;
		ownerThread = null;
		return true;
	}

	/**
	 * Marks this region as dead. Only allowed from READY or TRANSIENT.
	 */
	public void markDead() {
		if (state == RegionState.TICKING) {
			throw new IllegalStateException("Cannot mark a ticking region dead");
		}
		state = RegionState.DEAD;
	}

	/**
	 * Marks this region as transient (a buffer around a ticking region).
	 */
	public void markTransient() {
		if (state == RegionState.TICKING) {
			throw new IllegalStateException("Cannot mark a ticking region transient");
		}
		state = RegionState.TRANSIENT;
	}

	/**
	 * Stores region-local data keyed by an arbitrary object. Region-local data is only ever accessed
	 * by the owner thread while the region is ticking, so no synchronization is needed.
	 *
	 * @param key the key
	 * @param value the value
	 */
	public <T> void putRegionLocal(Object key, T value) {
		regionLocalData.put(key, value);
	}

	/**
	 * Retrieves region-local data.
	 *
	 * @param key the key
	 * @return the value, or {@code null} if absent
	 */
	@SuppressWarnings("unchecked")
	public <T> T getRegionLocal(Object key) {
		return (T) regionLocalData.get(key);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Region that)) {
			return false;
		}
		return id == that.id;
	}

	@Override
	public int hashCode() {
		return Long.hashCode(id);
	}

	@Override
	public String toString() {
		return "Region(id=" + id + ", state=" + state + ", sections=" + sections.size() + ", owner=" +
				(ownerThread == null ? "none" : ownerThread.getName()) + ")";
	}

	/** @return this region's cross-region message queue. */
	public RegionMessageQueue messageQueue() {
		return messageQueue;
	}

	/** @return whether this region is flagged as problematic and should fall back to the main thread. */
	public boolean isProblematic() {
		return problematic;
	}

	/**
	 * Marks this region as problematic, meaning it had cross-region interactions that could not be
	 * resolved safely and should be ticked on the main thread from now on.
	 */
	public void markProblematic() {
		this.problematic = true;
	}

	/**
	 * Clears the problematic flag, allowing the region to return to worker-thread ticking.
	 */
	public void clearProblematic() {
		this.problematic = false;
	}
}
