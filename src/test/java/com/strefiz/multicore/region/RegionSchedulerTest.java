package com.strefiz.multicore.region;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.strefiz.multicore.thread.TaskScheduler;
import com.strefiz.multicore.thread.ThreadChecker;
import com.strefiz.multicore.thread.WorkerPool;

/**
 * Unit tests for {@link RegionScheduler}.
 */
class RegionSchedulerTest {

	@Test
	void awaitBarrierWithNoRegionsReturnsZero() {
		WorkerPool pool = new WorkerPool("test", 1);
		pool.start();
		TaskScheduler scheduler = new TaskScheduler(pool, ThreadChecker.debug(false));
		RegionScheduler regionScheduler = new RegionScheduler(pool, scheduler, ThreadChecker.debug(false));

		assertEquals(0, regionScheduler.awaitBarrier());
		pool.shutdown();
	}

	@Test
	void regionAlreadyTickingIsNotReticked() throws InterruptedException {
		WorkerPool pool = new WorkerPool("test", 1);
		pool.start();
		TaskScheduler scheduler = new TaskScheduler(pool, ThreadChecker.debug(false));
		RegionScheduler regionScheduler = new RegionScheduler(pool, scheduler, ThreadChecker.debug(false));

		Region alreadyTicking = new Region();
		alreadyTicking.tryMarkTicking();
		alreadyTicking.markTickingOwner(Thread.currentThread());
		Region other = new Region();

		regionScheduler.tickRegions(List.of(alreadyTicking, other));
		assertEquals(RegionState.TICKING, other.state());
		assertEquals(1, scheduler.drainToMain());
		other.markNotTicking();
		alreadyTicking.markNotTicking();
		pool.shutdown();
	}

	@Test
	void tickRegionsMarksReadyRegionsTicking() {
		RegionScheduler regionScheduler = new RegionScheduler(
				new WorkerPool("test", 1),
				new TaskScheduler(new WorkerPool("test", 1), ThreadChecker.debug(false)),
				ThreadChecker.debug(false));

		List<Region> regions = new ArrayList<>();
		for (int i = 0; i < 3; i++) {
			regions.add(new Region());
		}

		regionScheduler.tickRegions(regions);
		for (Region r : regions) {
			assertEquals(RegionState.TICKING, r.state());
		}

		// Manually return to ready (as awaitBarrier would do)
		for (Region r : regions) {
			r.markNotTicking();
			assertEquals(RegionState.READY, r.state());
			assertNull(r.ownerThread());
		}
	}
}
