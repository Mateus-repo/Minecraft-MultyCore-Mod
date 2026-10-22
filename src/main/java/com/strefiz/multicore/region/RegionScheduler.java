package com.strefiz.multicore.region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import com.strefiz.multicore.thread.TaskScheduler;
import com.strefiz.multicore.thread.ThreadChecker;
import com.strefiz.multicore.thread.WorkerPool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Schedules regions to tick in parallel on the worker pool, with a barrier at the end of the tick.
 *
 * <p>Each region that is {@link RegionState#READY} attempts {@link Region#tryMarkTicking()}; exactly
 * one thread succeeds per region and becomes its owner. Regions whose neighbor is ticking stay
 * READY (Folia invariant #4: no adjacent ticking regions) and are retried next tick.
 *
 * <p>After all regions have been submitted, {@link #awaitBarrier()} waits for every ticking region
 * to finish on the main thread, then calls {@link Region#markNotTicking()} on each.
 *
 * <p>This class is Minecraft-free: it owns no {@code ServerLevel}. The mixin layer calls
 * {@link #tickRegions(Collection)} and {@link #awaitBarrier()} at the appropriate points in the
 * server tick.
 */
public final class RegionScheduler {

	private static final Logger LOGGER = LoggerFactory.getLogger("multicore/region");

	private final TaskScheduler scheduler;
	private final ThreadChecker threadChecker;
	private final WorkerPool pool;

	private final Map<Long, Region> tickingRegions = new ConcurrentHashMap<>();

	/**
	 * Creates a region scheduler bound to a pool.
	 *
	 * @param pool the worker pool to run region ticks on
	 * @param scheduler the task scheduler (for the barrier)
	 * @param threadChecker the debug thread-checker
	 */
	public RegionScheduler(WorkerPool pool, TaskScheduler scheduler, ThreadChecker threadChecker) {
		this.pool = Objects.requireNonNull(pool, "pool");
		this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
		this.threadChecker = Objects.requireNonNull(threadChecker, "threadChecker");
	}

	/** @return whether there are regions currently ticking. */
	public boolean hasTickingRegions() {
		return !tickingRegions.isEmpty();
	}

	/**
	 * Attempts to start ticking every READY region, submitting each to the worker pool.
	 *
	 * <p>Regions whose neighbor is already ticking stay READY and are retried next tick.
	 *
	 * @param regions the regions to consider for ticking this tick
	 */
	public void tickRegions(Collection<Region> regions) {
		Objects.requireNonNull(regions, "regions");
		List<Region> toTick = new ArrayList<>();
		for (Region region : regions) {
			if (region.tryMarkTicking()) {
				toTick.add(region);
			}
		}
		for (Region region : toTick) {
			tickingRegions.put(region.id(), region);
			scheduler.submitOffload(() -> {
				tickRegion(region);
				return null;
			}, ignored -> { });
		}
		if (toTick.isEmpty()) {
			LOGGER.debug("No regions ready to tick this tick.");
		}
	}

	private void tickRegion(Region region) {
		region.markTickingOwner(Thread.currentThread());
		threadChecker.assertRegionOwner("region " + region.id(), Thread.currentThread());
	}

	/**
	 * Waits for every submitted region to finish, then marks them not-ticking on the main thread.
	 *
	 * <p>Must be called on the main thread (typically at {@code END_SERVER_TICK}).
	 *
	 * @return the number of regions that finished ticking
	 */
	public int awaitBarrier() {
		if (threadChecker.enabled()) {
			threadChecker.assertMainThread("awaitBarrier");
		}
		if (tickingRegions.isEmpty()) {
			return 0;
		}
		int finished = scheduler.drainToMain();
		for (Region region : tickingRegions.values()) {
			region.markNotTicking();
		}
		tickingRegions.clear();
		return finished;
	}
}
