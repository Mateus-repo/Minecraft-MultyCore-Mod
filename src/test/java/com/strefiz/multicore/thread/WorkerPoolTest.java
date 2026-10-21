package com.strefiz.multicore.thread;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link WorkerPool} lifecycle and threading guarantees.
 */
class WorkerPoolTest {

	@Test
	void zeroSizedPoolNeverStarts() {
		WorkerPool pool = new WorkerPool("test", 0);

		pool.start();

		assertFalse(pool.isRunning());
		assertFalse(pool.submit(() -> { }));
	}

	@Test
	void tasksRunOffTheMainThread() throws InterruptedException {
		MainThread.capture(); // the test thread pretends to be the server main thread
		WorkerPool pool = new WorkerPool("test", 2);
		pool.start();
		AtomicReference<Thread> observed = new AtomicReference<>();
		CountDownLatch done = new CountDownLatch(1);

		assertTrue(pool.submit(() -> {
			observed.set(Thread.currentThread());
			done.countDown();
		}));
		assertTrue(done.await(5, TimeUnit.SECONDS));

		assertNotEquals(MainThread.get(), observed.get(), "work must not run on the main thread");
		assertTrue(observed.get().getName().startsWith("test-"), "worker threads must be named");
		pool.shutdown();
	}

	@Test
	void submitReturnsFalseAfterShutdown() {
		WorkerPool pool = new WorkerPool("test", 1);
		pool.start();
		pool.shutdown();

		assertFalse(pool.isRunning());
		assertFalse(pool.submit(() -> { }));
	}

	@Test
	void shutdownLetsQueuedTasksFinish() throws InterruptedException {
		WorkerPool pool = new WorkerPool("test", 1);
		pool.start();
		CountDownLatch done = new CountDownLatch(1);
		assertTrue(pool.submit(done::countDown));

		pool.shutdown();

		assertTrue(done.await(1, TimeUnit.SECONDS), "queued task should have run before shutdown returned");
	}

	@Test
	void threadCountMatchesConfiguration() {
		assertEquals(3, new WorkerPool("test", 3).size());
		assertEquals(0, new WorkerPool("test", -1).size());
	}
}
