package com.strefiz.multicore.thread;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.strefiz.multicore.thread.ThreadChecker;

/**
 * Unit tests for {@link OffloadRegistry}: safety filtering and submission.
 */
class OffloadRegistryTest {

	@Test
	void submitAllOnlyRunsOffloadsAtOrBelowTheAllowedSafety() {
		MainThread.capture();
		WorkerPool pool = new WorkerPool("test", 1);
		pool.start();
		TaskScheduler scheduler = new TaskScheduler(pool, ThreadChecker.debug(false));
		OffloadRegistry registry = new OffloadRegistry(scheduler);
		AtomicInteger pure = new AtomicInteger();
		AtomicInteger readOnly = new AtomicInteger();
		AtomicInteger world = new AtomicInteger();

		registry.register("pure", Safety.PURE, () -> 1, v -> pure.incrementAndGet());
		registry.register("read-only", Safety.READ_ONLY, () -> 1, v -> readOnly.incrementAndGet());
		registry.register("world", Safety.WORLD_STATE, () -> 1, v -> world.incrementAndGet());

		registry.submitAll(Safety.PURE); // SAFE mode
		assertEquals(1, scheduler.drainToMain());

		assertEquals(1, pure.get(), "PURE offload must run in SAFE mode");
		assertEquals(0, readOnly.get(), "READ_ONLY offload must not run in SAFE mode");
		assertEquals(0, world.get(), "WORLD_STATE offload must not run in SAFE mode");
		pool.shutdown();
	}

	@Test
	void submitAllRunsEverythingInAggressiveMode() {
		MainThread.capture();
		WorkerPool pool = new WorkerPool("test", 3);
		pool.start();
		TaskScheduler scheduler = new TaskScheduler(pool, ThreadChecker.debug(false));
		OffloadRegistry registry = new OffloadRegistry(scheduler);
		AtomicInteger ran = new AtomicInteger();

		registry.register("a", Safety.PURE, () -> 1, v -> ran.incrementAndGet());
		registry.register("b", Safety.READ_ONLY, () -> 1, v -> ran.incrementAndGet());
		registry.register("c", Safety.WORLD_STATE, () -> 1, v -> ran.incrementAndGet());

		registry.submitAll(Safety.WORLD_STATE); // AGGRESSIVE mode
		assertEquals(3, scheduler.drainToMain());
		assertEquals(3, ran.get());
		pool.shutdown();
	}

	@Test
	void exposesRegisteredNames() {
		OffloadRegistry registry = new OffloadRegistry(
				new TaskScheduler(new WorkerPool("test", 0), ThreadChecker.debug(false)));
		registry.register("a", Safety.PURE, () -> 1, v -> { });
		registry.register("b", Safety.PURE, () -> 2, v -> { });

		assertEquals(List.of("a", "b"), registry.names());
		assertEquals(2, registry.size());
		assertTrue(registry.names().indexOf("a") < registry.names().indexOf("b"));
	}
}
